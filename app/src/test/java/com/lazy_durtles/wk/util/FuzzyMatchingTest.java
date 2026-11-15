package com.lazy_durtles.wk.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.lazy_durtles.wk.enums.CloseEnoughAction;
import com.lazy_durtles.wk.model.AnswerVerdict;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Unit tests for {@link FuzzyMatching}.
 * Verifies answer matching with OSA distance, special cases (Woman/tree/say),
 * and all CloseEnoughAction behaviors.
 */
public class FuzzyMatchingTest {

    private static final List<String> ACCEPTED = Arrays.asList("hello", "world");
    private static final List<String> REJECTED = Collections.singletonList("hell");

    // --- Exact matches ---

    @Test
    public void exactMatch_accepted_silentlyAccept() {
        AnswerVerdict v = FuzzyMatching.matches("hello", ACCEPTED, REJECTED, CloseEnoughAction.SILENTLY_ACCEPT);
        assertTrue(v.isOk());
        assertFalse(v.isRetry());
        assertFalse(v.isNearMatch());
        assertEquals("hello", v.getMatchedAnswer());
    }

    @Test
    public void exactMatch_caseInsensitive() {
        AnswerVerdict v = FuzzyMatching.matches("HELLO", ACCEPTED, REJECTED, CloseEnoughAction.SILENTLY_ACCEPT);
        assertTrue(v.isOk());
        assertFalse(v.isNearMatch());
    }

    @Test
    public void exactMatch_secondAccepted() {
        AnswerVerdict v = FuzzyMatching.matches("world", ACCEPTED, REJECTED, CloseEnoughAction.SILENTLY_ACCEPT);
        assertTrue(v.isOk());
        assertEquals("world", v.getMatchedAnswer());
    }

    // --- No match ---

    @Test
    public void noMatch_returnsNokWithoutRetry() {
        AnswerVerdict v = FuzzyMatching.matches("xyz", ACCEPTED, REJECTED, CloseEnoughAction.SILENTLY_ACCEPT);
        assertFalse(v.isOk());
        assertFalse(v.isRetry());
    }

    @Test
    public void emptyAcceptedList_alwaysNok() {
        AnswerVerdict v = FuzzyMatching.matches("anything", Collections.emptyList(), REJECTED, CloseEnoughAction.SILENTLY_ACCEPT);
        assertFalse(v.isOk());
    }

    // --- Special case: "Woman" ---

    @Test
    public void woman_specialCase_matchesWomanOrFemale() {
        AnswerVerdict v = FuzzyMatching.matches("Woman", Arrays.asList("woman", "female"), REJECTED, CloseEnoughAction.SILENTLY_ACCEPT);
        assertTrue(v.isOk());
        assertFalse(v.isNearMatch());
    }

    @Test
    public void woman_specialCase_matchesFemale() {
        AnswerVerdict v = FuzzyMatching.matches("woman", Collections.singletonList("Female"), REJECTED, CloseEnoughAction.SILENTLY_ACCEPT);
        assertTrue(v.isOk());
    }

    @Test
    public void woman_specialCase_notInAccepted_fallsThroughToFuzzy() {
        // "woman" is not in accepted, so it should go through normal fuzzy matching
        AnswerVerdict v = FuzzyMatching.matches("Woman", Collections.singletonList("man"), REJECTED, CloseEnoughAction.SILENTLY_ACCEPT);
        assertFalse(v.isOk());
    }

    // --- Special case: "tree" / "say" ---

    @Test
    public void tree_specialCase_matchesTreeOrSay() {
        AnswerVerdict v = FuzzyMatching.matches("tree", Arrays.asList("tree", "say"), REJECTED, CloseEnoughAction.SILENTLY_ACCEPT);
        assertTrue(v.isOk());
    }

    @Test
    public void say_specialCase_matchesTreeOrSay() {
        AnswerVerdict v = FuzzyMatching.matches("say", Collections.singletonList("tree"), REJECTED, CloseEnoughAction.SILENTLY_ACCEPT);
        assertTrue(v.isOk());
    }

    // --- Close enough (near match) with different actions ---

    @Test
    public void nearMatch_silentlyAccept_acceptedAsNearMatch() {
        // "hellp" is 1 edit from "hello" (length 5, threshold 1)
        AnswerVerdict v = FuzzyMatching.matches("hellp", ACCEPTED, REJECTED, CloseEnoughAction.SILENTLY_ACCEPT);
        assertTrue(v.isOk());
        assertTrue(v.isNearMatch());
        assertEquals("hellp", v.getGivenAnswer());
        assertEquals("hello", v.getMatchedAnswer());
    }

