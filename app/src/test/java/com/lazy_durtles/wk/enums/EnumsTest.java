package com.lazy_durtles.wk.enums;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Unit tests for the core enum types that drive session logic, question types,
 * subject types, and ordering. These enums are pure Java (no Android dependencies
 * for the parts tested here) and are the backbone of the app's behavior.
 */
public class EnumsTest {

    // --- SessionType ---

    @Test
    public void sessionType_none_notSrsRelevant_notReportingNeeded() {
        assertFalse(SessionType.NONE.isSrsRelevant());
        assertFalse(SessionType.NONE.isReportingTaskNeeded());
        assertEquals("None", SessionType.NONE.getDescription());
    }

    @Test
    public void sessionType_lesson_notSrsRelevant_reportingNeeded() {
        assertFalse(SessionType.LESSON.isSrsRelevant());
        assertTrue(SessionType.LESSON.isReportingTaskNeeded());
        assertEquals("Lesson", SessionType.LESSON.getDescription());
    }

    @Test
    public void sessionType_review_srsRelevant_reportingNeeded() {
        assertTrue(SessionType.REVIEW.isSrsRelevant());
        assertTrue(SessionType.REVIEW.isReportingTaskNeeded());
        assertEquals("Review", SessionType.REVIEW.getDescription());
    }

    @Test
    public void sessionType_selfStudy_notSrsRelevant_notReportingNeeded() {
        assertFalse(SessionType.SELF_STUDY.isSrsRelevant());
        assertFalse(SessionType.SELF_STUDY.isReportingTaskNeeded());
        assertEquals("Self-study", SessionType.SELF_STUDY.getDescription());
    }

    @Test
    public void sessionType_hasFourValues() {
        assertEquals(4, SessionType.values().length);
    }

    // --- SessionState ---

    @Test
    public void sessionState_hasFourValues() {
        assertEquals(4, SessionState.values().length);
        assertNotNull(SessionState.INACTIVE);
        assertNotNull(SessionState.IN_LESSON_PRESENTATION);
        assertNotNull(SessionState.ACTIVE);
        assertNotNull(SessionState.FINISHING);
    }

    // --- SessionItemState ---

    @Test
    public void sessionItemState_hasFourValues() {
        assertEquals(4, SessionItemState.values().length);
        assertNotNull(SessionItemState.ACTIVE);
        assertNotNull(SessionItemState.PENDING);
        assertNotNull(SessionItemState.REPORTED);
        assertNotNull(SessionItemState.ABANDONED);
    }

    // --- CloseEnoughAction ---

    @Test
    public void closeEnoughAction_silentlyAccept_noToast() {
        assertFalse(CloseEnoughAction.SILENTLY_ACCEPT.isShowToast());
    }

    @Test
    public void closeEnoughAction_acceptWithToast_showsToast() {
        assertTrue(CloseEnoughAction.ACCEPT_WITH_TOAST.isShowToast());
    }

    @Test
    public void closeEnoughAction_acceptWithToastNoLm_showsToast() {
        assertTrue(CloseEnoughAction.ACCEPT_WITH_TOAST_NO_LM.isShowToast());
    }

    @Test
    public void closeEnoughAction_shakeAndRetry_noToast() {
        assertFalse(CloseEnoughAction.SHAKE_AND_RETRY.isShowToast());
    }

    @Test
    public void closeEnoughAction_reject_noToast() {
        assertFalse(CloseEnoughAction.REJECT.isShowToast());
    }

    @Test
    public void closeEnoughAction_hasFiveValues() {
        assertEquals(5, CloseEnoughAction.values().length);
    }

    // --- SubjectSource ---

    @Test
    public void subjectSource_hasOneValue() {
        assertEquals(1, SubjectSource.values().length);
        assertEquals(SubjectSource.WANIKANI, SubjectSource.valueOf("WANIKANI"));
    }

    // --- SubjectType ---

    @Test
    public void subjectType_from_dbTypeName() {
        assertEquals(SubjectType.WANIKANI_RADICAL, SubjectType.from("radical"));
        assertEquals(SubjectType.WANIKANI_KANJI, SubjectType.from("kanji"));
        assertEquals(SubjectType.WANIKANI_VOCAB, SubjectType.from("vocabulary"));
        assertEquals(SubjectType.WANIKANI_KANA_VOCAB, SubjectType.from("kana_vocabulary"));
    }

