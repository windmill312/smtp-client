package com.windmill312.smtp.client.common.utils;

import com.windmill312.smtp.client.common.logger.Logger;
import com.windmill312.smtp.client.common.logger.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {
    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    public static String getOrCreateFolder(String directory) {
        Path path = Paths.get(directory);

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

    public static void copyFile(File sourceFile, Path destinationFile) {
        try {
            if (Files.isRegularFile(sourceFile.toPath())) {
                if (!Files.exists(destinationFile.getParent())) {
                    Files.createDirectory(destinationFile.getParent());
                }
                Files.copy(sourceFile.toPath(), destinationFile);
            }
        } catch (FileAlreadyExistsException ex) {
            // do nothing
        } catch (IOException ex) {
            logger.error("Got error while coping file: " + ex.getMessage());
        }
    }

    public static void deleteFile(File file) {
        if (file.delete()) {
            return;
        }

        logger.error("File " + file.toString() + " can't be deleted");
    }
}
