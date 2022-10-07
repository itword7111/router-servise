package com.example.router_web_service;

import com.exemple.generate.CommandWs;
import com.exemple.generate.CommandWsImplService;

import java.net.MalformedURLException;
import java.net.URL;

public class CommandWebClient {
    public static void main(String[] args) throws MalformedURLException {
        // создаем ссылку на wsdl описание
        URL url = new URL("http://localhost:8081/ws/service-command?wsdl");

        CommandWsImplService servicehe = new CommandWsImplService(url);
        CommandWs hello = servicehe.getCommandWsImplPort();

        System.out.println(hello.getRoleNameByUserName(""));
    }
}
