package org.blockchain.identity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.loopj.android.http.AsyncHttpResponseHandler;

import org.blockchain.identity.feature.MobileKeys;
import org.blockchain.identity.utils.HttpUtils;
import org.blockchain.identity.utils.JwtUtils;
import org.blockchain.identity.utils.KeyUtils;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.jwt.consumer.JwtContext;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

import cz.msebera.android.httpclient.Header;

public class RegistrationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        Button btnSignup = findViewById(R.id.btnSignup);
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText emailEditText = findViewById(R.id.email);
                if(TextUtils.isEmpty(emailEditText.getText())) {
                    emailEditText.setError("Email required.");
                } else {
                    new RegistrationTask(RegistrationActivity.this).execute(emailEditText.getText().toString());
                }
            }
        });
    }

    private static class RegistrationTask extends AsyncTask<String, String, JSONObject> {

        private ProgressDialog progressDialog;
        private RegistrationActivity activity;

        private RegistrationTask(RegistrationActivity activity) {
            this.activity = activity;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(activity, R.style.Theme_AppCompat_Light_Dialog);
            if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Registering...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setProgress(0);
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(String... strings) {
            try {
                String email = strings[0];
                JSONObject jsonObjectRequest = new JSONObject();
                jsonObjectRequest.put("email", email);
                jsonObjectRequest.put("publicKey", MobileKeys.getMobilePublicKey());
                String jsonResponse = HttpUtils.post(Constants.SERVER_REGISTER_URL, jsonObjectRequest.toString(), "application/json");
                JSONObject jsonObjectResponse = new JSONObject(jsonResponse);
                jsonObjectResponse.put(Constants.REGISTERED_EMAIL_KEY, email);
                return jsonObjectResponse;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonResponse) {
            if ((progressDialog != null) && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if(jsonResponse == null) {
                showErrorAlertDialog();
                return;
            } else {
                try {
                    final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(Constants.REGISTERED_EMAIL_KEY, jsonResponse.getString(Constants.REGISTERED_EMAIL_KEY));
                    editor.putString(Constants.SERVER_PUBLIC_KEY, jsonResponse.getString("serverPublicKey"));
                    editor.putString(Constants.REGISTERED_ID_KEY, jsonResponse.getString("id"));
                    editor.commit();
                    Intent intent = new Intent(activity, MainActivity.class);
                    activity.startActivity(intent);
                    activity.finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                    showErrorAlertDialog();
                }
            }
        }

        private void showErrorAlertDialog() {
            final AlertDialog.Builder dialog = new AlertDialog.Builder(activity, R.style
                    .Theme_AppCompat_Light_Dialog_Alert).setTitle("Failed!")
                    .setMessage("Registration failed. Please try again.");
            dialog.setPositiveButton("Got it", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int whichButton) {
                    dialog.dismiss();
                }
            });
            dialog.setCancelable(false);
            final AlertDialog alert = dialog.create();
            alert.show();
        }
    }
}
