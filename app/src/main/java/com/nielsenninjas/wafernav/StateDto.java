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
    private String mBluSiteName;
    private String mBluSiteDescription;
    private String mBluSiteLocation;
    private String[] mBibIds;
    private String mSltId;
    private String mSltSiteName;
    private String mSltSiteDescription;
    private String mSltSiteLocation;
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

    public void setBluSiteLocation(String bluSiteLocation) {
        Log.v(TAG, "setBluSiteLocation()");
        this.mBluSiteName = bluSiteLocation;
    }

    public void setBluSiteDescription(String bluSiteDescription) {
        Log.v(TAG, "setBluSiteDescription()");
        this.mBluSiteName = bluSiteDescription;
    }

    public void setBluSiteName(String bluSiteName) {
        Log.v(TAG, "setBluSiteName()");
        this.mBluSiteName = bluSiteName;
    }

    public String getBluSiteLocation() {
        Log.v(TAG, "getBluSiteLocation()");
        return mBluSiteLocation;
    }

    public String getBluSiteName() {
        Log.v(TAG, "getBluSiteName()");
        return mBluSiteName;
    }

    public String getBluSiteDescription() {
        Log.v(TAG, "getBluSiteDescription()");
        return mBluSiteDescription;
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
    public void setSltSiteLocation(String sltSiteLocation) {
        Log.v(TAG, "setSltSiteLocation()");
        this.mSltSiteName = sltSiteLocation;
    }

    public void setSltSiteDescription(String sltSiteDescription) {
        Log.v(TAG, "setSltSiteDescription()");
        this.mSltSiteName = sltSiteDescription;
    }

    public void setSltSiteName(String sltSiteName) {
        Log.v(TAG, "setSltSiteName()");
        this.mSltSiteName = sltSiteName;
    }

    public String getSltSiteLocation() {
        Log.v(TAG, "getSltSiteLocation()");
        return mSltSiteLocation;
    }

    public String getSltSiteName() {
        Log.v(TAG, "getSltSiteName()");
        return mSltSiteName;
    }

    public String getSltSiteDescription() {
        Log.v(TAG, "getSltSiteDescription()");
        return mSltSiteDescription;
    }
}
