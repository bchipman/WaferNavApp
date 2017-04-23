package com.nielsenninjas.wafernav;

import android.util.Log;
import com.nielsenninjas.wafernav.enums.Operation;

/**
 Created by brian on 4/23/2017.
 */

public class StateDto {

    // Logging
    private static final String TAG = "WNAV-" + StateDto.class.getSimpleName();

    private static StateDto INSTANCE = new StateDto();

    private Operation mOperation;
    private String mBluId;
    private String mBluLocation;
    private String[] mBibIds;
    private String mSltId;
    private String mSltLocation;
    private String mLotId;

    private StateDto() {
    }

    public static StateDto getInstance() {
        return INSTANCE;
    }

    public static void clearState() {
        INSTANCE = new StateDto();
    }

    public Operation getOperation() {
        Log.v(TAG, "Operation ()");
        return mOperation;
    }
    public void setOperation(Operation operation) {
        Log.v(TAG, "setOperation()");
        this.mOperation = operation;
    }
    public String getLotId() {
        Log.v(TAG, "getLotId()");
        return mLotId;
    }
    public void setLotId(String lotId) {
        Log.v(TAG, "setLotId()");
        this.mLotId = lotId;
    }
    public String getBluId() {
        Log.v(TAG, "getBluId()");
        return mBluId;
    }
    public void setBluId(String bluId) {
        Log.v(TAG, "setBluId()");
        this.mBluId = bluId;
    }
    public String getBluLocation() {
        Log.v(TAG, "getBluLocation()");
        return mBluLocation;
    }
    public void setBluLocation(String bluLocation) {
        Log.v(TAG, "setBluLocation()");
        this.mBluLocation = bluLocation;
    }
    public String[] getBibIds() {
        Log.v(TAG, "getBibIds()");
        return mBibIds;
    }
    public void setBibIds(String[] bibIds) {
        Log.v(TAG, "setBibIds()");
        this.mBibIds = bibIds;
    }
    public String getSltId() {
        Log.v(TAG, "getSltId()");
        return mSltId;
    }
    public void setSltId(String sltId) {
        Log.v(TAG, "setSltId()");
        this.mSltId = sltId;
    }
    public String getSltLocation() {
        Log.v(TAG, "getSltLocation()");
        return mSltLocation;
    }
    public void setSltLocation(String sltLocation) {
        Log.v(TAG, "setSltLocation()");
        this.mSltLocation = sltLocation;
    }
}
