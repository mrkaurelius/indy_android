package com.example.sampleindywallet;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class DIDsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dids);


        IndyFacade indyFacade = IndyFacade.getInstance();
        String walletName = "client_wallet";
        String walletPassword = "1234";
        // TODO check if wallet already opened
        String dids = "";

        try {
            indyFacade.openWallet(walletName, walletPassword);
            dids = indyFacade.getDids().toString();
            indyFacade.closeWallet();

        } catch (Exception e){
            e.printStackTrace();
        }


        TextView textView = (TextView) findViewById(R.id.DIDTextView);
        textView.setText(dids);

    }


}