package com.sentegrity.core_detection.baseline_analysis;

import android.os.Build;
import android.text.TextUtils;

import com.sentegrity.core_detection.assertion_storage.SentegrityAssertionStore;
import com.sentegrity.core_detection.assertion_storage.SentegrityStoredAssertion;
import com.sentegrity.core_detection.assertion_storage.SentegrityStoredTrustFactor;
import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorOutput;
import com.sentegrity.core_detection.assertion_storage.SentegrityTrustFactorStore;
import com.sentegrity.core_detection.constants.DNEStatusCode;
import com.sentegrity.core_detection.dispatch.trust_factors.SentegrityTrustFactorDatasets;
import com.sentegrity.core_detection.logger.ErrorDetails;
import com.sentegrity.core_detection.logger.ErrorDomain;
import com.sentegrity.core_detection.logger.Logger;
import com.sentegrity.core_detection.logger.SentegrityError;
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

    public static List<SentegrityTrustFactorOutput> performBaselineAnalysis(List<SentegrityTrustFactorOutput> outputs, SentegrityPolicy policy) {

        SentegrityAssertionStore assertionStore = SentegrityTrustFactorStore.getInstance().getAssertionStore();

        if (assertionStore == null) {
            SentegrityError error = SentegrityError.INVALID_STARTUP_INSTANCE;
            error.setDomain(ErrorDomain.SENTEGRITY_DOMAIN);
            error.setDetails(new ErrorDetails().setDescription("Failed to get assertion file").setFailureReason("No assertion file received").setRecoverySuggestion("Try validating the assertion file"));

            Logger.INFO("Failed to Get Assertion file", error);
            return null;
        }

        SentegrityStartup startup = SentegrityStartupStore.getInstance().getStartupStore();

        if (startup == null) {
            SentegrityError error = SentegrityError.INVALID_STARTUP_INSTANCE;
            error.setDomain(ErrorDomain.SENTEGRITY_DOMAIN);
            error.setDetails(new ErrorDetails().setDescription("Failed to get startup file").setFailureReason("No startup file received").setRecoverySuggestion("Try validating the startup file"));

            Logger.INFO("Failed to Get Startup file", error);
            return null;
        }

        boolean shouldWipeData = false;

        if (!TextUtils.equals(startup.getLastOSVersion(), Build.VERSION.RELEASE)) {
            shouldWipeData = true;

            startup.setLastOSVersion(Build.VERSION.RELEASE);
        }

        SentegrityStoredTrustFactor storedTrustFactor;

        SentegrityTrustFactorOutput updatedTrustFactorOutput;

        for (SentegrityTrustFactorOutput trustFactorOutput : outputs) {
            if (trustFactorOutput == null) {
                SentegrityError error = SentegrityError.INVALID_STORED_TRUST_FACTOR_OBJECTS_PROVIDED;
                error.setDomain(ErrorDomain.SENTEGRITY_DOMAIN);
                error.setDetails(new ErrorDetails().setDescription("Failed to add trustFactorOutputObject").setFailureReason("Invalid trustFactorOutputObject passed").setRecoverySuggestion("Try passing a valid trustFactorOutputObject"));

                Logger.INFO("Failed to Add TrustFactorOutputObject", error);

                if (policy.continueOnError()) {
                    updatedTrustFactorOutput = performBaselineAnalysis(trustFactorOutput);
                    continue;
                } else {
                    return null;
                }
            }

            storedTrustFactor = assertionStore.getStoredTrustFactor(trustFactorOutput.getTrustFactor().getID());

            if (storedTrustFactor == null) {

                Logger.INFO("Couldn't find stored trust factor object in local store, creating new one.");

                storedTrustFactor = assertionStore.createStoredTrustFactor(trustFactorOutput);

                if (storedTrustFactor == null) {
                    SentegrityError error = SentegrityError.UNABLE_TO_CREATE_NEW_STORED_ASSERTION;
                    error.setDomain(ErrorDomain.SENTEGRITY_DOMAIN);
                    error.setDetails(new ErrorDetails().setDescription("No trust factor output object were able to be added").setFailureReason("Unable to create a new stored trust factor object").setRecoverySuggestion("Try passing a valid trustFactorOutputObject"));

                    Logger.INFO("No trust factor output object were able to be added", error);
                    if (policy.continueOnError()) {
                        continue;
                    } else {
                        return null;
                    }
                }

                trustFactorOutput.setStoredTrustFactor(storedTrustFactor);

                updatedTrustFactorOutput = performBaselineAnalysis(trustFactorOutput);

                if (updatedTrustFactorOutput == null) {
                    SentegrityError error = SentegrityError.UNABLE_TO_PERFORM_BASE_ANALYSIS_FOR_TRUST_FACTOR;
                    error.setDomain(ErrorDomain.SENTEGRITY_DOMAIN);
                    error.setDetails(new ErrorDetails().setDescription("Failed to compare").setFailureReason("Unable to perform base analysis for trust factor output object").setRecoverySuggestion("Try updating trust factor output objects"));

                    Logger.INFO("Failed to compare trust factor", error);
                    if (policy.continueOnError()) {
                        continue;
                    } else {
                        return null;
                    }
                }

                if (!assertionStore.addToStore(updatedTrustFactorOutput.getStoredTrustFactor())) {
                    SentegrityError error = SentegrityError.NO_ASSERTIONS_ADDED_TO_STORE;
                    error.setDomain(ErrorDomain.SENTEGRITY_DOMAIN);
                    error.setDetails(new ErrorDetails().setDescription("Failed to add stored trust factor objects").setFailureReason("Unable to add stored trust factor objects to the runtime local store").setRecoverySuggestion("Try providing a valid object to store"));

                    Logger.INFO("Failed to compare trust factor", error);
                    if (policy.continueOnError()) {
                        continue;
                    } else {
                        return null;
                    }
                }
            } else {
                if (!checkTrustFactorRevision(trustFactorOutput, storedTrustFactor) || (shouldWipeData && trustFactorOutput.getTrustFactor().isWipeOnUpdate())) {
                    storedTrustFactor = assertionStore.createStoredTrustFactor(trustFactorOutput);

                    trustFactorOutput.setStoredTrustFactor(storedTrustFactor);

                    updatedTrustFactorOutput = performBaselineAnalysis(trustFactorOutput);

                    if (updatedTrustFactorOutput == null) {
                        SentegrityError error = SentegrityError.UNABLE_TO_PERFORM_BASE_ANALYSIS_FOR_TRUST_FACTOR;
                        error.setDomain(ErrorDomain.SENTEGRITY_DOMAIN);
                        error.setDetails(new ErrorDetails().setDescription("Failed to compare").setFailureReason("Unable to perform base analysis for trust factor output object").setRecoverySuggestion("Try updating trust factor output objects"));

                        Logger.INFO("Failed to compare trust factor", error);
                        if (policy.continueOnError()) {
                            continue;
                        } else {
                            return null;
                        }
                    }

                    if (!assertionStore.replaceInStore(updatedTrustFactorOutput.getStoredTrustFactor())) {
                        SentegrityError error = SentegrityError.NO_ASSERTIONS_ADDED_TO_STORE;
                        error.setDomain(ErrorDomain.SENTEGRITY_DOMAIN);
                        error.setDetails(new ErrorDetails().setDescription("Failed to add stored trust factor objects").setFailureReason("Unable to add stored trust factor objects to the runtime local store").setRecoverySuggestion("Try providing a valid object to store"));

                        Logger.INFO("Failed to compare trust factor", error);
                        if (policy.continueOnError()) {
                            continue;
                        } else {
                            return null;
                        }
                    }
                } else {
                    trustFactorOutput.setStoredTrustFactor(storedTrustFactor);
                    updatedTrustFactorOutput = performBaselineAnalysis(trustFactorOutput);

                    if (updatedTrustFactorOutput == null) {
                        SentegrityError error = SentegrityError.UNABLE_TO_PERFORM_BASE_ANALYSIS_FOR_TRUST_FACTOR;
                        error.setDomain(ErrorDomain.SENTEGRITY_DOMAIN);
                        error.setDetails(new ErrorDetails().setDescription("Failed to compare").setFailureReason("Unable to perform base analysis for trust factor output object").setRecoverySuggestion("Try updating trust factor output objects"));

                        Logger.INFO("Failed to perform", error);
                        if (policy.continueOnError()) {
                            continue;
                        } else {
                            return null;
                        }
                    }

                    if (!assertionStore.replaceInStore(updatedTrustFactorOutput.getStoredTrustFactor())) {
                        SentegrityError error = SentegrityError.UNABLE_TO_SET_ASSERTION_TO_STORE;
                        error.setDomain(ErrorDomain.SENTEGRITY_DOMAIN);
                        error.setDetails(new ErrorDetails().setDescription("No trust factor output object were able to be added").setFailureReason("Unable to replace stored assertion").setRecoverySuggestion("Try passing a valid trustFactorOutputObject"));

                        Logger.INFO("Failed to perform", error);
                        return null;
                    }
                }


            }
        }

        /*SentegrityAssertionStore localStoreOutput = */
        SentegrityTrustFactorStore.getInstance().setAssertionStore();

        /*if (localStoreOutput == null) {
            Logger.INFO("Failed to write store");
            return null;
        }*/

        return outputs;
    }

    private static boolean checkTrustFactorRevision(SentegrityTrustFactorOutput output, SentegrityStoredTrustFactor storedTrustFactor) {
        return output.getTrustFactor().getRevision() == storedTrustFactor.getRevision();
    }

    private static SentegrityTrustFactorOutput performBaselineAnalysis(SentegrityTrustFactorOutput output) {
        if (output == null) {
            SentegrityError error = SentegrityError.NO_TRUST_FACTOR_OUTPUT_OBJECTS_RECEIVED;
            error.setDomain(ErrorDomain.SENTEGRITY_DOMAIN);
            error.setDetails(new ErrorDetails().setDescription("No trust factor output object found").setFailureReason("No trust factor ouput objects received or candidate assertions to compare").setRecoverySuggestion("Try passing a valid trustFactorOutputObject"));

            Logger.INFO("Failed to perform", error);
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
            SentegrityError error = SentegrityError.ERROR_DURING_LEARNING_CHECK;
            error.setDomain(ErrorDomain.SENTEGRITY_DOMAIN);
            error.setDetails(new ErrorDetails().setDescription("No trust factor output object found").setFailureReason("Error during learning check").setRecoverySuggestion("Try passing or updating trustFactorOutputObject"));

            Logger.INFO("Failed to perform", error);
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
                    if (TextUtils.equals(candidate.getHash(), stored.getHash())) {
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

        boolean currentCandidateMatch;

        for (SentegrityStoredAssertion candidate : output.getCandidateAssertionObjects()) {
            currentCandidateMatch = false;

            for (SentegrityStoredAssertion stored : output.getStoredTrustFactor().getAssertions()) {
                if (TextUtils.equals(candidate.getHash(), stored.getHash())) {
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

        for (SentegrityStoredAssertion assertion : output.getStoredTrustFactor().getAssertions()) {
            daysSinceCreation = (SentegrityTrustFactorDatasets.getInstance().getRunTime() - assertion.getCreated()) / miliSecondsInDay;
            if (daysSinceCreation < 1) {
                daysSinceCreation = 1;
            }

            hitsPerDay = (double) assertion.getHitCount() / daysSinceCreation;

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
            if (o1.getDecayMetric() >= o2.getDecayMetric()) return -1;
            return 1;
        }
    }
}
