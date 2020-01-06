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
import java.util.stream.Collectors;

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
            logger.trace("Sent to server: " + data);
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
            logger.trace("Received from server: " + response);

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

        logger.trace("Received from server: " + in.lines().collect(Collectors.joining()));
        while ((line = in.readLine()) != null) {
            result = getResponseCode(line);

            if (line.charAt(3) != '-') {
                break;
            }
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
