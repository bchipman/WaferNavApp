package com.nielsenninjas.wafernav;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.nielsenninjas.wafernav.enums.Operation;

public class HomeActivity extends AppCompatActivity {

    // Logging
    private static final String TAG = "WNAV-HomeActivity";

    public static final String INITIAL_FRAGMENT = "INITIAL_FRAGMENT";
    public static final String CURRENT_OPERATION = "CURRENT_OPERATION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        int versionCode = BuildConfig.VERSION_CODE;
        TextView version  = (TextView) this.findViewById(R.id.textViewVersion);
        version.setText(String.format("Version %s", Integer.toString(versionCode)));
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
