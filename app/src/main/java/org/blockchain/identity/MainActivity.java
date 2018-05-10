package org.blockchain.identity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        String email = sharedPref.getString(Constants.REGISTERED_EMAIL_KEY, null);
        if(email == null) {
            Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        TextView emailTextView = findViewById(R.id.emailTextView);
        emailTextView.setText(email);

        Button btnAuthenticate = findViewById(R.id.btnAuthenticate);
        btnAuthenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ScanQRActivity.class);
                startActivity(intent);
            }
        });
    }
}
