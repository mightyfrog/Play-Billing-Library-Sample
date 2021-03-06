package com.codelab.sample;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Shigehiro Soejima
 */
public class MainActivity extends AppCompatActivity implements BillingClientStateListener, PurchasesUpdatedListener {
    private static final String TAG = "PBL Sample";

    private BillingClient mBillingClient;

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> launchPurchase());

        mTextView = findViewById(R.id.textView);

        mBillingClient = BillingClient.newBuilder(this).setListener(this).build();
        mBillingClient.startConnection(this);
    }

    @Override
    public void onBillingSetupFinished(int resultCode) {
        if (resultCode == BillingClient.BillingResponse.OK) {
            Log.d(TAG, "Billing setup successful");
            queryPurchases();
        } else {
            Log.d(TAG, "Billing setup failed");
        }
    }

    @Override
    public void onBillingServiceDisconnected() {
        Log.d(TAG, "Billing setup failed");
    }

    @Override
    public void onPurchasesUpdated(int responseCode, List<Purchase> purchases) {
        switch (responseCode) {
            case BillingClient.BillingResponse.OK: {
                Log.d(TAG, "onPurchasesUpdated: OK");
                break;
            }
            case BillingClient.BillingResponse.USER_CANCELED: {
                Log.d(TAG, "onPurchasesUpdated: User canceled");
                break;
            }
            default: {
                Log.d(TAG, "onPurchasesUpdated: responseCode=" + responseCode);
            }
        }
    }

    private void queryPurchases() {
        List<String> skuList = new ArrayList<>();
        skuList.add("premium");
        skuList.add("gas");
        skuList.add("dummy"); // non-existing id
        SkuDetailsParams params = SkuDetailsParams.newBuilder()
                .setSkusList(skuList)
                .setType(BillingClient.SkuType.INAPP)
                .build();
        mBillingClient.querySkuDetailsAsync(params, (responseCode, skuDetailsList) -> {
            if (responseCode == BillingClient.BillingResponse.OK) {
                if (skuDetailsList.size() != 0) {
                    for (SkuDetails sd : skuDetailsList) {
                        mTextView.append(sd.toString() + "\n\n");
                    }
                } else {
                    Toast.makeText(MainActivity.this, "No purchases yet", Toast.LENGTH_LONG).show();
                }
            } else {
                Log.d(TAG, "Query failed: (response code=" + responseCode + ")");
            }
        });
    }

    private void launchPurchase() {
        BillingFlowParams params = BillingFlowParams.newBuilder()
                .setSku("gas")
                .setType(BillingClient.SkuType.INAPP)
                .build();
        mBillingClient.launchBillingFlow(this, params);
    }
}
