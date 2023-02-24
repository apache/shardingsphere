/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.data.pipeline.cdc.generator;

import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;

import java.util.Comparator;

/**
 * Data record comparator generator.
 */
public final class DataRecordComparatorGenerator {
    
    /**
     * Generator comparator.
     *
     * @param databaseType database type
     * @return data record comparator
     */
    public static Comparator<DataRecord> generatorIncrementalComparator(final DatabaseType databaseType) {
        if (databaseType instanceof OpenGaussDatabaseType) {
            return Comparator.comparing(DataRecord::getCsn, Comparator.nullsFirst(Comparator.naturalOrder()));
        }
        // TODO MySQL and PostgreSQL not support now
        return null;
    }
}
