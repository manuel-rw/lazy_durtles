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

import com.lazy_durtles.wk.util.MnemonicImageUtil;

/**
 * LiveData carrying the number of mnemonic image files actually present on
 * disk, so the settings screen can show the locally downloaded count. The
 * value is refreshed by calling [update] (e.g. after a download finishes or
 * the data pack is deleted).
 */
public final class LiveMnemonicImageFileCount extends LiveData<Integer> {
    /**
     * The singleton instance.
     */
    private static final LiveMnemonicImageFileCount instance = new LiveMnemonicImageFileCount();

    /**
     * Get the singleton instance.
     *
     * @return the instance
     */
    public static LiveMnemonicImageFileCount getInstance() {
        return instance;
    }

    private LiveMnemonicImageFileCount() {
        //
    }

    /**
     * Recount the files on disk (must not be called on the main thread) and
     * post the new value.
     */
    public void update() {
        postValue(MnemonicImageUtil.countDownloadedFiles());
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
}
