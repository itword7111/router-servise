package com.example.router_web_service;



import com.example.router_web_service.model.ReportSender;
import com.exemple.generate.CommandWs;
import com.google.gson.Gson;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.request.SendMessage;

import javax.jws.WebService;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@WebService(endpointInterface = "com.example.router_web_service.RouterSenderService")
public class RouterSenderServiceImpl implements RouterSenderService {
    private String urlForAccounting="http://localhost:8086/accounting_war";
    private Gson gson=new Gson();
    private CommandWs connectionToCommandService=new ConnectionToCommandService().getInstance();
    public void notifyUser(String user, String message) {
        String chatId =connectionToCommandService.getChatIdByUserName(user);
        SendMessage response = new SendMessage(chatId,message);
        TelegramBotSingleton.getInstance().execute(response);
    }

    public List<String> getAdmins() {
        List<String> ans=connectionToCommandService.getUserNameListByRoleName("admin").getItem();
        return ans;
    }

    public List<String> getLecturers() {
        List<String> ans=connectionToCommandService.getUserNameListByRoleName("lector").getItem();
        return ans;
    }
    //бух
    public List<String> getThreeDaysNotTrackingUsers() {
        try {
            URL url = new URL(urlForAccounting+"/withoutThreeTrack");
            List<String> ans=gson.fromJson(getJsonFromUrl(url),List.class);
            return ans;
        }
        catch (IOException e){
            e.printStackTrace();
            return new ArrayList<>();
        }

    }
    //бух
    public List<String> getOneDaysNotTrackingUsers() {
        try {
            URL url = new URL(urlForAccounting+"/withoutOneTrack");
            List<String> ans=gson.fromJson(getJsonFromUrl(url),List.class);
            return ans;
        }
        catch (IOException e){
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
//бух
    public List<ReportSender> getReportsToday() {
        try {
            URL url = new URL(urlForAccounting+"/nonReportedTasks");
            List<ReportSender> ans=gson.fromJson(getJsonFromUrl(url),List.class);
            return ans;
        }
        catch (IOException e){
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<String> getUserNamesByRole(String roleName) {
        List<String> ans=connectionToCommandService.getUserNameListByRoleName(roleName).getItem();
        return ans;
    }

    public void pdf(byte[] pdfBytes, List<String> lectorNickName) throws IOException {
        for (String lector: lectorNickName) {
            String chatId =connectionToCommandService.getChatIdByUserName(lector);
            SendDocument response = new SendDocument(chatId,pdfBytes);
            TelegramBotSingleton.getInstance().execute(response);
        }
    }
    private String getJsonFromUrl(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder responseFromRouter = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            responseFromRouter.append(inputLine);
        }
        in.close();
        return responseFromRouter.toString();
    }

}
