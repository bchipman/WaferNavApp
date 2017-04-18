package com.nielsenninjas.wafernav;

import android.app.Fragment;
import android.util.Log;
import com.nielsenninjas.wafernav.enums.Directive;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.IOException;
import java.util.IllegalFormatException;
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
        Directive directive;
        try {
            directive = Directive.valueOf(jsonMap.get("directive"));
        }
        catch (Exception e) {
            directive = Directive.NULL;
        }

        switch(directive) {
            case GET_NEW_BLU_RETURN:
                Log.i(TAG, Directive.GET_NEW_BLU_RETURN.toString());
                String handlerId = jsonMap.get("bluId");
                String handlerLocation = jsonMap.get("bluInfo");
                fragment = AssignHandlerFragment.newInstance(handlerId, handlerLocation);
                break;
            case COMPLETE_NEW_BLU_RETURN:
                Log.i(TAG, Directive.COMPLETE_NEW_BLU_RETURN.toString());
                String confirmed = jsonMap.get("confirm");
                if (!confirmed.equals("true")) {
                    Log.e(TAG, "Confirmed was not true.");
                    return;
                }
                fragment = DeliveryCompleteFragment.newInstance();
                break;
            case GET_NEW_SLT_RETURN:
                Log.i(TAG, Directive.GET_NEW_SLT_RETURN.toString());
                //TODO parse return json, pass to new fragment
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
