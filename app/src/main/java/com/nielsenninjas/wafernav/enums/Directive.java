package com.nielsenninjas.wafernav.enums;

/**
 Created by Brian on 4/17/2017.
 */

public enum Directive {
    NULL, UNKNOWN, ERROR,

    GET_NEW_BLU,        GET_NEW_BLU_RETURN,
    COMPLETE_NEW_BLU,   COMPLETE_NEW_BLU_RETURN,

    GET_NEW_SLT,        GET_NEW_SLT_RETURN,
    COMPLETE_NEW_SLT,   COMPLETE_NEW_SLT_RETURN,

    GET_DONE_BLU,       GET_DONE_BLU_RETURN,
    COMPLETE_DONE_BLU,  COMPLETE_DONE_BLU_RETURN;
}
