package com.lazy_durtles.wk.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit tests for {@link OptimalStringAlignmentDistance}.
 * Verifies the OSA edit distance computation including transpositions.
 */
public class OptimalStringAlignmentDistanceTest {

    @Test
    public void emptyStrings_haveZeroDistance() {
        assertEquals(0, OptimalStringAlignmentDistance.getDistance("", ""));
    }

    @Test
    public void emptyVsNonEmpty_returnsLength() {
        assertEquals(3, OptimalStringAlignmentDistance.getDistance("", "abc"));
        assertEquals(3, OptimalStringAlignmentDistance.getDistance("abc", ""));
    }

    @Test
    public void identicalStrings_haveZeroDistance() {
        assertEquals(0, OptimalStringAlignmentDistance.getDistance("hello", "hello"));
        assertEquals(0, OptimalStringAlignmentDistance.getDistance("日本語", "日本語"));
    }

    @Test
    public void singleSubstitution() {
        assertEquals(1, OptimalStringAlignmentDistance.getDistance("cat", "bat"));
    }

    @Test
    public void singleInsertion() {
        assertEquals(1, OptimalStringAlignmentDistance.getDistance("cat", "cats"));
    }

    @Test
    public void singleDeletion() {
        assertEquals(1, OptimalStringAlignmentDistance.getDistance("cats", "cat"));
    }

    @Test
    public void transposition_countsAsOne() {
        // OSA allows transposition of adjacent characters as a single edit
        assertEquals(1, OptimalStringAlignmentDistance.getDistance("ab", "ba"));
        assertEquals(1, OptimalStringAlignmentDistance.getDistance("abcd", "abdc"));
    }

    @Test
    public void multipleEdits() {
        // kitten -> sitting: k->s, e->i, insert g = 3 edits in OSA
        assertEquals(3, OptimalStringAlignmentDistance.getDistance("kitten", "sitting"));
    }

    @Test
    public void completelyDifferent() {
        assertEquals(3, OptimalStringAlignmentDistance.getDistance("abc", "xyz"));
    }

    @Test
    public void unicodeStrings() {
        assertEquals(0, OptimalStringAlignmentDistance.getDistance("かな", "かな"));
        assertEquals(1, OptimalStringAlignmentDistance.getDistance("かな", "かに"));
        assertEquals(2, OptimalStringAlignmentDistance.getDistance("東京", "大阪"));
    }

    @Test
    public void caseSensitive() {
        // OSA is case-sensitive; callers lowercase before comparing
        assertEquals(1, OptimalStringAlignmentDistance.getDistance("Hello", "hello"));
    }

    @Test
    public void repeatedCharacters() {
        assertEquals(1, OptimalStringAlignmentDistance.getDistance("aaa", "aa"));
        assertEquals(2, OptimalStringAlignmentDistance.getDistance("aaa", "a"));
    }

    @Test
    public void longStrings() {
        String a = "the quick brown fox jumps over the lazy dog";
        String b = "the quick brown fox jumps over the lazy cat";
        // dog -> cat: d->c, o->a, g->t = 3 substitutions
        assertEquals(3, OptimalStringAlignmentDistance.getDistance(a, b));
    }
}
