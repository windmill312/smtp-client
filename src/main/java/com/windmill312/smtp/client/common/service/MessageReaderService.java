package com.windmill312.smtp.client.common.service;

import com.windmill312.smtp.client.common.config.ApplicationProperties;
import com.windmill312.smtp.client.common.logger.Logger;
import com.windmill312.smtp.client.common.logger.LoggerFactory;
import com.windmill312.smtp.client.common.model.DirectMessage;
import com.windmill312.smtp.client.common.model.PluralMessage;
import com.windmill312.smtp.client.common.queue.MessageQueueMap;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.windmill312.smtp.client.common.utils.FileUtils.deleteFile;
import static com.windmill312.smtp.client.common.utils.FileUtils.getOrCreateSentDirectory;
import static java.lang.Thread.sleep;

public class MessageReaderService implements Runnable, AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(MessageReaderService.class);
    private final MessageQueueMap queueMap;

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
        List<PluralMessage> pluralMessages = new ArrayList<>();
        ApplicationProperties instance = ApplicationProperties.instance();

        while (!stopped) {
            try {
                Files.walk(Paths.get(instance.getMailDir()), 1).filter(Files::isRegularFile)
                        .forEach(messagePath -> {
                            pluralMessages.add(getMessageFromFile(messagePath.toFile()));

                            if (messagePath.toFile().renameTo(
                                    new File(
                                            getOrCreateSentDirectory(instance.getMailDir()) +
                                                    File.separator +
                                                    messagePath.getFileName()
                                    )
                            )) {
                                deleteFile(messagePath);
                            } else {
                                logger.error("Can't copy file: " + messagePath.getFileName().toString() +
                                        " to sent directory");
                                close();
                            }
                        });

                for (PluralMessage pluralMessage : pluralMessages) {
                    for (String receiver: pluralMessage.getTo()) {
                        String domain = receiver.split("@")[1];
                        DirectMessage directMessage = new DirectMessage()
                            .setFrom(pluralMessage.getFrom())
                            .setTo(receiver)
                            .setData(pluralMessage.getData());

                        queueMap.putForDomain(domain, directMessage);

                        logger.debug("Message from: " + directMessage.getFrom() +
                                " to: " + directMessage.getTo() + " successfully added to queue");
                    }
                }

                pluralMessages.clear();
                sleep(DELAY_MILLIS);

            } catch (IOException e) {
                logger.error("Mail directory is invalid: " + e.getLocalizedMessage());
                close();
            } catch (InterruptedException e) {
                logger.info("MessageReader thread is interrupted");
            }
        }

        System.out.println("MessageReader thread is stopped");
    }

    @SneakyThrows(IOException.class)
    private PluralMessage getMessageFromFile(File file) {
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