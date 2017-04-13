package com.nielsenninjas.wafernav;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.nielsenninjas.wafernav.barcodereader.BarcodeCaptureActivity;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements EnterIdFragment.OnFragmentInteractionListener, AssignHandlerFragment.OnFragmentInteractionListener, DeliveringToFragment.OnFragmentInteractionListener {

    // Logging
    private static final String TAG = "MainActivity";
    private static final String TAG_BARCODE = "BarcodeMain";

    // Connection info
    private static final String BROKER_URL = "tcp://iot.eclipse.org:1883";
    private static final String PUB_TOPIC = "wafernav/location_requests";
    private static final String SUB_TOPIC = "wafernav/location_data";
    private static final String CLIENT_ID = UUID.randomUUID().toString();

    private static final int RC_BARCODE_CAPTURE = 9001;

    // UI elements
    protected AutoCompleteTextView mAutoCompleteTextViewId; // TODO I WILL BE NULL FIX ME
    protected ScrollView mScrollViewOutputLog;
    protected TextView mTextViewOutputLog;

    // MQTT
    private MqttAndroidClient mqttAndroidClient;
    private IMqttToken mqttSubToken;

    // Reference to instance
    private MainActivity mainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivity = this;

        Fragment fragment = EnterIdFragment.newInstance("param1", "param2");

        getFragmentManager().beginTransaction().replace(R.id.fragmentContainer, fragment).commit();

        Log.i(TAG, "onCreate()");

        // Set the UI elements
        mTextViewOutputLog = (TextView) findViewById(R.id.textViewOutputLog);
        mScrollViewOutputLog = (ScrollView) findViewById(R.id.scrollViewOutputLog);

        initMqtt();
    }

    private void initMqtt() {
        mqttAndroidClient = new MqttAndroidClient(this.getApplicationContext(), BROKER_URL, CLIENT_ID);
        mqttAndroidClient.setCallback(new SubscribeCallback());

        try {
            mqttAndroidClient.connect(null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    try {
                        mqttSubToken = mqttAndroidClient.subscribe(SUB_TOPIC, 0);
                        Toast
                                .makeText(getApplicationContext(), "Subscribed to " + SUB_TOPIC, Toast.LENGTH_SHORT)
                                .show();
                    }
                    catch (MqttException ex) {
                        ex.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast
                            .makeText(getApplicationContext(), "Failed to connect to " + BROKER_URL + "!", Toast.LENGTH_SHORT)
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
            Toast.makeText(getApplicationContext(), "Subscribed to " + SUB_TOPIC, Toast.LENGTH_SHORT).show();
        }
        catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStackImmediate();
            Log.i(TAG, "onBackPressed 1");
        }
        else {
            Log.i(TAG, "onBackPressed 2");
            super.onBackPressed();
        }
    }
    private class SubscribeCallback implements MqttCallback {

        @Override
        public void connectionLost(Throwable cause) {
            Toast.makeText(getApplicationContext(), "Lost connection!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void messageArrived(String topic, final MqttMessage message) throws Exception {
            String jsonMessage = new String(message.getPayload());
            System.out.println("Message Arrived!: " + topic + ": " + jsonMessage);
            mTextViewOutputLog.append("\n" + topic + ": " + jsonMessage);


            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> jsonMap;
            String handlerId = null;
            String handlerLocation = null;

            try {
                jsonMap = mapper.readValue(jsonMessage, new TypeReference<Map<String, String>>() {
                });
                handlerId = jsonMap.get("id");
                handlerLocation = jsonMap.get("location");
            }
            catch (IOException e) {
                e.printStackTrace();
            }


            Fragment fragment = AssignHandlerFragment.newInstance(handlerId, handlerLocation);

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.fragmentContainer, fragment);
            ft.addToBackStack(null);
            ft.commit();



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

    @Override
    public void publishButtonHandler(View view) {
        mAutoCompleteTextViewId = (AutoCompleteTextView) view;
        // Get text field
        String idString = mAutoCompleteTextViewId.getText().toString();

        // Display toast message and return if nothing entered
        if (idString == null || idString.isEmpty()) {
            Toast.makeText(getApplicationContext(), "ID cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create JSON string to publish, e.g. {"id":123}
        Map<String, String> returnMap = new HashMap<>();
        returnMap.put("id", idString);
        String returnJsonString = null;
        try {
            returnJsonString = new ObjectMapper().writeValueAsString(returnMap);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Publishing message..");
        try {
            mqttAndroidClient.publish(PUB_TOPIC, new MqttMessage(returnJsonString.getBytes()));
        }
        catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startDeliveryButtonHandler(String id, String loc) {
        Log.i(TAG, "startDeliveryButtonHandler");
        Fragment fragment = DeliveringToFragment.newInstance(id, loc);

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragmentContainer, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public void confirmDeliveryButtonHandler() {
        Log.i(TAG, "confirmDeliveryButtonHandler");
    }

    @Override
    public void readBarcodeButtonHandler(View view) {
        mAutoCompleteTextViewId = (AutoCompleteTextView) view;

        // launch barcode activity.
        Intent intent = new Intent(this, BarcodeCaptureActivity.class);
        intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
        intent.putExtra(BarcodeCaptureActivity.UseFlash, false);

        startActivityForResult(intent, RC_BARCODE_CAPTURE);
    }

    public void clearOutputLogButtonHandler(View view) {
        mTextViewOutputLog.setText(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    mTextViewOutputLog.append("\n" + getResources().getString(R.string.barcode_success) + ".");
                    mAutoCompleteTextViewId.setText(barcode.displayValue);
                    Log.d(TAG_BARCODE, "Barcode read: " + barcode.displayValue);
                }
                else {
                    mTextViewOutputLog.append("\n" + getResources().getString(R.string.barcode_failure));
                    Log.d(TAG_BARCODE, "No barcode captured, intent data is null");
                }
            }
            else {
                mTextViewOutputLog.append(String.format(getString(R.string.barcode_error), CommonStatusCodes.getStatusCodeString(resultCode)));
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
