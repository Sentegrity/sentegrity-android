package com.sentegrity.android.activities;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.sentegrity.android.R;
import com.sentegrity.android.activity.ActivitiesIntentService;
import com.sentegrity.core_detection.CoreDetection;
import com.sentegrity.core_detection.CoreDetectionCallback;
import com.sentegrity.core_detection.computation.SentegrityTrustScoreComputation;
import com.sentegrity.core_detection.logger.SentegrityError;
import com.sentegrity.core_detection.policy.SentegrityPolicy;
import com.sentegrity.core_detection.protect_mode.ProtectMode;
import com.sentegrity.core_detection.startup.SentegrityStartupStore;

public class LoginActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private SentegrityTrustScoreComputation computationResults;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(ActivityRecognition.API)
                .build();

        startAnalyzing();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private void startAnalyzing() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Analyzing");
        progressDialog.setMessage("Mobile Security Posture");
        progressDialog.show();

        final SentegrityPolicy policy = CoreDetection.getInstance().parsePolicy("default.policy");
        CoreDetection.getInstance().performCoreDetection(policy, new CoreDetectionCallback() {
            @Override
            public void onFinish(final SentegrityTrustScoreComputation computationResult, SentegrityError error, boolean success) {
                if (success) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            analyzeResults(computationResult, policy);
                            progressDialog.cancel();
                            //showError();
                        }
                    });
                }
            }
        });
    }

    private void analyzeResults(SentegrityTrustScoreComputation computationResults, SentegrityPolicy policy) {
        this.computationResults = computationResults;
        if (computationResults.isDeviceTrusted()) {
            showInfoDialog();
            //update current state
        } else {

            final ProtectMode protectMode = new ProtectMode(policy, computationResults.getProtectModeWhitelist());

            switch (computationResults.getProtectModeAction()) {
                case 1:
                    SentegrityStartupStore.getInstance().setCurrentState("Waiting for user password after anomaly");
                    showError("Login required", "Enter password to continue", new OnLoginListener() {
                        @Override
                        public boolean onLogin(String password) {
                            return protectMode.deactivateProtectMode(1, password);
                        }
                    });
                    break;
                case 2:
                    SentegrityStartupStore.getInstance().setCurrentState("Waiting for user password after policy violation");
                    showError("Policy violation", "You are in violation of a policy. This attempt has been recorded. \n\nEnter password to continue.", new OnLoginListener() {
                        @Override
                        public boolean onLogin(String password) {
                            return protectMode.deactivateProtectMode(1, password);
                        }
                    });
                    break;
                case 3:
                    SentegrityStartupStore.getInstance().setCurrentState("Waiting for user password after warning");
                    showError("High risk device", "Access may result in data breach. This attempt has been recorded. \n\nEnter password to continue.", new OnLoginListener() {
                        @Override
                        public boolean onLogin(String password) {
                            return protectMode.deactivateProtectMode(1, password);
                        }
                    });
                    break;
                case 4:
                    SentegrityStartupStore.getInstance().setCurrentState("Waiting after locking out");
                    accessDeniedError("Application locked", "Access denied");
                    break;
                case 5:
                    SentegrityStartupStore.getInstance().setCurrentState("Waiting for user override password");
                    showError("High risk device", "The conditions of this device require administrator approval to continue. \n\nEnter override PIN to continue.", new OnLoginListener() {
                        @Override
                        public boolean onLogin(String password) {
                            return protectMode.deactivateProtectMode(1, password);
                        }
                    });
                    break;
            }
        }

    }

    private void showError(final String title, final String message, final OnLoginListener clickListener) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        final View view = LayoutInflater.from(this).inflate(R.layout.dialog_login_error_password, null);
        final EditText password = (EditText) view.findViewById(R.id.password);

        dialogBuilder.setView(view);

        dialogBuilder.setTitle(title);
        dialogBuilder.setMessage(message);
        dialogBuilder.setPositiveButton("Login", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                if (clickListener.onLogin(password.getText().toString())) {
                    showInfoDialog();

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(password.getWindowToken(), 0);
                } else {
                    dialog.dismiss();
                    showError(title, message, clickListener);
                }
            }
        });
        dialogBuilder.setNegativeButton("View Issues", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                finishAffinity();
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.setCancelable(false);
        b.show();
    }

    private void accessDeniedError(String title, String message) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        dialogBuilder.setTitle(title);
        dialogBuilder.setMessage(message);
        AlertDialog b = dialogBuilder.create();
        b.setCancelable(false);
        b.show();
    }

    private void showInfoDialog() {
        if (computationResults.isDeviceTrusted()) {
            showDialog("AccessGranted", "You've been transparently authenticated.");
        } else if (computationResults.isSystemTrusted()) {
            showDialog("What happened?", "A password was required\ndue to abnormal user activity.");
        } else {
            showDialog("What happened?", "Data breach may occur\ndue to a high risk device.");
        }
    }

    private void showDialog(final String title, final String message) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        dialogBuilder.setTitle(title);
        dialogBuilder.setMessage(message);
        dialogBuilder.setPositiveButton("View dashboard", null);
        AlertDialog b = dialogBuilder.create();
        b.setCancelable(false);
        b.show();

        b.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                finishAffinity();
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        requestActivityUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void requestActivityUpdates() {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, "GoogleApiClient not yet connected", Toast.LENGTH_SHORT).show();
        } else {
            removeActivityUpdates();
            ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mGoogleApiClient, 1000 * 60 * 3, getActivityDetectionPendingIntent());
        }
    }

    public void removeActivityUpdates() {
        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mGoogleApiClient, getActivityDetectionPendingIntent());
    }

    private PendingIntent getActivityDetectionPendingIntent() {
        Intent intent = new Intent(this, ActivitiesIntentService.class);

        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private interface OnLoginListener {
        boolean onLogin(String password);
    }
}
