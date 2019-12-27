package com.windmill312.smtp.client.service;

import com.google.gson.Gson;
import com.windmill312.smtp.client.config.ApplicationProperties;
import com.windmill312.smtp.client.logger.Logger;
import com.windmill312.smtp.client.logger.LoggerFactory;
import com.windmill312.smtp.client.model.PluralMessage;
import com.windmill312.smtp.client.model.DirectMessage;
import com.windmill312.smtp.client.queue.MessageQueueMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.lang.Thread.sleep;

public class MessageReaderService implements Runnable, AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(MessageReaderService.class);
    private final MessageQueueMap queueMap;

    private static final Long DELAY_MILLIS = 10000L;
    private volatile boolean stopped = false;

    MessageReaderService() {
        this.queueMap = MessageQueueMap.instance();
    }

    @Override
    public void close() {
        stopped = true;
    }

    @Override
    public void run() {
        System.out.println("MessageReader thread started");
        List<PluralMessage> pluralMessages = new ArrayList<>();
        ApplicationProperties instance = ApplicationProperties.instance();
        Gson gson = new Gson();

        while (!stopped) {
            try {
                Files.walk(Paths.get(instance.getMailDir()), 1).filter(Files::isRegularFile)
                        .forEach(messagePath -> {
                            try {
                                pluralMessages.add(
                                        gson.fromJson(
                                                new BufferedReader(
                                                        new FileReader(messagePath.toFile())
                                                ),
                                                PluralMessage.class
                                        )
                                );

                                if (messagePath.toFile().renameTo(
                                        new File(
                                                getOrCreateSentDirectory(instance.getMailDir()) +
                                                        File.separator +
                                                        messagePath.getFileName()
                                        )
                                )) {
                                    messagePath.toFile().delete();
                                } else {
                                    System.out.println("Can't remove copied file: " + messagePath.getFileName().toString());
                                }

                            } catch (FileNotFoundException e) {
                                System.out.println("PluralMessage file " + messagePath + " not found");
                            }
                        });

                for (PluralMessage pluralMessage : pluralMessages) {
                    for (String receiver: pluralMessage.getTo()) {
                        ConcurrentLinkedQueue<DirectMessage> queue = queueMap.get(receiver.split("@")[1]);
                        DirectMessage directMessage = new DirectMessage(
                                new DirectMessage.MessageBody(
                                    pluralMessage.getFrom(),
                                    receiver,
                                    pluralMessage.getData()
                                )
                        );

                        if (queue != null) {
                            queue.add(directMessage);
                        } else {
                            queueMap.get("default").add(directMessage);
                        }

                        logger.debug("Message from: " + directMessage.getBody().getFrom() +
                                " to: " + directMessage.getBody().getTo() + " successfully added to queue");
                    }
                }

                pluralMessages.clear();
                sleep(DELAY_MILLIS);

            } catch (InterruptedException e) {
                System.out.println("MessageReader thread is interrupted");
            } catch (IOException e) {
                logger.error("Mail directory is invalid. Error: " + e.getLocalizedMessage());
            }
        }

        System.out.println("MessageReader thread is stopped");
    }

    private String getOrCreateSentDirectory(String mainDir) {
        Path path = Paths.get(mainDir + "/sent");

        try {
            if (!Files.exists(path)) {
                Files.createDirectory(path);
            }
            return path.toString();
        } catch (IOException e) {
            logger.error("Can't create " + path + " directory");
        }
        return null;
    }
}
