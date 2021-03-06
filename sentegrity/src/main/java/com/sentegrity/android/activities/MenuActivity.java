package com.sentegrity.android.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.SyncStateContract;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;

import com.sentegrity.android.R;
import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorStore;
import com.sentegrity.core_detection.constants.SentegrityConstants;
import com.trustlook.sdk.cloudscan.CloudScanClient;
import com.sentegrity.core_detection.startup.SentegrityStartup;
import com.sentegrity.core_detection.startup.SentegrityStartupStore;

import java.io.File;
import java.util.MissingResourceException;

/**
 * Created by dmestrov on 20/03/16.
 */
public abstract class MenuActivity extends Activity {

    private DrawerLayout drawer;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        View userDebug = findViewById(R.id.user_debug);
        if(userDebug != null)
            userDebug.setOnClickListener(listener);

        View transparentDebug = findViewById(R.id.transparent_debug);
        if(transparentDebug != null)
            transparentDebug.setOnClickListener(listener);

        View systemDebug = findViewById(R.id.system_debug);
        if(systemDebug != null)
            systemDebug.setOnClickListener(listener);

        View scoreDebug = findViewById(R.id.score_debug);
        if(scoreDebug != null)
            scoreDebug.setOnClickListener(listener);

        View wipeProfile = findViewById(R.id.wipe_profile);
        if(wipeProfile != null)
            wipeProfile.setOnClickListener(listener);

        View details = findViewById(R.id.details);
        if(details != null)
            details.setOnClickListener(listener);

        View menuHandle = findViewById(R.id.menu_handle);
        if(menuHandle == null)
            throw new MissingResourceException("Missing menu handle for the MenuActivity. Add view in layout with id menu_handle.", "view", "menu_handle");
        menuHandle.setOnClickListener(listener);
    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.user_debug:
                    startActivity(new Intent(MenuActivity.this, UserDebugActivity.class));
                    break;
                case R.id.transparent_debug:
                    startActivity(new Intent(MenuActivity.this, TransparentDebugActivity.class));
                    break;
                case R.id.system_debug:
                    startActivity(new Intent(MenuActivity.this, SystemDebugActivity.class));
                    break;
                case R.id.score_debug:
                    startActivity(new Intent(MenuActivity.this, ScoreDebugActivity.class));
                    break;
                case R.id.wipe_profile:
                    showWipeAlert();
                    break;
                case R.id.details:
                    startActivity(new Intent(MenuActivity.this, DetailsActivity.class));
                    break;
                case R.id.menu_handle:
                    drawer.openDrawer(Gravity.RIGHT);
                    break;
            }
        }
    };

    private void showWipeAlert() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        dialogBuilder.setTitle("Wipe profile");
        dialogBuilder.setMessage("Are you sure you want to wipe the device profile? The demo will wipe all learned data.");
        dialogBuilder.setPositiveButton("Reset", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //TODO: is this correct way to do this? set empty startup store? who'll and how set the password
                SentegrityStartupStore.getInstance().resetStartupStore();
                //SentegrityStartupStore.getInstance().setCurrentStartupStore(new SentegrityStartup());
                //SentegrityStartupStore.getInstance().setStartupStore();
                SentegrityTrustFactorStore.getInstance().resetAssertionStore();
                
                //we'll also reset all the trustlook cache
                SharedPreferences sp = getSharedPreferences(SentegrityConstants.SHARED_PREFS_NAME, SentegrityConstants.SHARED_PREFS_MODE);
                sp.edit().putString("cachedList", "[]").apply();
                sp.edit().putString("cachedBadApps", "[]").apply();
                new CloudScanClient.Builder().setContext(getBaseContext()).build().clearAppInfoCache();
            }
        });
        dialogBuilder.setNegativeButton("Cancel", null);
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(Gravity.RIGHT))
            drawer.closeDrawer(Gravity.RIGHT);
        else
            super.onBackPressed();
    }
}
