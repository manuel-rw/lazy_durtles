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

/**
 * LiveData carrying the progress of the optional WaniKani mnemonic image
 * download, so the settings screen can show a progress bar. The value is
 * null when no download is running.
 */
public final class LiveMnemonicDownloadProgress extends LiveData<LiveMnemonicDownloadProgress.Progress> {
    /**
     * Progress snapshot.
     */
    public static final class Progress {
        public final int done;
        public final int total;
        public final boolean indeterminate;

        public Progress(final int done, final int total, final boolean indeterminate) {
            this.done = done;
            this.total = total;
            this.indeterminate = indeterminate;
        }
    }

    /**
     * The singleton instance.
     */
    private static final LiveMnemonicDownloadProgress instance = new LiveMnemonicDownloadProgress();

    /**
     * Get the singleton instance.
     *
     * @return the instance
     */
    public static LiveMnemonicDownloadProgress getInstance() {
        return instance;
    }

    private LiveMnemonicDownloadProgress() {
        //
    }

    /**
     * Report progress. Posts on the main thread via LiveData semantics.
     *
     * @param done  number of images processed so far
     * @param total total number of images to process
     */
    public static void report(final int done, final int total) {
        instance.postValue(new Progress(done, total, false));
    }

    /**
     * Report the indeterminate "preparing" phase.
     */
    public static void reportStarted() {
        instance.postValue(new Progress(0, 0, true));
    }

    /**
     * Clear the progress (download finished, aborted or cancelled).
     */
    public static void clear() {
        instance.postValue(null);
    }
}
