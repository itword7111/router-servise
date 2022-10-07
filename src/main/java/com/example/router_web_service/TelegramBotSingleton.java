package com.example.router_web_service;

public class TelegramBotSingleton {
    private static final String BOT_TOKEN = "5417168592:AAG6-FZGMZgHv4uII3Ql34aHkEbmIqw_UCw";
    final static private TelegramBotApplication application=TelegramBotApplication.builder()
            .botToken(BOT_TOKEN)
                .build();
    private TelegramBotSingleton(){

    }
    public static TelegramBotApplication getInstance(){
        return application;
    }
}
