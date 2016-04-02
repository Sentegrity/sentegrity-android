package com.sentegrity.core_detection.baseline_analysis;

import android.os.Build;

import com.sentegrity.core_detection.assertion_storage.SentegrityAssertionStore;
import com.sentegrity.core_detection.assertion_storage.SentegrityStoredAssertion;
import com.sentegrity.core_detection.assertion_storage.SentegrityStoredTrustFactor;
import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorOutput;
import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorStore;
import com.sentegrity.core_detection.constants.DNEStatusCode;
import com.sentegrity.core_detection.dispatch.trust_factors.SentegrityTrustFactorDatasets;
import com.sentegrity.core_detection.policy.SentegrityPolicy;
import com.sentegrity.core_detection.startup.SentegrityStartup;
import com.sentegrity.core_detection.startup.SentegrityStartupStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by dmestrov on 22/03/16.
 */
public class SentegrityBaselineAnalysis {

    //TODO: add error checks and logger for all methods;


    public static List<SentegrityTrustFactorOutput> performBaselineAnalysis(List<SentegrityTrustFactorOutput> outputs, SentegrityPolicy policy) {
        boolean exists = false;

        SentegrityAssertionStore assertionStore = SentegrityTrustFactorStore.getInstance().getAssertionStore(policy.getAppID());

        if (assertionStore == null) {
            assertionStore = new SentegrityAssertionStore();
            assertionStore.setAppId(policy.getAppID());
        }

        SentegrityStartup startup = SentegrityStartupStore.getInstance().getStartupData();

        if (startup == null) {
            return null;
        }

        boolean shouldWipeData = false;

        if (!startup.getLastOSVersion().equals(Build.VERSION.RELEASE)) {
            shouldWipeData = true;

            startup.setLastOSVersion(Build.VERSION.RELEASE);

            if (!SentegrityStartupStore.getInstance().setStartupData(startup)) {
                return null;
            }
        }

        SentegrityStoredTrustFactor storedTrustFactor;

        SentegrityTrustFactorOutput outputTrustFactor;

        for (SentegrityTrustFactorOutput output : outputs) {
            if (output == null) {
                return null;
            }

            storedTrustFactor = assertionStore.getStoredTrustFactor(output.getTrustFactor().getID());

            if (storedTrustFactor == null) {

                storedTrustFactor = assertionStore.createStoredTrustFactor(output);

                if (storedTrustFactor == null) {
                    return null;
                }

                output.setStoredTrustFactor(storedTrustFactor);

                outputTrustFactor = performBaselineAnalysis(output);

                if (outputTrustFactor == null)
                    return null;

                if (!assertionStore.addToStore(outputTrustFactor.getStoredTrustFactor())) {
                    return null;
                }
            } else {
                if (!checkTrustFactorRevision(output, storedTrustFactor) || (shouldWipeData && output.getTrustFactor().isWipeOnUpdate())) {

                    storedTrustFactor = assertionStore.createStoredTrustFactor(output);
                    output.setStoredTrustFactor(storedTrustFactor);

                    outputTrustFactor = performBaselineAnalysis(output);

                    if (outputTrustFactor == null) {
                        return null;
                    }

                    if (!assertionStore.replaceInStore(outputTrustFactor.getStoredTrustFactor())) {
                        return null;
                    }


                } else {

                    output.setStoredTrustFactor(storedTrustFactor);
                    outputTrustFactor = performBaselineAnalysis(output);

                    if (outputTrustFactor == null)
                        return null;

                    if (!assertionStore.replaceInStore(outputTrustFactor.getStoredTrustFactor())) {
                        return null;
                    }
                }
            }
        }

        exists = true;

        SentegrityAssertionStore localStoreOutput = SentegrityTrustFactorStore.getInstance().setAssertionStore(assertionStore, policy.getAppID());

        if (localStoreOutput == null) {
            return null;
        }

        return outputs;
    }

    private static boolean checkTrustFactorRevision(SentegrityTrustFactorOutput output, SentegrityStoredTrustFactor storedTrustFactor) {
        return output.getTrustFactor().getRevision() == storedTrustFactor.getRevision();
    }

    private static SentegrityTrustFactorOutput performBaselineAnalysis(SentegrityTrustFactorOutput output) {
        if (output == null) {
            return null;
        }

        SentegrityTrustFactorOutput updated = null;

        output.setCandidateAssertionObjectsForWhitelisting(new ArrayList<SentegrityStoredAssertion>());

        if (output.getTrustFactor().getDecayMode() == 1) {
            output = performMetricBasedDecay(output);
        }

        if (!output.getStoredTrustFactor().isLearned()) {
            updated = updateLearningAndAddCandidatesAssertions(output);
        }

        if (output.getCandidateAssertionObjects() != null && output.getCandidateAssertionObjects().size() < 1
                && output.getStatusCode() == DNEStatusCode.OK || output.getStatusCode() == DNEStatusCode.NO_DATA) {
            output.setForComputation(false);
            updated = output;
        } else {
            output.setForComputation(true);
            if (output.getStoredTrustFactor().isLearned()) {
                updated = checkBaseLineForMatch(output);
            }
        }

        if (updated == null) {
            return null;
        }

        return updated;
    }

