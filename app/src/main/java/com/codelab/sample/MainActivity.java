package com.codelab.sample;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsResponseListener;

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
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchPurchase();
            }
        });

        mTextView = (TextView) findViewById(R.id.textView);

        mBillingClient = new BillingClient.Builder(this).setListener(this).build();
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
        mBillingClient.querySkuDetailsAsync(BillingClient.SkuType.INAPP, skuList, new SkuDetailsResponseListener() {
            @Override
            public void onSkuDetailsResponse(SkuDetails.SkuDetailsResult result) {
                if (result.getResponseCode() == BillingClient.BillingResponse.OK) {
                    List<SkuDetails> list = result.getSkuDetailsList();
                    if (list.size() != 0) {
                        for (SkuDetails sd : list) {
                            mTextView.append(sd.toString() + "\n\n");
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "No purchases yet", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.d(TAG, "Query failed: (response code=" + result.getResponseCode() + ")");
                }
            }
        });
    }

    private void launchPurchase() {
        BillingFlowParams params = new BillingFlowParams.Builder()
                .setSku("gas")
                .setType(BillingClient.SkuType.INAPP)
                .build();
        mBillingClient.launchBillingFlow(this, params);
    }
}
