package com.lazy_durtles.wk.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Robolectric tests for {@link PseudoIme}.
 * Verifies romaji-to-kana conversion (simulateInput) and hiragana-to-katakana conversion.
 * Uses Robolectric because PseudoIme depends on android.text.Editable.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class PseudoImeTest {

    // --- simulateInput: basic romaji to hiragana ---

    @Test
    public void simulateInput_vowels() {
        // "aiueo" -> a->あ, i->い, u->う, e->え, o->お
        // But "a" followed by "i" might combine; let's check actual behavior
        // The fixup processes left-to-right, so "aiueo" = あいうえお
        assertEquals("あいうえお", PseudoIme.simulateInput("aiueo"));
    }

    @Test
    public void simulateInput_aAlone() {
        // Single "a" converts to あ
        assertEquals("あ", PseudoIme.simulateInput("a"));
    }

    @Test
    public void simulateInput_kaRow() {
        assertEquals("かきくけこ", PseudoIme.simulateInput("kakikukeko"));
    }

    @Test
    public void simulateInput_saRow() {
        assertEquals("さしすせそ", PseudoIme.simulateInput("sashisuseso"));
    }

    @Test
    public void simulateInput_shi_si_bothMapToShi() {
        assertEquals("し", PseudoIme.simulateInput("shi"));
        assertEquals("し", PseudoIme.simulateInput("si"));
    }

    @Test
    public void simulateInput_chi_ti_bothMapToChi() {
        assertEquals("ち", PseudoIme.simulateInput("chi"));
        assertEquals("ち", PseudoIme.simulateInput("ti"));
    }

    @Test
    public void simulateInput_tsu_tu_bothMapToTsu() {
        assertEquals("つ", PseudoIme.simulateInput("tsu"));
        assertEquals("つ", PseudoIme.simulateInput("tu"));
    }

    @Test
    public void simulateInput_ji_zi_bothMapToJi() {
        assertEquals("じ", PseudoIme.simulateInput("ji"));
        assertEquals("じ", PseudoIme.simulateInput("zi"));
    }

    @Test
    public void simulateInput_fu_hu_bothMapToFu() {
        assertEquals("ふ", PseudoIme.simulateInput("fu"));
        assertEquals("ふ", PseudoIme.simulateInput("hu"));
    }

    @Test
    public void simulateInput_doubleConsonant_smallTsu() {
        // "kka" -> "っか"
        assertEquals("っか", PseudoIme.simulateInput("kka"));
        // "sshi" -> "っし"
        assertEquals("っし", PseudoIme.simulateInput("sshi"));
    }

    @Test
    public void simulateInput_standaloneN() {
        // Trailing "n" becomes ん
        assertEquals("ん", PseudoIme.simulateInput("n"));
        assertEquals("かん", PseudoIme.simulateInput("kan"));
    }

    @Test
    public void simulateInput_nBeforeConsonant() {
        // "n" before a consonant (not a vowel or y) becomes ん
        assertEquals("しんぶん", PseudoIme.simulateInput("shinbun"));
    }

    @Test
    public void simulateInput_word() {
        assertEquals("にほん", PseudoIme.simulateInput("nihon"));
    }

    @Test
    public void simulateInput_sentence() {
        // "ha" maps to は, but "wa" maps to わ; the input "watashiwagakuseidesu"
        // converts as: wa-ta-shi-wa-ga-ku-se-i-de-su
        assertEquals("わたしわがくせいです", PseudoIme.simulateInput("watashiwagakuseidesu"));
    }

    @Test
    public void simulateInput_mixedCase() {
        // "K" is not in the map as a standalone; "Ka" would be カ (katakana)
        // "Kana" -> K is unmapped, a->あ, n->ん, a->あ => "Kあんあ"
        String result = PseudoIme.simulateInput("Kana");
        assertEquals("Kあな", result);
    }

    @Test
    public void simulateInput_singleChar() {
        assertEquals("あ", PseudoIme.simulateInput("a"));
    }

    @Test
    public void simulateInput_nonRomaji_partiallyConverted() {
        // "a" converts to あ, "bc123" passes through
        assertEquals("あbc123", PseudoIme.simulateInput("abc123"));
    }

    @Test
    public void simulateInput_yaRow() {
        assertEquals("やゆよ", PseudoIme.simulateInput("yayuyo"));
    }

    @Test
    public void simulateInput_waRow() {
        assertEquals("わをん", PseudoIme.simulateInput("wawon"));
    }

    @Test
    public void simulateInput_gaRow() {
        assertEquals("がぎぐげご", PseudoIme.simulateInput("gagigugego"));
    }

    @Test
    public void simulateInput_zaRow() {
        assertEquals("ざじずぜぞ", PseudoIme.simulateInput("zajizuzezo"));
    }

    @Test
    public void simulateInput_daRow() {
        assertEquals("だぢづでど", PseudoIme.simulateInput("dadidudedo"));
    }

    @Test
    public void simulateInput_baRow() {
        assertEquals("ばびぶべぼ", PseudoIme.simulateInput("babibubebo"));
    }

    @Test
    public void simulateInput_paRow() {
        assertEquals("ぱぴぷぺぽ", PseudoIme.simulateInput("papipupepo"));
    }

    // --- toKatakana ---

    @Test
    public void toKatakana_null_returnsNull() {
        assertNull(PseudoIme.toKatakana(null));
    }

    @Test
    public void toKatakana_hiraganaToKatakana() {
        assertEquals("アイウエオ", PseudoIme.toKatakana("あいうえお"));
    }

    @Test
    public void toKatakana_mixedContent() {
        assertEquals("ニホンゴabc", PseudoIme.toKatakana("にほんごabc"));
    }

    @Test
    public void toKatakana_kanji_unchanged() {
        assertEquals("日本語", PseudoIme.toKatakana("日本語"));
    }

    @Test
    public void toKatakana_emptyString() {
        assertEquals("", PseudoIme.toKatakana(""));
    }

    @Test
    public void toKatakana_alreadyKatakana_unchanged() {
        // Katakana is outside the hiragana range (0x3040-0x3096), so it stays as-is
        assertEquals("カタカナ", PseudoIme.toKatakana("カタカナ"));
    }
}
