package org.blockchain.identity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.blockchain.identity.feature.MobileKeys;
import org.blockchain.identity.utils.HttpUtils;
import org.blockchain.identity.utils.JwtUtils;
import org.blockchain.identity.utils.KeyUtils;
import org.jose4j.jwt.JwtClaims;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ScanQRActivity extends Activity {

    private static final int CAMERA_ID = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(ScanQRActivity.this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ScanQRActivity.this, new String[]{
                    Manifest.permission.CAMERA}, CAMERA_ID);
        } else {
            initiateScan();
        }
    }

    public void onBackPressed() {
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if(resultCode == RESULT_OK && scanResult != null) {
            String scannedData = scanResult.getContents();
            if(scannedData != null && !scannedData.trim().equals("")) {
                String[] data = scannedData.trim().split("&");
                String appId = data[0].substring(6);
                String txId = data[1].substring(5);
                new AuthenticationTask(ScanQRActivity.this).execute(appId, txId);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == CAMERA_ID) {
            for (int i = 0, len = permissions.length; i < len; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    finish();
                } else {
                    initiateScan();
                }
            }
        } else {
            finish();
        }
    }

    private void initiateScan() {
        IntentIntegrator integrator = new IntentIntegrator(ScanQRActivity.this);
        integrator.setBeepEnabled(false);
        integrator.setCaptureActivity(PortraitViewScanActivity.class);
        integrator.setOrientationLocked(true);
        integrator.initiateScan();
    }

    public static class AuthenticationTask extends AsyncTask<String, String, JwtClaims> {

        private ProgressDialog progressDialog;
        private Activity activity;

        public AuthenticationTask(Activity activity) {
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
            progressDialog.setMessage("Verifying...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setProgress(0);
            progressDialog.show();
        }

        @Override
        protected JwtClaims doInBackground(String... strings) {
            String appId = strings[0];
            String txId = strings[1];
            Map<String, String> data = new HashMap<>();
            data.put("appId", appId);
            data.put("txId", txId);
            try {
                final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
                String sub = sharedPref.getString(Constants.REGISTERED_ID_KEY, null);
                String serverPublicKey = sharedPref.getString(Constants.SERVER_PUBLIC_KEY, null);
                String jwt = JwtUtils.createJwtToken(sub, data, KeyUtils.getPrivateKey(MobileKeys.getMobilePrivateKey()));
                String response = HttpUtils.post(Constants.SERVER_VERIFY_TRANSACTION_URL, jwt, "text/plain");
                JwtClaims claims = JwtUtils.verifyJwtResponse(response, KeyUtils.getPublicKey(serverPublicKey));
                if(claims != null) {
                    claims.setStringClaim("txId", txId);
                    return claims;
                }
            } catch (Exception e) {
                Log.e("SCAN", "", e);
            }
            Log.e("SCAN", "Return null claims");
            return null;
        }

        @Override
        protected void onPostExecute(JwtClaims jwtClaims) {
            super.onPostExecute(jwtClaims);
            try {
                if (jwtClaims != null) {
                    String partnerName = jwtClaims.getStringClaimValue("partnerName");
                    String partnerUrl = jwtClaims.getStringClaimValue("partnerUrl");
                    String requestedClaims = jwtClaims.getStringClaimValue("requestedClaims");
                    String txId = jwtClaims.getStringClaimValue("txId");
                    Log.d("SCAN", partnerName + partnerUrl + requestedClaims + txId);
                    Intent intent = new Intent(activity, AuthorizeActivity.class);
                    intent.putExtra("identity.partnerName", partnerName);
                    intent.putExtra("identity.partnerUrl", partnerUrl);
                    intent.putExtra("identity.requestedClaims", requestedClaims);
                    intent.putExtra("identity.txId", txId);
                    activity.startActivity(intent);
                } else {

                }
            } catch (Exception e) {
                Log.e("SCAN", "", e);
            }
            activity.finish();
        }
    }
}
