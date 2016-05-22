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
import com.sentegrity.core_detection.constants.AuthenticationResult;
import com.sentegrity.core_detection.constants.PostAuthAction;
import com.sentegrity.core_detection.constants.PreAuthAction;
import com.sentegrity.core_detection.logger.SentegrityError;
import com.sentegrity.core_detection.login_action.SentegrityLoginAction;
import com.sentegrity.core_detection.login_action.SentegrityLoginResponseObject;
import com.sentegrity.core_detection.startup.SentegrityStartupStore;

import java.io.File;

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

        File f = new File(SentegrityStartupStore.getInstance().getStorePath());
        if(!f.exists()){
            String dummyPass = "asdf";
            String masterKey = SentegrityStartupStore.getInstance().createNewStartupFileWithUserPassword(dummyPass);
        }

        CoreDetection.getInstance().performCoreDetection(new CoreDetectionCallback() {
            @Override
            public void onFinish(final SentegrityTrustScoreComputation computationResult, SentegrityError error, boolean success) {
                if (success) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            analyzePreAuthenticationActions();
                            progressDialog.cancel();
                            //showError();
                        }
                    });
                }
            }
        });
    }

    private void analyzePreAuthenticationActions() {
        final SentegrityTrustScoreComputation computationResults = CoreDetection.getInstance().getComputationResult();

        switch (computationResults.getPreAuthenticationAction()) {
            case PreAuthAction.TRANSPARENTLY_AUTHENTICATE:

                SentegrityLoginResponseObject loginResponseObject1 = SentegrityLoginAction.getInstance().attemptLoginWithUserInput(null);

                computationResults.setAuthenticationResult(loginResponseObject1.getAuthenticationResponseCode());

                SentegrityStartupStore.getInstance().setStartupDataWithComputationResults(computationResults);

                switch (computationResults.getAuthenticationResult()){
                    case AuthenticationResult.SUCCESS:
                        byte[] decryptedMasterKey = loginResponseObject1.getDecryptedMasterKey();
                        startActivity(new Intent(this, DashboardActivity.class));
                        finishAffinity();
                        break;
                    default:
                        computationResults.setPreAuthenticationAction(PreAuthAction.PROMPT_USER_FOR_PASSWORD);
                        computationResults.setPostAuthenticationAction(PostAuthAction.WHITELIST_USER_ASSERTIONS);
                        analyzePreAuthenticationActions();
                        break;
                }

                break;
            case PreAuthAction.BLOCK_AND_WARN:

                SentegrityLoginResponseObject loginResponseObject2 = SentegrityLoginAction.getInstance().attemptLoginWithUserInput(null);

                computationResults.setAuthenticationResult(loginResponseObject2.getAuthenticationResponseCode());

                SentegrityStartupStore.getInstance().setStartupDataWithComputationResults(computationResults);

                showDialog(loginResponseObject2.getResponseLoginTitle(), loginResponseObject2.getResponseLoginDescription(), "TrustScore details", new OnLoginListener() {
                    @Override
                    public void onLogin(String password) {
                        startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                        finishAffinity();
                    }
                });

                break;
            case PreAuthAction.PROMPT_USER_FOR_PASSWORD:

                showError("User Login", "Enter user password", new OnLoginListener() {
                    @Override
                    public void onLogin(String password) {
                        SentegrityLoginResponseObject loginResponseObject3 = SentegrityLoginAction.getInstance().attemptLoginWithUserInput(password);

                        computationResults.setAuthenticationResult(loginResponseObject3.getAuthenticationResponseCode());

                        SentegrityStartupStore.getInstance().setStartupDataWithComputationResults(computationResults);

                        if(computationResults.getAuthenticationResult() == AuthenticationResult.SUCCESS || computationResults.getAuthenticationResult() == AuthenticationResult.RECOVERABLE_ERROR){
                            byte[] decryptedMasterKey = loginResponseObject3.getDecryptedMasterKey();

                            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                            finishAffinity();
                        }else if(computationResults.getAuthenticationResult() == AuthenticationResult.INCORRECT_LOGIN){

                            showDialog(loginResponseObject3.getResponseLoginTitle(), loginResponseObject3.getResponseLoginDescription(), "Retry", new OnLoginListener() {
                                @Override
                                public void onLogin(String password) {
                                    analyzePreAuthenticationActions();
                                }
                            });

                        }else if(computationResults.getAuthenticationResult() == AuthenticationResult.IRRECOVERABLE_ERROR){

                            showDialog(loginResponseObject3.getResponseLoginTitle(), loginResponseObject3.getResponseLoginDescription(), "Retry", new OnLoginListener() {
                                @Override
                                public void onLogin(String password) {
                                    startActivity(new Intent(LoginActivity.this, LoginActivity.class));
                                    finishAffinity();
                                }
                            });

                        }
                    }
                });

                break;
            case PreAuthAction.PROMPT_USER_FOR_PASSWORD_AND_WARN:

                showError("Warning", "This device is high risk or in violation of policy, this access attempt will be reported.", new OnLoginListener() {
                    @Override
                    public void onLogin(String password) {
                        SentegrityLoginResponseObject loginResponseObject4 = SentegrityLoginAction.getInstance().attemptLoginWithUserInput(password);

                        computationResults.setAuthenticationResult(loginResponseObject4.getAuthenticationResponseCode());

                        SentegrityStartupStore.getInstance().setStartupDataWithComputationResults(computationResults);

                        if(computationResults.getAuthenticationResult() == AuthenticationResult.SUCCESS || computationResults.getAuthenticationResult() == AuthenticationResult.RECOVERABLE_ERROR){
                            byte[] decryptedMasterKey = loginResponseObject4.getDecryptedMasterKey();

                            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                            finishAffinity();
                        }else if(computationResults.getAuthenticationResult() == AuthenticationResult.INCORRECT_LOGIN){

                            showDialog(loginResponseObject4.getResponseLoginTitle(), loginResponseObject4.getResponseLoginDescription(), "Retry", new OnLoginListener() {
                                @Override
                                public void onLogin(String password) {
                                    analyzePreAuthenticationActions();
                                }
                            });

                        }else if(computationResults.getAuthenticationResult() == AuthenticationResult.IRRECOVERABLE_ERROR){

                            showDialog(loginResponseObject4.getResponseLoginTitle(), loginResponseObject4.getResponseLoginDescription(), "Retry", new OnLoginListener() {
                                @Override
                                public void onLogin(String password) {
                                    startActivity(new Intent(LoginActivity.this, LoginActivity.class));
                                    finishAffinity();
                                }
                            });

                        }
                    }
                });

                break;
            default:
                break;
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
                //showInfoDialog();
                clickListener.onLogin(password.getText().toString());
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(password.getWindowToken(), 0);
            }
        });
        dialogBuilder.setNegativeButton("TrustScore details", new DialogInterface.OnClickListener() {
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

    private void showDialog(String title, String message, String actionTitle, final OnLoginListener onLoginListener) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        dialogBuilder.setTitle(title);
        dialogBuilder.setMessage(message);
        dialogBuilder.setPositiveButton(actionTitle, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onLoginListener.onLogin(null);
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.setCancelable(false);
        b.show();
    }

    /*private void showInfoDialog() {
        if (computationResults.isDeviceTrusted()) {
            showDialog("AccessGranted", "You've been transparently authenticated.");
        } else if (computationResults.isSystemTrusted()) {
            showDialog("What happened?", "A password was required\ndue to abnormal user activity.");
        } else {
            showDialog("What happened?", "Data breach may occur\ndue to a high risk device.");
        }
    }*/

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
            ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mGoogleApiClient, 1000 * 90, getActivityDetectionPendingIntent());
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
        void onLogin(String password);
    }
}
