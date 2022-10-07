package com.example.router_web_service;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.xml.ws.Endpoint;
@WebListener
public class Publisher implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        new Thread(() -> {
            Endpoint.publish("http://localhost:8082/ws/router", new RouterSenderServiceImpl());
        }).start();
        System.out.println("---------------------------------------------");

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
