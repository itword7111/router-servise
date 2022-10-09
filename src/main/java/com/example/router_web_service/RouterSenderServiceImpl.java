package com.example.router_web_service;



import com.example.router_web_service.model.ReportSender;
import com.exemple.generate.CommandWs;
import com.exemple.generate.ListOfString;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.request.SendMessage;
import com.sun.xml.ws.fault.ServerSOAPFaultException;

import javax.jws.WebService;
import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@WebService(endpointInterface = "com.example.router_web_service.RouterSenderService")
public class RouterSenderServiceImpl implements RouterSenderService {
    private String urlForAccounting="http://localhost:8080/Accounting";
    private Gson gson=new Gson();
    private CommandWs connectionToCommandService=new ConnectionToCommandService().getInstance();
    public void notifyUser(String user, String message) {
        try {
            String chatId =connectionToCommandService.getChatIdByUserName(user);
            SendMessage response = new SendMessage(chatId,message);
            TelegramBotSingleton.getInstance().execute(response);
        }catch (ServerSOAPFaultException e){

        }

    }

    public List<String> getAdmins() {
        List<String> ans=connectionToCommandService.getUserNameListByRoleName("lead").getItem();
        return ans;
    }

    public List<String> getLecturers() {
        List<String> ans=connectionToCommandService.getUserNameListByRoleName("lector").getItem();
        return ans;
    }
    //бух
    public List<String> getThreeDaysNotTrackingUsers() {
        try {
            List<String> users=getUsers();
            URL url = new URL(urlForAccounting+"/withoutThreeTrack");
            List<String> ans=gson.fromJson(getJsonFromUrl(url),List.class);
            for (String user: ans) {
                users.remove(user);
            }
            return users;
        }
        catch (IOException e){
            e.printStackTrace();
            return new ArrayList<>();
        }

    }
    //бух
    public List<String> getOneDaysNotTrackingUsers() {
        try {
            List<String> users=getUsers();
            URL url = new URL(urlForAccounting+"/withoutOneTrack");
            List<String> ans=gson.fromJson(getJsonFromUrl(url),List.class);
            for (String user: ans) {
                users.remove(user);
            }
            return users;
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
            Type reportSenderListType = new TypeToken<ArrayList<ReportSender>>(){}.getType();
            List<ReportSender> ans= gson.fromJson(getJsonFromUrl(url), reportSenderListType);
            return ans;
        }
        catch (IOException e){
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    private List<String> getUsers(){
        List<String> users = connectionToCommandService.getUserNameListByRoleName("user").getItem();
        List<String> admins=  connectionToCommandService.getUserNameListByRoleName("lead").getItem();
        List<String> lectors=  connectionToCommandService.getUserNameListByRoleName("lector").getItem();
        users.addAll(admins);
        users.addAll(lectors);
        return users;
    }

    public List<String> getUserNamesByRole(String roleName) {
        List<String> ans=connectionToCommandService.getUserNameListByRoleName(roleName).getItem();
        return ans;
    }

    public void pdf(byte[] pdfBytes, List<String> lectorNickName) throws IOException {
        for (String lector: lectorNickName) {
            try {
                String s=System.getProperty("user.dir");
                OutputStream out = new FileOutputStream("report.pdf");
                out.write(pdfBytes);
                out.close();
                File file2=new File(System.getProperty("user.dir"),"report.pdf");
                String chatId =connectionToCommandService.getChatIdByUserName(lector);
                SendDocument response = new SendDocument(chatId,file2);
                TelegramBotSingleton.getInstance().execute(response);
            }
            catch (Exception e){
                e.printStackTrace();
            }
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
