package com.lazy_durtles.wk.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for {@link AnswerVerdict} and {@link DigraphMatch}.
 */
public class AnswerVerdictTest {

    @Test
    public void constructor_setsAllFields() {
        DigraphMatch dm = new DigraphMatch('ゆ', 'ゅ');
        AnswerVerdict v = new AnswerVerdict(true, false, true, "given", "matched", dm);
        assertTrue(v.isOk());
        assertFalse(v.isRetry());
        assertTrue(v.isNearMatch());
        assertEquals("given", v.getGivenAnswer());
        assertEquals("matched", v.getMatchedAnswer());
        assertEquals(dm, v.getDigraphMatch());
    }

    @Test
    public void nokWithRetry_constant() {
        assertFalse(AnswerVerdict.NOK_WITH_RETRY.isOk());
        assertTrue(AnswerVerdict.NOK_WITH_RETRY.isRetry());
        assertFalse(AnswerVerdict.NOK_WITH_RETRY.isNearMatch());
        assertNull(AnswerVerdict.NOK_WITH_RETRY.getGivenAnswer());
        assertNull(AnswerVerdict.NOK_WITH_RETRY.getMatchedAnswer());
        assertNull(AnswerVerdict.NOK_WITH_RETRY.getDigraphMatch());
    }

    @Test
    public void nokWithoutRetry_constant() {
        assertFalse(AnswerVerdict.NOK_WITHOUT_RETRY.isOk());
        assertFalse(AnswerVerdict.NOK_WITHOUT_RETRY.isRetry());
        assertFalse(AnswerVerdict.NOK_WITHOUT_RETRY.isNearMatch());
    }

    @Test
    public void toString_containsAllFields() {
        AnswerVerdict v = new AnswerVerdict(true, true, false, "a", "b", null);
        String s = v.toString();
        assertTrue(s.contains("true"));  // ok
        assertTrue(s.contains("a"));     // givenAnswer
        assertTrue(s.contains("b"));     // matchedAnswer
    }

    // --- DigraphMatch ---

    @Test
    public void digraphMatch_getters() {
        DigraphMatch dm = new DigraphMatch('ゆ', 'ゅ');
        assertEquals('ゆ', dm.getRegularKana());
        assertEquals('ゅ', dm.getSmallKana());
    }

    @Test
    public void digraphMatch_toString() {
        DigraphMatch dm = new DigraphMatch('に', 'ゃ');
        String s = dm.toString();
        assertTrue(s.startsWith("Digraph:"));
        assertTrue(s.contains("に"));
        assertTrue(s.contains("ゃ"));
    }
}
