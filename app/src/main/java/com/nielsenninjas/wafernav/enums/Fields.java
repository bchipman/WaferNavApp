package com.nielsenninjas.wafernav.enums;

/**
 Created by Brian on 4/17/2017.
 */

public enum Fields {
    DIRECTIVE("directive"),
    LOT_ID("lotId"),
    SLT_ID("sltId"),
    BIB_IDS("bibIds"),
    BLU_ID("bluId"),
    BLU_INFO("bluInfo"),
    CONFIRM("confirm");

    private String field;

    Fields(String field) {
        this.field = field;
    }

    public String field() {
        return field;
    }
}