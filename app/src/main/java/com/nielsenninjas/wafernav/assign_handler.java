package com.nielsenninjas.wafernav;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.util.Map;

public class assign_handler extends AppCompatActivity {

    private static final String TAG = "assign_handler";
    private TextView mTextViewHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_handler);

        mTextViewHandler = (TextView) findViewById(R.id.textViewHandler);

        String jsonMessage = getIntent().getStringExtra("MESSAGE");

        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> jsonMap;
        String handlerId = null;

        try {
            jsonMap = mapper.readValue(jsonMessage, new TypeReference<Map<String, String>>(){});
            handlerId = jsonMap.get("id");
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        Log.i(TAG, "I got this message: " + jsonMessage);
        mTextViewHandler.append(": " + handlerId);
    }
}
