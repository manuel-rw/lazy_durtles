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

package com.lazy_durtles.wk.db.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import javax.annotation.Nullable;

/**
 * Room entity for the mnemonic_image table. One record per subject that has a
 * WaniKani mnemonic background drawing (radicals only). The artwork itself is
 * copyright WaniKani / Tofugu and is stored as a local SVG file; this table
 * only tracks where it came from and where it is cached.
 */
@Entity(tableName = "mnemonic_image")
public final class MnemonicImage {
    @PrimaryKey private long subjectId = 0;
    private @Nullable String url;
    private @Nullable String description;
    private @Nullable String fileName;

    /**
     * The subject this mnemonic image belongs to, also primary key.
     * @return the value
     */
    public long getSubjectId() {
        return subjectId;
    }

    /**
     * The subject this mnemonic image belongs to, also primary key.
     * @param subjectId the value
     */
    public void setSubjectId(final long subjectId) {
        this.subjectId = subjectId;
    }

    /**
     * The remote URL of the SVG on files.wanikani.com.
     * @return the value
     */
    public @Nullable String getUrl() {
        return url;
    }

    /**
     * The remote URL of the SVG on files.wanikani.com.
     * @param url the value
     */
    public void setUrl(final @Nullable String url) {
        this.url = url;
    }

    /**
     * The alt-text description WaniKani provides for the image.
     * @return the value
     */
    public @Nullable String getDescription() {
        return description;
    }

    /**
     * The alt-text description WaniKani provides for the image.
     * @param description the value
     */
    public void setDescription(final @Nullable String description) {
        this.description = description;
    }

    /**
     * The local file name of the downloaded SVG, relative to the mnemonic image directory.
     * @return the value
     */
    public @Nullable String getFileName() {
        return fileName;
    }

    /**
     * The local file name of the downloaded SVG, relative to the mnemonic image directory.
     * @param fileName the value
     */
    public void setFileName(final @Nullable String fileName) {
        this.fileName = fileName;
    }
}
