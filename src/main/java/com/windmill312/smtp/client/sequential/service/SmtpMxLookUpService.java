package com.windmill312.smtp.client.sequential.service;

import com.windmill312.smtp.client.common.config.ApplicationProperties;
import com.windmill312.smtp.client.common.logger.Logger;
import com.windmill312.smtp.client.common.logger.LoggerFactory;
import com.windmill312.smtp.client.common.model.DirectMessage;
import com.windmill312.smtp.client.sequential.model.MessageBatch;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

import static com.windmill312.smtp.client.common.utils.MailUtils.getMxRecords;
import static com.windmill312.smtp.client.common.utils.SocketUtils.getDomainFromEmail;
import static com.windmill312.smtp.client.common.utils.SocketUtils.readFromBuffer;
import static com.windmill312.smtp.client.common.utils.SocketUtils.writeToBuffer;

class SmtpMxLookUpService {

    private static Logger logger = LoggerFactory.getLogger(SmtpMxLookUpService.class);
    private static ApplicationProperties applicationProperties = ApplicationProperties.instance();
    private static final int DEFAULT_SMTP_PORT = 25;
    private static BufferedReader reader;
    private static BufferedWriter writer;

    static boolean sendBatch(MessageBatch messageBatch) {
        String domain = getDomainFromEmail(messageBatch.getMessages().get(0).getTo());
        List<String> mxList = getMxRecords(domain);

        if (mxList != null) {
            for (String mxRecord : mxList) {
                try {
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(mxRecord, DEFAULT_SMTP_PORT), applicationProperties.getSocketTimeoutMs());

                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    int responseStatus;

                    responseStatus = readFromBuffer(reader);
                    if (responseStatus == 220) {
                        logger.info("MX record found for  <" + domain + ">: " + mxRecord);
                    } else {
                        logger.error("Got error while connecting to " + mxRecord + ":25");
                        continue;
                    }

                    for (DirectMessage message : messageBatch.getMessages()) {

                        executeStep(
                                message,
                                "HELO local.server",
                                250,
                                "Got error while connecting to SMTP port"
                        );

                        executeStep(
                                message,
                                "MAIL FROM: " + message.getFrom(),
                                250,
                                "Got error after MAIL FROM construct"
                        );

                        executeStep(
                                message,
                                "RCPT TO: " + message.getTo(),
                                250,
                                "Got error after RCPT TO construct"
                        );

                        executeStep(
                                message,
                                "DATA",
                                354,
                                "Got error after DATA construct"
                        );

                        for (String line : message.getData().split("\n")) {
                            writeToBuffer(writer, line);
                        }
                        executeStep(
                                message,
                                ".",
                                250,
                                "Got error while sending mail data"
                        );
                    }

                    executeStep(
                            null,
                            "QUIT",
                            221,
                            "Got error while closing connection"
                    );

                    reader.close();
                    writer.close();
                    socket.close();
                    break;

                } catch (IOException ex) {
                    logger.error("Got exception: " + ex.getLocalizedMessage());
                    return false;
                }
            }
        } else {
            return false;
        }

        return true;
    }

    private static void executeStep(
            DirectMessage message,
            String stepText,
            int expectedResponseStatus,
            String errorText
    ) throws IOException {
        int responseStatus;
        writeToBuffer(writer, stepText);
        responseStatus = readFromBuffer(reader);
        if (responseStatus == expectedResponseStatus) {
            if (message != null) {
                message.nextState();
            }
        } else {
            logger.error(errorText);
        }
    }
}
