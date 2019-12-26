package com.windmill312.smtp.client.service;

import com.windmill312.smtp.client.exceptions.SendException;
import com.windmill312.smtp.client.logger.Logger;
import com.windmill312.smtp.client.logger.LoggerFactory;
import com.windmill312.smtp.client.model.PreparedMessage;

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
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;

class SMTPMXLookUpService {

    public static Logger logger = LoggerFactory.getLogger(SMTPMXLookUpService.class);

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

    private static void say(BufferedWriter wr, String text) throws IOException {
        wr.write(text + "\r\n");
        wr.flush();
    }

    private static ArrayList<String> getMX(String hostName)
            throws NamingException {
        Hashtable env = new Hashtable();
        env.put(
                "java.naming.factory.initial",
                "com.sun.jndi.dns.DnsContextFactory"
        );
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

        return result;
    }

    static boolean sendMessage(PreparedMessage message) {
        int position = message.getTo().indexOf('@');

        if (position == -1) {
            return false;
        }

        String domain = message.getTo().substring(++position);
        ArrayList<String> mxList;
        try {
            mxList = getMX(domain);
        } catch (NamingException ex) {
            logger.error("Got error while receiving MX records");
            return false;
        }

        if (mxList.size() == 0) {
            logger.warn("There is no MX records");
            return false;
        }

        for (String mxRecord : mxList) {
            boolean isValid = false;
            try {
                int responseStatus;

                Socket socket = new Socket(mxRecord, 25);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                responseStatus = hear(reader);
                if (responseStatus != 220) {
                    logger.error("Got error while opening the socket");
                    throw new SendException("Invalid header");
                }
                say(writer, "HELO local.server");

                responseStatus = hear(reader);
                if (responseStatus != 250) {
                    logger.error("Got error while connecting to SMTP port");
                    throw new SendException("Not SMTP");
                }

                // validate the sender address
                say(writer, "MAIL FROM: " + message.getFrom());
                responseStatus = hear(reader);
                if (responseStatus != 250) {
                    logger.error("Got error after MAIL FROM construct");
                    throw new SendException("Sender rejected");
                }

                say(writer, "RCPT TO: " + message.getTo());
                responseStatus = hear(reader);
                if (responseStatus != 250) {
                    logger.error("Got error after RCPT TO construct");
                    throw new SendException("Receiver rejected");
                }

                say(writer, "DATA");
                responseStatus = hear(reader);
                if (responseStatus != 354) {
                    logger.error("Got error after DATA construct");
                    throw new SendException("Data rejected");
                }

                say(writer, "From: " + message.getFrom().split("@")[0] + " <" + message.getFrom() + ">");
                say(writer, "To: " + message.getTo().split("@")[0] + " <" + message.getTo() + ">");
                say(writer, "Subject: Message from SMTP client");
                say(writer, "Content-Type: text/plain");
                say(writer, "\n");
                for (String line : message.getData().split("\n")) {
                    say(writer, line);
                }
                say(writer, ".");
                responseStatus = hear(reader);
                if (responseStatus != 250) {
                    logger.error("Got error while sending mail data [status:" + responseStatus + "]");
                    throw new SendException("Data content rejected: [status=" + responseStatus + "] " + reader.readLine());
                }

                say(writer, "QUIT");
                responseStatus = hear(reader);
                if (responseStatus != 221) {
                    logger.error("Got error while closing connection [status:" + responseStatus + "]");
                    throw new SendException("Address is not isValid!");
                }

                isValid = true;
                reader.close();
                writer.close();
                socket.close();
            } catch (Exception ex) {
                logger.error("Got unusual exception: " + ex.getLocalizedMessage());
            } finally {
                if (isValid) {
                    return true;
                }
            }
        }
        return false;
    }
}
