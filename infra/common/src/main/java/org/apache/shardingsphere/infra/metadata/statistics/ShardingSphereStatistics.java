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

package org.apache.shardingsphere.infra.metadata.statistics;

import com.cedarsoftware.util.CaseInsensitiveMap;
import lombok.Getter;

import java.util.Collections;
import java.util.Map;

/**
 * ShardingSphere statistics.
 */
@Getter
public final class ShardingSphereStatistics {
    
    private final Map<String, DatabaseStatistics> databaseStatisticsMap = Collections.synchronizedMap(new CaseInsensitiveMap<>());
    
    /**
     * Get database statistics.
     *
     * @param databaseName database name
     * @return database statistics
     */
    public DatabaseStatistics getDatabaseStatistics(final String databaseName) {
        return databaseStatisticsMap.get(databaseName);
    }
    
    /**
     * Put database statistics.
     *
     * @param databaseName database name
     * @param databaseStatistics database statistics
     */
    public void putDatabaseStatistics(final String databaseName, final DatabaseStatistics databaseStatistics) {
        databaseStatisticsMap.put(databaseName, databaseStatistics);
    }
    
    /**
     * Drop database statistics.
     *
     * @param databaseName database name
     */
    public void dropDatabaseStatistics(final String databaseName) {
        databaseStatisticsMap.remove(databaseName);
    }
    
    /**
     * Judge whether contains database statistics.
     *
     * @param databaseName database name
     * @return contains database statistics or not
     */
    public boolean containsDatabaseStatistics(final String databaseName) {
        return databaseStatisticsMap.containsKey(databaseName);
    }
}
