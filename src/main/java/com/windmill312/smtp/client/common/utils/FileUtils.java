package com.windmill312.smtp.client.common.utils;

import com.windmill312.smtp.client.common.logger.Logger;
import com.windmill312.smtp.client.common.logger.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {
    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    public static String getOrCreateSentDirectory(String mainDir) {
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

    public static void deleteFile(Path filePath) {
        if (filePath.toFile().delete()) {
            return;
        }

        logger.error("File " + filePath.toString() + " can't be deleted");
    }
}
