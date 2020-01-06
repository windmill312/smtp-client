package com.windmill312.smtp.client.common.utils;

import com.windmill312.smtp.client.common.config.ApplicationProperties;
import com.windmill312.smtp.client.common.logger.Logger;
import com.windmill312.smtp.client.common.logger.LoggerFactory;
import lombok.SneakyThrows;

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

    @SneakyThrows(IOException.class)
    public static void writeToChannel(SelectionKey key, String data) {
        ByteBuffer buffer = ByteBuffer.allocate(properties.getBufferSize()).order(ByteOrder.LITTLE_ENDIAN);
        try {
            WritableByteChannel channel = (WritableByteChannel) key.channel();

            data = data.endsWith("\r\n") ? data : data + "\r\n";

            byte[] bytes = data.getBytes();
            channel.write(ByteBuffer.wrap(bytes));

            key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
            key.interestOps(key.interestOps() | SelectionKey.OP_READ);
        } finally {
            buffer.clear();
        }
    }

    @SneakyThrows(IOException.class)
    public static int readFromChannel(SelectionKey key) {
        ByteBuffer buffer = ByteBuffer.allocate(properties.getBufferSize()).order(ByteOrder.LITTLE_ENDIAN);
        try {
            ReadableByteChannel channel = (ReadableByteChannel) key.channel();

            int result = channel.read(buffer);
            if (result <= 0) {
                return -1;
            }

            buffer.flip();

            final String response = new String(buffer.array(), buffer.position(), buffer.remaining(), StandardCharsets.US_ASCII);
            logger.debug(response);

            key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);
            key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);

            return getResponseCode(response);
        } finally {
            buffer.clear();
        }
    }

    public static int readFromBuffer(BufferedReader in) throws IOException {
        String line;
        int result = 0;

        while ((line = in.readLine()) != null) {
            String prefix = line.substring(0, 3);
            try {
                result = Integer.parseInt(prefix);
            } catch (Exception ex) {
                logger.error("Got error while hearing smtp server: " + ex.getLocalizedMessage());
                result = -1;
            }
            if (line.charAt(3) != '-') break;
        }

        return result;
    }

    public static String getDomainFromEmail(String email) {
        int position = email.indexOf('@');
        if (position == -1) {
            logger.warn("Email: <" + email + "> is invalid");
        }

        return email.substring(++position);
    }

    public static void writeToBuffer(BufferedWriter wr, String text) throws IOException {
        wr.write(text + "\r\n");
        wr.flush();
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
