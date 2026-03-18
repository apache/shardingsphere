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
 * Database statistics.
 */
@Getter
public final class DatabaseStatistics {
    
    private final Map<String, SchemaStatistics> schemaStatisticsMap = Collections.synchronizedMap(new CaseInsensitiveMap<>());
    
    /**
     * Judge whether to contains schema statistics.
     *
     * @param schemaName schema name
     * @return contains schema statistics or not
     */
    public boolean containsSchemaStatistics(final String schemaName) {
        return schemaStatisticsMap.containsKey(schemaName);
    }
    
    /**
     * Get schema statistics.
     *
     * @param schemaName schema name
     * @return schema statistics
     */
    public SchemaStatistics getSchemaStatistics(final String schemaName) {
        return schemaStatisticsMap.get(schemaName);
    }
    
    /**
     * Put schema statistics.
     *
     * @param schemaName schema name
     * @param schemaStatistics schema statistics
     */
    public void putSchemaStatistics(final String schemaName, final SchemaStatistics schemaStatistics) {
        schemaStatisticsMap.put(schemaName, schemaStatistics);
    }
    
    /**
     * Remove schema statistics.
     *
     * @param schemaName schema name
     */
    public void removeSchemaStatistics(final String schemaName) {
        schemaStatisticsMap.remove(schemaName);
    }
}
