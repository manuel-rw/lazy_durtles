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

package com.lazy_durtles.wk.livedata;

import androidx.lifecycle.LiveData;

import com.lazy_durtles.wk.WkApplication;
import com.lazy_durtles.wk.db.AppDatabase;

/**
 * LiveData that tracks the number of downloaded WaniKani mnemonic images,
 * so the settings screen can show progress and the downloaded count.
 */
public final class LiveMnemonicImageCount extends LiveData<Integer> {
    /**
     * The singleton instance.
     */
    private static final LiveMnemonicImageCount instance = new LiveMnemonicImageCount();

    /**
     * The backing LiveData created by Room.
     */
    private LiveData<Integer> count = null;

    /**
     * Get the singleton instance.
     *
     * @return the instance
     */
    public static LiveMnemonicImageCount getInstance() {
        return instance;
    }

    /**
     * Private constructor.
     */
    private LiveMnemonicImageCount() {
        //
    }

    @Override
    protected void onActive() {
        super.onActive();
        final AppDatabase db = WkApplication.getDatabase();
        if (count == null) {
            count = db.mnemonicImageDao().getCountLive();
            count.observeForever(this::postValue);
        }
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        if (count != null) {
            count.removeObserver(this::postValue);
            count = null;
        }
    }

    /**
     * Get the current value, or 0.
     *
     * @return the count
     */
    public int get() {
        final Integer value = getValue();
        return value == null ? 0 : value;
    }

    /**
     * Force a refresh of the count from the database.
     */
    public void update() {
        final AppDatabase db = WkApplication.getDatabase();
        postValue(db.mnemonicImageDao().getCount());
    }
}
