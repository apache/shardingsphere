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

package org.apache.shardingsphere.data.pipeline.core.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Schema table util.
 */
@Slf4j
public final class SchemaTableUtil {
    
    /**
     * Get schema table map.
     *
     * @param databaseName database name
     * @param logicTables logic tables
     * @return schema table map
     */
    public static Map<String, List<String>> getSchemaTablesMap(final String databaseName, final Set<String> logicTables) {
        // TODO get by search_path
        ShardingSphereDatabase database = PipelineContext.getContextManager().getMetaDataContexts().getMetaData().getDatabase(databaseName);
        Map<String, List<String>> result = new LinkedHashMap<>();
        database.getSchemas().forEach((schemaName, schema) -> {
            for (String each : schema.getAllTableNames()) {
                if (!logicTables.contains(each)) {
                    continue;
                }
                result.computeIfAbsent(schemaName, unused -> new LinkedList<>()).add(each);
            }
        });
        log.info("getSchemaTablesMap, result={}", result);
        return result;
    }
}
