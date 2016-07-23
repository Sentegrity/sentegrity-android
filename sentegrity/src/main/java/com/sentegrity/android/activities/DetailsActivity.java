package com.sentegrity.android.activities;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.internal.LinkedTreeMap;
import com.sentegrity.android.R;
import com.sentegrity.core_detection.constants.DNEStatusCode;
import com.sentegrity.core_detection.dispatch.trust_factors.SentegrityTrustFactorDatasets;
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.SentegrityTrustFactorDatasetMotion;
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.gyro.AccelRadsObject;
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.gyro.GyroRadsObject;
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.gyro.MagneticObject;
import com.sentegrity.core_detection.dispatch.trust_factors.helpers.gyro.PitchRollObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Created by dmestrov on 23/07/16.
 */
public class DetailsActivity extends Activity {

    TextView gyroSensorOutput, gyroValue;
    TextView rollSensorOutput, rollValue;
    TextView pitchSensorOutput, pitchValue;
    TextView magnetSensorOutput, magnetValue;
    TextView orientationSensorOutput, orientationValue;

    EditText gyroBlockSize, pitchBlockSize, rollBlockSize, magneticBlockSize;

    List<SensorEventListener> listenerList = new ArrayList<>();
    boolean startCollecting = false;


    private List<AccelRadsObject> accelRadsArray = new ArrayList<>();
    private List<PitchRollObject> pitchRollArray = new ArrayList<>();
    private List<MagneticObject> headingsArray = new ArrayList<>();
    private List<GyroRadsObject> gyroRadsArray = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_details);

        gyroSensorOutput = (TextView) findViewById(R.id.gyro_sensor_output);
        gyroValue = (TextView) findViewById(R.id.gyro_value);
        rollSensorOutput = (TextView) findViewById(R.id.roll_sensor_output);
        rollValue = (TextView) findViewById(R.id.roll_value);
        pitchSensorOutput = (TextView) findViewById(R.id.pitch_sensor_output);
        pitchValue = (TextView) findViewById(R.id.pitch_value);
        magnetSensorOutput = (TextView) findViewById(R.id.magnet_sensor_output);
        magnetValue = (TextView) findViewById(R.id.magnet_value);
        orientationSensorOutput = (TextView) findViewById(R.id.orientation_sensor_output);
        orientationValue = (TextView) findViewById(R.id.orientation_value);


        gyroBlockSize = (EditText) findViewById(R.id.gyro_block_size);
        pitchBlockSize = (EditText) findViewById(R.id.pitch_block_size);
        rollBlockSize = (EditText) findViewById(R.id.roll_block_size);
        magneticBlockSize = (EditText) findViewById(R.id.magnetic_block_size);

        findViewById(R.id.close_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        setup();

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateValues();
            }
        }, 0, TimeUnit.SECONDS.toMillis(1));
    }

    private double getBlockSize(EditText et){
        if(TextUtils.isEmpty(et.getText()))
            return 1;
        else {
            try {
                double value = Double.valueOf(et.getText().toString());
                if(value == 0)
                    return 1;
                else
                    return value;
            }catch (Exception e){
                return 1;
            }
        }
    }

    private void updateValues(){
        accelRadsArray = new ArrayList<>();
        pitchRollArray = new ArrayList<>();
        headingsArray = new ArrayList<>();
        gyroRadsArray = new ArrayList<>();
    }

    private void setup(){
        accelRadsArray = new ArrayList<>();
        pitchRollArray = new ArrayList<>();
        headingsArray = new ArrayList<>();
        gyroRadsArray = new ArrayList<>();

        final SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //Gyro Data (grip)
        Sensor gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (gyroSensor == null) {
            SentegrityTrustFactorDatasets.getInstance().setGyroMotionDNEStatus(DNEStatusCode.UNSUPPORTED);
        } else {
            //USER MOVEMENT!?


            //GRIP MOVEMENT
            SensorEventListener listener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {

                    GyroRadsObject object = new GyroRadsObject(event.values);

                    if(gyroRadsArray.size() < 5)
                        gyroRadsArray.add(object);

                    gyroSensorOutput.setText(object.toString());

                    if (gyroRadsArray.size() == 4){
                        gyroValue.setText("" + (int)(SentegrityTrustFactorDatasetMotion.getGripMovement(gyroRadsArray) / getBlockSize(gyroBlockSize)));
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            };

            listenerList.add(listener);

            sensorManager.registerListener(listener, gyroSensor, SensorManager.SENSOR_DELAY_UI, 100000);
        }


        Sensor acc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor mag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (acc == null || mag == null) {
            //we don't have pitch/roll data ?
        } else {
            SensorEventListener listener = new SensorEventListener() {
                float rotation[] = null; //for gravity rotational data
                float accels[] = new float[3];
                float mags[] = new float[3];
                float[] values = new float[3];

                @Override
                public void onSensorChanged(SensorEvent event) {
                    switch (event.sensor.getType()) {
                        case Sensor.TYPE_MAGNETIC_FIELD:
                            mags = event.values.clone();
                            break;
                        case Sensor.TYPE_ACCELEROMETER:
                            accels = event.values.clone();
                            break;
                    }

                    if (mags != null && accels != null) {
                        rotation = new float[9];
                        SensorManager.getRotationMatrix(rotation, null, accels, mags);

                        float[] outR = new float[9];
                        SensorManager.remapCoordinateSystem(rotation, SensorManager.AXIS_X, SensorManager.AXIS_Y, outR);
                        SensorManager.getOrientation(outR, values);

                        mags = null;
                        accels = null;

                        //first values can come as 0.0, 0.0, 0.0 --> we don't need those
                        if (values[0] == 0 && values[1] == 0 && values[2] == 0)
                            return;

                        PitchRollObject object = new PitchRollObject(values);

                        if(pitchRollArray.size() < 5)
                            pitchRollArray.add(object);

                        pitchSensorOutput.setText(object.pitch + "");
                        rollSensorOutput.setText(object.roll + "");

                        if (pitchRollArray.size() == 4){

                            float pitchTotal = 0.0f;
                            float rollTotal = 0.0f;

                            float pitchAvg = 0.0f;
                            float rollAvg = 0.0f;

                            int counter = 0;

                            for(PitchRollObject pr : pitchRollArray){
                                pitchTotal += pr.pitch;
                                rollTotal += pr.roll;
                                counter ++;
                            }

                            pitchAvg = pitchTotal / counter;
                            rollAvg = rollTotal / counter;

                            pitchValue.setText("" + (int) Math.round(pitchAvg / getBlockSize(pitchBlockSize)));
                            rollValue.setText("" + (int) Math.round(rollAvg / getBlockSize(rollBlockSize)));
                        }
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            };
            listenerList.add(listener);
            sensorManager.registerListener(listener, acc, SensorManager.SENSOR_DELAY_UI, 100000);
            sensorManager.registerListener(listener, mag, SensorManager.SENSOR_DELAY_UI, 100000);
        }

        //MAGNETOMETER DATA
        Sensor magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED);
        if (magnetometerSensor == null) {
            SentegrityTrustFactorDatasets.getInstance().setMagneticHeadingDNEStatus(DNEStatusCode.UNSUPPORTED);
        } else {
            SensorEventListener listener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {

                    MagneticObject object = new MagneticObject(event.values);

                    if(headingsArray.size() < 7)
                        headingsArray.add(object);

                    magnetSensorOutput.setText(object.toString());

                    if (headingsArray.size() == 6) {
                        float magnitudeAverage;
                        float magnitudeTotal = 0.0f;
                        float magnitude;

                        int counter = 0;

                        for (MagneticObject heading : headingsArray) {
                            magnitude = (float) Math.sqrt(Math.pow(heading.x, 2) + Math.pow(heading.y, 2) + Math.pow(heading.z, 2));
                            magnitudeTotal += magnitude;
                            counter++;
                        }

                        magnitudeAverage = magnitudeTotal / counter;

                        magnetValue.setText("" + (int) Math.ceil(Math.abs(magnitudeAverage) / getBlockSize(magneticBlockSize)));
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            };

            listenerList.add(listener);

            sensorManager.registerListener(listener, magnetometerSensor, SensorManager.SENSOR_DELAY_UI, 100000);
        }

        //ACCELEROMETER DATA
        Sensor accelerometerData = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        if (accelerometerData == null) {

        } else {
            SensorEventListener listener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {

                    AccelRadsObject object = new AccelRadsObject(event.values);

                    if(accelRadsArray.size() < 5)
                        accelRadsArray.add(object);

                    orientationSensorOutput.setText(object.toString());

                    if (accelRadsArray.size() == 4){
                        orientationValue.setText(SentegrityTrustFactorDatasetMotion.getOrientation(DetailsActivity.this, accelRadsArray));
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            };

            listenerList.add(listener);

            sensorManager.registerListener(listener, accelerometerData, SensorManager.SENSOR_DELAY_UI, 100000);
        }
    }

    @Override
    protected void onDestroy() {
        final SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        for(SensorEventListener listener : listenerList){
            sensorManager.unregisterListener(listener);
        }
        super.onDestroy();
    }
}
