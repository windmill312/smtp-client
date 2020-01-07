package com.windmill312.smtp.client.unit.utils;

import com.windmill312.smtp.client.common.utils.FileUtils;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtilsTest {

    @Test
    @SneakyThrows({NullPointerException.class, IOException.class})
    public void getOrCreateSentDirectory() {
        String mailDirectory = getClass().getClassLoader().getResource("").getPath();
        Path sentDirectoryPath = Paths.get(mailDirectory + File.separator + "sent");

        String returnPath = FileUtils.getOrCreateSentDirectory(mailDirectory);
        Assert.assertEquals(returnPath, sentDirectoryPath.toString());
        Assert.assertTrue(Files.exists(sentDirectoryPath));

        Files.deleteIfExists(sentDirectoryPath);
    }
}
