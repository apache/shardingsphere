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

package org.apache.shardingsphere.infra.metadata.database.schema.manager;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Generic schema manager.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GenericSchemaManager {
    
    /**
     * Get to be added tables by schemas.
     *
     * @param reloadSchemas reload schemas
     * @param currentSchemas current schemas
     * @return To be added table meta data
     */
    public static Map<String, ShardingSphereSchema> getToBeAddedTablesBySchemas(final Map<String, ShardingSphereSchema> reloadSchemas, final Map<String, ShardingSphereSchema> currentSchemas) {
        Map<String, ShardingSphereSchema> result = new LinkedHashMap<>(currentSchemas.size(), 1F);
        reloadSchemas.entrySet().stream().filter(entry -> !currentSchemas.containsKey(entry.getKey())).forEach(entry -> result.put(entry.getKey(), entry.getValue()));
        reloadSchemas.entrySet().stream().filter(entry -> currentSchemas.containsKey(entry.getKey())).collect(Collectors.toMap(Entry::getKey, Entry::getValue))
                .forEach((key, value) -> result.put(key, getToBeAddedTablesBySchema(value, currentSchemas.get(key))));
        return result;
    }
    
    private static ShardingSphereSchema getToBeAddedTablesBySchema(final ShardingSphereSchema reloadSchema, final ShardingSphereSchema currentSchema) {
        return new ShardingSphereSchema(currentSchema.getName(), getToBeAddedTables(reloadSchema.getTables(), currentSchema.getTables()), new LinkedHashMap<>());
    }
    
    /**
     * Get to be added tables.
     *
     * @param reloadTables  reload tables
     * @param currentTables current tables
     * @return to be added tables
     */
    public static Map<String, ShardingSphereTable> getToBeAddedTables(final Map<String, ShardingSphereTable> reloadTables, final Map<String, ShardingSphereTable> currentTables) {
        return reloadTables.entrySet().stream().filter(entry -> !entry.getValue().equals(currentTables.get(entry.getKey()))).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
    
    /**
     * Get to be dropped tables by schemas.
     *
     * @param reloadSchemas reload schemas
     * @param currentSchemas current schemas
     * @return to be dropped table
     */
    public static Map<String, ShardingSphereSchema> getToBeDroppedTablesBySchemas(final Map<String, ShardingSphereSchema> reloadSchemas, final Map<String, ShardingSphereSchema> currentSchemas) {
        Map<String, ShardingSphereSchema> result = new LinkedHashMap<>(currentSchemas.size(), 1F);
        currentSchemas.entrySet().stream().filter(entry -> reloadSchemas.containsKey(entry.getKey())).collect(Collectors.toMap(Entry::getKey, Entry::getValue))
                .forEach((key, value) -> result.put(key, getToBeDroppedTablesBySchema(reloadSchemas.get(key), value)));
        return result;
    }
    
    private static ShardingSphereSchema getToBeDroppedTablesBySchema(final ShardingSphereSchema reloadSchema, final ShardingSphereSchema currentSchema) {
        return new ShardingSphereSchema(currentSchema.getName(), getToBeDroppedTables(reloadSchema.getTables(), currentSchema.getTables()), new LinkedHashMap<>());
    }
    
    /**
     * Get to be drop tables.
     *
     * @param reloadTables reload tables
     * @param currentTables current tables
     * @return to be dropped table
     */
    public static Map<String, ShardingSphereTable> getToBeDroppedTables(final Map<String, ShardingSphereTable> reloadTables, final Map<String, ShardingSphereTable> currentTables) {
        return currentTables.entrySet().stream().filter(entry -> !reloadTables.containsKey(entry.getKey())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
    
    /**
     * Get to be dropped schemas.
     *
     * @param reloadSchemas reload schemas
     * @param currentSchemas current schemas
     * @return to be dropped schemas
     */
    public static Map<String, ShardingSphereSchema> getToBeDroppedSchemas(final Map<String, ShardingSphereSchema> reloadSchemas, final Map<String, ShardingSphereSchema> currentSchemas) {
        return currentSchemas.entrySet().stream().filter(entry -> !reloadSchemas.containsKey(entry.getKey())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
}
