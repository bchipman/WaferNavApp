package com.nielsenninjas.wafernav;

import android.app.Fragment;
import android.util.Log;
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
        Log.i(TAG, "Message Arrived!: " + topic + ": " + jsonMessage);

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

        Fragment fragment = null;

        String directive = jsonMap.get("directive");
        switch(directive) {
            case ("GET_NEW_BLU_RETURN"):
                Log.i(TAG, "GET_NEW_BLU_RETURN");
                String handlerId = jsonMap.get("bluId");
                String handlerLocation = jsonMap.get("bluInfo");
                fragment = AssignHandlerFragment.newInstance(handlerId, handlerLocation);
                break;
            case ("COMPLETE_NEW_BLU_RETURN"):
                Log.i(TAG, "COMPLETE_NEW_BLU_RETURN");
                String confirmed = jsonMap.get("confirm");
                if (!confirmed.equals("true")) {
                    Log.e(TAG, "Confirmed was not true.");
                    return;
                }
                fragment = DeliveryCompleteFragment.newInstance();
                break;
            case ("GET_NEW_SLT_RETURN"):
                Log.i(TAG, "GET_NEW_SLT_RETURN");
                //TODO parse return json, pass to new fragment
                break;
            default:
                Log.i(TAG, "unknown directive, returning");
                return;
        }

        mMainActivity.changeFragment(fragment);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
            System.out.println("Delivery Complete!");
        }
}
