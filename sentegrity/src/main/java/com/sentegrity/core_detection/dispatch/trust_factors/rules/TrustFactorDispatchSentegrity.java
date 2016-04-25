package com.sentegrity.core_detection.dispatch.trust_factors.rules;

import android.content.pm.ApplicationInfo;
import android.text.TextUtils;
import android.util.Log;

import com.sentegrity.core_detection.CoreDetection;
import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorOutput;
import com.sentegrity.core_detection.constants.DNEStatusCode;
import com.sentegrity.core_detection.dispatch.trust_factors.SentegrityTrustFactorDatasets;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dalvik.system.DexFile;

/**
 * Created by dmestrov on 25/03/16.
 */
public class TrustFactorDispatchSentegrity {

    public static SentegrityTrustFactorOutput tamper2(List<Object> payload){
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        List<String> outputList = new ArrayList<>();

        if (!SentegrityTrustFactorDatasets.validatePayload(payload)) {
            output.setStatusCode(DNEStatusCode.ERROR);
            return output;
        }

        String tamper = "";

        Boolean appSignatureChanged = SentegrityTrustFactorDatasets.getInstance().isAppSignatureOk();

        if(appSignatureChanged == null){
            //unknown
        }else if(!appSignatureChanged){
            tamper += "APK_SIGNATURE_CHANGED";
        }

        Boolean appRunOnEmulator = SentegrityTrustFactorDatasets.getInstance().isOnEmulator();

        if(appRunOnEmulator == null){
            //unknown
        }else if(appRunOnEmulator){
            tamper += "_RUN_ON_EMULATOR";
        }

        Boolean isDebuggable = SentegrityTrustFactorDatasets.getInstance().checkDebuggable();

        if(isDebuggable == null){
            //unknown
        }else if(isDebuggable){
            tamper += "_APP_IS_DEBUGGABLE";
        }

        Boolean isFromPlayStore = SentegrityTrustFactorDatasets.getInstance().isFromPlayStore();

        if(isFromPlayStore == null){
            //unknown
        }else if(!isFromPlayStore){
            tamper += "_APP_NOT_FROM_PLAY_STORE";
        }

        List<ApplicationInfo> userApps = SentegrityTrustFactorDatasets.getInstance().getInstalledAppInfo();

        if (userApps == null || userApps.size() == 0) {
            output.setStatusCode(DNEStatusCode.ERROR);
            return output;
        }

        for(ApplicationInfo applicationInfo : userApps) {
            if(applicationInfo.packageName.equals("de.robv.android.xposed.installer")) {
                tamper += "_XPOSED_FOUND";
            }
            if(applicationInfo.packageName.equals("com.saurik.substrate")) {
                tamper += "_SUBSTRATE_FOUND";
            }
        }

        try {
            throw new Exception("blah");
        }
        catch(Exception e) {
            int zygoteInitCallCount = 0;
            for(StackTraceElement stackTraceElement : e.getStackTrace()) {
                if(stackTraceElement.getClassName().equals("com.android.internal.os.ZygoteInit")) {
                    zygoteInitCallCount++;
                    if(zygoteInitCallCount == 2) {
                        tamper += "_SUBSTRATE_ACTIVE";
                    }
                }
                if(stackTraceElement.getClassName().equals("com.saurik.substrate.MS$2") &&
                        stackTraceElement.getMethodName().equals("invoked")) {
                    tamper += "_SUBSTRATE_HOOK_FOUND";
                }
                if(stackTraceElement.getClassName().equals("de.robv.android.xposed.XposedBridge") &&
                        stackTraceElement.getMethodName().equals("main")) {
                    tamper += "_XPOSED_ACTIVE";
                }
                if(stackTraceElement.getClassName().equals("de.robv.android.xposed.XposedBridge") &&
                        stackTraceElement.getMethodName().equals("handleHookedMethod")) {
                    tamper += "_XPOSED_HOOK_FOUND";
                }

            }
        }

        for (ApplicationInfo applicationInfo : userApps) {
            if (applicationInfo.processName.equals("com.android.sentegrity")) {
                Set<String> classes = new HashSet();
                DexFile dex;
                try {
                    dex = new DexFile(applicationInfo.sourceDir);
                    Enumeration<String> entries = dex.entries();
                    while(entries.hasMoreElements()) {
                        String entry = entries.nextElement();
                        classes.add(entry);
                    }
                    dex.close();
                }
                catch (IOException e) {
                }
                for(String className : classes) {
                    if(className.startsWith("com.android.sentegrity")) {
                        try {
                            //TODO: what to check here
                            Class clazz = CoreDetection.class.forName(className);
                            for(Method method : clazz.getDeclaredMethods()) {
                                if(Modifier.isNative(method.getModifiers())){
                                    tamper += "_FOUND_NATIVE";
                                }
                            }
                        }
                        catch(ClassNotFoundException e) {
                        }
                    }
                }
                break;
            }
        }

        try {
            Set<String> libraries = new HashSet();
            String mapsFilename = "/proc/" + android.os.Process.myPid() + "/maps";
            BufferedReader reader = new BufferedReader(new FileReader(mapsFilename));
            String line;
            while((line = reader.readLine()) != null) {
                if (line.endsWith(".so") || line.endsWith(".jar")) {
                    int n = line.lastIndexOf(" ");
                    libraries.add(line.substring(n + 1));
                }
            }
            for (String library : libraries) {
                if(library.contains("com.saurik.substrate")) {
                    //has substrate shared object
                }
                if(library.contains("XposedBridge.jar")) {
                    //has exposed jar
                }
            }
            reader.close();
        }
        catch (Exception e) {
        }



        if(!TextUtils.isEmpty(tamper)) {
            if(tamper.startsWith("_"))
                tamper = tamper.substring(1);
            outputList.add(tamper);
        }

        output.setOutput(outputList);

        return output;
    }
}
