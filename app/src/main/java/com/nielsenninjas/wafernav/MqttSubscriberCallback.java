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

    private static final String TAG = "MqttSubscriberCallback";
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

        Fragment fragment;
        Directive directive;
        try {
            directive = Directive.valueOf(jsonMap.get(Field.DIRECTIVE.field()));
        }
        catch (Exception e) {
            directive = Directive.NULL;
        }

        String id;
        String location;
        String confirmed;

        switch(directive) {

            case GET_NEW_BLU_RETURN:
                Log.i(TAG, Directive.GET_NEW_BLU_RETURN.toString());
                id = jsonMap.get(Field.BLU_ID.field());
                location = jsonMap.get(Field.BLU_INFO.field());
                fragment = AssignHandlerFragment.newInstance(mMainActivity.getCurrentOperation(), id, location);
                break;

            case COMPLETE_NEW_BLU_RETURN:
                Log.i(TAG, Directive.COMPLETE_NEW_BLU_RETURN.toString());
                confirmed = jsonMap.get(Field.CONFIRM.field());
                if (!confirmed.equals(Field.TRUE.field())) {
                    Log.e(TAG, "Confirmed was not true.");
                    return;
                }
                fragment = DeliveryCompleteFragment.newInstance(mMainActivity.getCurrentOperation());
                break;

            case GET_NEW_SLT_RETURN:
                Log.i(TAG, Directive.GET_NEW_SLT_RETURN.toString());
                id = jsonMap.get(Field.SLT_ID.field());
                location = jsonMap.get(Field.SLT_INFO.field());
                fragment = DeliveringToFragment.newInstance(mMainActivity.getCurrentOperation(), id, location);
                break;

            case COMPLETE_NEW_SLT_RETURN:
                Log.i(TAG, Directive.COMPLETE_NEW_SLT_RETURN.toString());
                confirmed = jsonMap.get(Field.CONFIRM.field());
                if (!confirmed.equals(Field.TRUE.field())) {
                    Log.e(TAG, "Confirmed was not true.");
                    return;
                }
                fragment = DeliveryCompleteFragment.newInstance(mMainActivity.getCurrentOperation());
                break;

            case GET_DONE_BLU_RETURN:
                // same as GET_NEW_BLU_RETURN above
                Log.i(TAG, Directive.GET_DONE_BLU_RETURN.toString());
                id = jsonMap.get(Field.BLU_ID.field());
                location = jsonMap.get(Field.BLU_INFO.field());
                fragment = AssignHandlerFragment.newInstance(mMainActivity.getCurrentOperation(), id, location);
                break;

            case COMPLETE_DONE_BLU_RETURN:
                // same as COMPLETE_NEW_SLT_RETURN above
                Log.i(TAG, Directive.COMPLETE_DONE_BLU_RETURN.toString());
                confirmed = jsonMap.get(Field.CONFIRM.field());
                if (!confirmed.equals(Field.TRUE.field())) {
                    Log.e(TAG, "Confirmed was not true.");
                    return;
                }
                fragment = DeliveryCompleteFragment.newInstance(mMainActivity.getCurrentOperation());
                break;

            case NULL:
                Log.i(TAG, Directive.NULL.toString());
                mMainActivity.makeShortToast("No directive received!");
                return;

            default:
                Log.i(TAG, Directive.UNKNOWN.toString());
                mMainActivity.makeShortToast("Unknown directive received!");
                return;
        }

        mMainActivity.changeFragment(fragment);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
            System.out.println("Delivery Complete!");
        }
}