    @Test
    public void nearMatch_acceptWithToast_acceptedAsNearMatch() {
        AnswerVerdict v = FuzzyMatching.matches("hellp", ACCEPTED, REJECTED, CloseEnoughAction.ACCEPT_WITH_TOAST);
        assertTrue(v.isOk());
        assertTrue(v.isNearMatch());
    }

    @Test
    public void nearMatch_acceptWithToastNoLm_acceptedAsNearMatch() {
        AnswerVerdict v = FuzzyMatching.matches("hellp", ACCEPTED, REJECTED, CloseEnoughAction.ACCEPT_WITH_TOAST_NO_LM);
        assertTrue(v.isOk());
        assertTrue(v.isNearMatch());
    }

    @Test
    public void nearMatch_shakeAndRetry_returnsNokWithRetry() {
        AnswerVerdict v = FuzzyMatching.matches("hellp", ACCEPTED, REJECTED, CloseEnoughAction.SHAKE_AND_RETRY);
        assertFalse(v.isOk());
        assertTrue(v.isRetry());
    }

    @Test
    public void nearMatch_reject_returnsNokWithoutRetry() {
        AnswerVerdict v = FuzzyMatching.matches("hellp", ACCEPTED, REJECTED, CloseEnoughAction.REJECT);
        assertFalse(v.isOk());
        assertFalse(v.isRetry());
    }

    // --- Short answers (threshold 0 for length 1-3) ---

    @Test
    public void shortAnswer_exactMatchOnly() {
        List<String> shortAccepted = Collections.singletonList("go");
        // Exact match works
        assertTrue(FuzzyMatching.matches("go", shortAccepted, Collections.emptyList(), CloseEnoughAction.SILENTLY_ACCEPT).isOk());
        // 1 edit away does NOT match (threshold 0 for length 2)
        assertFalse(FuzzyMatching.matches("to", shortAccepted, Collections.emptyList(), CloseEnoughAction.SILENTLY_ACCEPT).isOk());
    }

    // --- Rejected answers take priority ---

    @Test
    public void rejectedCloserThanAccepted_returnsNok() {
        // "hell" is rejected and is 1 edit from "hello" (accepted)
        // If answer is "hell", it matches rejected exactly (score 0) and accepted with score 1
        // Since bestRejectedScore (0) < bestAcceptedScore (1), it should be NOK
        AnswerVerdict v = FuzzyMatching.matches("hell", ACCEPTED, REJECTED, CloseEnoughAction.SILENTLY_ACCEPT);
        assertFalse(v.isOk());
    }

    // --- Unicode / cleaning ---

    @Test
    public void fullWidthDigits_cleanedToAscii() {
        // Full-width "１" (U+FF11) should be cleaned to "1"
        List<String> accepted = Collections.singletonList("1");
        AnswerVerdict v = FuzzyMatching.matches("１", accepted, Collections.emptyList(), CloseEnoughAction.SILENTLY_ACCEPT);
        assertTrue(v.isOk());
    }

    @Test
    public void smartQuotes_cleanedToAscii() {
        List<String> accepted = Collections.singletonList("it's");
        // Right single quotation mark U+2019 should be cleaned to '
        AnswerVerdict v = FuzzyMatching.matches("it\u2019s", accepted, Collections.emptyList(), CloseEnoughAction.SILENTLY_ACCEPT);
        assertTrue(v.isOk());
    }

    @Test
    public void extraWhitespace_trimmedAndCollapsed() {
        AnswerVerdict v = FuzzyMatching.matches("  hello  world  ", Arrays.asList("hello world"), Collections.emptyList(), CloseEnoughAction.SILENTLY_ACCEPT);
        assertTrue(v.isOk());
    }

    // --- Verdict constants ---

    @Test
    public void nokWithRetry_constant() {
        assertFalse(AnswerVerdict.NOK_WITH_RETRY.isOk());
        assertTrue(AnswerVerdict.NOK_WITH_RETRY.isRetry());
        assertFalse(AnswerVerdict.NOK_WITH_RETRY.isNearMatch());
        assertNull(AnswerVerdict.NOK_WITH_RETRY.getGivenAnswer());
        assertNull(AnswerVerdict.NOK_WITH_RETRY.getMatchedAnswer());
    }

    @Test
    public void nokWithoutRetry_constant() {
        assertFalse(AnswerVerdict.NOK_WITHOUT_RETRY.isOk());
        assertFalse(AnswerVerdict.NOK_WITHOUT_RETRY.isRetry());
        assertFalse(AnswerVerdict.NOK_WITHOUT_RETRY.isNearMatch());
    }

    @Test
    public void verdict_toString_containsFields() {
        AnswerVerdict v = FuzzyMatching.matches("hello", ACCEPTED, REJECTED, CloseEnoughAction.SILENTLY_ACCEPT);
        assertNotNull(v.toString());
        assertTrue(v.toString().startsWith("Verdict:"));
    }
}
