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
 * Schema statistics.
 */
@Getter
public final class SchemaStatistics {
    
    private final Map<String, TableStatistics> tableStatisticsMap = Collections.synchronizedMap(new CaseInsensitiveMap<>());
    
    /**
     * Get table statistics.
     *
     * @param tableName tableName table name
     * @return table statistics
     */
    public TableStatistics getTableStatistics(final String tableName) {
        return tableStatisticsMap.get(tableName);
    }
    
    /**
     * Add table statistics.
     *
     * @param tableName table name
     * @param tableStatistics table statistics
     */
    public void putTableStatistics(final String tableName, final TableStatistics tableStatistics) {
        tableStatisticsMap.put(tableName, tableStatistics);
    }
    
    /**
     * Remove table statistics.
     *
     * @param tableName table name
     */
    public void removeTableStatistics(final String tableName) {
        tableStatisticsMap.remove(tableName);
    }
    
    /**
     * Judge whether contains table statistics.
     *
     * @param tableName table name
     * @return contains table statistics or not
     */
    public boolean containsTableStatistics(final String tableName) {
        return tableStatisticsMap.containsKey(tableName);
    }
}
