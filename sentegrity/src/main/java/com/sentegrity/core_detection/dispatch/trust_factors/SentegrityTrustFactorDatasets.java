package com.sentegrity.core_detection.dispatch.trust_factors;

import java.util.Calendar;
import java.util.List;

/**
 * Created by dmestrov on 23/03/16.
 */
public class SentegrityTrustFactorDatasets {

    final private long runTime;

    private int hourOfDay = -1;
    private int dayOfWeek = -1;

    private static SentegrityTrustFactorDatasets sInstance;

    public SentegrityTrustFactorDatasets() {
        this.runTime = System.currentTimeMillis();
    }

    public static synchronized SentegrityTrustFactorDatasets getInstance() {
        if (sInstance == null) {
            sInstance = new SentegrityTrustFactorDatasets();
        }
        return sInstance;
    }

    public long getRunTime() {
        return runTime;
    }

    public static boolean validatePayload(List<Object> payload) {
        return !(payload == null || payload.size() < 1);
    }

    public String getTimeDateString(double blockSize, boolean withDayOfWeek) {
        if (hourOfDay < 0) {

            this.dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
            this.hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            int minutes = Calendar.getInstance().get(Calendar.MINUTE);

            if (minutes > 30) {
                this.hourOfDay++;
            }

            if (this.hourOfDay == 0) {
                this.hourOfDay = 1;
            }
        }

        int hourBlock = (int) Math.ceil((double) this.hourOfDay / blockSize);

        if (withDayOfWeek) {
            return "DAY_" + dayOfWeek + "_" + "HOUR_" + hourBlock;
        } else {
            return "HOUR_" + hourOfDay;
        }
    }


    /**
     * Call on reloading login
     */
    public static void destroy() {
        sInstance = null;
    }


}
