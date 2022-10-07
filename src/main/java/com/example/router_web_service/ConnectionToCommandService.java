package com.example.router_web_service;

import com.exemple.generate.CommandWs;
import com.exemple.generate.CommandWsImplService;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectionToCommandService {
    private Logger logger=Logger.getLogger(ConnectionToCommandService.class.getName());
    private static URL url;

    static {
        try {
            url = new URL("http://localhost:8081/ws/service-command?wsdl");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private CommandWsImplService servicehe ;
    private CommandWs commandServiceMethods ;
    ConnectionToCommandService(){

    }
    public CommandWs getInstance(){
        try {
            this.servicehe = new CommandWsImplService(url);
            this.commandServiceMethods = servicehe.getCommandWsImplPort();
        }catch (Exception e ){
            logger.log(Level.WARNING,ConnectionToCommandService.class+"   "+e.getCause());
            System.out.println(ConnectionToCommandService.class+"   "+e.getCause());
        }
        return commandServiceMethods;
    }
}
