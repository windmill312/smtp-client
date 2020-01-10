package com.windmill312.smtp.client.unit.utils;

import com.windmill312.smtp.client.common.utils.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtilsTest {

    @Test
    public void getOrCreateDirectory() {
        String sentFolderName = "sent";
        String mailDirectory = getClass().getClassLoader().getResource("").getPath();
        Path sentDirectoryPath = Paths.get(mailDirectory + File.separator + sentFolderName);

        String returnPath = FileUtils.getOrCreateFolder(sentDirectoryPath.toString());
        Assert.assertEquals(returnPath, sentDirectoryPath.toString());
        Assert.assertTrue(Files.exists(sentDirectoryPath));

        try {
            Files.deleteIfExists(sentDirectoryPath);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
