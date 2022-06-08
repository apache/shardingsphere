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
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.metadata.LogicTableName;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;

import java.util.HashMap;
import java.util.Map;

/**
 * Table name and schema name mapping.
 */
@RequiredArgsConstructor
@Slf4j
public final class TableNameSchemaNameMapping {
    
    private final Map<LogicTableName, String> mapping;
    
    /**
     * Convert table name and schema name mapping from schemas.
     *
     * @param schemas logic table name and schema map
     * @return logic table name and schema name map
     */
    public static Map<LogicTableName, String> convert(final Map<String, ShardingSphereSchema> schemas) {
        Map<LogicTableName, String> result = new HashMap<>();
        schemas.forEach((schemaName, schema) -> {
            for (String each : schema.getAllTableNames()) {
                result.put(new LogicTableName(each), schemaName);
            }
        });
        log.info("mapping={}", result);
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
