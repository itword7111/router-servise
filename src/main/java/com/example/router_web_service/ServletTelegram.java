package com.example.router_web_service;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.xml.ws.Endpoint;

@WebListener
public class ServletTelegram implements ServletContextListener {
    private static final String BOT_TOKEN = "5417168592:AAG6-FZGMZgHv4uII3Ql34aHkEbmIqw_UCw";
    @Override
    public void contextInitialized(ServletContextEvent sce) {


        TelegramBotApplication application = TelegramBotSingleton.getInstance();
        application.run();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