    private static SentegrityTrustFactorOutput updateLearningAndAddCandidatesAssertions(SentegrityTrustFactorOutput output) {
        output.getStoredTrustFactor().setRunCount(output.getStoredTrustFactor().getRunCount() + 1);
        switch (output.getTrustFactor().getLearnMode()) {
            case 0:
                output.getStoredTrustFactor().setLearned(true);
                break;
            case 1:
                addLearnedAssertions(output);
                output.getStoredTrustFactor().setLearned(true);
                break;
            case 2:
                addLearnedAssertions(output);
                if (output.getStoredTrustFactor().getRunCount() >= output.getTrustFactor().getLearnRunCount()) {
                    if (getDaysBetweenDates(System.currentTimeMillis(), output.getStoredTrustFactor().getFirstRun()) >= output.getTrustFactor().getLearnTime()) {
                        output.getStoredTrustFactor().setLearned(true);
                    } else {
                        output.getStoredTrustFactor().setLearned(false);
                    }
                } else {
                    output.getStoredTrustFactor().setLearned(false);
                }
                break;
            case 3:
                addLearnedAssertions(output);
                if (getDaysBetweenDates(System.currentTimeMillis(), output.getStoredTrustFactor().getFirstRun()) >= output.getTrustFactor().getLearnTime()) {
                    if (output.getStoredTrustFactor().getAssertions().size() >= output.getTrustFactor().getLearnAssertionCount()) {
                        output.getStoredTrustFactor().setLearned(true);
                    } else {
                        output.getStoredTrustFactor().setLearned(false);
                    }
                } else {
                    output.getStoredTrustFactor().setLearned(false);
                }
                break;
            default:
                return null;
        }

        return output;
    }

    private static void addLearnedAssertions(SentegrityTrustFactorOutput output) {
        int startHitCount = 0;
        int newHitCount = 0;
        SentegrityStoredAssertion matchingAssertion = null;
        boolean foundMatch;

        List<SentegrityStoredAssertion> storedAssertions = output.getStoredTrustFactor().getAssertions();

        if (storedAssertions == null || storedAssertions.size() < 1) {
            output.getStoredTrustFactor().setAssertions(output.getCandidateAssertionObjects());
        } else {
            for (SentegrityStoredAssertion candidate : output.getCandidateAssertionObjects()) {
                foundMatch = false;

                for (SentegrityStoredAssertion stored : output.getStoredTrustFactor().getAssertions()) {
                    if (candidate.getHash().equals(stored.getHash())) {
                        foundMatch = true;

                        matchingAssertion = stored;
                        break;
                    }
                }

                if (foundMatch) {
                    startHitCount = matchingAssertion.getHitCount();
                    newHitCount = startHitCount + 1;
                    matchingAssertion.setHitCount(newHitCount);
                } else {
                    storedAssertions.add(candidate);
                }
            }
        }
    }

    private static SentegrityTrustFactorOutput checkBaseLineForMatch(SentegrityTrustFactorOutput output) {
        int startHitCount;
        int newHitCount;
        List<SentegrityStoredAssertion> candidateAssertionToWhiteList = new ArrayList();
        List<SentegrityStoredAssertion> storedAssertionObjectsMatched = new ArrayList();

        boolean currentCandidateMatch = false;

        for (SentegrityStoredAssertion candidate : output.getCandidateAssertionObjects()) {
            currentCandidateMatch = false;

            for (SentegrityStoredAssertion stored : output.getStoredTrustFactor().getAssertions()) {
                if (candidate.getHash().equals(stored.getHash())) {
                    startHitCount = stored.getHitCount();
                    newHitCount = startHitCount + 1;
                    stored.setHitCount(newHitCount);
                    stored.setLastTime(SentegrityTrustFactorDatasets.getInstance().getRunTime());

                    currentCandidateMatch = true;
                    output.setMatchFound(true);

                    storedAssertionObjectsMatched.add(stored);
                    break;
                }


            }

            if (!currentCandidateMatch) {
                if (output.getTrustFactor().isWhitelistable()) {
                    candidateAssertionToWhiteList.add(candidate);
                }
            }
        }

        output.setCandidateAssertionObjectsForWhitelisting(candidateAssertionToWhiteList);
        output.setStoredAssertionObjectsMatched(storedAssertionObjectsMatched);

        return output;
    }

    private static SentegrityTrustFactorOutput performMetricBasedDecay(SentegrityTrustFactorOutput output) {

        double miliSecondsInDay = 86400 * 1000;
        double daysSinceCreation = 0;
        double hitsPerDay = 0;

        List<SentegrityStoredAssertion> assertionsToKeep = new ArrayList();

        for(SentegrityStoredAssertion assertion : output.getStoredTrustFactor().getAssertions()){
            daysSinceCreation = (SentegrityTrustFactorDatasets.getInstance().getRunTime() - assertion.getCreated()) / miliSecondsInDay;
            if(daysSinceCreation < 1){
                daysSinceCreation = 1;
            }

            hitsPerDay = (double)assertion.getHitCount() / daysSinceCreation;

            assertion.setDecayMetric(hitsPerDay);

            if (assertion.getDecayMetric() > output.getTrustFactor().getDecayMetric()) {
                assertionsToKeep.add(assertion);
            }
        }

        Collections.sort(assertionsToKeep, comparator);

        output.getStoredTrustFactor().setAssertions(assertionsToKeep);

        return output;
    }

    private static int getDaysBetweenDates(long date1, long date2) {
        double miliSecondsInDay = 86400 * 1000;
        return (int) (Math.abs(date1 - date2) / miliSecondsInDay);
    }

    private static DecayComparator comparator = new DecayComparator();

    static class DecayComparator implements Comparator<SentegrityStoredAssertion> {
        @Override
        public int compare(SentegrityStoredAssertion o1, SentegrityStoredAssertion o2) {
            if(o1.getDecayMetric() >= o2.getDecayMetric()) return -1;
            return 1;
        }
    }
}
