package com.windmill312.smtp.client.unit.service;

import com.windmill312.smtp.client.common.config.ApplicationProperties;
import com.windmill312.smtp.client.common.queue.MessageQueueMap;
import com.windmill312.smtp.client.common.service.MessageReaderService;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.windmill312.smtp.client.common.utils.FileUtils.deleteFile;
import static com.windmill312.smtp.client.integration.MessageSendingTest.EXTERNAL_MESSAGE;
import static com.windmill312.smtp.client.integration.MessageSendingTest.INTERNAL_MESSAGE;
import static com.windmill312.smtp.client.integration.MessageSendingTest.TEST_FILENAME;
import static com.windmill312.smtp.client.integration.MessageSendingTest.createMessageFile;

public class MessageReaderServiceTest {
    private static final ApplicationProperties properties = ApplicationProperties.instance();

    @Test
    @SneakyThrows(InterruptedException.class)
    public void testMessageReadingFromFile() {
        createMessageFile(EXTERNAL_MESSAGE, TEST_FILENAME);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new MessageReaderService());
        Thread.sleep(3000);
        executorService.shutdownNow();

        MessageQueueMap messageQueueMap = MessageQueueMap.instance();
        Assert.assertEquals(1, messageQueueMap.getAllDomains().size());
    }

    @Test
    @SneakyThrows(InterruptedException.class)
    public void checkInternalMessageSending() {
        createMessageFile(INTERNAL_MESSAGE, TEST_FILENAME);
        String intenalDomain = "bestmailer.ru";

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new MessageReaderService());
        Thread.sleep(3000);
        executorService.shutdownNow();

        MessageQueueMap messageQueueMap = MessageQueueMap.instance();
        Assert.assertNull(messageQueueMap.get(intenalDomain));

        Path myFolderPath = Paths.get(properties.getMailDir() + File.separator + "my");
        try {
            Assert.assertTrue(Files.exists(myFolderPath));
            Assert.assertTrue(Files.isDirectory(myFolderPath));

            Assert.assertTrue(Files.walk(myFolderPath, 1)
                    .filter(path -> !Files.isDirectory(path))
                    .anyMatch(p -> p.getFileName().toString().equals(TEST_FILENAME)));

        } catch (IOException e) {
            e.printStackTrace();
        }
        deleteFile(Paths.get(myFolderPath.toString() + File.separator + TEST_FILENAME).toFile());
    }

}
