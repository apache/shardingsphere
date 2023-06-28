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

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ShardingSphere statistics.
 */
@Getter
public final class ShardingSphereStatistics {
    
    private final Map<String, ShardingSphereDatabaseData> databaseData = new LinkedHashMap<>();
    
    /**
     * Get ShardingSphere database.
     *
     * @param databaseName database name
     * @return ShardingSphere database data
     */
    public ShardingSphereDatabaseData getDatabase(final String databaseName) {
        return databaseData.get(databaseName.toLowerCase());
    }
    
    /**
     * Put ShardingSphere database.
     *
     * @param databaseName database name
     * @param database ShardingSphere database
     */
    public void putDatabase(final String databaseName, final ShardingSphereDatabaseData database) {
        databaseData.put(databaseName.toLowerCase(), database);
    }
    
    /**
     * Drop ShardingSphere database.
     *
     * @param databaseName database name
     */
    public void dropDatabase(final String databaseName) {
        databaseData.remove(databaseName.toLowerCase());
    }
    
    /**
     * Judge contains ShardingSphere database from meta data or not.
     *
     * @param databaseName database name
     * @return contains ShardingSphere database from meta data or not
     */
    public boolean containsDatabase(final String databaseName) {
        return databaseData.containsKey(databaseName.toLowerCase());
    }
}
