package com.lazy_durtles.wk.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit tests for {@link KanaUtil}.
 * Verifies Katakana-to-Hiragana conversion for full-width and half-width kana.
 */
public class KanaUtilTest {

    @Test
    public void fullWidthKatakana_convertedToHiragana() {
        assertEquals("あいうえお", KanaUtil.convertKatakanaToHiragana("アイウエオ"));
    }

    @Test
    public void fullWidthKatakanaWord() {
        assertEquals("にほん", KanaUtil.convertKatakanaToHiragana("ニホン"));
    }

    @Test
    public void hiragana_unchanged() {
        assertEquals("にほん", KanaUtil.convertKatakanaToHiragana("にほん"));
    }

    @Test
    public void kanji_unchanged() {
        assertEquals("日本語", KanaUtil.convertKatakanaToHiragana("日本語"));
    }

    @Test
    public void ascii_unchanged() {
        assertEquals("hello", KanaUtil.convertKatakanaToHiragana("hello"));
    }

    @Test
    public void mixedContent() {
        assertEquals("にほんごabc", KanaUtil.convertKatakanaToHiragana("ニホンゴabc"));
    }

    @Test
    public void emptyString() {
        assertEquals("", KanaUtil.convertKatakanaToHiragana(""));
    }

    @Test
    public void halfWidthKatakana_convertedToHiragana() {
        // Half-width katakana ｱ (U+FF71) -> U+FF71 - 0xCF25 = U+304C = が
        assertEquals("が", KanaUtil.convertKatakanaToHiragana("ｱ"));
    }

    @Test
    public void katakanaLongVowelMark_converted() {
        // ー (U+30FC) is within the full-width katakana range (U+30A1-U+30FE)
        // U+30FC - 0x60 = U+309C (゜ voiced sound mark)
        String result = KanaUtil.convertKatakanaToHiragana("ー");
        assertEquals("゜", result);
    }

    @Test
    public void katakanaSmallTsu() {
        // ッ (U+30C3) -> っ (U+3063)
        assertEquals("っ", KanaUtil.convertKatakanaToHiragana("ッ"));
    }

    @Test
    public void katakanaKe() {
        assertEquals("け", KanaUtil.convertKatakanaToHiragana("ケ"));
    }
}
