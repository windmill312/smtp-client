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
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.sun.jmx.mbeanserver.Util.cast;
import static com.windmill312.smtp.client.common.utils.FileUtils.copyFile;
import static com.windmill312.smtp.client.common.utils.FileUtils.deleteFile;
import static com.windmill312.smtp.client.common.utils.MailUtils.getDomainFromEmail;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

public class MessageReaderService implements Runnable, AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(MessageReaderService.class);
    private String internalDomain;
    private MessageQueueMap queueMap;
    private ApplicationProperties properties;
    private static final String sentFolderName = "sent";
    private static final String myFolderName = "my";
    private boolean isStopped = false;

    public MessageReaderService() {
        this.queueMap = MessageQueueMap.instance();
        this.properties = ApplicationProperties.instance();
        this.internalDomain = properties.getInternalDomain();
    }

    @Override
    public void close() throws Exception {
        this.isStopped = true;
    }

    @Override
    public void run() {
        logger.info("MessageReader thread started");
        prepareExistsMessageFiles();
        startDirectoryPolling();
    }

    private void startDirectoryPolling() {
        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            Path path = Paths.get(properties.getMailDir());
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

            WatchKey key;
            while ((key = watchService.take()) != null || !isStopped) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() != OVERFLOW) {
                        logger.trace("Handle create event for: " + event.context());
                        WatchEvent<Path> messageFileName = cast(event);

                        Path messageFilePath = Paths.get(properties.getMailDir() + File.separator + messageFileName.context());
                        PluralMessage pluralMessage = getMessageFromFile(messageFilePath.toFile());
                        addToQueue(pluralMessage);
                    }
                }
                key.reset();
            }
        } catch (IOException ex) {
            logger.error("Got error while directory processing: " + ex.getLocalizedMessage());
            ex.printStackTrace();
        } catch (InterruptedException e) {
            logger.warn("MessageReaderService thread is stopped");
        }
    }

    private void prepareExistsMessageFiles() {
        try {
            List<PluralMessage> pluralMessages = readMessageFiles();
            pluralMessages.forEach(this::addToQueue);
        } catch (IOException ex) {
            logger.error("Got error while parsing existing files: " + ex.getLocalizedMessage());
        }
    }

    private void addToQueue(PluralMessage pluralMessage) {
        for (String receiver : pluralMessage.getTo()) {
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
            if (domain.equals(internalDomain)) {
                destinationFileName = Paths.get(
                        properties.getMailDir() +
                                File.separator +
                                myFolderName +
                                File.separator +
                                pluralMessage.getPath().getFileName()
                );
                logger.debug("Message from: " + directMessage.getFrom() +
                        " to: " + directMessage.getTo() + " successfully added to internal message directory");
            } else {
                destinationFileName = Paths.get(
                        properties.getMailDir() +
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

    private List<PluralMessage> readMessageFiles() throws IOException {
        List<PluralMessage> messages = new ArrayList<>();
        Files.walk(Paths.get(properties.getMailDir()), 1)
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
