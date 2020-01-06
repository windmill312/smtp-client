package com.windmill312.smtp.client.multiplexed;

import com.windmill312.smtp.client.multiplexed.service.ThreadFactoryService;

public class Client {
    public static void main(String[] args) {
        System.out.println("Client has been started");
        try {
            new ThreadFactoryService().start();

            Thread.currentThread().join();
        } catch (InterruptedException ex) {
            System.out.println("Client has been stopped");
        }
    }
}
