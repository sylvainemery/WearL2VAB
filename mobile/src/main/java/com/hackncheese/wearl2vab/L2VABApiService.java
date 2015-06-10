package com.hackncheese.wearl2vab;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.hackncheese.wearl2vab.helper.L2VABApiHelper;
import com.hackncheese.wearl2vab.helper.NetHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Hashtable;

public class L2VABApiService extends IntentService {

    private static final String TAG = L2VABApiService.class.getSimpleName();

    public static final String ACTION_OPENFRIDGE = "com.hackncheese.wearl2vab.action.OPENFRIDGE";
    public static final String ACTION_FETCHBALANCE = "com.hackncheese.wearl2vab.action.FETCHBALANCE";
    public static final String ACTION_SENDBALANCE = "com.hackncheese.wearl2vab.action.SENDBALANCE";
    public static final String EXTRA_EMAIL = "com.hackncheese.wearl2vab.extra.EMAIL";
    public static final String EXTRA_PASSWORD = "com.hackncheese.wearl2vab.extra.PASSWORD";
    public static final String EXTRA_SALT = "com.hackncheese.wearl2vab.extra.SALT";
    public static final String EXTRA_BALANCE = "com.hackncheese.wearl2vab.extra.BALANCE";

    public L2VABApiService() {
        super("L2VABApiService");
    }

    public static void startActionFetchBalance(Context context, String email, String password, String salt) {
        Intent intent = new Intent(context, L2VABApiService.class);
        intent.setAction(ACTION_FETCHBALANCE);
        intent.putExtra(EXTRA_EMAIL, email);
        intent.putExtra(EXTRA_PASSWORD, password);
        intent.putExtra(EXTRA_SALT, salt);
        context.startService(intent);
    }

    public static void startActionOpenFridge(Context context, String email, String password, String salt) {
        Intent intent = new Intent(context, L2VABApiService.class);
        intent.setAction(ACTION_OPENFRIDGE);
        intent.putExtra(EXTRA_EMAIL, email);
        intent.putExtra(EXTRA_PASSWORD, password);
        intent.putExtra(EXTRA_SALT, salt);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String email = intent.getStringExtra(EXTRA_EMAIL);
            final String password = intent.getStringExtra(EXTRA_PASSWORD);
            final String salt = intent.getStringExtra(EXTRA_SALT);

            Hashtable<String, String> headers = new Hashtable<>();
            headers.put("x-l2v-wsse", L2VABApiHelper.getSecuredHeader(email, password, salt));

            final String action = intent.getAction();
            if (ACTION_OPENFRIDGE.equals(action)) {
                NetHelper.getDataFromUrl(getString(R.string.url_open_door), headers, "PUT");
                LOGD(TAG, "Opening the fridge");
            } else if (ACTION_FETCHBALANCE.equals(action)) {
                String result;
                String balance;

                LOGD(TAG, "Fetching balance");
                result = NetHelper.getDataFromUrl(getString(R.string.url_get_account_info), headers, "GET");

                if (result != null && !result.equals("{}")) {
                    try {
                        JSONObject o = new JSONObject(result);
                        balance = o.getString("balance");
                    } catch (JSONException je) {
                        Log.e(TAG, je.getMessage());
                        balance = "Error";
                    }
                } else {
                    balance = "Error";
                }

                Intent resultIntent = new Intent(ACTION_SENDBALANCE);
                resultIntent.putExtra(EXTRA_BALANCE, balance);
                LocalBroadcastManager.getInstance(this).sendBroadcast(resultIntent);
            }
        }
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
