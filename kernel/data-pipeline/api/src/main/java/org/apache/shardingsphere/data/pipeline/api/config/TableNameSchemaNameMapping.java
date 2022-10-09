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

package org.apache.shardingsphere.data.pipeline.api.config;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.shardingsphere.data.pipeline.api.metadata.LogicTableName;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Table name and schema name mapping.
 */
@RequiredArgsConstructor
@ToString
public final class TableNameSchemaNameMapping {
    
    private final Map<LogicTableName, String> mapping;
    
    /**
     * Convert table name and schema name mapping from schemas.
     *
     * @param schemaTablesMap schema name and table names map
     * @return logic table name and schema name map
     */
    public static Map<LogicTableName, String> convert(final Map<String, List<String>> schemaTablesMap) {
        Map<LogicTableName, String> result = new LinkedHashMap<>();
        schemaTablesMap.forEach((schemaName, tableNames) -> {
            for (String each : tableNames) {
                result.put(new LogicTableName(each), schemaName);
            }
        });
        return result;
    }
    
    /**
     * Convert table name and schema name mapping.
     *
     * @param schemaName schema name
     * @param tables tables
     * @return logic table name and schema name map
     */
    public static Map<LogicTableName, String> convert(final String schemaName, final Collection<String> tables) {
        Map<LogicTableName, String> result = new LinkedHashMap<>();
        for (String each : tables) {
            result.put(new LogicTableName(each), schemaName);
        }
        return result;
    }
    
    /**
     * Get schema name.
     *
     * @param logicTableName logic table name
     * @return schema name
     */
    public String getSchemaName(final String logicTableName) {
        return mapping.get(new LogicTableName(logicTableName));
    }
    
    /**
     * Get schema name.
     *
     * @param logicTableName logic table name
     * @return schema name
     */
    public String getSchemaName(final LogicTableName logicTableName) {
        return mapping.get(logicTableName);
    }
}
