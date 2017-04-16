package com.nielsenninjas.wafernav;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

public class MainActivity extends AppCompatActivity implements EnterLotIdFragment.OnFragmentInteractionListener,
        AssignHandlerFragment.OnFragmentInteractionListener, DeliveringToFragment.OnFragmentInteractionListener,
        EnterBegStationIdFragment.OnFragmentInteractionListener, EnterBibIdsFragment.OnFragmentInteractionListener,
        EnterStationIdFragment.OnFragmentInteractionListener, DeliveryCompleteFragment.OnFragmentInteractionListener {

    // Logging
    private static final String TAG = "MainActivity";
    private static final String TAG_BARCODE = "BarcodeMain";

    // Connection info
    private static final String BROKER_URL = "tcp://iot.eclipse.org:1883";
    private static final String PUB_TOPIC = "wafernav/location_requests";
    private static final String SUB_TOPIC = "wafernav/location_data";
    private static final String CLIENT_ID = UUID.randomUUID().toString();

    private static final int ID_BARCODE_CAPTURE = 9001;
    private static final int STATION_BARCODE_CAPTURE = 9002;

    // MQTT
    private MqttAndroidClient mqttAndroidClient;
    private IMqttToken mqttSubToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Fragment fragment;
        String firstFragment = getIntent().getStringExtra("INITIAL_FRAGMENT");
        switch(firstFragment) {
            case "EnterLotIdFragment":
                fragment = EnterLotIdFragment.newInstance();
                break;
            case "EnterBegStationIdFragment":
                fragment = EnterBegStationIdFragment.newInstance("thing1", "thing2");
                break;
            default:
                Log.e(TAG, "INITIAL_FRAGMENT null!");
                return;
        }

        getFragmentManager().beginTransaction().replace(R.id.fragmentContainer, fragment).commit();

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

    private void publishMapAsJson(Map<String, String> map) {
        Log.i(TAG, "publishMapAsJson");

        String returnJsonString = null;
        try {
            returnJsonString = new ObjectMapper().writeValueAsString(map);
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
                default:
                    Log.i(TAG, "unknown directive, returning");
                    return;
            }

            // Replace current fragment and add to back stack so back button works properly
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.fragmentContainer, fragment);
            ft.addToBackStack(null);
            ft.commit();
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            System.out.println("Delivery Complete!");
        }
    }

    @Override
    public void publishButtonHandler(String lotId) {
        // Display toast message and return if nothing entered
        if (lotId == null || lotId.isEmpty()) {
            Toast.makeText(getApplicationContext(), "ID cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create JSON string to publish, e.g. {"id":123}
        Map<String, String> returnMap = new HashMap<>();
        returnMap.put("directive", "GET_NEW_BLU");
        returnMap.put("lotId", lotId);

        publishMapAsJson(returnMap);
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
    public void confirmDeliveryButtonHandler(String id, String loc) {
        Log.i(TAG, "confirmDeliveryButtonHandler");
        Fragment fragment = EnterStationIdFragment.newInstance(id, loc);

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragmentContainer, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public void readStationBarcodeButtonHandler(String id, String loc) {
        Log.i(TAG, "readStationBarcodeButtonHandler");

        // launch barcode activity.
        Intent intent = new Intent(this, BarcodeCaptureActivity.class);
        intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
        intent.putExtra(BarcodeCaptureActivity.UseFlash, false);

        startActivityForResult(intent, STATION_BARCODE_CAPTURE);
    }

    @Override
    public void publishStationIdButtonHandler(String stationId) {
        Log.i(TAG, "publishStationIdButtonHandler");

        // Create JSON string to publish, e.g. {"id":123}
        Map<String, String> returnMap = new HashMap<>();
        returnMap.put("directive", "COMPLETE_NEW_BLU");
        returnMap.put("bluId", stationId);

        publishMapAsJson(returnMap);
    }

    @Override
    public void newDeliveryButtonHandler() {
        Log.i(TAG, "newDeliveryButtonHandler");
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    @Override
    public void exitAppButtonHandler() {
        Log.i(TAG, "exitAppButtonHandler");
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    @Override
    public void readBarcodeButtonHandler() {
        // launch barcode activity.
        Intent intent = new Intent(this, BarcodeCaptureActivity.class);
        intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
        intent.putExtra(BarcodeCaptureActivity.UseFlash, false);

        startActivityForResult(intent, ID_BARCODE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ID_BARCODE_CAPTURE || requestCode == STATION_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    Log.d(TAG_BARCODE, "Barcode read: " + barcode.displayValue);

                    if (requestCode == ID_BARCODE_CAPTURE) {
                        EnterLotIdFragment enterLotIdFragment = (EnterLotIdFragment) getFragmentManager().findFragmentById(R.id.fragmentContainer);
                        enterLotIdFragment.setLotIdText(barcode.displayValue);
                    }

                    else if (requestCode == STATION_BARCODE_CAPTURE) {
                        EnterStationIdFragment enterStationIdFragment = (EnterStationIdFragment) getFragmentManager().findFragmentById(R.id.fragmentContainer);
                        enterStationIdFragment.setStationIdText(barcode.displayValue);
                    }
                }
                else {
                    Log.d(TAG_BARCODE, "No barcode captured, intent data is null");
                }
            }
            else {
                Log.d(TAG_BARCODE, String.format(getString(R.string.barcode_error), CommonStatusCodes.getStatusCodeString(resultCode)));
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
