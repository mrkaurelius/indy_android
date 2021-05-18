package com.example.sampleindywallet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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

import org.hyperledger.indy.sdk.did.DidResults;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;

// TODO indy loglarini ac

public class MainActivity extends AppCompatActivity {

    /*
    TODO CredentialActivity yi List credential ve Get Credentail Seklinde 2 aktiviteye bol
     */

    IndyFacade indyFacade;

    private static final String TAG = "OzHan";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        try {
            /*
            1. we have to setup the environment
            */
            String applicationEnvironmentPath = getExternalFilesDir(null).getAbsolutePath();
            indyFacade = IndyFacade.createInstance(applicationEnvironmentPath, IndyFacade.Platform.ANDROID, IndyFacade.DEFAULT_POOL_PROTOCOL_VERSION);
            Log.i(TAG, "onCreate: environment set successfully");
        } catch (IndyFacade.IndyFacadeException e) {
            Log.e(TAG, "onCreate: There went something wrong with indy", e);
        }

        Button credentialButton = (Button) findViewById(R.id.CredentialButton);
        credentialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), CredentialActivity.class);
                view.getContext().startActivity(intent);
            }
        });

        Button WebAuthButton = (Button) findViewById(R.id.WebAuthButton);
        WebAuthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick: Starting DidAuthActivity");
                Intent intent = new Intent(view.getContext(), DIDAuthActivity.class);
                view.getContext().startActivity(intent);
            }
        });


        Button SetupWalletButton = (Button) findViewById(R.id.SetupWalletButton);
        SetupWalletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick: Setting up Indy");

                try {
                    indyFacade.deleteGenesisTransactions(IndyFacade.DEFAULT_GENESIS_FILE); // delete genesis file if exists
                    String ip = "192.168.1.101";
                    indyFacade.writeDefaultGenesisTransactions(ip); // create genesis file
                    Log.d(TAG, "onClick: genesis file created successfully");

                    indyFacade.deletePool(IndyFacade.DEFAULT_POOL_NAME); // delete pool if exists
                    indyFacade.createDefaultPool(); // create pool
                    Log.d(TAG, "onClick: pool created successfully");

                    indyFacade.openDefaultPool();
                    Log.d(TAG, "onCreate: pool opened successfully");

                    String walletName = "client_wallet";
                    String walletPassword = "1234";
                    indyFacade.deleteWallet(walletName); // delete wallet if it exists
                    indyFacade.createWallet(walletName, walletPassword); // create wallet
                    indyFacade.openWallet(walletName, walletPassword); // open wallet
                    Log.d(TAG, "onClick: wallet opened successfully");

                    String seed = "000000000000000000000000Client11";
                    DidResults.CreateAndStoreMyDidResult clientDIDResult = indyFacade.createDID(seed);
                    String clientDID = clientDIDResult.getDid();
                    Log.d(TAG, "onClick: client DID is " + clientDID);

                    ArrayList<String> DIDs = indyFacade.getDids();
                    Log.d(TAG, "onClick: dids: " + DIDs.toString());

                    indyFacade.closePool();
                    Log.d(TAG, "onClick: pool closed successfully");

                    indyFacade.closeWallet();
                    Log.d(TAG, "onClick: wallet closed successfully");

                } catch (IndyFacade.IndyFacadeException e) {
                    Log.e(TAG, "onClick: There went something wrong with indy", e);
                }
            }
        });


        Button DevVolleyButton = (Button) findViewById(R.id.DevVolley);
        DevVolleyButton.setOnClickListener(new View.OnClickListener() {

            // TODO https://stackoverflow.com/questions/37468403/post-request-with-json-body-in-volley-android
            @Override
            public void onClick(View view) {

                Log.d(TAG, "onClick: Dev Volley");
                try {
                    RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("username", "Shozib@gmail.com");
                    jsonBody.put("password", "Shozib123");
                    final String mRequestBody = jsonBody.toString();
                    String url = "http://192.168.1.101:3001/postbox";

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
                                return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
                            } catch (UnsupportedEncodingException uee) {
                                VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
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
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

    }
}

/*
// volley get google
RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
String url = "http://192.168.1.101:9000/genesis";

// Request a string response from the provided URL.
StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
        new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Display the first 500 characters of the response string.
                Log.d(TAG, "Response is: " + response.substring(0, 500));
            }
        }, new Response.ErrorListener() {
    @Override
    public void onErrorResponse(VolleyError error) {
        Log.d(TAG, "onErrorResponse: That didn't work!");
        Log.e(TAG, "onErrorResponse: " + error.toString());
    }
});

// Add the request to the RequestQueue.
queue.add(stringRequest);

 */
