package org.blockchain.identity;

import android.app.Activity;
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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.blockchain.identity.utils.HttpUtils;
import org.json.JSONObject;

public class AuthorizeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorize);

        String partnerName = getIntent().getStringExtra("identity.partnerName");
        final String partnerUrl = getIntent().getStringExtra("identity.partnerUrl");
        String requestedClaims = getIntent().getStringExtra("identity.requestedClaims");
        final String txId = getIntent().getStringExtra("identity.txId");

        TextView partnameTextView = findViewById(R.id.partnerNameTextView);
        partnameTextView.setText(partnerName);

        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(AuthorizeActivity.this);
        final String email = sharedPref.getString(Constants.REGISTERED_EMAIL_KEY, null);
        Log.d("AUTHO", "Email: " + email);
        TextView emailClaimTextView = findViewById(R.id.requestedClaimEmail);
        emailClaimTextView.setText(email);

        Button btnAuthorize = findViewById(R.id.btnAuthorize);
        btnAuthorize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("txId", txId);
                    jsonObject.put("email", email);
                    new AuthorizeTask(AuthorizeActivity.this).execute(partnerUrl, jsonObject.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    showErrorAlertDialog();
                }
            }
        });

        Button btnDeny = findViewById(R.id.btnDeny);
        btnDeny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void showErrorAlertDialog() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(AuthorizeActivity.this, R.style
                .Theme_AppCompat_Light_Dialog_Alert).setTitle("Failed!")
                .setMessage("Could not authorize. Please try again.");
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

    private static class AuthorizeTask extends AsyncTask<String, String, Void> {

        private ProgressDialog progressDialog;
        private Activity activity;

        public AuthorizeTask(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(activity, R.style.Theme_AppCompat_Light_Dialog);
            if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
            progressDialog.setCancelable(false);
            progressDialog.setMessage("Authorizing...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setProgress(0);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(String... strings) {
            String url = strings[0];
            String json = strings[1];
            Log.d("AUTHO", url + json);
            HttpUtils.post(url, json, "application/json");
            return null;
        }

        @Override
        protected void onPostExecute(Void voi) {
            super.onPostExecute(voi);
            progressDialog.dismiss();
            activity.finish();
        }
    }
}
