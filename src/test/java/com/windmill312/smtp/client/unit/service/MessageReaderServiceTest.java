package com.windmill312.smtp.client.unit.service;

import com.windmill312.smtp.client.common.queue.MessageQueueMap;
import com.windmill312.smtp.client.common.service.MessageReaderService;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.windmill312.smtp.client.integration.MessageSendingTest.createMessageFile;

public class MessageReaderServiceTest {

    @Test
    @SneakyThrows(InterruptedException.class)
    public void testMessageReadingFromFile() {
        createMessageFile();

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new MessageReaderService());
        Thread.sleep(3000);
        executorService.shutdownNow();

        MessageQueueMap messageQueueMap = MessageQueueMap.instance();
        Assert.assertEquals(1, messageQueueMap.getAllDomains().size());
    }
}
