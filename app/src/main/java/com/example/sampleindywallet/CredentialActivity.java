package com.example.sampleindywallet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class CredentialActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_credential);


        Button getCredentialButton = (Button) findViewById(R.id.GetCredentialButton);
        getCredentialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO read barcode
                Intent intent = new Intent(view.getContext(), GetCredentialActivity.class);
                view.getContext().startActivity(intent);}
        });
    }
}