package com.windmill312.smtp.client.sequential;

import com.windmill312.smtp.client.sequential.service.ThreadFactoryServiceImpl;

public class Client {
    public static void main(String[] args) {
        System.out.println("Client has been started");
        try {
            new ThreadFactoryServiceImpl().start();

            Thread.currentThread().join();
        } catch (InterruptedException ex) {
            System.out.println("Client has been stopped");
        }
    }
}
