package com.lazy_durtles.wk.test

import android.app.Application
import androidx.room.Room
import com.lazy_durtles.wk.WkApplication
import com.lazy_durtles.wk.db.AppDatabase
import com.lazy_durtles.wk.settings.SettingsPrefs
import java.lang.reflect.Field

/**
 * Application subclass for Robolectric tests.
 *
 * WkApplication.onCreate initializes an encrypted preference store backed by
 * the Android KeyStore (unavailable under Robolectric), WorkManager jobs and
 * many LiveData observers, none of which unit tests need. This class instead
 * wires up only the static singletons the production code reads:
 * SettingsPrefs.application and WkApplication's instance/database fields,
 * backed by an in-memory Room database.
 */
class TestApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // WkApplication declares no explicit constructor, so this only runs
        // Application's no-arg constructor; onCreate is invoked by the
        // framework, not by `new`, so the instance stays uninitialized.
        val app = WkApplication()
        // A `new`-constructed ContextWrapper has no base context; attach ours
        // so getSystemService/getResources work on it.
        val attach = android.content.ContextWrapper::class.java
            .getDeclaredMethod("attachBaseContext", android.content.Context::class.java)
        attach.isAccessible = true
        attach.invoke(app, this)
        setStaticField(WkApplication::class.java, "instance", app)
        val db = Room.inMemoryDatabaseBuilder(app, AppDatabase::class.java)
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()
        setStaticField(WkApplication::class.java, "database", db)
        SettingsPrefs.setApplication(app)
    }

    private fun setStaticField(clazz: Class<*>, name: String, value: Any?) {
        val f: Field = clazz.getDeclaredField(name)
        f.isAccessible = true
        f.set(null, value)
    }
}
