package com.hackncheese.wearl2vab;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi.DataItemResult;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.hackncheese.wearl2vab.helper.L2VABApiHelper;
import com.hackncheese.wearl2vab.helper.NetHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Hashtable;

public class MainActivity extends Activity implements MessageApi.MessageListener, ConnectionCallbacks,
        OnConnectionFailedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     * Request code for launching the Intent to resolve Google Play services errors.
     */
    private static final int REQUEST_RESOLVE_ERROR = 1000;

    private static final String START_OPEN_FRIDGE_ACTIVITY_PATH = "/open-fridge-activity";
    private static final String BALANCE_PATH = "/balance";
    private static final String BALANCE_KEY = "balance";

    private String mEmail;
    private String mPassword;
    private String mSalt;

    private String mBalance;

    private TextView mGoogleApiConnectionStatus;
    private TextView mBalanceStatus;

    private GoogleApiClient mGoogleApiClient;
    private boolean mResolvingError = false;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        mGoogleApiConnectionStatus = (TextView) findViewById(R.id.google_api_connection_status);
        mBalanceStatus = (TextView) findViewById(R.id.balance_status);

        mEmail = getString(R.string.l2vab_login);
        mPassword = getString(R.string.l2vab_password);
        mSalt = getString(R.string.l2vab_salt);

        mBalance = getString(R.string.balance_value_fetching);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mResolvingError) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        if (!mResolvingError) {
            Wearable.MessageApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override //ConnectionCallbacks
    public void onConnected(Bundle connectionHint) {
        LOGD(TAG, "Google API Client was connected");
        mResolvingError = false;
        mGoogleApiConnectionStatus.setText(getString(R.string.google_api_status_connected));
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
        // now get the balance
        //new GetBalanceTask().execute();
    }

    @Override //ConnectionCallbacks
    public void onConnectionSuspended(int cause) {
        LOGD(TAG, "Connection to Google API client was suspended");
        mGoogleApiConnectionStatus.setText(getString(R.string.google_api_status_connecting));
    }

    @Override //OnConnectionFailedListener
    public void onConnectionFailed(ConnectionResult result) {
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            Log.e(TAG, "Connection to Google API client has failed");
            mResolvingError = false;
            mGoogleApiConnectionStatus.setText(getString(R.string.google_api_status_failed));
            Wearable.MessageApi.removeListener(mGoogleApiClient, this);
        }
    }

    @Override //MessageListener
    public void onMessageReceived(final MessageEvent messageEvent) {
        LOGD(TAG, "onMessageReceived() A message from wear was received:" + messageEvent
                .getRequestId() + " " + messageEvent.getPath());

        if (START_OPEN_FRIDGE_ACTIVITY_PATH.equals(messageEvent.getPath())) {
            // go open the fridge here
            new SendOpenDoorOrderTask().execute();
        }

    }

    /**
     * an AsyncTask that will call the open door API URL
     */
    private class SendOpenDoorOrderTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... p) {
            Hashtable<String, String> headers = new Hashtable<>();
            headers.put("x-l2v-wsse", L2VABApiHelper.getSecuredHeader(mEmail, mPassword, mSalt));

            return NetHelper.getDataFromUrl(getString(R.string.url_open_door), headers, "PUT");
        }

        protected void onPreExecute() {
        }

        protected void onPostExecute(String result) {
            LOGD(TAG, result);
        }
    }

    /**
     * an AsyncTask that will call the open door API URL
     */
    private class GetBalanceTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... p) {
            Hashtable<String, String> headers = new Hashtable<>();
            headers.put("x-l2v-wsse", L2VABApiHelper.getSecuredHeader(mEmail, mPassword, mSalt));

            return NetHelper.getDataFromUrl(getString(R.string.url_get_account_info), headers, "GET");
        }

        protected void onPreExecute() {
        }

        protected void onPostExecute(String result) {
            LOGD(TAG, result);

            if (result != null && !result.equals("{}")) {
                try {
                    JSONObject o = new JSONObject(result);
                    mBalance = o.getString("balance");
                } catch (JSONException je) {
                    Log.e(TAG, je.getMessage());
                    mBalance = "Error";
                }
            } else {
                mBalance = "Error";
            }


            mBalanceStatus.setText(getString(R.string.balance_value, mBalance));
            SetBalanceToWear(mBalance);

        }
    }

    protected void SetBalanceToWear(String balance) {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(BALANCE_PATH);
        putDataMapRequest.getDataMap().putString(BALANCE_KEY, balance);
        PutDataRequest request = putDataMapRequest.asPutDataRequest();

        if (!mGoogleApiClient.isConnected()) {
            return;
        }
        Wearable.DataApi.putDataItem(mGoogleApiClient, request)
                .setResultCallback(new ResultCallback<DataItemResult>() {
                    @Override
                    public void onResult(DataItemResult dataItemResult) {
                        if (!dataItemResult.getStatus().isSuccess()) {
                            Log.e(TAG, "ERROR: failed to putDataItem, status code: "
                                    + dataItemResult.getStatus().getStatusCode());
                        }
                    }
                });
    }

    public void onSendNotifClick(View view) {
        int notificationId = 1;
        long[] vibrationPattern = {0,500,110,500,110,450,110,200,110,170,40,450,110,200,110,170,40,500};//star wars imperial march

        Intent openFridgeIntent = new Intent(this, L2VABApiService.class);
        openFridgeIntent.putExtra(L2VABApiService.EXTRA_EMAIL, mEmail);
        openFridgeIntent.putExtra(L2VABApiService.EXTRA_PASSWORD, mPassword);
        openFridgeIntent.putExtra(L2VABApiService.EXTRA_SALT, mSalt);
        PendingIntent openFridgePendingIntent = PendingIntent.getService(this, 0, openFridgeIntent, 0);

        // Create the action
        NotificationCompat.Action action =
                new NotificationCompat.Action.Builder(R.mipmap.ic_lock_open_white,
                        getString(R.string.open_the_fridge), openFridgePendingIntent)
                        .build();

        // Create a WearableExtender to add functionality for wearables
        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
                .setHintHideIcon(true)
                .setBackground(BitmapFactory.decodeResource(getResources(), R.drawable.l2v_300))
                .addAction(action);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.notif_title))
                .setContentText(getString(R.string.balance_value, mBalance))
                .extend(wearableExtender)
                .addAction(action)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(vibrationPattern);

        // Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // Build the notification and issues it with notification manager.
        notificationManager.notify(notificationId, notificationBuilder.build());
    }


    /**
     * As simple wrapper around Log.d
     */
    private static void LOGD(final String tag, String message) {
        if (Log.isLoggable(tag, Log.DEBUG)) {
            Log.d(tag, message);
        }
    }

}