package com.windmill312.smtp.client.service;

import com.windmill312.smtp.client.config.ApplicationProperties;
import com.windmill312.smtp.client.logger.Logger;
import com.windmill312.smtp.client.logger.LoggerFactory;
import com.windmill312.smtp.client.model.DirectMessage;
import com.windmill312.smtp.client.model.MessageBatch;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;

import static com.windmill312.smtp.client.enums.MessageState.*;

class SMTPMXLookUpService {

    public static Logger logger = LoggerFactory.getLogger(SMTPMXLookUpService.class);
    public static ApplicationProperties applicationProperties = ApplicationProperties.instance();

    private static int hear(BufferedReader in) throws IOException {
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

    private static String getDomain(String email) {
        int position = email.indexOf('@');
        if (position == -1) {
            logger.warn("Email: <" + email + "> is invalid");
        }

        return email.substring(++position);
    }

    private static void say(BufferedWriter wr, String text) throws IOException {
        wr.write(text + "\r\n");
        wr.flush();
    }

    private static ArrayList<String> getMX(String hostName) {
        Hashtable env = new Hashtable();
        env.put(
                "java.naming.factory.initial",
                "com.sun.jndi.dns.DnsContextFactory"
        );

        try {
            DirContext context = new InitialDirContext(env);
            Attributes attributes = context.getAttributes(hostName, new String[]{"MX"});
            Attribute attribute = attributes.get("MX");

            if ((attribute == null) || (attribute.size() == 0)) {
                attributes = context.getAttributes(hostName, new String[]{"A"});
                attribute = attributes.get("A");
                if (attribute == null) {
                    throw new NamingException
                            ("No match for name '" + hostName + "'");
                }
            }

            ArrayList result = new ArrayList();
            NamingEnumeration enumeration = attribute.getAll();

            while (enumeration.hasMore()) {
                String mailHost;
                String x = (String) enumeration.next();
                String f[] = x.split(" ");

                if (f.length == 1)
                    mailHost = f[0];
                else if (f[1].endsWith("."))
                    mailHost = f[1].substring(0, (f[1].length() - 1));
                else
                    mailHost = f[1];

                result.add(mailHost);
            }

            if (result.size() == 0) {
                logger.error("There is no MX records for " + hostName);
                return null;
            }

            return result;

        } catch (NamingException ex) {
            logger.error("Got error while receiving MX records");
            return null;
        }
    }

    static boolean sendBatch(MessageBatch messageBatch) {
        String domain = getDomain(messageBatch.getMessages().get(0).getBody().getTo());
        ArrayList<String> mxList = getMX(domain);

        if (mxList != null) {
            for (String mxRecord : mxList) {
                try {
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(mxRecord, 25), applicationProperties.getSocketTimeoutMs());

                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    int responseStatus;

                    responseStatus = hear(reader);
                    if (responseStatus == 220) {
                        logger.info("MX record found for  <" + domain + ">: " + mxRecord);
                    } else {
                        logger.error("Got error while connecting to " + mxRecord + ":25");
                        continue;
                    }

                    for (DirectMessage message : messageBatch.getMessages()) {

                        say(writer, "HELO local.server");
                        responseStatus = hear(reader);
                        if (responseStatus == 250) {
                            message.setState(HELO);
                        } else {
                            logger.error("Got error while connecting to SMTP port");
                        }

                        say(writer, "MAIL FROM: " + message.getBody().getFrom());
                        responseStatus = hear(reader);
                        if (responseStatus == 250) {
                            message.setState(MAIL_FROM);
                        } else {
                            logger.error("Got error after MAIL FROM construct");
                        }

                        say(writer, "RCPT TO: " + message.getBody().getTo());
                        responseStatus = hear(reader);
                        if (responseStatus == 250) {
                            message.setState(RCPT_TO);
                        } else {
                            logger.error("Got error after RCPT TO construct");
                        }

                        say(writer, "DATA");
                        responseStatus = hear(reader);
                        if (responseStatus == 354) {
                            message.setState(DATA);
                        } else {
                            logger.error("Got error after DATA construct");
                        }

                        say(writer, "From: " + message.getBody().getFrom().split("@")[0] + " <" + message.getBody().getFrom() + ">");
                        say(writer, "To: " + message.getBody().getTo().split("@")[0] + " <" + message.getBody().getTo() + ">");
                        say(writer, "Subject: Message from SMTP client");
                        say(writer, "Content-Type: text/plain");
                        say(writer, "\n");
                        for (String line : message.getBody().getData().split("\n")) {
                            say(writer, line);
                        }
                        say(writer, ".");
                        responseStatus = hear(reader);
                        if (responseStatus == 250) {
                            message.setState(SENT);
                        } else {
                            logger.error("Got error while sending mail data [status:" + responseStatus + "]");
                        }
                    }
                    say(writer, "QUIT");
                    responseStatus = hear(reader);
                    if (responseStatus != 221) {
                        logger.error("Got error while closing connection [status:" + responseStatus + "]");
                    }

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
}
