package com.windmill312.smtp.client.integration;

import com.windmill312.smtp.client.common.EmailAuthenticator;
import com.windmill312.smtp.client.common.config.ApplicationProperties;
import com.windmill312.smtp.client.common.service.ThreadFactoryService;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Test;

import javax.mail.Authenticator;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

public class MessageSendingTest {
    private static final ApplicationProperties properties = ApplicationProperties.instance();
    private static final String SUBJECT_HEADER = "I'm a test!";

    @Test
    public void checkSequentialMessageSending() {
        checkMessageSending(com.windmill312.smtp.client.sequential.service.ThreadFactoryServiceImpl.class);
    }

    @Test
    public void checkMultiplexedMessageSending() {
        checkMessageSending(com.windmill312.smtp.client.multiplexed.service.ThreadFactoryServiceImpl.class);
    }

    @SneakyThrows({
            InterruptedException.class,
            MessagingException.class,
            IllegalAccessException.class,
            InstantiationException.class})
    private void checkMessageSending(Class clientClazz) {
        final String IMAP_AUTH_EMAIL = "test5667@yandex.ru" ;
        final String IMAP_AUTH_PWD = "bW52LjNjeDYtY1dKZTNR";

        List<Message> messagesListBeforeSending = getMessagesViaImap(IMAP_AUTH_EMAIL, IMAP_AUTH_PWD);
        Assert.assertNotNull(messagesListBeforeSending);

        createMessageFile();
        ThreadFactoryService client = (ThreadFactoryService) clientClazz.newInstance();
        client.start();
        Thread.sleep(10000);
        client.stop();

        List<Message> messagesListAfterSending = getMessagesViaImap(IMAP_AUTH_EMAIL, IMAP_AUTH_PWD);
        Assert.assertNotNull(messagesListAfterSending);
        Assert.assertTrue(messagesListAfterSending.size() > messagesListBeforeSending.size());

        int counter = 0;
        for (Message message: messagesListAfterSending) {
            if (message.getSubject().equals(SUBJECT_HEADER)) {
                counter++;
            }
        }
        Assert.assertEquals(1, counter);

        deleteTestMessage(IMAP_AUTH_EMAIL, IMAP_AUTH_PWD);
    }

    @SneakyThrows(IOException.class)
    public static void createMessageFile() {
        Path testMessageFile = Files.createFile(
                Paths.get(properties.getMailDir() + File.separator + "test.txt")
        );

        FileWriter writer = new FileWriter(testMessageFile.toFile());
        writer.write(TEST_MESSAGE);
        writer.close();
    }

    @SneakyThrows(MessagingException.class)
    private void deleteTestMessage(String email, String secretPassword) {
        final String IMAP_Port = "993";
        final String password = new String(Base64.getDecoder().decode(secretPassword));
        final String host = "imap." + email.split("@")[1];

        Properties properties = new Properties();
        properties.put("mail.debug", "false");
        properties.put("mail.store.protocol", "imaps");
        properties.put("mail.imap.ssl.enable", "true");
        properties.put("mail.imap.port", IMAP_Port);

        Authenticator auth = new EmailAuthenticator(email, password);
        Session session = Session.getDefaultInstance(properties, auth);
        session.setDebug(false);

        Store store = session.getStore();
        store.connect(host, email, password);

        Folder emailFolder = store.getFolder("INBOX");
        emailFolder.open(Folder.READ_WRITE);
        List<Message> messages = Arrays.asList(emailFolder.getMessages());

        for (Message message: messages) {
            if (message.getSubject().equals(SUBJECT_HEADER)) {
                message.setFlag(Flags.Flag.DELETED, true);
            }
        }

        emailFolder.close(true);
        store.close();
    }

    @SneakyThrows(MessagingException.class)
    private List<Message> getMessagesViaImap(String email, String secretPassword) {
            final String IMAP_Port = "993";
            final String password = new String(Base64.getDecoder().decode(secretPassword));
            final String host = "imap." + email.split("@")[1];

            Properties properties = new Properties();
            properties.put("mail.debug", "false");
            properties.put("mail.store.protocol", "imaps");
            properties.put("mail.imap.ssl.enable", "true");
            properties.put("mail.imap.port", IMAP_Port);

            Authenticator auth = new EmailAuthenticator(email, password);
            Session session = Session.getDefaultInstance(properties, auth);
            session.setDebug(false);

            Store store = session.getStore();
            store.connect(host, email, password);

            Folder emailFolder = store.getFolder("INBOX");
            emailFolder.open(Folder.READ_ONLY);

            return Arrays.asList(emailFolder.getMessages());
    }

    public static final String TEST_MESSAGE = "from: <windmill5667@yandex.ru>\n" +
            "to: <test5667@yandex.ru>\n" +
            "data: From: windmill5667 <windmill5667@yandex.ru>\n" +
            "To: test5667 <test5667@yandex.ru>\n" +
            "Subject: " + SUBJECT_HEADER + "\n" +
            "Message-ID: <af66a2e0-5937-023f-1360-b7c979f30095@local.mail.server>\n" +
            "Date: Tue, 7 Jan 2020 14:30:26 -0300\n" +
            "User-Agent: Mozilla/5.0 (X11; Linux i686; rv:60.0) Gecko/20100101\n" +
            "Thunderbird/60.9.0\n" +
            "MIME-Version: 1.0\n" +
            "Content-Type: text/plain; charset=utf-8; format=flowed\n" +
            "Content-Transfer-Encoding: 7bit\n" +
            "Content-Language: en-US\n" +
            "\n" +
            "\n" +
            "Hello from test case!";
}