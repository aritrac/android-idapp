package org.blockchain.identity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

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
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("appId", appId);
                    jsonObject.put("txId", txId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        finish();
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
}
