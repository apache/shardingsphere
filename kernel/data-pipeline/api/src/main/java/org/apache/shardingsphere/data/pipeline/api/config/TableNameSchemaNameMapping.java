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

import lombok.ToString;
import org.apache.shardingsphere.data.pipeline.api.metadata.LogicTableName;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Table name and schema name mapping.
 */
@ToString
public final class TableNameSchemaNameMapping {
    
    private final Map<LogicTableName, String> mapping;
    
    /**
     * Convert table name and schema name mapping from schemas.
     *
     * @param tableSchemaMap table name and schema name map
     */
    public TableNameSchemaNameMapping(final Map<String, String> tableSchemaMap) {
        if (null == tableSchemaMap) {
            mapping = Collections.emptyMap();
            return;
        }
        Map<LogicTableName, String> mapping = new HashMap<>();
        tableSchemaMap.forEach((tableName, schemaName) -> {
            if (null != schemaName) {
                mapping.put(new LogicTableName(tableName), schemaName);
            }
        });
        this.mapping = mapping;
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
