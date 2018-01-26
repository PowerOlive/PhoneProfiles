package sk.henrichg.phoneprofiles;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.ArrayList;
import java.util.List;

import sk.henrichg.phoneprofiles.billing.BillingProvider;

public class DonationFragment extends Fragment {

    private List<SkuDetails> SKU_DETAILS = null;

    private View mLoadingView;
    private TextView mErrorTextView;
    private Spinner mGoogleSpinner;
    private Button btGoogle;

    private BillingProvider mBillingProvider;

    // Debug tag, for logging
    private static final String TAG = "DonationFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // this is really important in order to save the state across screen
        // configuration changes for example
        setRetainInstance(true);
    }

    public static DonationFragment newInstance() {
        return new DonationFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        PPApplication.logE(TAG, "onCreateView");
        View root = inflater.inflate(R.layout.donation_fragment, container, false);

        //noinspection ConstantConditions
        mLoadingView = root.findViewById(R.id.donation_google_android_market_loading);
        mErrorTextView = root.findViewById(R.id.donation_google_android_market_error_textview);

        // choose donation amount
        mGoogleSpinner = root.findViewById(
                R.id.donation_google_android_market_spinner);

        btGoogle = root.findViewById(
                R.id.donation_google_android_market_donate_button);
        btGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                donateGoogleOnClick(/*v*/);
            }
        });

        mBillingProvider = (BillingProvider) getActivity();

        /*
        Button paypalButton = root.findViewById(R.id.donation_paypal_donate_button);
        paypalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mErrorTextView.setVisibility(View.GONE);
                String url = "https://www.paypal.me/HenrichGron";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
                } catch (Exception ignored) {
                }
            }
        });
        */

        return root;
    }

    /**
     * Enables or disables "please wait" screen.
     */
    public void setWaitScreen(boolean set) {
        if (mLoadingView != null)
            mLoadingView.setVisibility(set ? View.VISIBLE : View.GONE);
    }

    /**
     * Donate button executes donations based on selection in spinner
     */
    private void donateGoogleOnClick(/*View view*/) {
        final int index = mGoogleSpinner.getSelectedItemPosition();

        mBillingProvider.getBillingManager().startPurchaseFlow(SKU_DETAILS.get(index).getSku(), BillingClient.SkuType.INAPP);
    }

    public void updateGUIAfterBillingConnected() {
        // Start querying for SKUs
        PPApplication.logE(TAG, "handleManagerAndUiReady");
        final List<String> inAppSkus = mBillingProvider.getBillingManager()
                .getSkus(/*!mDebug, */BillingClient.SkuType.INAPP);
        mBillingProvider.getBillingManager().querySkuDetailsAsync(BillingClient.SkuType.INAPP,
                inAppSkus,
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(int responseCode,
                                                     List<SkuDetails> skuDetailsList) {
                        PPApplication.logE(TAG, "onSkuDetailsResponse responseCode="+responseCode);

                        String[] prices = new String[]{"1 €", "2 €", "3 €", "5 €", "8 €", "13 €", "20 €"};

                        if (skuDetailsList != null)
                            PPApplication.logE(TAG, "onSkuDetailsResponse skuDetailsList="+skuDetailsList.size());
                        if (responseCode == BillingClient.BillingResponse.OK && skuDetailsList != null) {
                            if (skuDetailsList.size() > 0) {
                                SKU_DETAILS = new ArrayList<>();
                                for (int i = 0; i < inAppSkus.size(); i++) {
                                    for (int j = 0; j < skuDetailsList.size(); j++) {
                                        if (skuDetailsList.get(j).getSku().equals(inAppSkus.get(i))) {
                                            PPApplication.logE(TAG, "Found sku: " + skuDetailsList.get(j));
                                            SKU_DETAILS.add(skuDetailsList.get(j));
                                            prices[i] = skuDetailsList.get(j).getPrice();
                                            break;
                                        }
                                    }
                                }

                                // update the UI
                                displayAnErrorIfNeeded(BillingClient.BillingResponse.OK);

                                if (getActivity() != null) {
                                    ArrayAdapter<CharSequence> adapter;
                                    adapter = new ArrayAdapter<CharSequence>(getActivity(),
                                            android.R.layout.simple_spinner_item, prices);
                                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    mGoogleSpinner.setAdapter(adapter);
                                    btGoogle.setEnabled(true);
                                }
                            }
                            else {
                                displayAnErrorIfNeeded(BillingClient.BillingResponse.FEATURE_NOT_SUPPORTED);
                            }
                        }
                    }
                });
    }

    @SuppressLint("SetTextI18n")
    public void displayAnErrorIfNeeded(int response) {
        if (getActivity() == null || getActivity().isFinishing()) {
            PPApplication.logE(TAG, "No need to show an error - activity is finishing already");
            return;
        }

        setWaitScreen(false);
        if (mErrorTextView != null) {
            if (response != BillingClient.BillingResponse.OK) {
                mErrorTextView.setVisibility(View.VISIBLE);
                switch (response) {
                    case BillingClient.BillingResponse.BILLING_UNAVAILABLE:
                        mErrorTextView.setText("BILLING_UNAVAILABLE");
                        break;
                    case BillingClient.BillingResponse.DEVELOPER_ERROR:
                        mErrorTextView.setText("DEVELOPER_ERROR");
                        break;
                    case BillingClient.BillingResponse.ERROR:
                        mErrorTextView.setText("ERROR");
                        break;
                    case BillingClient.BillingResponse.FEATURE_NOT_SUPPORTED:
                        mErrorTextView.setText("FEATURE_NOT_SUPPORTED");
                        break;
                    case BillingClient.BillingResponse.ITEM_ALREADY_OWNED:
                        mErrorTextView.setText("ITEM_ALREADY_OWNED");
                        break;
                    case BillingClient.BillingResponse.ITEM_NOT_OWNED:
                        mErrorTextView.setText("ITEM_NOT_OWNED");
                        break;
                    case BillingClient.BillingResponse.ITEM_UNAVAILABLE:
                        mErrorTextView.setText("ITEM_UNAVAILABLE");
                        break;
                    case BillingClient.BillingResponse.SERVICE_DISCONNECTED:
                        mErrorTextView.setText("SERVICE_DISCONNECTED");
                        break;
                    case BillingClient.BillingResponse.SERVICE_UNAVAILABLE:
                        mErrorTextView.setText("SERVICE_UNAVAILABLE");
                        break;
                    case BillingClient.BillingResponse.USER_CANCELED:
                        mErrorTextView.setText("USER_CANCELED");
                        break;
                }
            } else
                mErrorTextView.setVisibility(View.GONE);
        }
    }

}
