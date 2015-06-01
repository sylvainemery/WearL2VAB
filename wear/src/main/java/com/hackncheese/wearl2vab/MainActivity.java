
package com.hackncheese.wearl2vab;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;
import android.util.Log;
import android.view.WindowManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.hackncheese.wearl2vab.fragments.ActionFragment;
import com.hackncheese.wearl2vab.fragments.HomeFragment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;


public class MainActivity extends Activity implements ConnectionCallbacks, OnConnectionFailedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String START_OPEN_FRIDGE_ACTIVITY_PATH = "/open-fridge-activity";
    private static final String BALANCE_PATH = "/balance";
    private static final String BALANCE_KEY = "balance";

    private GoogleApiClient mGoogleApiClient;
    private GridViewPager mPager;
    private HomeFragment mHomeFragment;
    private Context mContext;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        mContext = this;
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setupViews();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        LOGD(TAG, "onConnected(): Successfully connected to Google API client");
        getBalanceFromHandheld();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        LOGD(TAG, "onConnectionSuspended(): Connection to Google API client was suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.e(TAG, "onConnectionFailed(): Failed to connect, with result: " + result);
    }

    private void getBalanceFromHandheld() {
        new GetBalanceAsyncTask().execute();
    }

    public void startOpenFridge() {
        new StartOpenFridgeActivityTask().execute();
    }

    private Collection<String> getNodes() {
        HashSet<String> results = new HashSet<>();
        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();

        for (Node node : nodes.getNodes()) {
            results.add(node.getId());
        }

        return results;
    }

    private void sendStartOpenFridgeActivityMessage(String node) {
        LOGD(TAG, "sending the 'open the fridge' message");
        Wearable.MessageApi.sendMessage(
                mGoogleApiClient, node, START_OPEN_FRIDGE_ACTIVITY_PATH, new byte[0]).setResultCallback(
                new ResultCallback<MessageApi.SendMessageResult>() {
                    @Override
                    public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                        if (!sendMessageResult.getStatus().isSuccess()) {
                            Log.e(TAG, "Failed to send message with status code: "
                                    + sendMessageResult.getStatus().getStatusCode());
                        } else {
                            //show success animation
                            Intent intent = new Intent(mContext, ConfirmationActivity.class);
                            intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.SUCCESS_ANIMATION);
                            mContext.startActivity(intent);

                            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                            long[] vibrationPattern = {0, 500, 50, 300};
                            //-1 - don't repeat
                            final int indexInPatternToRepeat = -1;
                            vibrator.vibrate(vibrationPattern, indexInPatternToRepeat);
                        }
                    }
                }
        );
    }

    private class StartOpenFridgeActivityTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... args) {
            Collection<String> nodes = getNodes();
            for (String node : nodes) {
                sendStartOpenFridgeActivityMessage(node);
            }
            return null;
        }
    }

    private class GetBalanceAsyncTask extends AsyncTask<Void, Void, String> {

        public GetBalanceAsyncTask() {
        }

        @Override
        protected String doInBackground(Void... args) {

            String balance;

            if (!mGoogleApiClient.isConnected()) {
                Log.e(TAG, "Not connected");
                return null;
            }

            Uri u = new Uri.Builder().scheme(PutDataRequest.WEAR_URI_SCHEME).path(BALANCE_PATH).build();

            DataItemBuffer dataItemResults =
                    Wearable.DataApi.getDataItems(mGoogleApiClient, u).await();

            if (dataItemResults.getStatus().isSuccess()) {
                DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItemResults.get(0));
                balance = dataMapItem.getDataMap().getString(BALANCE_KEY);
                LOGD(TAG, balance);
            } else {
                balance = "?";
            }
            dataItemResults.release();
            return balance;
        }

        protected void onPostExecute(String result) {
            if (result != null) {
                mHomeFragment.setBalanceText(result);
            }
        }
    }

    private void setupViews() {
        mPager = (GridViewPager) findViewById(R.id.pager);
        mPager.setOffscreenPageCount(1);
        DotsPageIndicator dotsPageIndicator = (DotsPageIndicator) findViewById(R.id.page_indicator);
        dotsPageIndicator.setDotSpacing((int) getResources().getDimension(R.dimen.dots_spacing));
        dotsPageIndicator.setPager(mPager);
        mHomeFragment = new HomeFragment();
        ActionFragment actionFragment = new ActionFragment();
        List<Fragment> pages = new ArrayList<>();
        pages.add(mHomeFragment);
        pages.add(actionFragment);
        final MyPagerAdapter adapter = new MyPagerAdapter(getFragmentManager(), pages);
        mPager.setAdapter(adapter);
    }

    /**
     * Switches to the page {@code index}. The first page has index 0.
     */
    private void moveToPage(int index) {
        mPager.setCurrentItem(0, index, true);
    }

    private class MyPagerAdapter extends FragmentGridPagerAdapter {

        private List<Fragment> mFragments;

        public MyPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            mFragments = fragments;
        }

        @Override
        public int getRowCount() {
            return 1;
        }

        @Override
        public int getColumnCount(int row) {
            return mFragments == null ? 0 : mFragments.size();
        }

        @Override
        public Fragment getFragment(int row, int column) {
            return mFragments.get(column);
        }

    }

    /**
     * As simple wrapper around Log.d
     */
    private static void LOGD(final String tag, String message) {
        //if (Log.isLoggable(tag, Log.DEBUG)) {
        Log.d(tag, message);
        //}
    }

}