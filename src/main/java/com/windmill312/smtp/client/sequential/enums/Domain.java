package com.windmill312.smtp.client.sequential.enums;

public enum Domain {
    MAIL_RU("mail.ru"),
    GOOGLE_COM("google.com"),
    YANDEX_RU("yandex.ru");

    public final String value;

    private Domain(String url) {
        this.value = url;
    }
}
