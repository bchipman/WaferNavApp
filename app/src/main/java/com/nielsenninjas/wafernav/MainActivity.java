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
import com.nielsenninjas.wafernav.enums.Directive;
import com.nielsenninjas.wafernav.enums.Field;
import com.nielsenninjas.wafernav.enums.Operation;
import com.nielsenninjas.wafernav.barcodereader.BarcodeCaptureActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements EnterLotIdFragment.OnFragmentInteractionListener,
        DeliveringToFragment.OnFragmentInteractionListener, EnterBibIdsFragment.OnFragmentInteractionListener,
        EnterStationIdFragment.OnFragmentInteractionListener, DeliveryCompleteFragment.OnFragmentInteractionListener {

    // Logging
    private static final String TAG = "WNAV-MainActivity";
    private static final String TAG_BARCODE = "WNAV-BarcodeMain";

    // Barcode reader
    public static final int ENTER_LOT_ID_BARCODE_CAPTURE = 9001;
    public static final int ENTER_STATION_BARCODE_CAPTURE = 9002;
    public static final int ENTER_BIB_IDS_BARCODE_CAPTURE = 9003;

    // State
    private Operation currentOperation;

    // MQTT
    private MqttClient mqttClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentOperation = (Operation) getIntent().getSerializableExtra(HomeActivity.CURRENT_OPERATION);

        Fragment fragment;
        String firstFragment = getIntent().getStringExtra(HomeActivity.INITIAL_FRAGMENT);
        switch(firstFragment) {
            case "EnterLotIdFragment":
                fragment = EnterLotIdFragment.newInstance(currentOperation);
                break;
            case "EnterStationIdFragment":
                fragment = EnterStationIdFragment.newInstance();
                break;
            default:
                Log.e(TAG, "INITIAL_FRAGMENT null!");
                return;
        }

        getFragmentManager().beginTransaction().replace(R.id.fragmentContainer, fragment).commit();

        mqttClient = new MqttClient(this);
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

    public void changeFragment(Fragment fragment) {
        if (fragment == null) {
            Log.e(TAG, "Can't transition to null fragment!");
            return;
        }
        // Replace current fragment and add to back stack so back button works properly
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragmentContainer, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    public void makeShortToast(String toastMessage) {
        Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
    }

    public void makeLongToast(String toastMessage) {
        Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_LONG).show();
    }

    @Override
    public void publishButtonHandler(String lotId) {
        // Display toast message and return if nothing entered
        if (lotId == null || lotId.isEmpty()) {
            Toast.makeText(getApplicationContext(), "ID cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create JSON string to publish, e.g. {"id":123}
        Map<String, Object> returnMap = new HashMap<>();
        returnMap.put(Field.DIRECTIVE.field(), Directive.GET_NEW_BLU);
        returnMap.put(Field.LOT_ID.field(), lotId);

        // Add lotId to current data map (current state)
        StateDto.getInstance().setLotId(lotId);

        mqttClient.publishMapAsJson(returnMap);
    }

    @Override
    public void addBibIdButtonHandler(String bibId) {
        Log.i(TAG, "addBibIdButtonHandler: " + currentOperation);
        EnterBibIdsFragment enterBibIdsFragment = (EnterBibIdsFragment) getFragmentManager().findFragmentById(R.id.fragmentContainer);
        enterBibIdsFragment.addBibId(bibId);
    }

    @Override
    public void startDeliveryButtonHandler(String id, Set<String> bibIds) {
        Log.i(TAG, "startDeliveryButtonHandler: " + currentOperation);

        // Create JSON string to publish, e.g. {"id":123}
        Map<String, Object> returnMap = new HashMap<>();

        switch (currentOperation) {
            case TEST:
                returnMap.put(Field.DIRECTIVE.field(), Directive.GET_NEW_SLT);
                returnMap.put(Field.BLU_ID.field(), id);
                returnMap.put(Field.BIB_IDS.field(), bibIds.toArray());

                // Add bluId, bibIds to current data map (current state)
                StateDto.getInstance().setBluId(id);
                StateDto.getInstance().setBibIds(bibIds.toArray(new String[bibIds.size()]));
                break;

            case UNLOAD:
                returnMap.put(Field.DIRECTIVE.field(), Directive.GET_DONE_BLU);
                returnMap.put(Field.SLT_ID.field(), id);
                returnMap.put(Field.BIB_IDS.field(), bibIds.toArray());

                // Add sltId, bibIds to current data map (current state)
                StateDto.getInstance().setSltId(id);
                StateDto.getInstance().setBibIds(bibIds.toArray(new String[bibIds.size()]));
                break;

            case LOAD:
            default:
                Log.w(TAG, "This should not have happened.");
                return;
        }

        mqttClient.publishMapAsJson(returnMap);
    }

    @Override
    public void confirmDeliveryButtonHandler(String id, String name, String description, String location) {
        Log.i(TAG, "confirmDeliveryButtonHandler: " + currentOperation);

        // Just transition to new EnterStationIdFragment
        Fragment fragment = EnterStationIdFragment.newInstance();
        changeFragment(fragment);
    }

    @Override
    public void publishStationIdButtonHandler(String id) {
        Log.i(TAG, "publishStationIdButtonHandler: " + currentOperation);

        Map<String, Object> returnMap;
        int backStackCount;

        switch(currentOperation) {

            case LOAD:
                // Create JSON string to publish, e.g. {"id":123}
                returnMap = new HashMap<>();
                returnMap.put(Field.DIRECTIVE.field(), Directive.COMPLETE_NEW_BLU);
                returnMap.put(Field.BLU_ID.field(), id);
                mqttClient.publishMapAsJson(returnMap);
                break;

            case TEST:
                backStackCount = getFragmentManager().getBackStackEntryCount();
                Log.i(TAG, "Number on back stack: " + backStackCount);
                if (backStackCount == 0) {
                    // On first page so just pass id to new EnterBibIdsFragment
                    Fragment fragment = EnterBibIdsFragment.newInstance(currentOperation, id);
                    changeFragment(fragment);
                } else {
                    // Not on first page, so next page is delivery complete page
                    returnMap = new HashMap<>();
                    returnMap.put(Field.DIRECTIVE.field(), Directive.COMPLETE_NEW_SLT);
                    returnMap.put(Field.SLT_ID.field(), id);
                    mqttClient.publishMapAsJson(returnMap);
                }
                break;

            case UNLOAD:
                backStackCount = getFragmentManager().getBackStackEntryCount();
                Log.i(TAG, "Number on back stack: " + backStackCount);
                if (backStackCount == 0) {
                    // On first page so just pass slt id to EnterBibIdsFragment //TODO stop passing id since already save it to StateDto?
                    Fragment fragment = EnterBibIdsFragment.newInstance(currentOperation, id);
                    changeFragment(fragment);
                    // Add sltId to current data map (current state)
                    StateDto.getInstance().setSltId(id);
                } else {
                    // Not on first page, so next page is delivery complete page
                    returnMap = new HashMap<>();
                    returnMap.put(Field.DIRECTIVE.field(), Directive.COMPLETE_DONE_BLU);
                    returnMap.put(Field.BLU_ID.field(), id);
                    mqttClient.publishMapAsJson(returnMap);
                }
                break;

            default:
                Log.i(TAG, "Unrecognized operation!");
                return;
        }

    }

    @Override
    public void newDeliveryButtonHandler() {
        Log.i(TAG, "newDeliveryButtonHandler: " + currentOperation);
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    @Override
    public void exitAppButtonHandler() {
        Log.i(TAG, "exitAppButtonHandler: " + currentOperation);
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    @Override
    public void readBarcodeButtonHandler(int barcodeCaptureId) {
        Log.i(TAG, "readBarcodeButtonHandler: " + currentOperation + " " + barcodeCaptureId);
        Intent intent = new Intent(this, BarcodeCaptureActivity.class);
        intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
        intent.putExtra(BarcodeCaptureActivity.UseFlash, false);

        startActivityForResult(intent, barcodeCaptureId);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ENTER_LOT_ID_BARCODE_CAPTURE || requestCode == ENTER_STATION_BARCODE_CAPTURE || requestCode == ENTER_BIB_IDS_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    Log.d(TAG_BARCODE, "Barcode read: " + barcode.displayValue);

                    if (requestCode == ENTER_LOT_ID_BARCODE_CAPTURE) {
                        EnterLotIdFragment enterLotIdFragment = (EnterLotIdFragment) getFragmentManager().findFragmentById(R.id.fragmentContainer);
                        enterLotIdFragment.setLotIdText(barcode.displayValue);
                    }

                    else if (requestCode == ENTER_STATION_BARCODE_CAPTURE) {
                        EnterStationIdFragment enterStationIdFragment = (EnterStationIdFragment) getFragmentManager().findFragmentById(R.id.fragmentContainer);
                        enterStationIdFragment.setStationIdText(barcode.displayValue);
                    }

                    else if (requestCode == ENTER_BIB_IDS_BARCODE_CAPTURE) {
                        EnterBibIdsFragment enterBibIdsFragment = (EnterBibIdsFragment) getFragmentManager().findFragmentById(R.id.fragmentContainer);
                        enterBibIdsFragment.setBibIdText(barcode.displayValue);
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

    public Operation getCurrentOperation() {
        return currentOperation;
    }

    public String getClientId() {
        return mqttClient.getClientId();
    }

}
