package com.lazy_durtles.wk.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for {@link ObjectSupport}.
 * Verifies null-safe helpers, string joining, list manipulation, and time formatting.
 * Uses Robolectric because ObjectSupport.safe() logs via android.util.Log on exceptions.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class ObjectSupportTest {

    // --- isEmpty ---

    @Test
    public void isEmpty_null_returnsTrue() {
        assertTrue(ObjectSupport.isEmpty(null));
    }

    @Test
    public void isEmpty_emptyString_returnsTrue() {
        assertTrue(ObjectSupport.isEmpty(""));
    }

    @Test
    public void isEmpty_nonEmpty_returnsFalse() {
        assertFalse(ObjectSupport.isEmpty("hello"));
    }

    // --- isEqualIgnoreCase ---

    @Test
    public void isEqualIgnoreCase_bothNull_returnsTrue() {
        assertTrue(ObjectSupport.isEqualIgnoreCase(null, null));
    }

    @Test
    public void isEqualIgnoreCase_firstNull_returnsFalse() {
        assertFalse(ObjectSupport.isEqualIgnoreCase(null, "abc"));
    }

    @Test
    public void isEqualIgnoreCase_secondNull_returnsFalse() {
        assertFalse(ObjectSupport.isEqualIgnoreCase("abc", null));
    }

    @Test
    public void isEqualIgnoreCase_sameIgnoringCase_returnsTrue() {
        assertTrue(ObjectSupport.isEqualIgnoreCase("Hello", "hELLO"));
    }

    @Test
    public void isEqualIgnoreCase_different_returnsFalse() {
        assertFalse(ObjectSupport.isEqualIgnoreCase("abc", "def"));
    }

    // --- isEqual ---

    @Test
    public void isEqual_bothNull_returnsTrue() {
        assertTrue(ObjectSupport.isEqual(null, null));
    }

    @Test
    public void isEqual_firstNull_returnsFalse() {
        assertFalse(ObjectSupport.isEqual(null, "x"));
    }

    @Test
    public void isEqual_equalObjects_returnsTrue() {
        assertTrue(ObjectSupport.isEqual("abc", "abc"));
        assertTrue(ObjectSupport.isEqual(42, 42));
    }

    @Test
    public void isEqual_differentObjects_returnsFalse() {
        assertFalse(ObjectSupport.isEqual("abc", "def"));
    }

    // --- hash ---

    @Test
    public void hash_sameValues_sameHash() {
        assertEquals(ObjectSupport.hash("a", 1, true), ObjectSupport.hash("a", 1, true));
    }

    @Test
    public void hash_differentValues_differentHash() {
        assertNotEquals(ObjectSupport.hash("a", 1), ObjectSupport.hash("b", 1));
    }

    // --- orElse ---

    @Test
    public void orElse_null_returnsFallback() {
        assertEquals("fallback", ObjectSupport.orElse(null, "fallback"));
    }

    @Test
    public void orElse_empty_returnsFallback() {
        assertEquals("fallback", ObjectSupport.orElse("", "fallback"));
    }

    @Test
    public void orElse_nonEmpty_returnsValue() {
        assertEquals("value", ObjectSupport.orElse("value", "fallback"));
    }

    // --- join ---

    @Test
    public void join_multipleParts() {
        assertEquals("[a, b, c]", ObjectSupport.join(", ", "[", "]", Arrays.asList("a", "b", "c")));
    }

    @Test
    public void join_singlePart() {
        assertEquals("[a]", ObjectSupport.join(", ", "[", "]", Arrays.asList("a")));
    }

    @Test
    public void join_empty() {
        assertEquals("[]", ObjectSupport.join(", ", "[", "]", new ArrayList<>()));
    }

    // --- removeEmpty ---

    @Test
    public void removeEmpty_removesNullsAndEmpty() {
        List<String> list = new ArrayList<>(Arrays.asList("a", null, "", "b", null));
        ObjectSupport.removeEmpty(list);
        assertEquals(Arrays.asList("a", "b"), list);
    }

    // --- removeDuplicates ---

    @Test
    public void removeDuplicates_removesDupes() {
        List<String> list = new ArrayList<>(Arrays.asList("a", "b", "a", "c", "b"));
        ObjectSupport.removeDuplicates(list);
        assertEquals(Arrays.asList("a", "b", "c"), list);
    }

    @Test
    public void removeDuplicates_nullList_noOp() {
        ObjectSupport.removeDuplicates(null); // should not throw
    }

    // --- shuffle ---

    @Test
    public void shuffle_preservesElements() {
        List<Integer> original = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        List<Integer> shuffled = ObjectSupport.shuffle(original);
        assertEquals(original.size(), shuffled.size());
        assertTrue(shuffled.containsAll(original));
    }

    // --- nextRandomInt ---

    @Test
    public void nextRandomInt_withinBounds() {
        for (int i = 0; i < 100; i++) {
            int value = ObjectSupport.nextRandomInt(10);
            assertTrue(value >= 0 && value < 10);
        }
    }

    // --- getWaitTimeAsInformalString ---

    @Test
    public void waitTime_zeroOrNegative_returnsNow() {
        assertEquals("now", ObjectSupport.getWaitTimeAsInformalString(0));
        assertEquals("now", ObjectSupport.getWaitTimeAsInformalString(-1000));
    }

    @Test
    public void waitTime_underTwoHours_returnsMinutes() {
        long thirtyMin = 30L * 60 * 1000;
        assertEquals("in 30 min", ObjectSupport.getWaitTimeAsInformalString(thirtyMin));
    }

    @Test
    public void waitTime_underTwoDays_returnsHours() {
        long threeHours = 3L * 60 * 60 * 1000;
        assertEquals("in 3 hours", ObjectSupport.getWaitTimeAsInformalString(threeHours));
    }

    @Test
    public void waitTime_overTwoDays_returnsDays() {
        long fiveDays = 5L * 24 * 60 * 60 * 1000;
        assertEquals("in 5 days", ObjectSupport.getWaitTimeAsInformalString(fiveDays));
    }

    // --- getShortWaitTimeAsInformalString ---

    @Test
    public void shortWaitTime_zeroOrNegative_returnsNow() {
        assertEquals("now", ObjectSupport.getShortWaitTimeAsInformalString(0));
        assertEquals("now", ObjectSupport.getShortWaitTimeAsInformalString(-1));
    }

    @Test
    public void shortWaitTime_underOneHour_returnsMinutes() {
        long tenMin = 10L * 60 * 1000;
        assertEquals("10m", ObjectSupport.getShortWaitTimeAsInformalString(tenMin));
    }

    @Test
    public void shortWaitTime_underTwoDays_returnsHours() {
        long fiveHours = 5L * 60 * 60 * 1000;
        assertEquals("5h", ObjectSupport.getShortWaitTimeAsInformalString(fiveHours));
    }

    @Test
    public void shortWaitTime_overTwoDays_returnsDays() {
        long threeDays = 3L * 24 * 60 * 60 * 1000;
        assertEquals("3d", ObjectSupport.getShortWaitTimeAsInformalString(threeDays));
    }

    // --- getTopOfHour ---

    @Test
    public void topOfHour_truncatesToHour() {
        long hour = 60L * 60 * 1000;
        long ts = 5 * hour + 23 * 60 * 1000 + 45 * 1000;
        assertEquals(5 * hour, ObjectSupport.getTopOfHour(ts));
    }

    @Test
    public void topOfHour_alreadyOnHour() {
        long hour = 60L * 60 * 1000;
        long ts = 3 * hour;
        assertEquals(ts, ObjectSupport.getTopOfHour(ts));
    }

    // --- isTrue ---

    @Test
    public void isTrue_trueBoolean_returnsTrue() {
        assertTrue(ObjectSupport.isTrue(Boolean.TRUE));
    }

    @Test
    public void isTrue_falseBoolean_returnsFalse() {
        assertFalse(ObjectSupport.isTrue(Boolean.FALSE));
    }

    @Test
    public void isTrue_null_returnsFalse() {
        assertFalse(ObjectSupport.isTrue(null));
    }

    @Test
    public void isTrue_nonBoolean_returnsFalse() {
        assertFalse(ObjectSupport.isTrue("true"));
        assertFalse(ObjectSupport.isTrue(1));
    }

    // --- safe ---

    @Test
    public void safe_noException_returnsValue() {
        assertEquals("ok", ObjectSupport.safe("default", () -> "ok"));
    }

    @Test
    public void safe_exception_returnsDefault() {
        assertEquals("default", ObjectSupport.safe("default", () -> {
            throw new RuntimeException("boom");
        }));
    }

    @Test
    public void safeNullable_noException_returnsValue() {
        assertEquals("ok", ObjectSupport.safeNullable(() -> "ok"));
    }

    @Test
    public void safeNullable_exception_returnsNull() {
        assertNull(ObjectSupport.safeNullable(() -> {
            throw new RuntimeException("boom");
        }));
    }

    // --- listOf ---

    @Test
    public void listOf_singleElement() {
        List<String> result = ObjectSupport.listOf("a");
        assertEquals(1, result.size());
        assertEquals("a", result.get(0));
    }

    @Test
    public void listOf_multipleElements() {
        List<String> result = ObjectSupport.listOf("a", "b", "c", "d");
        assertEquals(Arrays.asList("a", "b", "c", "d"), result);
    }
}
