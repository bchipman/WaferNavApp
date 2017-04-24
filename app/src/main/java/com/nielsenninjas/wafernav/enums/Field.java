package com.nielsenninjas.wafernav.enums;

/**
 Created by Brian on 4/17/2017.
 */

public enum Field {
    DIRECTIVE("directive"),
    CLIENT_ID("clientId"),
    DEVICE_MODEL("deviceModel"),
    LOT_ID("lotId"),
    SLT_ID("sltId"),
    SLT_INFO("sltInfo"),
    BIB_IDS("bibIds"),
    BLU_ID("bluId"),
    BLU_INFO("bluInfo"),
    CONFIRM("confirm"),
    TRUE("true");

    private String field;

    Field(String field) {
        this.field = field;
    }

    public String field() {
        return field;
    }
}
