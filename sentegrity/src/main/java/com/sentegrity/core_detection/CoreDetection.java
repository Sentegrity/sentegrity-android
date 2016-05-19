package com.sentegrity.core_detection;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorOutput;
import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorStore;
import com.sentegrity.core_detection.baseline_analysis.SentegrityBaselineAnalysis;
import com.sentegrity.core_detection.computation.SentegrityTrustScoreComputation;
import com.sentegrity.core_detection.dispatch.SentegrityTrustFactorDispatcher;
import com.sentegrity.core_detection.dispatch.activity_dispatcher.SentegrityActivityDispatcher;
import com.sentegrity.core_detection.dispatch.trust_factors.SentegrityTrustFactorDatasets;
import com.sentegrity.core_detection.logger.ErrorDetails;
import com.sentegrity.core_detection.logger.ErrorDomain;
import com.sentegrity.core_detection.logger.Logger;
import com.sentegrity.core_detection.logger.SentegrityError;
import com.sentegrity.core_detection.policy.SentegrityPolicy;
import com.sentegrity.core_detection.policy.SentegrityPolicyParser;
import com.sentegrity.core_detection.result_analysis.SentegrityResultAnalysis;
import com.sentegrity.core_detection.startup.SentegrityStartup;
import com.sentegrity.core_detection.startup.SentegrityStartupStore;
import com.sentegrity.core_detection.transparent_authentication.SentegrityTransparentAuthentication;
import com.sentegrity.core_detection.utilities.KeyValueStorage;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmestrov on 20/03/16.
 */
public class CoreDetection {

    private static final String STORAGE_NAME = "CoreDetection";
    private static final int STORAGE_MODE = Context.MODE_PRIVATE;

    private final Context context;

    private static CoreDetection sInstance;

    private SentegrityPolicy currentPolicy;

    private SentegrityTrustScoreComputation computationResult;

    private CoreDetectionCallback coreDetectionCallback;

    private KeyValueStorage keyValueStorage;

    private SentegrityActivityDispatcher activityDispatcher;

    private CoreDetection(Context context) {
        this.context = context;
        keyValueStorage = new KeyValueStorage(context.getSharedPreferences(STORAGE_NAME, STORAGE_MODE));
        reset();
    }

