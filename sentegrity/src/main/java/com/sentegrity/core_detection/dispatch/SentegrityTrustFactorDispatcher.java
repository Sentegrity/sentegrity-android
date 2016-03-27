package com.sentegrity.core_detection.dispatch;

import android.util.Log;

import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorOutput;
import com.sentegrity.core_detection.constants.DNEStatusCode;
import com.sentegrity.core_detection.policy.SentegrityTrustFactor;
import com.sentegrity.core_detection.startup.SentegrityStartup;
import com.sentegrity.core_detection.startup.SentegrityStartupStore;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by dmestrov on 23/03/16.
 */
public class SentegrityTrustFactorDispatcher {

    private static final String PACKAGE_NAME = SentegrityTrustFactorDispatcher.class.getPackage().getName() + ".trust_factors.rules." + "TrustFactorDispatch";

    public static List<SentegrityTrustFactorOutput> performTrustFactorAnalysis(List<SentegrityTrustFactor> trustFactors, int timeout) {

        boolean timeoutHit = false;
        long timeoutMS = timeout * 1000;
        long startTime = System.currentTimeMillis();
        SentegrityStartupStore.getInstance().setCurrentState("Performing TrustFactor Analysis");

        SentegrityStartup startup = SentegrityStartupStore.getInstance().getStartupData();

        List<SentegrityTrustFactorOutput> trustFactorOutputs = new ArrayList<>();

        long execTimeStart = System.nanoTime();
        for (SentegrityTrustFactor tf : trustFactors) {
            if(timeoutHit){
                SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();
                output.setStatusCode(DNEStatusCode.EXPIRED);
                output.setTrustFactor(tf);
                trustFactorOutputs.add(output);
                continue;
            }

            long currentTime = System.currentTimeMillis();
            if(currentTime - startTime > timeoutMS){
                SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();
                output.setStatusCode(DNEStatusCode.EXPIRED);
                output.setTrustFactor(tf);
                trustFactorOutputs.add(output);

                timeoutHit = true;
                continue;
            }

            SentegrityTrustFactorOutput output = executeTrustFactor(tf, startup);
            trustFactorOutputs.add(output);
        }
        long execTimeEnd = System.nanoTime();
        Log.d("coreDetection", "TrustFactorAnalysis done in: " + String.format("%.5f", (execTimeEnd - execTimeStart) / 1000.0f / 1000.0f / 1000.0f) + "seconds");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return trustFactorOutputs;
    }

    private static SentegrityTrustFactorOutput executeTrustFactor(SentegrityTrustFactor trustFactor, SentegrityStartup startup) {
        SentegrityTrustFactorOutput output;

        long startTime = System.nanoTime();
        output = runTrustFactorFromData(trustFactor.generateClassName(PACKAGE_NAME),
                trustFactor.generateImplementation(),
                trustFactor.getPayload());

        long endTime = System.nanoTime();
        Log.d("coreDetection", trustFactor.getName() + "Execution time: " + (endTime - startTime) + "ns");

        output.setTrustFactor(trustFactor);
        if(output.getOutput() != null && output.getOutput().size() > 0){
            output.setAssertionObjectsFromOutputWithDeviceSalt(startup.getDeviceSalt());
        }

        return output;
    }

    private static SentegrityTrustFactorOutput runTrustFactorFromData(String className, String method, List<Object> data) {
        SentegrityTrustFactorOutput output = null;
        try {
            //directly call method and pass payload
            Method m = Class.forName(className).getDeclaredMethod(method, List.class);
            output = (SentegrityTrustFactorOutput) m.invoke(null, data);

            //create new instance of interface and call run()
            //TrustFactorDispatch dispatch = (TrustFactorDispatch) Class.forName(className).newInstance();
            //output = dispatch.run(method, data);
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
            output = new SentegrityTrustFactorOutput();
            output.setStatusCode(DNEStatusCode.UNSUPPORTED);
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
            output = new SentegrityTrustFactorOutput();
            output.setStatusCode(DNEStatusCode.ERROR);
        }

        return output;
    }
}
