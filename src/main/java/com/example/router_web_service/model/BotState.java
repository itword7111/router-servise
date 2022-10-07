package com.example.router_web_service.model;

public enum BotState {
    ADD_TASK, //waiting of entering task text
    ADD_GROUP_NAME,
    ADD_USER_TO_GROUP,
    DELETE_USER_FROM_GROUP,
    GET_GROUP,
    DELETE_GROUP,
    RENAME_GROUP,
    DELETE_USER,
    ADD_USERNAME_FOR_CREATION_USER,
    ADD_ROLE_OF_USER_FOR_CREATION_USER,
    NEW_NAME_FOR_GROUP,
    ADD_GROUP_OF_USER_FOR_CREATION_USER
}
