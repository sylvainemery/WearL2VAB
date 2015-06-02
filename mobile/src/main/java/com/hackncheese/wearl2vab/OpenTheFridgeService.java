package com.hackncheese.wearl2vab;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.hackncheese.wearl2vab.helper.L2VABApiHelper;
import com.hackncheese.wearl2vab.helper.NetHelper;

import java.util.Hashtable;

public class OpenTheFridgeService extends IntentService {

    private static final String TAG = OpenTheFridgeService.class.getSimpleName();

    public static final String EXTRA_EMAIL = "com.hackncheese.wearl2vab.extra.EMAIL";
    public static final String EXTRA_PASSWORD = "com.hackncheese.wearl2vab.extra.PASSWORD";
    public static final String EXTRA_SALT = "com.hackncheese.wearl2vab.extra.SALT";

    public OpenTheFridgeService() {
        super("OpenTheFridgeService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String email = intent.getStringExtra(EXTRA_EMAIL);
            final String password = intent.getStringExtra(EXTRA_PASSWORD);
            final String salt = intent.getStringExtra(EXTRA_SALT);

            Hashtable<String, String> headers = new Hashtable<>();
            headers.put("x-l2v-wsse", L2VABApiHelper.getSecuredHeader(email, password, salt));

            //NetHelper.getDataFromUrl(getString(R.string.url_open_door), headers, "PUT");
            LOGD(TAG, "Opening the fridge");
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
