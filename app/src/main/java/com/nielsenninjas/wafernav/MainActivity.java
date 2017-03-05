package com.nielsenninjas.wafernav;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    // Logging
    private static final String TAG = "MainActivity";

    // Connection info
    private static final String DEFAULT_BROKER_URL = "tcp://iot.eclipse.org:1883";
    private static final String DEFAULT_PUB_TOPIC = "wafernav/location_requests";
    private static final String DEFAULT_SUB_TOPIC = "wafernav/location_data";
    private static final String CLIENT_ID = UUID.randomUUID().toString();
    private String brokerUrl;
    private String pubTopic;
    private String subTopic;

    // UI elements
    protected EditText mEditTextBrokerUrl;
    protected EditText mEditTextPubTopic;
    protected Spinner mSpinnerSubTopic;
    protected EditText mEditTextId;
    protected TextView mTextViewOutput;
    protected ScrollView mScrollViewOutput;

    // MQTT
    private MqttAndroidClient mqttAndroidClient;
    private IMqttToken mqttSubToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "onCreate()");

        // Set the UI elements
        mEditTextBrokerUrl = (EditText) findViewById(R.id.editTextBrokerUrl);
        mEditTextPubTopic = (EditText) findViewById(R.id.editTextPubTopic);
        mEditTextId = (EditText) findViewById(R.id.editTextId);
        mTextViewOutput = (TextView) findViewById(R.id.textViewOutput);
        mScrollViewOutput = (ScrollView) findViewById(R.id.scrollViewOutput);
        mEditTextBrokerUrl.setEnabled(false);

        mSpinnerSubTopic = (Spinner) findViewById(R.id.spinnerSubTopic);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.sub_topics, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerSubTopic.setAdapter(adapter);


        // Set default data
        mEditTextBrokerUrl.setText(DEFAULT_BROKER_URL);
        mEditTextPubTopic.setText(DEFAULT_PUB_TOPIC);

        // Pressing enter on keyboard triggers 'Publish' button
        mEditTextId.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    publishButtonHandler(mEditTextId);
                }
                return false;
            }
        });

        setConnectionInfoStrings();
        initMqtt();
    }

    private void setConnectionInfoStrings() {
        brokerUrl = mEditTextBrokerUrl.getText().toString();
        pubTopic = mEditTextPubTopic.getText().toString();
        subTopic = mSpinnerSubTopic.getSelectedItem().toString();
    }

    private void initMqtt() {
        mqttAndroidClient = new MqttAndroidClient(this.getApplicationContext(), brokerUrl, CLIENT_ID);
        mqttAndroidClient.setCallback(new SubscribeCallback());

        try {
            mqttAndroidClient.connect(null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    System.out.println("Connection Success!");
                    try {
                        mqttSubToken = mqttAndroidClient.subscribe(subTopic, 0);
                        System.out.println("Subscribed to " + subTopic);
                    }
                    catch (MqttException ex) {
                        ex.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    System.out.println("Connection Failure!");
                }
            });
        }
        catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void resubscribe() {
        try {
            for (String topic : mqttSubToken.getTopics()) {
                mqttAndroidClient.unsubscribe(topic);
            }
            mqttSubToken = mqttAndroidClient.subscribe(subTopic, 0);
        }
        catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private class SubscribeCallback implements MqttCallback {

        @Override
        public void connectionLost(Throwable cause) {
            System.out.println("Connection was lost!");
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            System.out.println("Message Arrived!: " + topic + ": " + new String(message.getPayload()));
            mTextViewOutput.append("\n" + topic + ": " + new String(message.getPayload()));

            // Auto scroll to bottom
            mScrollViewOutput.post(new Runnable() {
                @Override
                public void run() {
                    mScrollViewOutput.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            System.out.println("Delivery Complete!");
        }
    }

    public void publishButtonHandler(View view) {
        // Dismiss keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditTextId.getWindowToken(), 0);

        // Get text field and clear it
        String idString = mEditTextId.getText().toString();
        //mEditTextId.setText(null);

        // Just return if nothing entered
        if (idString == null || idString.isEmpty()) {
            return;
        }

        // Parse the non-null number-only string
        int id = Integer.parseInt(idString);

        // Create JSON string to publish, e.g. {"id":123}
        Map<String, Object> returnMap = new HashMap<>();
        returnMap.put("id", id);
        String returnJsonString = null;
        try {
            returnJsonString = new ObjectMapper().writeValueAsString(returnMap);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Publishing message..");
        try {
            mqttAndroidClient.publish(pubTopic, new MqttMessage(returnJsonString.getBytes()));
        }
        catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void ResubscribeButtonHandler(View view) {
        setConnectionInfoStrings();
        resubscribe();
    }

    public void ClearLogButtonHandler(View view) {
        mTextViewOutput.setText(null);
    }
}
