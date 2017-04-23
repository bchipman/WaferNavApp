package com.nielsenninjas.wafernav;

import android.util.Log;
import android.widget.Toast;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 Created by Brian on 4/16/2017.
 */

public class MqttClient {

    // Logging
    private static final String TAG = "WNAV-MqttClient";

    // Parent activity
    private MainActivity mMainActivity;

    // Connection info
    private static final String BROKER_URL = "tcp://iot.eclipse.org:1883";
    private static final String PUB_TOPIC = "wafernav/location_requests";
    private static final String SUB_TOPIC = "wafernav/location_data";
    private static final String CLIENT_ID = UUID.randomUUID().toString();

    // MQTT
    private MqttAndroidClient mqttAndroidClient;
    private IMqttToken mqttSubToken;

    public MqttClient(MainActivity mainActivity) {
        this.mMainActivity = mainActivity;
        initMqtt();
    }

    private void initMqtt() {
        mqttAndroidClient = new MqttAndroidClient(mMainActivity.getApplicationContext(), BROKER_URL, CLIENT_ID);
        mqttAndroidClient.setCallback(new MqttSubscriberCallback(mMainActivity));

        try {
            mqttAndroidClient.connect(null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    try {
                        mqttSubToken = mqttAndroidClient.subscribe(SUB_TOPIC, 0);
                        Toast
                                .makeText(mMainActivity.getApplicationContext(), "Subscribed to " + SUB_TOPIC, Toast.LENGTH_SHORT)
                                .show();
                    }
                    catch (MqttException ex) {
                        ex.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast
                            .makeText(mMainActivity.getApplicationContext(), "Failed to connect to " + BROKER_URL + "!", Toast.LENGTH_SHORT)
                            .show();
                }
            });
        }
        catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void resubscribe() {
        try {
            // Unsubscribe from all topics
            for (String topic : mqttSubToken.getTopics()) {
                mqttAndroidClient.unsubscribe(topic);
            }
            // Subscribe to new topic
            mqttSubToken = mqttAndroidClient.subscribe(SUB_TOPIC, 0);
            Toast.makeText(mMainActivity.getApplicationContext(), "Subscribed to " + SUB_TOPIC, Toast.LENGTH_SHORT).show();
        }
        catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void publishMapAsJson(Map<String, Object> map) {
        Log.i(TAG, "publishMapAsJson");

        String returnJsonString = null;
        try {
            returnJsonString = new ObjectMapper().writeValueAsString(map);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        Log.w(TAG, "Publishing message: " + PUB_TOPIC + ": " + returnJsonString);
        try {
            mqttAndroidClient.publish(PUB_TOPIC, new MqttMessage(returnJsonString.getBytes()));
        }
        catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
