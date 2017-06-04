package com.nielsenninjas.wafernav;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.nielsenninjas.wafernav.enums.Operation;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.message.BasicHeader;
import org.json.JSONException;
import org.json.JSONObject;

public class HomeActivity extends AppCompatActivity {

    // Logging
    private static final String TAG = "WNAV-HomeActivity";

    public static final String INITIAL_FRAGMENT = "INITIAL_FRAGMENT";
    public static final String CURRENT_OPERATION = "CURRENT_OPERATION";

    private AsyncHttpClient mAsyncHttpClient;
    private HomeActivity mHomeActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        int versionCode = BuildConfig.VERSION_CODE;
        TextView version  = (TextView) this.findViewById(R.id.textViewVersion);
        version.setText(String.format("Version %s", Integer.toString(versionCode)));

        mHomeActivity = this;
        performRestCallToGetBrokerUrl();
    }

    private void performRestCallToGetBrokerUrl() {
        Header[] headers = new Header[]{
                new BasicHeader("Content-Type", "application/json"),
                new BasicHeader("accept", "application/json")
        };
        mAsyncHttpClient = new AsyncHttpClient();
        mAsyncHttpClient.get(getApplicationContext(), MqttClient.BROKER_REDIRECT_REST_URL, headers, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    String responseUrl = response.getString("url");
                    Log.d(TAG, "Setting broker URL to " + responseUrl);
                    Toast.makeText(mHomeActivity.getApplicationContext(), "Setting broker URL to " + responseUrl, Toast.LENGTH_LONG).show();
                    MqttClient.BROKER_URL = responseUrl;
                }
                catch (JSONException e) {
                    Log.d(TAG, "Could not find field 'url' in received JSON object; defaulting to " + MqttClient.DEFAULT_BROKER_URL);
                    e.printStackTrace();
                    MqttClient.BROKER_URL = MqttClient.DEFAULT_BROKER_URL;
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d(TAG, "REST call to obtain MQTT broker URL failed; defaulting to " + MqttClient.DEFAULT_BROKER_URL);
                MqttClient.BROKER_URL = MqttClient.DEFAULT_BROKER_URL;
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject jsonObject) {
                Log.d(TAG, "REST call to obtain MQTT broker URL failed; defaulting to " + MqttClient.DEFAULT_BROKER_URL);
                MqttClient.BROKER_URL = MqttClient.DEFAULT_BROKER_URL;
            }
        });
    }

    public void GetJobButtonHandler(View view) {
        Toast.makeText(getApplicationContext(), "Feature coming soon!", Toast.LENGTH_SHORT).show();
    }

    public void LoadButtonHandler(View view) {
        Log.i(TAG, "LoadButtonHandler()");
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        intent.putExtra(INITIAL_FRAGMENT, EnterLotIdFragment.class.getSimpleName());
        intent.putExtra(CURRENT_OPERATION, Operation.LOAD);
        startActivity(intent);
    }

    public void TestButtonHandler(View view) {
        Log.i(TAG, "TestButtonHandler()");
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        intent.putExtra(INITIAL_FRAGMENT, EnterStationIdFragment.class.getSimpleName());
        intent.putExtra(CURRENT_OPERATION, Operation.TEST);
        startActivity(intent);
    }

    public void UnloadButtonHandler(View view) {
        Log.i(TAG, "UnloadButtonHandler()");
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        intent.putExtra(INITIAL_FRAGMENT, EnterStationIdFragment.class.getSimpleName());
        intent.putExtra(CURRENT_OPERATION, Operation.UNLOAD);
        startActivity(intent);
    }
}
