package com.example.router_web_service;

import com.example.router_web_service.ConnectionToCommandService;
import com.example.router_web_service.model.BotState;
import com.example.router_web_service.model.ReportSender;
import com.example.router_web_service.model.UserForAccounting;
import com.exemple.generate.CommandWs;
import com.exemple.generate.ListOfString;
import com.google.gson.Gson;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

public class TelegramBotApplication extends TelegramBot {
    private Logger logger=Logger.getLogger(TelegramBotApplication.class.getName());
    private Map<String, BotState> botStateCash = new HashMap<String, BotState>();
    private Map<String, String> groupCash = new HashMap<String, String>();
    private Map<String, String> roleCash = new HashMap<String, String>();
    private Map<String, String> usernameForCreationCash = new HashMap<String, String>();
    private Map<String, String> groupNameForEditCash = new HashMap<String, String>();
    private Gson gson=new Gson();
    private CommandWs connectionToCommandService=new ConnectionToCommandService().getInstance();

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
        try {
            BotState botState = botStateCash.get(userName);
            if (commandName.equals("/quit")) {
                botStateCash.remove(userName);
                groupCash.remove(userName);
            } else if (botState != null) {
                executeCashedCommand(botState, commandName, userName, chatId);
            } else {
                executeCommand(commandName, chatId, userName);
            }
        }
        catch (Exception e){
            System.out.println("Exception "+ e.getCause());
            logger.log(Level.WARNING,"Exception "+ e.getCause());
        }
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

