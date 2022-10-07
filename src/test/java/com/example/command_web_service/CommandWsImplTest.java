//package com.example.command_web_service;
//
//import com.example.command_web_service.persist.UserRepository;
//import org.junit.jupiter.api.Test;
//
//class CommandWsImplTest {
//    private UserRepository userRepository = new UserRepository();
//
//    @Test
//    void getChatIdByUserName() {
//        System.out.println(userRepository.getChatIdByUserName("gden"));
//    }
//
//    @Test
//    void getRoleNameByUserName() {
//        System.out.println(userRepository.getRoleNameByUserName("stepan"));
//    }
//
//    @Test
//    void getListOfGroupUsers() {
//        System.out.println(userRepository.getListOfUsersByGroupName("losers"));
//    }
//
//    @Test
//    void insertUser() {
//        userRepository.insertUser("stepan", "losers", "lead");
//        userRepository.insertUser("kate", "losers", "lector");
//        userRepository.insertUser("helen", "winners", "user");
//        userRepository.insertUser("den", "PythonDevelopers", "user");
//    }
//
//    @Test
//    void updateUser() {
//        userRepository.updateUser("den", "losers", "lead");
//    }
//
//    @Test
//    void deleteUser() {
//        userRepository.deleteUser("helen");
//    }
//
//    @Test
//    void getUserNameListByRoleName() {
//        System.out.println(userRepository.getUsersNamesByRoles("lector"));
//    }
//
//    @Test
//    void insertGroup() {
//        userRepository.insertGroup("PythonDevelopers");
//        userRepository.insertGroup("winners");
//        userRepository.insertGroup("losers");
//    }
//
//    @Test
//    void updateGroup() {
//        userRepository.updateGroup("PythonDevelopers", "FrontEndDevelopers");
//    }
//
//    @Test
//    void deleteGroup() {
//        userRepository.deleteGroup("FrontEndDevelopers");
//    }
//
//    @Test
//    void getGroups() {
//        System.out.println(userRepository.getGroups());
//    }
//
//    @Test
//    void getListOfUsers() {
//        System.out.println(userRepository.getListOfUsers());
//    }
//
//    @Test
//    void putChatIdByUserName() {
//        userRepository.putChatIdByUserName("2231", "den");
//        userRepository.putChatIdByUserName("2231", "sam");
//    }
//
//    @Test
//    void getListOfChatIdByRo() {
//        System.out.println(userRepository.getListOfChatIdByRoleName("lead"));
//    }
//}