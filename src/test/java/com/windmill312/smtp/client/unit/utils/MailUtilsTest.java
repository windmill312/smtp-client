package com.windmill312.smtp.client.unit.utils;

import com.windmill312.smtp.client.common.utils.MailUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class MailUtilsTest {

    @Test
    public void getMxRecordsTest() {
        List<String> mxRecords = MailUtils.getMxRecords("yandex.ru");

        Assert.assertFalse(mxRecords.isEmpty());
        Assert.assertTrue(mxRecords.contains("mx.yandex.ru"));
    }

    @Test
    public void getDomainFromEmail() {
        String domain = MailUtils.getDomainFromEmail("test5667@yandex.ru");

        Assert.assertNotNull(domain);
        Assert.assertEquals("yandex.ru", domain);

        domain = MailUtils.getDomainFromEmail("test5667@google.com");

        Assert.assertNotNull(domain);
        Assert.assertEquals("google.com", domain);

        domain = MailUtils.getDomainFromEmail("test5667@rambler.ru");

        Assert.assertNotNull(domain);
        Assert.assertEquals("rambler.ru", domain);
    }
}
