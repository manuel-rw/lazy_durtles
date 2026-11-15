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

package com.lazy_durtles.wk.jobs;

import com.lazy_durtles.wk.WkApplication;

/**
 * Job that queues the mnemonic image download task in the database. Runs on a
 * background thread, so the Room insert is safe here; the UI only schedules
 * this job and never touches the database directly.
 */
public final class StartMnemonicImageDownloadJob extends Job {
    /**
     * The constructor.
     *
     * @param data parameters
     */
    public StartMnemonicImageDownloadJob(final String data) {
        super(data);
    }

    @Override
    public void runLocal() {
        WkApplication.getDatabase().assertDownloadMnemonicImagesTask();
        houseKeeping();
    }
}
