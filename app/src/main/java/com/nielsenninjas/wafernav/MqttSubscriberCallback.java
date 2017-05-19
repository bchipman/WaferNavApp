package com.nielsenninjas.wafernav;

import android.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.nielsenninjas.wafernav.enums.Directive;
import com.nielsenninjas.wafernav.enums.Field;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.IOException;
import java.util.Map;

/**
 Created by Brian on 4/16/2017.
 */

public class MqttSubscriberCallback implements MqttCallback {

    private static final String TAG = "WNAV-MqttSubCallback";
    private MainActivity mMainActivity;

    public MqttSubscriberCallback(MainActivity mainActivity) {
        this.mMainActivity = mainActivity;
    }

    @Override
    public void connectionLost(Throwable cause) {
        mMainActivity.makeShortToast("Lost connection!");
    }

    @Override
    public void messageArrived(String topic, final MqttMessage message) throws Exception {
        String jsonMessage = new String(message.getPayload());
        Log.w(TAG, "Message Arrived!: " + topic + ": " + jsonMessage);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> jsonMap = null;

        try {
            jsonMap = mapper.readValue(jsonMessage, new TypeReference<Map<String, String>>() {
            });
        }
        catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error reading JSON message");
        }

        if (jsonMap == null) {
            Log.e(TAG, "jsonMap is null, returning");
            return;
        }

        if (!validateReceivedMessage(jsonMap)) {
            Log.e(TAG, "Failed to validate received message!");
            return;
        }

        if (!jsonMap.get(Field.CLIENT_ID.field()).equals(mMainActivity.getClientId())) {
            Log.e(TAG, "Ignoring message because it is meant for a different client!");
            return;
        }

        Fragment fragment = null;
        Directive directive = Directive.valueOf(jsonMap.get(Field.DIRECTIVE.field()));

        String id;
        String siteName;
        String siteDescription;
        String siteLocation;
        String confirmed;

        Log.i(TAG, directive.toString());

        switch(directive) {
            case ERROR:
                mMainActivity.makeShortToast(jsonMap.get(Field.ERROR.field()));
                break;

            case GET_NEW_BLU_RETURN:
                id = jsonMap.get(Field.BLU_ID.field());
                siteName = jsonMap.get(Field.BLU_SITE_NAME.field());
                siteDescription = jsonMap.get(Field.BLU_SITE_DESCRIPTION.field());
                siteLocation = jsonMap.get(Field.BLU_SITE_LOCATION.field());
                StateDto.getInstance().setBluId(id);
                StateDto.getInstance().setBluSiteName(siteName);
                StateDto.getInstance().setBluSiteDescription(siteDescription);
                StateDto.getInstance().setBluSiteLocation(siteLocation);
                fragment = DeliveringToFragment.newInstance(mMainActivity.getCurrentOperation(), id, siteName, siteDescription, siteLocation);
                break;

            case COMPLETE_NEW_BLU_RETURN:
                confirmed = jsonMap.get(Field.CONFIRM.field());
                if (!confirmed.equals(Field.TRUE.field())) {
                    Log.e(TAG, "Confirmed was not true.");
                    return;
                }
                fragment = DeliveryCompleteFragment.newInstance(mMainActivity.getCurrentOperation());
                break;

            case GET_NEW_SLT_RETURN:
                id = jsonMap.get(Field.SLT_ID.field());
                siteName = jsonMap.get(Field.SLT_SITE_NAME.field());
                siteDescription = jsonMap.get(Field.SLT_SITE_DESCRIPTION.field());
                siteLocation = jsonMap.get(Field.SLT_SITE_LOCATION.field());;
                StateDto.getInstance().setSltId(id);
                StateDto.getInstance().setSltSiteName(siteName);
                StateDto.getInstance().setSltSiteDescription(siteDescription);
                StateDto.getInstance().setSltSiteLocation(siteLocation);
                fragment = DeliveringToFragment.newInstance(mMainActivity.getCurrentOperation(), id, siteName, siteDescription, siteLocation);
                break;

            case COMPLETE_NEW_SLT_RETURN:
                confirmed = jsonMap.get(Field.CONFIRM.field());
                if (!confirmed.equals(Field.TRUE.field())) {
                    Log.e(TAG, "Confirmed was not true.");
                    return;
                }
                fragment = DeliveryCompleteFragment.newInstance(mMainActivity.getCurrentOperation());
                break;

            case GET_DONE_BLU_RETURN:
                // same as GET_NEW_BLU_RETURN above
                id = jsonMap.get(Field.BLU_ID.field());
                siteName = jsonMap.get(Field.BLU_SITE_NAME.field());
                siteDescription = jsonMap.get(Field.BLU_SITE_DESCRIPTION.field());
                siteLocation = jsonMap.get(Field.BLU_SITE_LOCATION.field());
                StateDto.getInstance().setBluId(id);
                StateDto.getInstance().setBluSiteName(siteName);
                StateDto.getInstance().setBluSiteDescription(siteDescription);
                StateDto.getInstance().setBluSiteLocation(siteLocation);
                fragment = DeliveringToFragment.newInstance(mMainActivity.getCurrentOperation(), id, siteName, siteDescription, siteLocation);
                break;

            case COMPLETE_DONE_BLU_RETURN:
                // same as COMPLETE_NEW_SLT_RETURN above
                confirmed = jsonMap.get(Field.CONFIRM.field());
                if (!confirmed.equals(Field.TRUE.field())) {
                    Log.e(TAG, "Confirmed was not true.");
                    return;
                }
                fragment = DeliveryCompleteFragment.newInstance(mMainActivity.getCurrentOperation());
                break;
        }

        mMainActivity.changeFragment(fragment);
    }

    private boolean validateReceivedMessage(Map<String, String> jsonMap) {
        Directive directive;
        try {
            directive = Directive.valueOf(jsonMap.get(Field.DIRECTIVE.field()));
        }
        catch (Exception e) {
            directive = Directive.NULL;
        }

        if (jsonMap.get(Field.CLIENT_ID.field()) == null) {
            return false;
        }

        switch(directive) {
            case ERROR:
                return true;
            case GET_NEW_BLU_RETURN:
            case GET_DONE_BLU_RETURN:
                return true;
                //return jsonMap.get(Field.BLU_ID.field()) != null && jsonMap.get(Field.BLU_SITE_NAME.field()) != null &&
                        //jsonMap.get(Field.BLU_SITE_DESCRIPTION.field())!= null && jsonMap.get(Field.BLU_SITE_LOCATION.field()) != null;

            case GET_NEW_SLT_RETURN:
                return true;
                //return jsonMap.get(Field.SLT_ID.field()) != null && jsonMap.get(Field.SLT_SITE_NAME.field()) != null &&
                        //jsonMap.get(Field.SLT_SITE_DESCRIPTION.field())!= null && jsonMap.get(Field.SLT_SITE_LOCATION.field()) != null;


            case COMPLETE_NEW_BLU_RETURN:
            case COMPLETE_NEW_SLT_RETURN:
            case COMPLETE_DONE_BLU_RETURN:
                return jsonMap.get(Field.CONFIRM.field()) != null;

            case NULL:
                mMainActivity.makeShortToast("No directive received!");
                return false;

            default:
                mMainActivity.makeShortToast("Unknown directive received!");
                return false;
        }

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
            System.out.println("Delivery Complete!");
        }
}
