package com.nfredrick.android.joglog;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.nfredrick.android.joglog.generator.MapGeneratorActivity;
import com.nfredrick.android.joglog.jog.JogActivity;
import com.nfredrick.android.joglog.log.LogActivity;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    private Button mJogButton;
    private Button mLogButton;
    private Button mRandomButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        mJogButton = findViewById(R.id.jog_button);
        mJogButton.setOnClickListener((View v) -> {
            Intent intent = new Intent(getApplicationContext(), JogActivity.class);
            startActivity(intent);
        });

        mLogButton = findViewById(R.id.log_button);
        mLogButton.setOnClickListener((View v) -> {
            Intent intent = new Intent(getApplicationContext(), LogActivity.class);
            startActivity(intent);
        });

        mRandomButton = findViewById(R.id.rand_button);
        mRandomButton.setOnClickListener((View v) -> {
            Intent intent = new Intent(getApplicationContext(), MapGeneratorActivity.class);
            startActivity(intent);
        });
    }

}
