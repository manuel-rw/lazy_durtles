/*
 * Copyright 2019-2020 Ernst Jan Plugge <rmc@dds.nl>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lazy_durtles.wk.settings;

import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.lazy_durtles.wk.WkApplication;

import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

/**
 * Shared access to the default SharedPreferences instance used by all settings classes.
 */
public class SettingsPrefs {
    public static @Nullable WkApplication application = null;

    public static void setApplication(final WkApplication app) {
        application = app;
    }

    private SettingsPrefs() {
    }

    public static SharedPreferences prefs() {
        return PreferenceManager.getDefaultSharedPreferences(requireNonNull(application));
    }

    public static WkApplication application() {
        return requireNonNull(application);
    }
}
