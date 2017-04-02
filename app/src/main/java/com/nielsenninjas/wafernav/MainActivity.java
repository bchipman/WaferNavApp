package com.nielsenninjas.wafernav;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.nielsenninjas.wafernav.barcodereader.BarcodeCaptureActivity;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.*;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

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
        mTextViewOutputLog = (TextView) findViewById(R.id.textViewOutputLog);
        mScrollViewOutputLog = (ScrollView) findViewById(R.id.scrollViewOutputLog);

        // AutoCompleteTextView for IDs
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.ids, android.R.layout.simple_dropdown_item_1line);
        mAutoCompleteTextViewId = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextViewId);
        mAutoCompleteTextViewId.setAdapter(adapter);

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
        }
        else {
            Log.w(TAG, "I WOULD HAVE CRASHED BECAUSE NOTHING IS FOCUSED!!");
        }
        findViewById(R.id.parent).clearFocus();
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
                        Toast.makeText(getApplicationContext(), "Subscribed to " + SUB_TOPIC, Toast.LENGTH_SHORT).show();
                    }
                    catch (MqttException ex) {
                        ex.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(getApplicationContext(), "Failed to connect to " + BROKER_URL + "!", Toast.LENGTH_SHORT).show();
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
        //int id = Integer.parseInt(idString);

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

    public void ClearOutputLogButtonHandler(View view) {
        mTextViewOutputLog.setText(null);
    }

    public void ReadBarcodeButtonHandler(View view) {
        // launch barcode activity.
        Intent intent = new Intent(this, BarcodeCaptureActivity.class);
        intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
        intent.putExtra(BarcodeCaptureActivity.UseFlash, false);

        startActivityForResult(intent, RC_BARCODE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    mTextViewOutputLog.append(getResources().getString(R.string.barcode_success));
                    mAutoCompleteTextViewId.setText(barcode.displayValue);
                    Log.d(TAG_BARCODE, "Barcode read: " + barcode.displayValue);
                }
                else {
                    mTextViewOutputLog.append(getResources().getString(R.string.barcode_failure));
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
