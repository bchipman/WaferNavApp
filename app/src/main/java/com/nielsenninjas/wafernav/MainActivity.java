package com.nielsenninjas.wafernav;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
    protected AutoCompleteTextView mAutoCompleteTextViewSubTopic;
    protected AutoCompleteTextView mAutoCompleteTextViewId;
    protected ScrollView mScrollViewOutputLog;
    protected TextView mTextViewOutputLog;

    // MQTT
    private MqttAndroidClient mqttAndroidClient;
    private IMqttToken mqttSubToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Hide keyboard when (1) click non-EditText object, or (2) press enter in EditText object
        setupHideKeyboardListeners(findViewById(R.id.parent));
        Log.i(TAG, "onCreate()");

        // Set the UI elements
        mEditTextBrokerUrl = (EditText) findViewById(R.id.editTextBrokerUrl);
        mEditTextBrokerUrl.setEnabled(false);
        mEditTextPubTopic = (EditText) findViewById(R.id.editTextPubTopic);
        mEditTextPubTopic.setEnabled(false);

        mTextViewOutputLog = (TextView) findViewById(R.id.textViewOutputLog);
        mScrollViewOutputLog = (ScrollView) findViewById(R.id.scrollViewOutputLog);

        // AutoCompleteTextView for sub topic
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.sub_topics, android.R.layout.simple_dropdown_item_1line);
        mAutoCompleteTextViewSubTopic = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextViewSubTopic);
        mAutoCompleteTextViewSubTopic.setAdapter(adapter);

        // AutoCompleteTextView for IDs
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.ids, android.R.layout.simple_dropdown_item_1line);
        mAutoCompleteTextViewId = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextViewId);
        mAutoCompleteTextViewId.setAdapter(adapter2);

        // Auto resubscribe when sub topic loses focus
        mAutoCompleteTextViewSubTopic.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    Log.i(TAG, "mAutoCompleteTextViewSubTopic does not have focus!");
                    setConnectionInfoStrings();
                    resubscribe();
                }
            }
        });


        // Set default data
        mEditTextBrokerUrl.setText(DEFAULT_BROKER_URL);
        mEditTextPubTopic.setText(DEFAULT_PUB_TOPIC);
        mAutoCompleteTextViewSubTopic.setText(DEFAULT_SUB_TOPIC);

        setConnectionInfoStrings();
        initMqtt();

        // Focus publish button when start app
        findViewById(R.id.buttonPublish).requestFocus();
        hideKeyboard();
    }

    private void setupHideKeyboardListeners(final View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideKeyboard();
                    return false;
                }
            });
        }
        // Set up editor listener to hide keyboard when press enter in TextEdit object
        else {
            ((EditText) view).setOnEditorActionListener(new TextView.OnEditorActionListener() {
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                        hideKeyboard();
                        // if this is mEditTextId, also trigger the 'Publish' button when press enter
                        if (view == mAutoCompleteTextViewId) {
                            publishButtonHandler(mAutoCompleteTextViewId);
                        }
                    }
                    return false;
                }
            });
        }

        // If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupHideKeyboardListeners(innerView);
            }
        }
    }

    private void hideKeyboard() {
        InputMethodManager inm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (getCurrentFocus() != null) {
            inm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } else {
            Log.w(TAG, "I WOULD HAVE CRASHED BECAUSE NOTHING IS FOCUSED!!");
        }
        findViewById(R.id.parent).clearFocus();
    }

    private void setConnectionInfoStrings() {
        brokerUrl = mEditTextBrokerUrl.getText().toString();
        pubTopic = mEditTextPubTopic.getText().toString();
        subTopic = mAutoCompleteTextViewSubTopic.getText().toString();
    }

    private void initMqtt() {
        mqttAndroidClient = new MqttAndroidClient(this.getApplicationContext(), brokerUrl, CLIENT_ID);
        mqttAndroidClient.setCallback(new SubscribeCallback());

        try {
            mqttAndroidClient.connect(null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    try {
                        mqttSubToken = mqttAndroidClient.subscribe(subTopic, 0);
                        Toast.makeText(getApplicationContext(), "Subscribed to " + subTopic, Toast.LENGTH_SHORT).show();
                    }
                    catch (MqttException ex) {
                        ex.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(getApplicationContext(), "Failed to connect to " + brokerUrl + "!", Toast.LENGTH_SHORT).show();
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
            mqttSubToken = mqttAndroidClient.subscribe(subTopic, 0);
            Toast.makeText(getApplicationContext(), "Subscribed to " + subTopic, Toast.LENGTH_SHORT).show();
        }
        catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private class SubscribeCallback implements MqttCallback {

        @Override
        public void connectionLost(Throwable cause) {
            Toast.makeText(getApplicationContext(), "Lost connection!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            System.out.println("Message Arrived!: " + topic + ": " + new String(message.getPayload()));
            mTextViewOutputLog.append("\n" + topic + ": " + new String(message.getPayload()));

            // Auto scroll to bottom
            mScrollViewOutputLog.post(new Runnable() {
                @Override
                public void run() {
                    mScrollViewOutputLog.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            System.out.println("Delivery Complete!");
        }
    }

    public void publishButtonHandler(View view) {
        // Get text field
        String idString = mAutoCompleteTextViewId.getText().toString();

        // Display toast message and return if nothing entered
        if (idString == null || idString.isEmpty()) {
            Toast.makeText(getApplicationContext(), "ID cannot be empty", Toast.LENGTH_SHORT).show();
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

    public void ClearOutputLogButtonHandler(View view) {
        mTextViewOutputLog.setText(null);
    }
}