    @Test
    public void subjectType_from_unknown_returnsDefaultRadical() {
        // from() returns WANIKANI_RADICAL as default for unknown types
        assertEquals(SubjectType.WANIKANI_RADICAL, SubjectType.from("unknown_type"));
    }

    @Test
    public void subjectType_dbTypeName_roundTrip() {
        for (SubjectType type : SubjectType.values()) {
            assertEquals(type, SubjectType.from(type.getDbTypeName()));
        }
    }

    @Test
    public void subjectType_radical_isRadical_notKanji_notVocabulary() {
        assertTrue(SubjectType.WANIKANI_RADICAL.isRadical());
        assertFalse(SubjectType.WANIKANI_RADICAL.isKanji());
        assertFalse(SubjectType.WANIKANI_RADICAL.isVocabulary());
    }

    @Test
    public void subjectType_kanji_isKanji_notRadical_notVocabulary() {
        assertFalse(SubjectType.WANIKANI_KANJI.isRadical());
        assertTrue(SubjectType.WANIKANI_KANJI.isKanji());
        assertFalse(SubjectType.WANIKANI_KANJI.isVocabulary());
    }

    @Test
    public void subjectType_vocabulary_isVocabulary_notRadical_notKanji() {
        assertFalse(SubjectType.WANIKANI_VOCAB.isRadical());
        assertFalse(SubjectType.WANIKANI_VOCAB.isKanji());
        assertTrue(SubjectType.WANIKANI_VOCAB.isVocabulary());
    }

    @Test
    public void subjectType_kanaVocabulary_isKanaVocabulary() {
        // KANA_VOCAB has kanaVocabulary=true but vocabulary=false (it's a separate flag)
        assertTrue(SubjectType.WANIKANI_KANA_VOCAB.isKanaVocabulary());
    }

    @Test
    public void subjectType_vocabulary_notKanaVocabulary() {
        assertFalse(SubjectType.WANIKANI_VOCAB.isKanaVocabulary());
    }

    @Test
    public void subjectType_order_radicalBeforeKanjiBeforeVocabulary() {
        assertTrue(SubjectType.WANIKANI_RADICAL.getOrder() < SubjectType.WANIKANI_KANJI.getOrder());
        assertTrue(SubjectType.WANIKANI_KANJI.getOrder() < SubjectType.WANIKANI_VOCAB.getOrder());
    }

    @Test
    public void subjectType_descriptions_notEmpty() {
        for (SubjectType type : SubjectType.values()) {
            assertNotNull(type.getDescription());
            assertFalse(type.getDescription().isEmpty());
            assertNotNull(type.getDescriptionPlural());
            assertNotNull(type.getShortDescription());
        }
    }

    @Test
    public void subjectType_radical_supportsOnlyQuestion1() {
        assertTrue(SubjectType.WANIKANI_RADICAL.supportsQuestion1());
        assertFalse(SubjectType.WANIKANI_RADICAL.supportsQuestion2(false));
        assertFalse(SubjectType.WANIKANI_RADICAL.supportsQuestion3(false));
        assertFalse(SubjectType.WANIKANI_RADICAL.supportsQuestion4(false));
    }

    @Test
    public void subjectType_kanji_supportsQuestions1And2() {
        assertTrue(SubjectType.WANIKANI_KANJI.supportsQuestion1());
        assertTrue(SubjectType.WANIKANI_KANJI.supportsQuestion2(false));
    }

    @Test
    public void subjectType_kanji_onkun_supportsQuestions3And4() {
        assertTrue(SubjectType.WANIKANI_KANJI.supportsQuestion3(true));
        assertTrue(SubjectType.WANIKANI_KANJI.supportsQuestion4(true));
    }

    @Test
    public void subjectType_kanji_notOnkun_noQuestions3And4() {
        assertFalse(SubjectType.WANIKANI_KANJI.supportsQuestion3(false));
        assertFalse(SubjectType.WANIKANI_KANJI.supportsQuestion4(false));
    }

    @Test
    public void subjectType_possibleQuestionTypes_radical() {
        Collection<QuestionType> types = SubjectType.WANIKANI_RADICAL.getPossibleQuestionTypes(false);
        assertTrue(types.contains(QuestionType.WANIKANI_RADICAL_NAME));
    }

