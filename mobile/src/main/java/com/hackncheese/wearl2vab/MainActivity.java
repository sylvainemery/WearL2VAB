package com.hackncheese.wearl2vab;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;


public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private String mEmail;
    private String mPassword;
    private String mSalt;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);

        mEmail = getString(R.string.l2vab_login);
        mPassword = getString(R.string.l2vab_password);
        mSalt = getString(R.string.l2vab_salt);
    }

    public void onShowNotifClick(View view) {
        L2VABApiService.startActionFetchBalance(this, mEmail, mPassword, mSalt);
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