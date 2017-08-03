package com.checkout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class SuccessPage extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.success_layout);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        TextView tokenDysplay = (TextView) findViewById(R.id.token);
        TextView seeMore = (TextView) findViewById(R.id.see_more);

        //Get and display the card token if it was passed
        try{
            Intent intent = getIntent();
            String cardToken = intent.getExtras().getString("cardToken");
            tokenDysplay.setText(cardToken);
        }catch (Exception e) {
            e.printStackTrace();
        }

        seeMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uriUrl = Uri.parse("https://docs.checkout.com/getting-started/checkout-js/charge-via-card-token");
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                startActivity(launchBrowser);
            }
        });

    }
}