    @Test
    public void subjectType_possibleQuestionTypes_kanji() {
        Collection<QuestionType> types = SubjectType.WANIKANI_KANJI.getPossibleQuestionTypes(false);
        assertTrue(types.contains(QuestionType.WANIKANI_KANJI_MEANING));
        assertTrue(types.contains(QuestionType.WANIKANI_KANJI_READING));
    }

    @Test
    public void subjectType_possibleQuestionTypes_kanjiOnkun() {
        Collection<QuestionType> types = SubjectType.WANIKANI_KANJI.getPossibleQuestionTypes(true);
        assertTrue(types.contains(QuestionType.WANIKANI_KANJI_MEANING));
        assertTrue(types.contains(QuestionType.WANIKANI_KANJI_ONYOMI));
        assertTrue(types.contains(QuestionType.WANIKANI_KANJI_KUNYOMI));
    }

    @Test
    public void subjectType_canHaveStudyMaterials() {
        for (SubjectType type : SubjectType.values()) {
            assertTrue(type.canHaveStudyMaterials());
        }
    }

    @Test
    public void subjectType_simpleInfoTitle_containsLevel() {
        String title = SubjectType.WANIKANI_KANJI.getSimpleInfoTitle(5);
        assertTrue(title.contains("5"));
        assertTrue(title.toLowerCase().contains("kanji"));
    }

    // --- QuestionType ---

    @Test
    public void questionType_slots() {
        assertEquals(1, QuestionType.WANIKANI_RADICAL_NAME.getSlot());
        assertEquals(1, QuestionType.WANIKANI_KANJI_MEANING.getSlot());
        assertEquals(2, QuestionType.WANIKANI_KANJI_READING.getSlot());
        assertEquals(3, QuestionType.WANIKANI_KANJI_ONYOMI.getSlot());
        assertEquals(4, QuestionType.WANIKANI_KANJI_KUNYOMI.getSlot());
    }

    @Test
    public void questionType_isMeaning() {
        assertTrue(QuestionType.WANIKANI_RADICAL_NAME.isMeaning());
        assertTrue(QuestionType.WANIKANI_KANJI_MEANING.isMeaning());
        assertFalse(QuestionType.WANIKANI_KANJI_READING.isMeaning());
    }

    @Test
    public void questionType_isReading() {
        assertFalse(QuestionType.WANIKANI_KANJI_MEANING.isReading());
        assertTrue(QuestionType.WANIKANI_KANJI_READING.isReading());
        assertTrue(QuestionType.WANIKANI_KANJI_ONYOMI.isReading());
        assertTrue(QuestionType.WANIKANI_KANJI_KUNYOMI.isReading());
    }

    @Test
    public void questionType_isKana() {
        assertFalse(QuestionType.WANIKANI_KANJI_MEANING.isKana());
        assertTrue(QuestionType.WANIKANI_KANJI_READING.isKana());
    }

    @Test
    public void questionType_isAscii() {
        assertTrue(QuestionType.WANIKANI_KANJI_MEANING.isAscii());
        assertFalse(QuestionType.WANIKANI_KANJI_READING.isAscii());
    }

    @Test
    public void questionType_shortTitles_notEmpty() {
        for (QuestionType type : QuestionType.values()) {
            assertNotNull(type.getShortTitle());
            assertFalse(type.getShortTitle().isEmpty());
        }
    }

    // --- ReviewOrder ---

    @Test
    public void reviewOrder_shuffle_comparatorAlwaysZero() {
        Comparator<com.lazy_durtles.wk.db.model.Subject> c = ReviewOrder.SHUFFLE.getComparator();
        // SHUFFLE comparator always returns 0 regardless of input
        assertEquals(0, c.compare(null, null));
    }

    @Test
    public void reviewOrder_hasAllValues() {
        // Verify all 16 review order values exist
        assertEquals(16, ReviewOrder.values().length);
    }

    @Test
    public void reviewOrder_comparatorsNotNull() {
        for (ReviewOrder order : ReviewOrder.values()) {
            assertNotNull(order.getComparator());
        }
    }

    // --- LessonOrder ---

    @Test
    public void lessonOrder_shuffle_isShuffle() {
        assertTrue(LessonOrder.SHUFFLE.isShuffle());
        assertTrue(LessonOrder.LEVEL_THEN_SHUFFLE.isShuffle());
        assertTrue(LessonOrder.TYPE_THEN_SHUFFLE.isShuffle());
    }

