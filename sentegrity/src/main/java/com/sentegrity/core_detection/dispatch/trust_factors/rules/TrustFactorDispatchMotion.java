package com.sentegrity.core_detection.dispatch.trust_factors.rules;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.internal.LinkedTreeMap;
import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorOutput;
import com.sentegrity.core_detection.constants.DNEStatusCode;
import com.sentegrity.core_detection.dispatch.trust_factors.SentegrityTrustFactorDatasets;
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.gyro.GyroRadsObject;
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.gyro.PitchRollObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmestrov on 25/03/16.
 */
public class TrustFactorDispatchMotion {

    public static SentegrityTrustFactorOutput orientation(List<Object> payload){
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        List<String> outputList = new ArrayList<>();

        String orientation = SentegrityTrustFactorDatasets.getInstance().getDeviceOrientation();

        if(TextUtils.equals("Face_Down", orientation) || TextUtils.equals("unknown", orientation)){
            output.setStatusCode(DNEStatusCode.INVALID);
            return output;
        }

        outputList.add(orientation);

        output.setOutput(outputList);

        return output;
    }

    public static SentegrityTrustFactorOutput movement(List<Object> payload){
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        if(!SentegrityTrustFactorDatasets.validatePayload(payload)){
            output.setStatusCode(DNEStatusCode.NO_DATA);
            return output;
        }

        List<String> outputList = new ArrayList<>();

        if(SentegrityTrustFactorDatasets.getInstance().getGyroMotionDNEStatus() != DNEStatusCode.OK &&
                SentegrityTrustFactorDatasets.getInstance().getGyroMotionDNEStatus() != DNEStatusCode.EXPIRED){
            output.setStatusCode(SentegrityTrustFactorDatasets.getInstance().getGyroMotionDNEStatus());
            return output;
        }

        float gripMovement = 0.0f;
        gripMovement = SentegrityTrustFactorDatasets.getInstance().getGripMovement();

        if(SentegrityTrustFactorDatasets.getInstance().getGyroMotionDNEStatus() != DNEStatusCode.OK){
            output.setStatusCode(SentegrityTrustFactorDatasets.getInstance().getGyroMotionDNEStatus());
            return output;
        }

        if(gripMovement == 0.0f){
            output.setStatusCode(DNEStatusCode.UNAVAILABLE);
            return output;
        }

        double movementBlockSize = (double) ((LinkedTreeMap)payload.get(0)).get("movementBlockSize");

        int movementBlock = (int) Math.round(gripMovement / movementBlockSize);

        String userMovement = SentegrityTrustFactorDatasets.getInstance().getUserMovement();

        Log.d("Movement", "current: " + userMovement);

        String motion = "grip_Movement_" + movementBlock + "_" + "device_Movement_" + userMovement;
        Log.d("grip", motion);

        outputList.add(motion);

        output.setOutput(outputList);

        return output;
    }

    public static SentegrityTrustFactorOutput grip(List<Object> payload){
        SentegrityTrustFactorOutput output = new SentegrityTrustFactorOutput();

        if(!SentegrityTrustFactorDatasets.validatePayload(payload)){
            output.setStatusCode(DNEStatusCode.NO_DATA);
            return output;
        }

        List<String> outputList = new ArrayList<>();

        List<GyroRadsObject> gyroRads;

        if(SentegrityTrustFactorDatasets.getInstance().getGyroMotionDNEStatus() != DNEStatusCode.OK &&
                SentegrityTrustFactorDatasets.getInstance().getGyroMotionDNEStatus() != DNEStatusCode.EXPIRED){
            output.setStatusCode(SentegrityTrustFactorDatasets.getInstance().getGyroMotionDNEStatus());
            return output;
        }else{
            gyroRads = SentegrityTrustFactorDatasets.getInstance().getGyroRads();

            if(SentegrityTrustFactorDatasets.getInstance().getGyroMotionDNEStatus() != DNEStatusCode.OK){
                output.setStatusCode(SentegrityTrustFactorDatasets.getInstance().getGyroMotionDNEStatus());
                return output;
            }

            if(gyroRads == null){
                output.setStatusCode(DNEStatusCode.UNAVAILABLE);
                return output;
            }
        }

        List<PitchRollObject> gyroPitch = SentegrityTrustFactorDatasets.getInstance().getGyroPitchRoll();

        if(gyroPitch == null){
            output.setStatusCode(DNEStatusCode.UNAVAILABLE);
            return output;
        }

        float gripMovement = 0.0f;
        gripMovement = SentegrityTrustFactorDatasets.getInstance().getGripMovement();

        if(gripMovement == 0.0f){
            output.setStatusCode(DNEStatusCode.UNAVAILABLE);
            return output;
        }

        float pitchTotal = 0.0f;
        float rollTotal = 0.0f;

        float pitchAvg = 0.0f;
        float rollAvg = 0.0f;

        int counter = 0;

        for(PitchRollObject pr : gyroPitch){
            pitchTotal += pr.pitch;
            rollTotal += pr.roll;
            counter ++;
        }

        pitchAvg = pitchTotal / counter;
        rollAvg = rollTotal / counter;


        Log.d("pitch", "pitch " + pitchAvg + ", roll " + rollAvg);

        double pitchBlockSize = (double) ((LinkedTreeMap)payload.get(0)).get("pitchBlockSize");
        double rollBlockSize = (double) ((LinkedTreeMap)payload.get(0)).get("rollBlockSize");

        int pitchBlock = (int) Math.round(pitchAvg / pitchBlockSize);
        int rollBlock = (int) Math.round(rollAvg / rollBlockSize);

        String motion = "pitch_" + pitchBlock + "_" + "roll_" + rollBlock;

        int movementBlock = (int) Math.round(gripMovement / 0.1);
        Log.d("pitch", motion + ", " + movementBlock);

        if(pitchBlock == 0 && movementBlock == 0){
            output.setStatusCode(DNEStatusCode.INVALID);
            return output;
        }

        outputList.add(motion);

        output.setOutput(outputList);

        return output;
    }
}
