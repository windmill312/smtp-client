package com.windmill312.smtp.client.common.utils;

import com.windmill312.smtp.client.common.config.ApplicationProperties;
import com.windmill312.smtp.client.common.logger.Logger;
import com.windmill312.smtp.client.common.logger.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;

public final class SocketUtils {
    private static final Logger logger = LoggerFactory.getLogger(SocketUtils.class);
    private static final ApplicationProperties properties = ApplicationProperties.instance();

    public static void writeToChannel(SelectionKey key, String data) {
        ByteBuffer buffer = ByteBuffer.allocate(properties.getBufferSize()).order(ByteOrder.LITTLE_ENDIAN);
        try {
            WritableByteChannel channel = (WritableByteChannel) key.channel();

            data = data.endsWith("\r\n") ? data : data + "\r\n";

            int batchSize = properties.getBatchSize();
            if (data.length() > batchSize) {
                for (int i = 0; i < data.length(); i += batchSize) {
                    String subStr;
                    if (data.length() < i + batchSize) {
                        subStr = data.substring(i);
                    } else {
                        subStr = data.substring(i, i + batchSize);
                    }
                    logger.trace("Sent to server: " + subStr);
                    channel.write(ByteBuffer.wrap(subStr.getBytes()));
                    Thread.sleep(100);
                }
            } else {
                byte[] bytes = data.getBytes();
                logger.trace("Sent to server: " + data);
                channel.write(ByteBuffer.wrap(bytes));
            }

            key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
            key.interestOps(key.interestOps() | SelectionKey.OP_READ);
        } catch (IOException e) {
            logger.error("Got error while writing to buffer: " + e.getLocalizedMessage());
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        } finally {
            buffer.clear();
        }
    }

    public static int getResponseCodeFromChannel(SelectionKey key) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(properties.getBufferSize()).order(ByteOrder.LITTLE_ENDIAN);
        try {
            ReadableByteChannel channel = (ReadableByteChannel) key.channel();

            int result = channel.read(buffer);
            if (result <= 0) {
                return -1;
            }

            buffer.flip();

            final String response = new String(buffer.array(), buffer.position(), buffer.remaining(), StandardCharsets.US_ASCII);
            logger.trace("Received from server: " + response);

            key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);
            key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);

            return getResponseCode(response);
        } finally {
            buffer.clear();
        }
    }

    public static int getResponseCodeFromBuffer(BufferedReader in) throws IOException {
        String line;
        int result = 0;

        while ((line = in.readLine()) != null) {
            logger.trace("Received from server: " + line);
            result = getResponseCode(line);

            if (line.charAt(3) != '-') {
                break;
            }
        }
        return result;
    }

    public static void writeToBuffer(BufferedWriter wr, String data) throws IOException {
        wr.write(data + "\r\n");
        wr.flush();
        logger.trace("Sent to server: " + data);
    }

    private static int getResponseCode(String response) {
        try {
            return Integer.parseInt(response.substring(0, 3));
        } catch (Exception ex) {
            logger.error("Got error while parsing response code: " + ex.getLocalizedMessage());
            return -1;
        }
    }
}
