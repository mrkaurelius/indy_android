package com.example.sampleindywallet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.hyperledger.indy.sdk.did.DidResults;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    IndyFacade indyFacade;
    private static final String TAG = "OzHan";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        try {
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

        Button DIDsButton = (Button) findViewById(R.id.DIDButton);
        DIDsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick: Starting DidAuthActivity");
                Intent intent = new Intent(view.getContext(), DIDsActivity.class);
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
            @Override
            public void onClick(View view) {
                OkHttpClient client = new OkHttpClient();

                try {
                    String postBody = "deneme";
                    MediaType MEDIA_TYPE_PLAINTEXT = MediaType.parse("text/plaintext; charset=utf-8");

                    Request request = new Request.Builder()
                            .url("http://192.168.1.101:3001")
                            .post(RequestBody.create(MEDIA_TYPE_PLAINTEXT, postBody))
                            .build();

                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful())
                        throw new IOException("Unexpected code " + response);

                    System.out.println(response.body().string());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

    }
}
