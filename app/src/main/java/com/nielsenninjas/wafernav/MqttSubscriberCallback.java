package com.nielsenninjas.wafernav;

import android.app.Fragment;
import android.util.Log;
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

        Fragment fragment = null;
        Directive directive = Directive.valueOf(jsonMap.get(Field.DIRECTIVE.field()));

        String id;
        String location;
        String confirmed;

        Log.i(TAG, directive.toString());

        switch(directive) {

            case GET_NEW_BLU_RETURN:
                id = jsonMap.get(Field.BLU_ID.field());
                location = jsonMap.get(Field.BLU_INFO.field());
                StateDto.getInstance().setBluId(id);
                StateDto.getInstance().setBluLocation(location);
                fragment = AssignHandlerFragment.newInstance(mMainActivity.getCurrentOperation(), id, location);
                break;

            case ACCEPT_NEW_BLU_RETURN:
                confirmed = jsonMap.get(Field.CONFIRM.field());
                if (!confirmed.equals(Field.TRUE.field())) {
                    Log.e(TAG, "Confirmed was not true.");
                    return;
                }
                id = StateDto.getInstance().getBluId();
                location = StateDto.getInstance().getBluLocation();
                fragment = DeliveringToFragment.newInstance(mMainActivity.getCurrentOperation(), id, location);
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
                location = jsonMap.get(Field.SLT_INFO.field());
                StateDto.getInstance().setSltId(id);
                StateDto.getInstance().setSltLocation(location);
                fragment = DeliveringToFragment.newInstance(mMainActivity.getCurrentOperation(), id, location);
                break;

            case ACCEPT_NEW_SLT_RETURN:
                confirmed = jsonMap.get(Field.CONFIRM.field());
                if (!confirmed.equals(Field.TRUE.field())) {
                    Log.e(TAG, "Confirmed was not true.");
                    return;
                }
                id = StateDto.getInstance().getSltId();
                location = StateDto.getInstance().getSltLocation();
                fragment = EnterStationIdFragment.newInstance(mMainActivity.getCurrentOperation(), id, location);
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
                location = jsonMap.get(Field.BLU_INFO.field());
                StateDto.getInstance().setBluId(id);
                StateDto.getInstance().setBluLocation(location);
                fragment = DeliveringToFragment.newInstance(mMainActivity.getCurrentOperation(), id, location);
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

            case ACCEPT_DONE_BLU_RETURN:
                confirmed = jsonMap.get(Field.CONFIRM.field());
                if (!confirmed.equals(Field.TRUE.field())) {
                    Log.e(TAG, "Confirmed was not true.");
                    return;
                }
                id = StateDto.getInstance().getSltId();
                location = StateDto.getInstance().getSltLocation();
                fragment = EnterStationIdFragment.newInstance(mMainActivity.getCurrentOperation(), id, location);
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

        switch(directive) {

            case GET_NEW_BLU_RETURN:
            case GET_DONE_BLU_RETURN:
                return jsonMap.get(Field.BLU_ID.field()) != null && jsonMap.get(Field.BLU_INFO.field()) != null;

            case GET_NEW_SLT_RETURN:
                return jsonMap.get(Field.SLT_ID.field()) != null && jsonMap.get(Field.SLT_INFO.field()) != null;

            case ACCEPT_NEW_BLU_RETURN:
            case COMPLETE_NEW_BLU_RETURN:
            case ACCEPT_NEW_SLT_RETURN:
            case COMPLETE_NEW_SLT_RETURN:
            case ACCEPT_DONE_BLU_RETURN:
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
