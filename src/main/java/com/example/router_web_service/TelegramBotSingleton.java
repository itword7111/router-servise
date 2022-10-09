package com.example.router_web_service;

public class TelegramBotSingleton {
    private static final String BOT_TOKEN = "5495596118:AAGIkxPZi_f3TrEb0J3WT1nSsiDWb7vol9s";
    final static private TelegramBotApplication application=TelegramBotApplication.builder()
            .botToken(BOT_TOKEN)
                .build();
    private TelegramBotSingleton(){

    }
    public static TelegramBotApplication getInstance(){
        return application;
    }
}
