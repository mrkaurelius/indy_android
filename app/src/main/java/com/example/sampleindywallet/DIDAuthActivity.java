package com.example.sampleindywallet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.apache.commons.lang3.ObjectUtils;
import org.hyperledger.indy.sdk.did.DidResults;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class DIDAuthActivity extends AppCompatActivity {

    private static final String TAG = "OzHan";
    private static String scannedData = null;
    private IndyFacade indyFacade = IndyFacade.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_d_i_d_auth);

        try {
            // indyFacade = IndyFacade.getInstance();
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
//        IntentIntegrator intentIntegrator = new IntentIntegrator(this); // `this` is the current Activity
//        intentIntegrator.setOrientationLocked(true);
//        intentIntegrator.setBeepEnabled(false);
//        intentIntegrator.initiateScan();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: starting");

        // Qr tarama adimini atla (Dummy data)
        DIDAuthActivity.scannedData = "{\"sdid\": \"Th7MpTaRZVRYnPiabds81Y\", \"nonce\": \"adb6b5f6f5c99418550b83c5277c6938\"}";

        if (DIDAuthActivity.scannedData != null) {
            Log.d(TAG, "onResume: Qr scanned resuming!");
        } else {
            Log.d(TAG, "onResume: Qr is not scanned!");
            // Qr tarama adimini atla, her turlu devam et
            return;
        }

        // 1. Parse String to json
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

            JSONObject responseJson = new JSONObject();
            responseJson.put("sender_did", clientDid);
            String msg = indyFacade.createSecureMessageB64(nonce, clientDid, serverDID);
            Log.d(TAG, "onResume: msg: " + msg);
            responseJson.put("response_msg", msg);
            Log.d(TAG, "onResume: responseJson: " + responseJson.toString());

            // TODO 3. send challenge

            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            final String responseJsonString = responseJson.toString();
            String url = "http://192.168.1.101:3000/auth/response";

            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i(TAG, response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, error.toString());
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return responseJsonString == null ? null : responseJsonString.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", responseJsonString, "utf-8");
                        return null;
                    }
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";
                    if (response != null) {
                        responseString = String.valueOf(response.statusCode);
                        Log.d(TAG, "parseNetworkResponse: " + new String(response.data));
                    }
                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                }
            };
            requestQueue.add(stringRequest);


            // TODO 4. parse response


            // TODO 5. set jwt to text view
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