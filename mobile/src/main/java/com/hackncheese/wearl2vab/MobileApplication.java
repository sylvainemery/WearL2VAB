package com.hackncheese.wearl2vab;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

public class MobileApplication extends Application implements BootstrapNotifier {
    private static final String TAG = MobileApplication.class.getSimpleName();

    private String mPackageName;
    private RegionBootstrap mRegionBootstrap;
    private BackgroundPowerSaver mBackgroundPowerSaver;

    private String mEmail;
    private String mPassword;
    private String mSalt;

    private String mBalance;

    public void onCreate() {
        super.onCreate();

        mEmail = getString(R.string.l2vab_login);
        mPassword = getString(R.string.l2vab_password);
        mSalt = getString(R.string.l2vab_salt);

        mBalance = getString(R.string.balance_value_fetching);

        Resources res = getResources();

        mPackageName = getPackageName();
        BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);
        /*beaconManager.setForegroundScanPeriod(1100);
        beaconManager.setForegroundBetweenScanPeriod(900);*/
        beaconManager.setBackgroundScanPeriod(1100);
        beaconManager.setBackgroundBetweenScanPeriod(2 * 60 * 1000);

        // set iBeacon specs
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

        // monitor for a specific iBeacon UUID/major/minor
        Region region = new Region(mPackageName, Identifier.parse(getString(R.string.beacon_uuid)), Identifier.fromInt(res.getInteger(R.integer.beacon_major)), Identifier.fromInt(res.getInteger(R.integer.beacon_minor)));
        mRegionBootstrap = new RegionBootstrap(this, region);

        // monitor sparingly
        mBackgroundPowerSaver = new BackgroundPowerSaver(this);

        IntentFilter iff = new IntentFilter(L2VABApiService.ACTION_SENDBALANCE);
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, iff);

    }

    @Override
    public void didDetermineStateForRegion(int state, Region region) {
    }

    @Override
    public void didEnterRegion(Region region) {
        LOGD(TAG, "did enter region " + region.getUniqueId());
        L2VABApiService.startActionFetchBalance(this, mEmail, mPassword, mSalt);
    }

    @Override
    public void didExitRegion(Region region) {
        LOGD(TAG, "did exit region " + region.toString());
        WearNotifyService.startActionRemoveNotif(this);
    }

    private BroadcastReceiver onNotice = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mBalance = intent.getStringExtra(L2VABApiService.EXTRA_BALANCE);
            WearNotifyService.startActionSendNotif(context, mEmail, mPassword, mSalt, mBalance);
        }
    };

    /**
     * As simple wrapper around Log.d
     */
    private static void LOGD(final String tag, String message) {
        if (Log.isLoggable(tag, Log.DEBUG)) {
            Log.d(tag, message);
        }
    }
}