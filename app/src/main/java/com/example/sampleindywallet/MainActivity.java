package com.example.sampleindywallet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.hyperledger.indy.sdk.did.DidResults;

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


//        try {


//            /*
//            5. now we populate the opened wallet with the steward did
//             */
//            String ourDidSeed = "10c73cc3fb2a74cde93bbbd853c93ee1";
//            DidResults.CreateAndStoreMyDidResult ourDidSeedResult = indyFacade.createDID(ourDidSeed);
//            String ourDID = ourDidSeedResult.getDid();
//            Log.d(TAG, "onCreate: ourDid is " + ourDID);
//
//
//            String seed = "000000000000000000000000Steward1";
//            DidResults.CreateAndStoreMyDidResult stewardResult = indyFacade.createDID(seed);
//            String stewardDID = stewardResult.getDid();
//            Log.d(TAG, "onCreate: Steward-DID is " + stewardDID);
//
//
//
//            /*
//            6. now we query the steward did from the ledger. we have to open the created pool first
//             */
//            String verKey = indyFacade.readVerKeyForDidFromLedger(stewardDID);
//            Log.d(TAG, "onCreate: read verKey from ledger successfully " + verKey);
//
//
//            // get dids
//            ArrayList<String> dids = indyFacade.getDids();
//            System.out.println(dids.toString());
//
//            // Secure message
//            String message = "merhaba yalan dunya";
//            String secureMessage = indyFacade.createSecureMessageB64(message, ourDID, stewardDID);
//            Log.d(TAG, String.format("onCreate: secureMessage: %s", secureMessage));
//
//            indyFacade.closePool();
//            Log.d(TAG, "onCreate: pool closed successfully");
//
//            /*
//            7. close wallet
//             */
//            indyFacade.closeWallet();
//            Log.d(TAG, "onCreate: wallet closed successfully");
//
//        } catch (IndyFacade.IndyFacadeException e) {
//            Log.e(TAG, "onCreate: There went something wrong with indy", e);
//        }


        Button DevVolleyButton = (Button) findViewById(R.id.DevVolley);
        DevVolleyButton.setOnClickListener(new View.OnClickListener() {
            // post icin bu calisabilir
            // TODO https://stackoverflow.com/questions/37468403/post-request-with-json-body-in-volley-android

            @Override
            public void onClick(View view) {
                // Volley basit istek cogu sey icin olur gibi

                Log.d(TAG, "onClick: Dev Volley");
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

            }
        });

    }
}
