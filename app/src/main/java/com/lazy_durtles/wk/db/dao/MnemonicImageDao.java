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

package com.lazy_durtles.wk.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.lazy_durtles.wk.db.model.MnemonicImage;

import java.util.List;

import javax.annotation.Nullable;

/**
 * DAO for mnemonic image records.
 */
@Dao
public abstract class MnemonicImageDao {
    /**
     * Room-generated method: get the record for one subject.
     *
     * @param subjectId the subject ID
     * @return the record or null
     */
    @Query("SELECT * FROM mnemonic_image WHERE subjectId = :subjectId")
    public abstract @Nullable MnemonicImage getBySubjectId(long subjectId);

    /**
     * Room-generated method: get the record for one subject, as LiveData.
     *
     * @param subjectId the subject ID
     * @return the LiveData instance
     */
    @Query("SELECT * FROM mnemonic_image WHERE subjectId = :subjectId")
    public abstract LiveData<MnemonicImage> getBySubjectIdLive(long subjectId);

    /**
     * Room-generated method: get all records.
     *
     * @return the list of records
     */
    @Query("SELECT * FROM mnemonic_image")
    public abstract List<MnemonicImage> getAll();

    /**
     * Room-generated method: count all records.
     *
     * @return the number of records
     */
    @Query("SELECT COUNT(*) FROM mnemonic_image")
    public abstract int getCount();

    /**
     * Room-generated method: count all records, as LiveData.
     *
     * @return the LiveData instance
     */
    @Query("SELECT COUNT(*) FROM mnemonic_image")
    public abstract LiveData<Integer> getCountLive();

    /**
     * Room-generated method: insert or replace a record.
     *
     * @param record the record
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insert(MnemonicImage record);

    /**
     * Room-generated method: delete all records.
     */
    @Query("DELETE FROM mnemonic_image")
    public abstract void deleteAll();
}
