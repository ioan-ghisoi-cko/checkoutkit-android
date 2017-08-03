package com.checkout;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import com.checkout.CardValidator;
import com.checkout.CheckoutKit;
import com.checkout.exceptions.CardException;
import com.checkout.exceptions.CheckoutException;
import com.checkout.httpconnector.Response;
import com.checkout.models.Card;
import com.checkout.models.CardToken;
import com.checkout.models.CardTokenResponse;

import java.io.IOException;

public class GetCardToken extends AppCompatActivity {

    private static final String PUBLIC_KEY = "pk_test_6ff46046-30af-41d9-bf58-929022d2cd14";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_token_layout);

        //Make application fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        final EditText mCardName = (EditText) findViewById(R.id.cko_name);
        final EditText mCard = (EditText) findViewById(R.id.cko_card);
        final EditText mCvv = (EditText) findViewById(R.id.cko_cvv);
        final Spinner mMonth = (Spinner) findViewById(R.id.cko_month);
        final Spinner mYear = (Spinner) findViewById(R.id.cko_year);

        final Button el_generateToken = (Button) findViewById(R.id.cko_generate_token);

        //When the user clicks the Generate Token button
        el_generateToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //Send card details to the ConnectionTask
                    new ConnectionTask(mCardName.getText().toString(), mCard.getText().toString(),
                            mMonth.getSelectedItem().toString(), mYear.getSelectedItem().toString(),
                            mCvv.getText().toString()).execute();
                } catch (Exception e) {
                    e.printStackTrace();
                    goToError();
                }
            }
        });

    }

    private void goToError() {
        Intent intent = new Intent(this, ErrorPage.class);
        startActivity(intent);
    }

    private void goToSuccess(String cardToken) {
        Intent intent = new Intent(this, SuccessPage.class);
        intent.putExtra("cardToken", cardToken);
        startActivity(intent);
    }

    class ConnectionTask extends AsyncTask<String, Void, String> {

        private String nameValue, numberValue, monthValue, yearValue, cvvValue;

        public ConnectionTask(String name, String number, String month, String year, String cvv) {
            this.nameValue = name;
            this.numberValue = number;
            this.monthValue = month;
            this.yearValue = year;
            this.cvvValue = cvv;
        }

        final EditText numberField = (EditText) findViewById(R.id.cko_card);
        final EditText cvvField = (EditText) findViewById(R.id.cko_cvv);
        final Spinner spinMonth = (Spinner) findViewById(R.id.cko_month);
        final Spinner spinYear = (Spinner) findViewById(R.id.cko_year);
        final int errorColor = Color.rgb(231, 76, 60);

        private boolean validateCardFields(final String number, final String month, final String year, final String cvv) {
            boolean error = false;
            clearFieldsError();

            if (!CardValidator.validateCardNumber(number)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        numberField.getBackground().setColorFilter(errorColor, PorterDuff.Mode.SRC_ATOP);
                    }
                });
                error = true;
            }
            if (!CardValidator.validateExpiryDate(month, year)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        spinMonth.getBackground().setColorFilter(errorColor, PorterDuff.Mode.SRC_ATOP);
                        spinYear.getBackground().setColorFilter(errorColor, PorterDuff.Mode.SRC_ATOP);
                    }
                });
                error = true;
            }
            if (cvv.equals("")) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cvvField.getBackground().setColorFilter(errorColor, PorterDuff.Mode.SRC_ATOP);
                    }
                });
                error = true;
            }
            return !error;
        }

        private void clearFieldsError() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    cvvField.getBackground().clearColorFilter();
                    spinMonth.getBackground().clearColorFilter();
                    spinYear.getBackground().clearColorFilter();
                    numberField.getBackground().clearColorFilter();
                }
            });
        }

        @Override
        protected String doInBackground(String... urls) {

            if (validateCardFields(numberValue, monthValue, yearValue, cvvValue)) {
                clearFieldsError();
                try {
                    Card card = new Card(numberValue, nameValue, monthValue, yearValue, cvvValue);
                    CheckoutKit ck = CheckoutKit.getInstance(PUBLIC_KEY);
                    final Response<CardTokenResponse> resp = ck.createCardToken(card);
                    if (resp.hasError) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                goToError();
                            }
                        });
                    } else {
                        CardToken ct = resp.model.getCard();

                        goToSuccess(resp.model.getCardToken());
                        return resp.model.getCardToken();
                    }
                } catch (final CardException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (e.getType().equals(CardException.CardExceptionType.INVALID_CVV)) {
                                cvvField.getBackground().setColorFilter(errorColor, PorterDuff.Mode.SRC_ATOP);
                            } else if (e.getType().equals(CardException.CardExceptionType.INVALID_EXPIRY_DATE)) {
                                spinMonth.getBackground().setColorFilter(errorColor, PorterDuff.Mode.SRC_ATOP);
                                spinYear.getBackground().setColorFilter(errorColor, PorterDuff.Mode.SRC_ATOP);
                            } else if (e.getType().equals(CardException.CardExceptionType.INVALID_NUMBER)) {
                                numberField.getBackground().setColorFilter(errorColor, PorterDuff.Mode.SRC_ATOP);
                            }
                        }
                    });
                } catch (CheckoutException | IOException e2) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            goToError();
                        }
                    });
                }
            }
            return "";
        }

    }

}
