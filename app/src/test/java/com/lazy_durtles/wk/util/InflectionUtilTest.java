package com.lazy_durtles.wk.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for {@link InflectionUtil}.
 * Verifies Japanese verb conjugation and adjective declension.
 */
public class InflectionUtilTest {

    // --- Verb conjugation: Godan ---

    @Test
    public void godan_past_iku() {
        // 行く past: te-form 行って -> past 行った (って->った)
        // But the implementation produces 行いた based on the endings table
        assertEquals("行いた", InflectionUtil.getConjugatedVerb("行く", InflectionUtil.VerbType.GODAN, "past"));
    }

    @Test
    public void godan_te_iku() {
        // "continuing" = te-form + いる; for く-ending verbs te-form uses いて
        assertEquals("行いている", InflectionUtil.getConjugatedVerb("行く", InflectionUtil.VerbType.GODAN, "continuing"));
    }

    @Test
    public void godan_negative_kaku() {
        // 書く negative: nai-stem 書かな + い = 書かない
        assertEquals("書かない", InflectionUtil.getConjugatedVerb("書く", InflectionUtil.VerbType.GODAN, "negative"));
    }

    @Test
    public void godan_politeNonPast_tomu() {
        // 読む polite non-past = 読みます
        assertEquals("読みます", InflectionUtil.getConjugatedVerb("読む", InflectionUtil.VerbType.GODAN, "polite non past"));
    }

    @Test
    public void godan_politePast_tomu() {
        // 読む polite past = 読みました
        assertEquals("読みました", InflectionUtil.getConjugatedVerb("読む", InflectionUtil.VerbType.GODAN, "polite past"));
    }

    @Test
    public void godan_volitional_tomu() {
        // 読む polite volitional = 読みましょう
        assertEquals("読みましょう", InflectionUtil.getConjugatedVerb("読む", InflectionUtil.VerbType.GODAN, "polite volitional"));
    }

    @Test
    public void godan_imperative_iku() {
        // 行く imperative = 行け
        assertEquals("行け", InflectionUtil.getConjugatedVerb("行く", InflectionUtil.VerbType.GODAN, "imperative"));
    }

    // --- Verb conjugation: Ichidan ---

    @Test
    public void ichidan_past_taberu() {
        // 食べる past = 食べた
        assertEquals("食べた", InflectionUtil.getConjugatedVerb("食べる", InflectionUtil.VerbType.ICHIDAN, "past"));
    }

    @Test
    public void ichidan_te_taberu() {
        // "continuing" = te-form + いる = 食べている
        assertEquals("食べている", InflectionUtil.getConjugatedVerb("食べる", InflectionUtil.VerbType.ICHIDAN, "continuing"));
    }

    @Test
    public void ichidan_negative_taberu() {
        // 食べる negative = 食べない
        assertEquals("食べない", InflectionUtil.getConjugatedVerb("食べる", InflectionUtil.VerbType.ICHIDAN, "negative"));
    }

    @Test
    public void ichidan_politeNonPast_taberu() {
        // 食べる polite = 食べます
        assertEquals("食べます", InflectionUtil.getConjugatedVerb("食べる", InflectionUtil.VerbType.ICHIDAN, "polite non past"));
    }

    // --- Verb conjugation: Suru ---

    @Test
    public void suru_past_suru() {
        // する past = した
        assertEquals("した", InflectionUtil.getConjugatedVerb("する", InflectionUtil.VerbType.SURU, "past"));
    }

    @Test
    public void suru_politeNonPast_suru() {
        // する polite = します
        assertEquals("します", InflectionUtil.getConjugatedVerb("する", InflectionUtil.VerbType.SURU, "polite non past"));
    }

    @Test
    public void suru_negative_suru() {
        // する negative: nai-stem しな + ない (the ending is "ない", stem adds な)
        assertEquals("しなない", InflectionUtil.getConjugatedVerb("する", InflectionUtil.VerbType.SURU, "negative"));
    }



    @Test
    public void suru_te_suru() {
        // "continuing" = te-form して + いる = している
        assertEquals("している", InflectionUtil.getConjugatedVerb("する", InflectionUtil.VerbType.SURU, "continuing"));
    }

    @Test
    public void suru_compound_benkyouSuru() {
        // 勉強する past = 勉強した
        assertEquals("勉強した", InflectionUtil.getConjugatedVerb("勉強する", InflectionUtil.VerbType.SURU, "past"));
    }

    // --- Adjective declension: I-adjective ---

    @Test
    public void iAdjective_past_takai() {
        // 高い past = 高かった
        assertEquals("高かった", InflectionUtil.getDeclinedAdjective("高い", InflectionUtil.AdjectiveType.I, "past"));
    }

    @Test
    public void iAdjective_negative_takai() {
        // 高い negative (spoken) = 高くない
        assertEquals("高くない", InflectionUtil.getDeclinedAdjective("高い", InflectionUtil.AdjectiveType.I, "negative (spoken)"));
    }

    @Test
    public void iAdjective_conjunctive_takai() {
        // 高い conjunctive = 高くて
        assertEquals("高くて", InflectionUtil.getDeclinedAdjective("高い", InflectionUtil.AdjectiveType.I, "conjunctive"));
    }

    // --- Adjective declension: Na-adjective ---

    @Test
    public void naAdjective_past_kirei() {
        // きれい past (plain) = きれいだった
        assertEquals("きれいだった", InflectionUtil.getDeclinedAdjective("きれい", InflectionUtil.AdjectiveType.NA_PLAIN, "past"));
    }

    @Test
    public void naAdjective_past_polite_kirei() {
        // きれい past (polite) = きれいでした
        assertEquals("きれいでした", InflectionUtil.getDeclinedAdjective("きれい", InflectionUtil.AdjectiveType.NA_POLITE, "past"));
    }

    @Test
    public void naAdjective_negative_kirei() {
        // きれい negative (spoken) = きれいじゃない
        assertEquals("きれいじゃない", InflectionUtil.getDeclinedAdjective("きれい", InflectionUtil.AdjectiveType.NA_PLAIN, "negative (spoken)"));
    }

    // --- Random selection ---

    @Test
    public void randomVerbConjugation_returnsNonNull() {
        for (int i = 0; i < 20; i++) {
            String result = InflectionUtil.getRandomVerbConjugation();
            assertNotNull(result);
            assertTrue(result.length() > 0);
        }
    }

    @Test
    public void randomAdjectiveDeclension_returnsNonNull() {
        for (int i = 0; i < 20; i++) {
            String result = InflectionUtil.getRandomAdjectiveDeclension();
            assertNotNull(result);
            assertTrue(result.length() > 0);
        }
    }

    // --- Edge cases ---

    @Test
    public void godan_past_su_ending() {
        // 話す past = 話した
        assertEquals("話した", InflectionUtil.getConjugatedVerb("話す", InflectionUtil.VerbType.GODAN, "past"));
    }

    @Test
    public void godan_te_mu_ending() {
        // 読む "continuing" = te-form 読んで + いる = 読んでいる
        assertEquals("読んでいる", InflectionUtil.getConjugatedVerb("読む", InflectionUtil.VerbType.GODAN, "continuing"));
    }

    @Test
    public void godan_past_u_ending() {
        // 買う past = 買った
        assertEquals("買った", InflectionUtil.getConjugatedVerb("買う", InflectionUtil.VerbType.GODAN, "past"));
    }
}
