package com.lazy_durtles.wk.test.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;

import com.lazy_durtles.wk.db.AppDatabase;
import com.lazy_durtles.wk.db.dao.PropertiesDao;
import com.lazy_durtles.wk.db.dao.SessionItemDao;
import com.lazy_durtles.wk.db.dao.SubjectDao;
import com.lazy_durtles.wk.db.model.SessionItem;
import com.lazy_durtles.wk.db.model.Subject;
import com.lazy_durtles.wk.db.model.SubjectEntity;
import com.lazy_durtles.wk.enums.SessionItemState;
import com.lazy_durtles.wk.enums.SessionType;
import com.lazy_durtles.wk.enums.SubjectType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * Instrumented tests for the Room DAOs: PropertiesDao, SessionItemDao, SubjectDao.
 * Verifies CRUD operations, property get/set, mute toggle, session type persistence,
 * and session item lifecycle states.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class DatabaseDaoTest {
    private AppDatabase db;

    @Before
    public void createDb() {
        db = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                AppDatabase.class
        ).allowMainThreadQueries().build();
    }

    @After
    public void closeDb() {
        db.close();
    }

    // --- PropertiesDao tests ---

    @Test
    public void propertiesDao_muteToggle() {
        PropertiesDao dao = db.propertiesDao();
        // Default should be false (not muted)
        assertFalse(dao.getIsMuted());
        dao.setIsMuted(true);
        // Note: setIsMuted uses async write, so we need to wait or use the helper directly
        // The preload cache should reflect the change immediately
        assertTrue(dao.getIsMuted());
        dao.setIsMuted(false);
        assertFalse(dao.getIsMuted());
    }

    @Test
    public void propertiesDao_sessionType() {
        PropertiesDao dao = db.propertiesDao();
        dao.setSessionType(SessionType.REVIEW);
        assertEquals(SessionType.REVIEW, dao.getSessionType());
        dao.setSessionType(SessionType.LESSON);
        assertEquals(SessionType.LESSON, dao.getSessionType());
        dao.setSessionType(SessionType.NONE);
        assertEquals(SessionType.NONE, dao.getSessionType());
    }

    @Test
    public void propertiesDao_sessionOnkun() {
        PropertiesDao dao = db.propertiesDao();
        dao.setSessionOnkun(true);
        assertTrue(dao.getSessionOnkun());
        dao.setSessionOnkun(false);
        assertFalse(dao.getSessionOnkun());
    }

    @Test
    public void propertiesDao_apiInError() {
        PropertiesDao dao = db.propertiesDao();
        assertFalse(dao.isApiInError());
        dao.setApiInError(true);
        assertTrue(dao.isApiInError());
    }

    @Test
    public void propertiesDao_apiKeyRejected() {
        PropertiesDao dao = db.propertiesDao();
        assertFalse(dao.isApiKeyRejected());
        dao.setApiKeyRejected(true);
        assertTrue(dao.isApiKeyRejected());
    }

    @Test
    public void propertiesDao_migrationFlags() {
        PropertiesDao dao = db.propertiesDao();
        dao.setMigrationDoneAnkiSplit(true);
        assertTrue(dao.getMigrationDoneAnkiSplit());
        dao.setMigrationDoneAudio2(true);
        assertTrue(dao.getMigrationDoneAudio2());
        dao.setMigrationDoneNotif(true);
        assertTrue(dao.getMigrationDoneNotif());
        dao.setMigrationDoneDump(true);
        assertTrue(dao.getMigrationDoneDump());
    }

    // --- SessionItemDao tests ---

    @Test
    public void sessionItemDao_insertAndGetById() {
        SessionItemDao dao = db.sessionItemDao();
        SessionItem item = new SessionItem();
        item.setId(1L);
        item.setAssignmentId(100L);
        item.setState(SessionItemState.ACTIVE);
        item.setLevel(5);
        dao.insert(item);

        SessionItem fetched = dao.getById(1L);
        assertNotNull(fetched);
        assertEquals(1L, fetched.getId());
        assertEquals(100L, fetched.getAssignmentId());
        assertEquals(SessionItemState.ACTIVE, fetched.getState());
        assertEquals(5, fetched.getLevel());
    }

    @Test
    public void sessionItemDao_getAll_returnsOrdered() {
        SessionItemDao dao = db.sessionItemDao();
        SessionItem item1 = new SessionItem();
        item1.setId(1L);
        item1.setOrder(2);
        dao.insert(item1);

        SessionItem item2 = new SessionItem();
        item2.setId(2L);
        item2.setOrder(1);
        dao.insert(item2);

        List<SessionItem> all = dao.getAll();
        assertEquals(2, all.size());
        // Ordered by `order` column ascending
        assertEquals(2L, all.get(0).getId());
        assertEquals(1L, all.get(1).getId());
    }

    @Test
    public void sessionItemDao_update() {
        SessionItemDao dao = db.sessionItemDao();
        SessionItem item = new SessionItem();
        item.setId(1L);
        item.setState(SessionItemState.ACTIVE);
        dao.insert(item);

        item.setState(SessionItemState.REPORTED);
        dao.update(item);

        SessionItem fetched = dao.getById(1L);
        assertNotNull(fetched);
        assertEquals(SessionItemState.REPORTED, fetched.getState());
    }

    @Test
    public void sessionItemDao_deleteAll() {
        SessionItemDao dao = db.sessionItemDao();
        SessionItem item = new SessionItem();
        item.setId(1L);
        dao.insert(item);
        assertEquals(1, dao.getAll().size());

        dao.deleteAll();
        assertEquals(0, dao.getAll().size());
    }

    @Test
    public void sessionItemDao_getById_notFound_returnsNull() {
        SessionItemDao dao = db.sessionItemDao();
        assertNull(dao.getById(999L));
    }

    // --- SubjectDao tests ---

    @Test
    public void subjectDao_deleteAll_onEmptyDb_noError() {
        SubjectDao dao = db.subjectDao();
        dao.deleteAll(); // should not throw
    }

    @Test
    public void subjectDao_getById_notFound_returnsNull() {
        SubjectDao dao = db.subjectDao();
        assertNull(dao.getById(999L));
    }

    @Test
    public void subjectDao_getKanjiByCharacters_notFound_returnsNull() {
        SubjectDao dao = db.subjectDao();
        assertNull(dao.getKanjiByCharacters("non-existent"));
    }

    // --- AppDatabase version ---

    @Test
    public void appDatabase_hasExpectedVersion() {
        // The database version should be 68 (matching LATEST_VERSION in DatabaseMigrationTest)
        assertEquals(68, db.getOpenHelper().getReadableDatabase().getVersion());
    }
}