    public static CoreDetection getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("Please call CoreDetection.initialize({context}) before requesting the instance.");
        } else {
            return sInstance;
        }
    }

    public static synchronized void initialize(Context context) {
        if (sInstance == null) {
            sInstance = new CoreDetection(context);
            Dexter.initialize(context);

            //sInstance.startCoreDetectionActivities();
        } else {
            Log.d("coreDetection", "Core Detection has already been initialized");
        }
    }

    public KeyValueStorage getKeyValueStorage() {
        return keyValueStorage;
    }

    public SentegrityPolicy parsePolicy(String policyName) {
        AssetManager mg = context.getResources().getAssets();

        String policyJson;
        try {
            InputStream is = mg.open(policyName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            policyJson = new String(buffer, "UTF-8");

            return new Gson().fromJson(policyJson, SentegrityPolicy.class);

        } catch (IOException ex) {
            ex.printStackTrace();
            Logger.INFO("Core Detection Parse policy error: Error occurred during policy file read.");
            return null;
        } catch (JsonSyntaxException ex) {
            ex.printStackTrace();
            Logger.INFO("Core Detection Parse policy error: Json syntax exception during policy parsing");
            return null;
        }
    }

    public void performCoreDetection(final CoreDetectionCallback callback){
        if (activityDispatcher == null)
            activityDispatcher = new SentegrityActivityDispatcher(context);

        activityDispatcher.startBluetooth();
        activityDispatcher.startAmbientLight();
        activityDispatcher.startCellularSignal();
        activityDispatcher.startMotion();
        activityDispatcher.startNetstat();
        activityDispatcher.startRootCheck();
        activityDispatcher.startPkgCollection();

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            activityDispatcher.startLocation();
            startCoreDetection(callback);
            return;
        }

        final String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        MultiplePermissionsListener listener = new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                activityDispatcher.startLocation();
                startCoreDetection(callback);
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                //we should use either one of these
                //depending on do we want to wait for permission or just continue
                //token.continuePermissionRequest();
                activityDispatcher.startLocation();
                startCoreDetection(callback);
            }
        };
        if (!Dexter.isRequestOngoing())
            Dexter.continuePendingRequestsIfPossible(listener);
        Dexter.checkPermissions(listener, permissions);
    }

    public void startCoreDetection(final CoreDetectionCallback callback) {

        AsyncTask coreDetectionTask = new AsyncTask() {

            private boolean success;
            private SentegrityTrustScoreComputation computation;
            private SentegrityError error;

            @Override
            protected Object doInBackground(Object[] params) {
                //testing purposes
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // set task to max priority
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

                computationResult = null;

                if (callback == null) {
                    SentegrityError error = SentegrityError.NO_CALLBACK_BLOCK_PROVIDED;
                    error.setDomain(ErrorDomain.CORE_DETECTION_DOMAIN);
                    error.setDetails(new ErrorDetails().setDescription("Perform Core Detection Unsuccessful").setFailureReason("An invalid callback was provided").setRecoverySuggestion("Try passing a valid callback block"));

                    Logger.INFO("Perform Core Detection Unsuccessful", error);
                    this.success = false;
                    this.computation = null;
                    this.error = error;
                    //processCoreDetectionResponse(new SentegrityTrustScoreComputation(), false, error);

                    return null;
                }
                coreDetectionCallback = callback;

                SentegrityPolicyParser.getInstance().setCurrentPolicy(null);

                SentegrityPolicy policy = SentegrityPolicyParser.getInstance().getPolicy();

                if (policy == null || policy.getTrustFactors() == null || policy.getTrustFactors().size() < 1) {
                    SentegrityError error = SentegrityError.CORE_DETECTION_NO_POLICY_PROVIDED;
                    error.setDomain(ErrorDomain.CORE_DETECTION_DOMAIN);
                    error.setDetails(new ErrorDetails().setDescription("Perform Core Detection Unsuccessful").setFailureReason("An invalid policy was provided").setRecoverySuggestion("Try passing a valid policy"));

                    Logger.INFO("Perform Core Detection Unsuccessful", error);
                    this.success = false;
                    this.computation = null;
                    this.error = error;
                    //processCoreDetectionResponse(new SentegrityTrustScoreComputation(), false, error);
                    return null;
                }

                SentegrityStartup startup = SentegrityStartupStore.getInstance().getStartupStore();

                if(startup == null){
                    SentegrityError error = SentegrityError.INVALID_STARTUP_INSTANCE;
                    error.setDomain(ErrorDomain.SENTEGRITY_DOMAIN);
                    error.setDetails(new ErrorDetails().setDescription("Perform Core Detection Unsuccessful").setFailureReason("No startup file received").setRecoverySuggestion("Try validating the startup file"));

                    Logger.INFO("Perform Core Detection Unsuccessful", error);
                }

                startup.setRunCount(startup.getRunCount() + 1);

                SentegrityStartupStore.getInstance().setCurrentState("Starting Core Detection");

                List<SentegrityTrustFactorOutput> outputs = SentegrityTrustFactorDispatcher.performTrustFactorAnalysis(policy.getTrustFactors(), policy.getTimeout());
                if (outputs == null) {
                    SentegrityError error = SentegrityError.NO_TRUSTFACTOR_OUTPUT_OBJECTS_FOR_COMPUTATION;
                    error.setDomain(ErrorDomain.CORE_DETECTION_DOMAIN);
                    error.setDetails(new ErrorDetails().setDescription("Perform Core Detection Unsuccessful").setFailureReason("No trust factors output objects available for computation").setRecoverySuggestion("Double check provided trust factors to analyze"));

                    Logger.INFO("Perform Core Detection Unsuccessful", error);
                    this.success = false;
                    this.computation = null;
                    this.error = error;
                    //processCoreDetectionResponse(new SentegrityTrustScoreComputation(), false, error);
                    return null;
                }

                SentegrityStartupStore.getInstance().setCurrentState("Performing baseline analysis");

                List<SentegrityTrustFactorOutput> updatedOutputs = SentegrityBaselineAnalysis.performBaselineAnalysis(outputs, policy);

                if (updatedOutputs == null) {
                    SentegrityError error = SentegrityError.NO_TRUSTFACTOR_OUTPUT_OBJECTS_FOR_COMPUTATION;
                    error.setDomain(ErrorDomain.CORE_DETECTION_DOMAIN);
                    error.setDetails(new ErrorDetails().setDescription("Perform Core Detection Unsuccessful").setFailureReason("No trust factors output objects available for computation").setRecoverySuggestion("Double check provided trust factors to analyze"));

                    Logger.INFO("Perform Core Detection Unsuccessful", error);
                    this.success = false;
                    this.computation = null;
                    this.error = error;
                    //processCoreDetectionResponse(new SentegrityTrustScoreComputation(), false, error);
                    return null;
                }

                SentegrityStartupStore.getInstance().setCurrentState("Performing computation");

                SentegrityTrustScoreComputation computationResults = SentegrityTrustScoreComputation.performTrustFactorComputation(policy, updatedOutputs);

                if (computationResults == null) {
                    SentegrityError error = SentegrityError.ERROR_DURING_COMPUTATION;
                    error.setDomain(ErrorDomain.CORE_DETECTION_DOMAIN);
                    error.setDetails(new ErrorDetails().setDescription("Perform Core Detection Unsuccessful").setFailureReason("No computation objects returned / Error during computation").setRecoverySuggestion("Check error logs for details"));

                    Logger.INFO("Perform Core Detection Unsuccessful", error);
                    this.success = false;
                    this.computation = null;
                    this.error = error;
                    //processCoreDetectionResponse(new SentegrityTrustScoreComputation(), false, error);
                    return null;
                }

                SentegrityStartupStore.getInstance().setCurrentState("Performing result analysis");

                computationResults = SentegrityResultAnalysis.analyzeResults(computationResults, policy);

                if (computationResults == null) {
                    SentegrityError error = SentegrityError.ERROR_DURING_COMPUTATION;
                    error.setDomain(ErrorDomain.CORE_DETECTION_DOMAIN);
                    error.setDetails(new ErrorDetails().setDescription("Perform Core Detection Unsuccessful").setFailureReason("No result analysis objects returned / Error during result analysis").setRecoverySuggestion("Check error logs for details"));

                    Logger.INFO("Perform Core Detection Unsuccessful", error);
                    this.success = false;
                    this.computation = null;
                    this.error = error;
                    //processCoreDetectionResponse(new SentegrityTrustScoreComputation(), false, error);
                    return null;
                }

                if(policy.getTransparentAuthEnabled() == 1 && computationResults.isShouldAttemptTransparentAuthentication()){
                    SentegrityStartupStore.getInstance().setCurrentState("Performing transparent authentication");

                    computationResults = SentegrityTransparentAuthentication.getInstance().attemptTransparentAuthentication(computationResults, policy);

                    if(computationResults == null){
                        SentegrityError error = SentegrityError.ERROR_DURING_COMPUTATION;
                        error.setDomain(ErrorDomain.CORE_DETECTION_DOMAIN);
                        error.setDetails(new ErrorDetails().setDescription("Perform Core Detection Unsuccessful").setFailureReason("No result analysis objects returned / Error during result analysis").setRecoverySuggestion("Check error logs for details"));

                        Logger.INFO("Perform Core Detection Unsuccessful", error);
                        this.success = false;
                        this.computation = null;
                        this.error = error;
                        //processCoreDetectionResponse(new SentegrityTrustScoreComputation(), false, error);
                        return null;
                    }
                }

                if(computationResults.getPostAuthenticationAction() == 0 || computationResults.getPreAuthenticationAction() == 0 || computationResults.getCoreDetectionResult() == 0){
                    SentegrityError error = SentegrityError.ERROR_DURING_COMPUTATION;
                    error.setDomain(ErrorDomain.CORE_DETECTION_DOMAIN);
                    error.setDetails(new ErrorDetails().setDescription("Perform Core Detection Unsuccessful").setFailureReason("No result analysis objects returned / Error during result analysis").setRecoverySuggestion("Check error logs for details"));

                    Logger.INFO("Perform Core Detection Unsuccessful", error);
                    this.success = false;
                    this.computation = null;
                    this.error = error;
                    //processCoreDetectionResponse(new SentegrityTrustScoreComputation(), false, error);
                    return null;
                }

                setComputationResult(computationResults);

                this.success = true;
                this.computation = computationResults;
                this.error = null;
                //processCoreDetectionResponse(computationResults, true, null);

                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                coreDetectionResponse(computation, success, error);
            }
        };

        coreDetectionTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    private void coreDetectionResponse(SentegrityTrustScoreComputation computationResult, boolean success, SentegrityError error) {
        if (coreDetectionCallback != null){
            coreDetectionCallback.onFinish(computationResult, error, success);
        }else{
            SentegrityError error2 = SentegrityError.ERROR_DURING_COMPUTATION;
            error2.setDomain(ErrorDomain.CORE_DETECTION_DOMAIN);
            error2.setDetails(new ErrorDetails().setDescription("Perform Core Detection Unsuccessful").setFailureReason("No result analysis objects returned / Error during result analysis").setRecoverySuggestion("Check error logs for details"));

            Logger.INFO("Perform Core Detection Unsuccessful", error2);
        }
    }

    public SentegrityTrustScoreComputation getComputationResult() {
        return computationResult;
    }

    public void setComputationResult(SentegrityTrustScoreComputation computationResult) {
        this.computationResult = computationResult;
    }


    /**
     * Call if you want to restart data
     */
    public synchronized void reset() {
        SentegrityTrustFactorStore.initialize(context);
        SentegrityStartupStore.initialize(context);
        SentegrityTrustFactorDatasets.initialize(context);
        SentegrityPolicyParser.initialize(context);

        //startCoreDetectionActivities();
    }

}
