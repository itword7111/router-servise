package com.example.router_web_service;

import com.example.router_web_service.model.*;
import com.exemple.generate.CommandWs;
import com.google.gson.Gson;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalTime;
import java.util.*;

public class TelegramBotApplication extends TelegramBot {
    private Gson gson=new Gson();
    private CommandWs connectionToCommandService=new ConnectionToCommandService().getInstance();
    private RouterSenderService routerSenderService=new RouterSenderServiceImpl();

    @lombok.Builder
    public TelegramBotApplication(String botToken) {
        super(botToken);
    }

    void run() {
        this.setUpdatesListener(new UpdatesListener() {
            public int process(List<Update> updates) {
                updates.forEach(TelegramBotApplication.this::process);
                return UpdatesListener.CONFIRMED_UPDATES_ALL;
            }
        });
    }

    private void process(Update update) {
        Message message = update.message();
        if (message != null) {
            String text = message.text();
            Optional.ofNullable(text)
                    .ifPresent(commandName -> this.serveCommand(commandName, message.chat().id(),message.from().username()));
        }
    }

    private void serveCommand(String commandName, Long chatId, String userName)  {
        String information = "";
        final String text = commandName.trim();
        switch (commandName.split(" ")[0]) {
            case "":
                SendMessage response = new SendMessage(chatId,"Сообщение не является текстом");
                this.execute(response);
                break;
            case "/start":
                information = getInformationStart(userName, chatId);
                break;
            case "/track":
                String textTrack = text.substring(6);
                information = getInformationTrack(userName, textTrack);
                break;
            case "/report":
                List<ReportSender> tasksList = routerSenderService.getReportsToday();
                StringBuilder builder = new StringBuilder("Список людей, которые затрекались сегодня:\n");
                for (ReportSender report : tasksList) {
                    builder.append(report.toString()).append("\n");
                }
                information = builder.toString();
                break;
            case "/no_report":
                List<String> tasksListNoReport = routerSenderService.getOneDaysNotTrackingUsers();
                StringBuilder builder2 = new StringBuilder("Список людей, которые не затрекались сегодня:\n");
                tasksListNoReport.forEach(user -> builder2.append("@").append(user).append("\n"));
                information =  builder2.toString();
                break;
            case "/no_3report":
                List<String> tasksListNoThreeReport = routerSenderService.getThreeDaysNotTrackingUsers();
                StringBuilder builder3 = new StringBuilder("Список людей, которые не затрекались сегодня:\n");
                tasksListNoThreeReport.forEach(user -> builder3.append("@").append(user).append("\n"));
                information = builder3.toString();

                break;
            default:
                information = "введите сообщение согласно меню ";
        }
        new SendMessage(chatId, information);
    }
    private String getInformationStart(String userName, Long chatId) {
        String information;
        connectionToCommandService.insertUser(userName,Groups.first_group.toString(),Role.lector.toString());
        RouteRequest("POST","http://localhost:8080/Accounting/insertNewUser",new UserForAccounting(userName));
        connectionToCommandService.putChatIdByUserName(chatId.toString(),userName);
        information = "Поздравляю ты добавлен @" + userName + "\n" +
                "/track 23ч59м Текст выполненой таски " +
                "чтобы добавить время выполнения задачи веди задачу и время\n" +
                "/report - отчет за сегодня\n" +
                "/no_report - кто не трекался сегодня\n" +
                "/no_3report - кто не трекался 3 дня\n";
        return information;
    }

    private String getInformationTrack(String userName, String text) {
        String information;
        int indexHour = text.indexOf('ч');
        int indexMin = text.indexOf('м');
        Time time = Time.valueOf(LocalTime.of(Integer.parseInt(text.substring(0, indexHour).trim()), Integer.parseInt(text.substring(indexHour + 1, indexMin).trim())));
        ReportSender reportSender=new ReportSender(userName, text.substring(indexMin + 1), new Timestamp(time.getTime()));
        try {
            RouteRequest("POST","http://localhost:8080/Accounting/insertNewTrack", reportSender);
            information = "Информация добавлена в базу";
        } catch (Exception e) {
            information = "Неправильный формат данных";
        }
        return information;
    }

    private void RouteRequest(String requestMethod, String requestUrl, Object object){
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(requestMethod);
            String jsonInputString=gson.toJson(object);
            if(!jsonInputString.equals("null")) {
                connection.setDoOutput(true);
                OutputStream os = connection.getOutputStream();
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
                os.close();
            }
            InputStream input=connection.getInputStream();
            input.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

}
