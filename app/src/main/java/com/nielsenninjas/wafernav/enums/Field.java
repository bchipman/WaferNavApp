package com.nielsenninjas.wafernav.enums;

/**
 Created by Brian on 4/17/2017.
 */

public enum Field {
    ERROR("error"),
    DIRECTIVE("directive"),
    CLIENT_ID("clientId"),
    DEVICE_MODEL("deviceModel"),
    LOT_ID("lotId"),
    SLT_ID("sltId"),
    BIB_IDS("bibIds"),
    BLU_ID("bluId"),
    SLT_SITE_NAME("sltSiteName"),
    SLT_SITE_DESCRIPTION("sltSiteDescription"),
    SLT_SITE_LOCATION("sltSiteLocation"),
    BLU_SITE_NAME("bluSiteName"),
    BLU_SITE_DESCRIPTION("bluSiteDescription"),
    BLU_SITE_LOCATION("bluSiteLocation"),
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
