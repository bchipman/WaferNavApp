package com.nielsenninjas.wafernav;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    // Keys for storing activity state in the Bundle.
    protected final static String REQUESTING_LOCATION_UPDATES_KEY = "requesting-location-updates-key";
    protected final static String LAST_LOCATION_NAME_KEY = "last-location-name-key";
    protected final static String LOCATION_KEY = "location-key";
    protected final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";
    private final static int PERMISSIONS_REQUEST_ACCESS_LOCATION = 0;

    protected GoogleApiClient mGoogleApiClient;
    protected Location mCurrentLocation;
    protected LocationRequest mLocationRequest;

    // UI elements
    protected Button mStartUpdatesButton;
    protected Button mStopUpdatesButton;
    protected Button mGetLocationButton;
    protected TextView mLocationNameTextView;
    protected TextView mLatitudeTextView;
    protected TextView mLongitudeTextView;
    protected TextView mLastUpdateTimeTextView;
    protected EditText mIdEditText;

    // Labels
    protected String mLocationNameLabel;
    protected String mLatitudeLabel;
    protected String mLongitudeLabel;
    protected String mLastUpdateTimeLabel;

    protected Boolean mRequestingLocationUpdates;
    protected String mLastLocationName;
    protected String mLastUpdateTime;
    private MyMapFragment myMapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myMapFragment = (MyMapFragment) getSupportFragmentManager().findFragmentById(R.id.activity_fragment_mapview);

        // Set the UI elements
        mStartUpdatesButton = (Button) findViewById(R.id.start_updates_button);
        mStopUpdatesButton = (Button) findViewById(R.id.stop_updates_button);
        mGetLocationButton = (Button) findViewById(R.id.get_location_button);
        mLocationNameTextView = (TextView) findViewById(R.id.location_name_text);
        mLatitudeTextView = (TextView) findViewById(R.id.latitude_text);
        mLongitudeTextView = (TextView) findViewById(R.id.longitude_text);
        mLastUpdateTimeTextView = (TextView) findViewById(R.id.last_update_time_text);
        mIdEditText = (EditText) findViewById(R.id.id_edit_text);

        // Set labels
        mLocationNameLabel = "Name";
        mLatitudeLabel = getResources().getString(R.string.latitude_label);
        mLongitudeLabel = getResources().getString(R.string.longitude_label);
        mLastUpdateTimeLabel = getResources().getString(R.string.last_update_time_label);

        mRequestingLocationUpdates = false;
        mLastLocationName = "";
        mLastUpdateTime = "";

        updateValuesFromBundle(savedInstanceState);

        buildGoogleApiClient();

        // Pressing enter on keyboard triggers 'Get Location' button
        mIdEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    getLocationButtonHandler(mIdEditText);
                }
                return false;
            }
        });
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES_KEY);
                setButtonsEnabledState();
            }

            if (savedInstanceState.keySet().contains(LAST_LOCATION_NAME_KEY)) {
                mLastLocationName = savedInstanceState.getString(LAST_LOCATION_NAME_KEY);
            }

            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
            }
            updateUI();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        createLocationRequest();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void startUpdatesButtonHandler(View view) {
        if (!mRequestingLocationUpdates) {
            mRequestingLocationUpdates = true;
            setButtonsEnabledState();
            startLocationUpdates();
        }
    }

    public void stopUpdatesButtonHandler(View view) {
        if (mRequestingLocationUpdates) {
            mRequestingLocationUpdates = false;
            setButtonsEnabledState();
            stopLocationUpdates();
        }
    }

    public void getLocationButtonHandler(View view) {
        mRequestingLocationUpdates = false;
        setButtonsEnabledState();
        stopLocationUpdates();

        // Dismiss keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mIdEditText.getWindowToken(), 0);

        // This anonymous class is here just to slow down the update time by 500ms after tapping 'Get Location' button.
        //  Otherwise, it's hard to tell the location was updated.
        new CountDownTimer(500, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
            }
            @Override
            public void onFinish() {
                String id = mIdEditText.getText().toString().toUpperCase();
                Location loc = MockData.getData().get(id);
                if (loc != null) {
                    mLastLocationName = id;
                    mIdEditText.setText(null);
                    onLocationChanged(loc);
                }
                else {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.id_not_found_message), Toast.LENGTH_SHORT).show();
                }
            }
        }.start();
    }

    protected void startLocationUpdates() {
        boolean fineLocationPermissionOk = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarseLocationPermissionOk = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (!fineLocationPermissionOk || !coarseLocationPermissionOk) {
            // Ask for permissions
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_LOCATION);
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    private void setButtonsEnabledState() {
        if (mRequestingLocationUpdates) {
            mStartUpdatesButton.setEnabled(false);
            mStopUpdatesButton.setEnabled(true);
        }
        else {
            mStartUpdatesButton.setEnabled(true);
            mStopUpdatesButton.setEnabled(false);
        }
    }

    private void updateUI() {
        if (mCurrentLocation != null) {
            mLocationNameTextView.setText(String.format(Locale.US, "%s: %s", mLocationNameLabel, mLastLocationName));
            mLatitudeTextView.setText(String.format(Locale.US, "%s: %f", mLatitudeLabel, mCurrentLocation.getLatitude()));
            mLongitudeTextView.setText(String.format(Locale.US, "%s: %f", mLongitudeLabel, mCurrentLocation.getLongitude()));
            mLastUpdateTimeTextView.setText(String.format("%s: %s", mLastUpdateTimeLabel, mLastUpdateTime));
            myMapFragment.updateMap(mCurrentLocation);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                }
                else {
                    // Permission denied
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, mRequestingLocationUpdates);
        savedInstanceState.putString(LAST_LOCATION_NAME_KEY, mLastLocationName);
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onConnected(Bundle connectionHint) {

        if (mCurrentLocation == null) {
            boolean fineLocationPermissionOk = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            boolean coarseLocationPermissionOk = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            if (!fineLocationPermissionOk || !coarseLocationPermissionOk) {
                // Ask for permissions
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_LOCATION);
                return;
            }
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            updateUI();
        }

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastLocationName = location.getProvider().equals(MockData.MOCK_DATA_PROVIDER) ? mLastLocationName : "GPS";
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateUI();
        Toast.makeText(this, getResources().getString(R.string.location_updated_message), Toast.LENGTH_SHORT).show();
    }
}
