package com.nielsenninjas.wafernav;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class HomeActivity extends AppCompatActivity {

    // Logging
    private static final String TAG = "HomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    public void LoadButtonHandler(View view) {
        Log.i(TAG, "LoadButtonHandler()");
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        intent.putExtra("INITIAL_FRAGMENT", "EnterLotIdFragment");
        startActivity(intent);
    }

    public void TestButtonHandler(View view) {
        Log.i(TAG, "TestButtonHandler()");
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        intent.putExtra("INITIAL_FRAGMENT", "EnterLotIdFragment"); // TODO - change me
        startActivity(intent);
    }

    public void UnloadButtonHandler(View view) {
        Log.i(TAG, "UnloadButtonHandler()");
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        intent.putExtra("INITIAL_FRAGMENT", "EnterLotIdFragment"); // TODO - change me
        startActivity(intent);
    }
}
