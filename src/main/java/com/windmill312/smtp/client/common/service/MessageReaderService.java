package com.windmill312.smtp.client.common.service;

import com.windmill312.smtp.client.common.config.ApplicationProperties;
import com.windmill312.smtp.client.common.logger.Logger;
import com.windmill312.smtp.client.common.logger.LoggerFactory;
import com.windmill312.smtp.client.common.model.DirectMessage;
import com.windmill312.smtp.client.common.model.PluralMessage;
import com.windmill312.smtp.client.common.queue.MessageQueueMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.windmill312.smtp.client.common.utils.FileUtils.copyFile;
import static com.windmill312.smtp.client.common.utils.FileUtils.deleteFile;
import static com.windmill312.smtp.client.common.utils.MailUtils.getDomainFromEmail;
import static java.lang.Thread.sleep;

public class MessageReaderService implements Runnable, AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(MessageReaderService.class);
    private static final String INTERNAL_DOMAIN = "bestmailer.ru";
    private final MessageQueueMap queueMap;
    private final ApplicationProperties instance = ApplicationProperties.instance();

    private static final Long DELAY_MILLIS = 10000L;
    private volatile boolean stopped = false;

    public MessageReaderService() {
        this.queueMap = MessageQueueMap.instance();
    }

    @Override
    public void close() {
        stopped = true;
    }

    @Override
    public void run() {
        logger.info("MessageReader thread started");
        List<PluralMessage> messages;
        String sentFolderName = "sent";
        String myFolderName = "my";

        while (!stopped) {
            try {
                messages = readMessageFiles();

                for (PluralMessage pluralMessage : messages) {
                    for (String receiver: pluralMessage.getTo()) {
                        String domain = getDomainFromEmail(receiver);
                        if (domain == null) {
                            logger.error("Domain of: " + receiver + " is invalid");
                            continue;
                        }

                        DirectMessage directMessage = new DirectMessage()
                            .setFrom(pluralMessage.getFrom())
                            .setTo(receiver)
                            .setData(pluralMessage.getData());

                        Path destinationFileName;
                        if (domain.equals(INTERNAL_DOMAIN)) {
                            destinationFileName = Paths.get(
                                    instance.getMailDir() +
                                            File.separator +
                                            myFolderName +
                                            File.separator +
                                            pluralMessage.getPath().getFileName()
                            );
                            logger.debug("Message from: " + directMessage.getFrom() +
                                    " to: " + directMessage.getTo() + " successfully added to internal message directory");
                        } else {
                            destinationFileName = Paths.get(
                                    instance.getMailDir() +
                                            File.separator +
                                            sentFolderName +
                                            File.separator +
                                            pluralMessage.getPath().getFileName()
                            );
                            queueMap.putForDomain(domain, directMessage);
                            logger.debug("Message from: " + directMessage.getFrom() +
                                    " to: " + directMessage.getTo() + " successfully added to queue");
                        }
                        copyFile(pluralMessage.getPath().toFile(), destinationFileName);
                    }
                    deleteFile(pluralMessage.getPath().toFile());
                }

                messages.clear();
                sleep(DELAY_MILLIS);

            } catch (IOException e) {
                logger.error("Can't get access to message file: " + e.getLocalizedMessage());
                close();
            } catch (InterruptedException e) {
                logger.info("MessageReader thread is interrupted");
            }
        }

        System.out.println("MessageReader thread is stopped");
    }

    private List<PluralMessage> readMessageFiles() throws IOException {
        List<PluralMessage> messages = new ArrayList<>();
        Files.walk(Paths.get(instance.getMailDir()), 1)
                .filter(Files::isRegularFile)
                .forEach(messagePath -> {
                    try {
                        PluralMessage message = getMessageFromFile(messagePath.toFile());
                        messages.add(message);
                    } catch (IOException e) {
                        logger.error("Can't get message from: " + messagePath.toString());
                    }
                });
        return messages;
    }

    private PluralMessage getMessageFromFile(File file) throws IOException {
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        List<String> values = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            String line = bufferedReader.readLine();
            int valueStartIndex = line.indexOf(" ") + 1;
            values.add(line.substring(valueStartIndex));

            if (i == 2) {
                StringBuilder builder = new StringBuilder();
                bufferedReader.lines().forEach(l -> builder.append("\r\n").append(l));
                values.set(2, values.get(2) + builder.toString());
            }
        }

        return new PluralMessage(
                file.toPath(),
                values.get(0).replace("<", "").replace(">", ""),
                Arrays.asList(values.get(1)
                        .replace("<", "")
                        .replace(">", "")
                        .split(", ")
                ),
                values.get(2).replace("    ", "")
        );
    }
}
