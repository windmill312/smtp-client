package com.windmill312.smtp.client.common.utils;

import com.windmill312.smtp.client.common.logger.Logger;
import com.windmill312.smtp.client.common.logger.LoggerFactory;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static java.util.Collections.singletonList;

public final class MailUtils {
    private static final Logger logger = LoggerFactory.getLogger(MailUtils.class);

    public static String getDomainFromEmail(String email) {
        int position = email.indexOf('@');
        if (position == -1) {
            logger.warn("Email: <" + email + "> is invalid");
            return null;
        }

        return email.substring(++position);
    }

    public static List<String> getMxRecords(String domainName) {
        try {
            InitialDirContext iDirC = new InitialDirContext();
            Attributes attributes = iDirC.getAttributes("dns:/" + domainName, new String[]{"MX"});
            Attribute attributeMX = attributes.get("MX");

            if (attributeMX == null) {
                return singletonList(domainName);
            }

            String[][] preferenceValuesHostNames = new String[attributeMX.size()][2];
            for (int i = 0; i < attributeMX.size(); i++) {
                preferenceValuesHostNames[i] = ("" + attributeMX.get(i)).split("\\s+");
            }

            Arrays.sort(preferenceValuesHostNames, Comparator.comparingInt(o -> Integer.parseInt(o[0])));

            String[] sortedHostNames = new String[preferenceValuesHostNames.length];
            for (int i = 0; i < preferenceValuesHostNames.length; i++) {
                sortedHostNames[i] = preferenceValuesHostNames[i][1].endsWith(".") ?
                        preferenceValuesHostNames[i][1].substring(0, preferenceValuesHostNames[i][1].length() - 1) : preferenceValuesHostNames[i][1];
            }

            return Arrays.asList(sortedHostNames);
        } catch (NamingException ex) {
            logger.error("Got error while getting MX records: " + ex.getLocalizedMessage());
            return new ArrayList<>();
        }
    }
}