    @Test
    public void lessonOrder_nonShuffle_notShuffle() {
        assertFalse(LessonOrder.LEVEL_THEN_TYPE.isShuffle());
        assertFalse(LessonOrder.RADICALS_THEN_LEVEL_THEN_TYPE.isShuffle());
        assertFalse(LessonOrder.TYPE_THEN_LEVEL.isShuffle());
    }

    @Test
    public void lessonOrder_hasSixValues() {
        assertEquals(6, LessonOrder.values().length);
    }

    @Test
    public void lessonOrder_comparatorsNotNull() {
        for (LessonOrder order : LessonOrder.values()) {
            assertNotNull(order.getComparator());
        }
    }

    // --- SearchSortOrder ---

    @Test
    public void searchSortOrder_forDescription_findsValue() {
        assertEquals(SearchSortOrder.TYPE, SearchSortOrder.forDescription("Type", SearchSortOrder.LEVEL_TYPE));
        assertEquals(SearchSortOrder.LEVEL_TYPE, SearchSortOrder.forDescription("Level, Type", SearchSortOrder.TYPE));
    }

    @Test
    public void searchSortOrder_forDescription_unknown_returnsDefault() {
        assertEquals(SearchSortOrder.TYPE, SearchSortOrder.forDescription("nonexistent", SearchSortOrder.TYPE));
    }

    @Test
    public void searchSortOrder_descriptionValues() {
        List<String> descriptions = SearchSortOrder.descriptionValues();
        assertEquals(SearchSortOrder.values().length, descriptions.size());
        assertTrue(descriptions.contains("Type"));
        assertTrue(descriptions.contains("Level, Type"));
    }

    @Test
    public void searchSortOrder_type_isSingleLevel() {
        assertTrue(SearchSortOrder.TYPE.isSingleLevel());
        assertFalse(SearchSortOrder.LEVEL_TYPE.isSingleLevel());
    }

    @Test
    public void searchSortOrder_comparatorsNotNull() {
        for (SearchSortOrder order : SearchSortOrder.values()) {
            assertNotNull(order.getComparator(0L));
        }
    }

    // --- SessionPriority ---

    @Test
    public void sessionPriority_none_returnsBaseComparator() {
        Comparator<com.lazy_durtles.wk.db.model.Subject> base = (a, b) -> 0;
        Comparator<com.lazy_durtles.wk.db.model.Subject> result = SessionPriority.NONE.getComparator(base, Collections.emptyList(), 1, 60);
        assertEquals(0, result.compare(null, null));
    }

    @Test
    public void sessionPriority_hasFiveValues() {
        assertEquals(5, SessionPriority.values().length);
    }

    @Test
    public void sessionPriority_comparatorsNotNull() {
        Comparator<com.lazy_durtles.wk.db.model.Subject> base = (a, b) -> 0;
        for (SessionPriority priority : SessionPriority.values()) {
            assertNotNull(priority.getComparator(base, Collections.emptyList(), 1, 60));
        }
    }

    // --- DontKnowButtonBehavior ---

    @Test
    public void dontKnowButtonBehavior_disable_cannotShow() {
        // DISABLE.canShow() always returns false regardless of session state
        assertFalse(DontKnowButtonBehavior.DISABLE.canShow());
    }

    @Test
    public void dontKnowButtonBehavior_hasThreeValues() {
        assertEquals(3, DontKnowButtonBehavior.values().length);
    }

    // --- SpecialButtonBehavior ---

    @Test
    public void specialButtonBehavior_hasValues() {
        assertTrue(SpecialButtonBehavior.values().length > 0);
        for (SpecialButtonBehavior behavior : SpecialButtonBehavior.values()) {
            assertNotNull(behavior);
        }
    }

    // --- KanjiAcceptedReadingType ---

    @Test
    public void kanjiAcceptedReadingType_hasValues() {
        assertTrue(KanjiAcceptedReadingType.values().length > 0);
    }

    // --- NetworkRule ---

    @Test
    public void networkRule_hasValues() {
        assertTrue(NetworkRule.values().length > 0);
    }

    // --- OnlineStatus ---

    @Test
    public void onlineStatus_hasValues() {
        assertTrue(OnlineStatus.values().length > 0);
    }
}
