package com.example.sampleindywallet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.io.IOException;

public class DIDAuthActivity extends AppCompatActivity {

    private static final String TAG = "OzHan";
    private static String scannedData = null;
    private IndyFacade indyFacade = IndyFacade.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_did_auth);

        try {
            Log.d(TAG, "onCreate: pool opened successfully");
            // TODO check if pool already opened
            indyFacade.openDefaultPool();
            String walletName = "client_wallet";
            String walletPassword = "1234";
            // TODO check if wallet already opened
            indyFacade.openWallet(walletName, walletPassword);

        } catch (IndyFacade.IndyFacadeException e) {
            Log.e(TAG, "onClick: There went something wrong with indy", e);
        }

        // Zxing intent
        IntentIntegrator intentIntegrator = new IntentIntegrator(this); // `this` is the current Activity
        intentIntegrator.setOrientationLocked(true);
        intentIntegrator.setBeepEnabled(false);
        intentIntegrator.initiateScan();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: starting");

        if (DIDAuthActivity.scannedData != null) {
            Log.d(TAG, "onResume: Qr scanned resuming!");
        } else {
            Log.d(TAG, "onResume: Qr is not scanned!");
            // Qr tarama adimini atla, her turlu devam et
            return;
        }

        JSONObject challengeJson;
        String nonce;
        String serverDID;

        try {
            challengeJson = new JSONObject(DIDAuthActivity.scannedData);
            nonce = challengeJson.getString("nonce");
            serverDID = challengeJson.getString("sdid");
            Log.d(TAG, "onResume: " + challengeJson.toString());
            Log.d(TAG, "onResume: serverDID: " + serverDID);
            Log.d(TAG, "onResume: nonce: " + nonce);

            // TODO 2. create challenge
            String clientDid = indyFacade.getDids().get(0);
            Log.d(TAG, "onResume: clientDID: " + clientDid);

            JSONObject challengeJSON = new JSONObject();
            challengeJSON.put("sender_did", clientDid);
            String msg = indyFacade.createSecureMessageB64(nonce, clientDid, serverDID);
            Log.d(TAG, "onResume: msg: " + msg);
            challengeJSON.put("response_msg", msg);
            Log.d(TAG, "onResume: responseJson: " + challengeJSON.toString());

            // TODO 3. send challenge
            OkHttpClient client = new OkHttpClient();
            String url = "http://192.168.1.101:3000/auth/response";
            String serverResponse = "";
            String jwt = "";
            try {
                String postBody = challengeJSON.toString();
                MediaType MEDIA_TYPE_PLAINTEXT = MediaType.parse("text/json; charset=utf-8");

                com.squareup.okhttp.Request request = new Request.Builder()
                        .url(url)
                        .post(RequestBody.create(MEDIA_TYPE_PLAINTEXT, postBody))
                        .build();

                Response response = client.newCall(request).execute();
                if (!response.isSuccessful())
                    throw new IOException("Unexpected code " + response);
                serverResponse = response.body().string();
                Log.d(TAG, "onResume: response.body(): " + serverResponse);

            } catch (Exception e) {
                e.printStackTrace();
            }

            // TODO 4. parse response
            try {

                // TODO 5. unpack message
                jwt = indyFacade.unPackMessage(serverResponse);
                if (jwt != null){
                    Log.d(TAG, "onResume: jwt: " + jwt);
                }

            } catch (Error e) {
                e.printStackTrace();
            }

            // TODO 6. set jwt to text view
            TextView textView = (TextView) findViewById(R.id.JWTTextView);
            textView.setText(jwt);
        } catch (Exception err) {
            Log.d("Error", err.toString());
            return;
        }
    }

    // Get the results:
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                String scanResult = result.getContents();
                DIDAuthActivity.scannedData = scanResult;
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}