    private void executeCashedCommand(BotState botState, String parameter, String userName,Long chatId){
        switch (botState){
            //for user
            case ADD_TASK:{
                RouteRequest("POST","http://localhost:8080/Accounting/insertNewTrack",new ReportSender(userName,parameter,Timestamp.valueOf(LocalDateTime.now())));
                botStateCash.remove(userName);
                break;
            }
            case ADD_GROUP_NAME:{
                connectionToCommandService.insertGroup(parameter);
                botStateCash.remove(userName);
                break;
            }
            case ADD_USER_TO_GROUP:{
                String role=connectionToCommandService.getRoleNameByUserName(parameter);
                connectionToCommandService.updateUser(parameter,groupCash.get(userName),role);
                SendMessage response = new SendMessage(chatId,"введите никнейм для добавления в группу (без @) \nдля выхода из режима добавления введите /quit");
                this.execute(response);
                break;
            }
            case DELETE_USER_FROM_GROUP:{
                String role=connectionToCommandService.getRoleNameByUserName(parameter);
                connectionToCommandService.updateUser(parameter,"null",role);
                SendMessage response = new SendMessage(chatId,"введите имя пользователя для удаления из группы (без @) \nдля выхода из режима удаления введите /quit");
                this.execute(response);
                break;
            }
            case GET_GROUP:{
                ListOfString users=  connectionToCommandService.getListOfUsersByGroupName(parameter);//
                StringBuilder message=new StringBuilder();
                for (String user:users.getItem()) {
                    message.append(user).append("\n");
                }
                SendMessage response = new SendMessage(chatId, "Группa "+parameter+"\nУчастники :\n"+message);
                this.execute(response);
                botStateCash.remove(userName);
                groupCash.put(userName,parameter);
                break;
            }
            case DELETE_GROUP:{
                connectionToCommandService.deleteGroup(parameter);
                botStateCash.remove(userName);
                break;
            }
            case DELETE_USER:{
                connectionToCommandService.deleteUser(parameter);
                botStateCash.remove(userName);
                break;
            }
            case ADD_USERNAME_FOR_CREATION_USER:{
                usernameForCreationCash.put(userName,parameter);
                botStateCash.put(userName,BotState.ADD_ROLE_OF_USER_FOR_CREATION_USER);
                SendMessage response = new SendMessage(chatId,"введите роль пользователя (user, lead или lector) для выхода из операции введите /quit");
                this.execute(response);
                break;
            }
            case ADD_ROLE_OF_USER_FOR_CREATION_USER:{
                roleCash.put(userName,parameter);
                botStateCash.put(userName,BotState.ADD_GROUP_OF_USER_FOR_CREATION_USER);
                SendMessage response = new SendMessage(chatId,"введите группу пользователя для выхода из операции введите /quit");
                this.execute(response);
                break;
            }
            case ADD_GROUP_OF_USER_FOR_CREATION_USER:{
                connectionToCommandService.insertUser(usernameForCreationCash.get(userName),parameter,roleCash.get(userName));
                RouteRequest("POST","http://localhost:8080/Accounting/insertNewUser",new UserForAccounting(usernameForCreationCash.get(userName)));
                usernameForCreationCash.remove(userName);
                botStateCash.remove(userName);
                roleCash.remove(userName);
                break;
            }
            case RENAME_GROUP:{
                groupNameForEditCash.put(userName,parameter);
                botStateCash.put(userName,BotState.NEW_NAME_FOR_GROUP);
                SendMessage response = new SendMessage(chatId,"введите новое название группы, для прерывания операции введите /quit");
                this.execute(response);
                break;
            }
            case NEW_NAME_FOR_GROUP:{
                connectionToCommandService.updateGroup(groupNameForEditCash.get(userName),parameter);
                groupNameForEditCash.remove(userName);
                botStateCash.remove(userName);
                break;
            }
        }
    }
    private void executeCommand(String commandName, Long chatId, String userName){
        switch (commandName) {
            case "/start": {
                try {
                    connectionToCommandService.getRoleNameByUserName(userName);
                }
                catch (Exception e){
                    connectionToCommandService.insertUser(userName,"winners","lector");
                    RouteRequest("POST","http://localhost:8080/Accounting/insertNewUser",new UserForAccounting(userName));
                }

                connectionToCommandService.putChatIdByUserName(chatId.toString(),userName);
                String responseFromRouter=connectionToCommandService.getRoleNameByUserName(userName);
                String role =gson.fromJson(responseFromRouter, String.class);
                if(role.equals("lead")||role.equals("lector")){
                    SendMessage response = new SendMessage(chatId, "Доступные вам команды:\n" +
                            "/get_groups\n" +
                            "/get_users\n" +
                            "для добавления и удаления пользователей из группы,\n" +
                            "воспользуйтесь сначала командой /get_group \n" +
                            "команды изменения группы\n" +
                            "/add_group /delete_group /rename_group /delete_users_from_group /add_users_to_group \n" +
                            "команды изменения пользователя\n" +
                            "/del_user /add_user\n " +
                            "Создать отчет\n" +
                            "/add_track\n");
                    this.execute(response);
                }
                else if(responseFromRouter.equals("user")){
                    SendMessage response = new SendMessage(chatId,"Ваша роль user\n" +
                            "Доступные вам команды:\n" +
                            "/add_track\n");
                    this.execute(response);
                }
                else {
                    SendMessage response = new SendMessage(chatId,"Пользователь не найден");
                    this.execute(response);
                }
                break;
            }
            case "/get_groups": {
                ListOfString groups=  connectionToCommandService.getGroups();
                StringBuilder message=new StringBuilder();
                for (String group:groups.getItem()) {
                    message.append(group).append("\n");
                }
                SendMessage response = new SendMessage(chatId, "Группы :\n"+message);
                this.execute(response);
                break;
            }
            case "/get_users": {
                ListOfString users = connectionToCommandService.getUserNameListByRoleName("user");
                StringBuilder messageUsers=new StringBuilder();
                for (String user:users.getItem()) {
                    messageUsers.append("@").append(user).append("\n");
                }
                ListOfString admins=  connectionToCommandService.getUserNameListByRoleName("lead");
                StringBuilder messageAdmins=new StringBuilder();
                for (String admin:admins.getItem()) {
                    messageAdmins.append("@").append(admin).append("\n");
                }
                ListOfString lectors=  connectionToCommandService.getUserNameListByRoleName("lector");
                StringBuilder messageLectors=new StringBuilder();
                for (String lector:lectors.getItem()) {
                    messageLectors.append("@").append(lector).append("\n");
                }
                SendMessage response = new SendMessage(chatId, "Пользователи :\n"+messageUsers+" Тимлиды :\n"+messageAdmins+" Лектора :\n"+messageLectors);
                this.execute(response);
                break;
            }
            case "/get_group": {
                botStateCash.put(userName, BotState.GET_GROUP);
                SendMessage response = new SendMessage(chatId, "Введите название группы для получении информации о участниках");
                this.execute(response);
                break;
            }
            case "/add_group": {
                botStateCash.put(userName,BotState.ADD_GROUP_NAME);
                SendMessage response = new SendMessage(chatId, "Введите название группы для создания");
                this.execute(response);
                break;
            }
            case "/delete_group": {
                botStateCash.put(userName,BotState.DELETE_GROUP);
                SendMessage response = new SendMessage(chatId, "Введите название группы для удаления");
                this.execute(response);
                break;
            }
            case "/rename_group": {
                botStateCash.put(userName,BotState.RENAME_GROUP);
                SendMessage response = new SendMessage(chatId, "Введите название группы для изменения названия");
                this.execute(response);
                break;
            }
            case "/add_users_to_group": {
                if(groupCash.get(userName)==null){
                    SendMessage response = new SendMessage(chatId, "Выполните сначала команду /get_group");
                    this.execute(response);
                    break;
                }
                botStateCash.put(userName,BotState.ADD_USER_TO_GROUP);
                SendMessage response = new SendMessage(chatId, "Введите пользователей для добавления в группу");
                this.execute(response);
                break;
            }
            case "/delete_users_from_group": {
                if(groupCash.get(userName)==null){
                    SendMessage response = new SendMessage(chatId, "Выполните сначала команду /get_group");
                    this.execute(response);
                    break;
                }
                botStateCash.put(userName,BotState.DELETE_USER_FROM_GROUP);
                SendMessage response = new SendMessage(chatId, "Введите участника группы для удаления");
                this.execute(response);
                break;
            }
            case "/del_user":{
                botStateCash.put(userName,BotState.DELETE_USER);
                SendMessage response = new SendMessage(chatId, "Введите название пользователя для удаления");
                this.execute(response);
                break;
            }
            case "/add_user":{
                botStateCash.put(userName,BotState.ADD_USERNAME_FOR_CREATION_USER);
                SendMessage response = new SendMessage(chatId, "Введите название пользователя для добавления");
                this.execute(response);
                break;
            }
            // case for user
            case "/add_track": {
                RouterSenderServiceImpl routerSenderService=new RouterSenderServiceImpl();
                botStateCash.put(userName,BotState.ADD_TASK);
                SendMessage response = new SendMessage(chatId, "Введите текст отчета");
                this.execute(response);
                break;
            }
            default: {
                SendMessage response = new SendMessage(chatId, "Команда не найдена");
                this.execute(response);
                break;
            }
        }
    }


}
