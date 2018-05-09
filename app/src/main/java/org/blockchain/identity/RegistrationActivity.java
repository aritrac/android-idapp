package org.blockchain.identity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

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
            String email = strings[0];
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject status) {
            if ((progressDialog != null) && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            if(status == null) {
                showErrorAlertDialog();
                return;
            } else {
                try {
                    final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(Constants.REGISTERED_EMAIL_KEY, status.getString("email"));
                    editor.commit();
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
