package com.disruption.darajampesa;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.disruption.darajampesa.model.AccessToken;
import com.disruption.darajampesa.model.STKPush;
import com.disruption.darajampesa.service.DarajaApiClient;
import com.disruption.darajampesa.utils.DarajaUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.disruption.darajampesa.utils.Constants.BUSINESS_SHORT_CODE;
import static com.disruption.darajampesa.utils.Constants.CALLBACKURL;
import static com.disruption.darajampesa.utils.Constants.PARTYB;
import static com.disruption.darajampesa.utils.Constants.PASSKEY;
import static com.disruption.darajampesa.utils.Constants.TRANSACTION_TYPE;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private DarajaApiClient mApiClient;
    private ProgressDialog mProgressDialog;

    @BindView(R.id.etAmount)
    EditText mAmount;
    @BindView(R.id.etPhone)
    EditText mPhone;
    @BindView(R.id.btnPay)
    Button mPay;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mProgressDialog = new ProgressDialog(this);
        mApiClient = new DarajaApiClient();
        mApiClient.setIsDebug(true); //Set True to enable logging, false to disable.

        mPay.setOnClickListener(this);

        getAccessToken();

    }

    public void getAccessToken() {
        mApiClient.setGetAccessToken(true);
        mApiClient.mpesaService().getAccessToken().enqueue(new Callback<AccessToken>() {
            @Override
            public void onResponse(@NonNull Call<AccessToken> call, @NonNull Response<AccessToken> response) {

                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        mApiClient.setAuthToken(response.body().accessToken);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<AccessToken> call, @NonNull Throwable t) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view == mPay) {
            String phoneNumber = mPhone.getText().toString();
            String amountToPay = mAmount.getText().toString();
            performSTKPush(phoneNumber, amountToPay);
        }
    }

    public void performSTKPush(String phone_number, String amount) {
        mProgressDialog.setMessage("Processing your request");
        mProgressDialog.setTitle("Please Wait...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.show();
        String timestamp = DarajaUtils.getTimestamp();
        STKPush stkPush = new STKPush(
                BUSINESS_SHORT_CODE,
                DarajaUtils.getBase64Password(BUSINESS_SHORT_CODE, PASSKEY, timestamp),
                timestamp,
                TRANSACTION_TYPE,
                String.valueOf(amount),
                DarajaUtils.sanitizePhoneNumber(phone_number),
                PARTYB,
                DarajaUtils.sanitizePhoneNumber(phone_number),
                CALLBACKURL,
                "MPESA Android Test", //Account reference
                "Testing"  //Transaction description
        );

        mApiClient.setGetAccessToken(false);

        //Sending the data to the Mpesa API, remember to remove the logging when in production.
        mApiClient.mpesaService().sendPush(stkPush).enqueue(new Callback<STKPush>() {
            @Override
            public void onResponse(@NonNull Call<STKPush> call, @NonNull Response<STKPush> response) {
                mProgressDialog.dismiss();
                try {
                    if (response.isSuccessful()) {
                        Log.e(TAG,"Post submitted to API-------------------. %s" + response.body());
                    } else {
                        Log.e(TAG,"Response------------------- %s" + response.errorBody().string());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<STKPush> call, @NonNull Throwable t) {
                mProgressDialog.dismiss();
                Log.d(TAG, "onFailure: " + t);
            }
        });
    }
}
