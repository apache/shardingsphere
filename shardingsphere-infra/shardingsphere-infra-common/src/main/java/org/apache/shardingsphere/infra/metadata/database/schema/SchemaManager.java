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

package org.apache.shardingsphere.infra.metadata.database.schema;

import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Schema meta data manager.
 */
public final class SchemaManager {
    
    
    /**
     * Get to be added tables by schemas.
     *
     * @param reloadSchemas reload schemas
     * @param currentSchemas current schemas
     * @return To be added table meta data
     */
    public static Map<String, ShardingSphereSchema> getToBeAddedTablesBySchemas(final Map<String, ShardingSphereSchema> reloadSchemas, final Map<String, ShardingSphereSchema> currentSchemas) {
        Map<String, ShardingSphereSchema> result = new LinkedHashMap<>(currentSchemas.size(), 1);
        reloadSchemas.entrySet().stream().filter(entry -> currentSchemas.containsKey(entry.getKey())).collect(Collectors.toMap(Entry::getKey, Entry::getValue))
                .forEach((key, value) -> result.put(key, getToBeAddedTablesBySchema(value, currentSchemas.get(key))));
        return result;
    }
    
    private static ShardingSphereSchema getToBeAddedTablesBySchema(final ShardingSphereSchema reloadSchema, final ShardingSphereSchema currentSchema) {
        return new ShardingSphereSchema(getToBeAddedTables(reloadSchema.getTables(), currentSchema.getTables()), new LinkedHashMap<>());
    }
    
    /**
     * Get to be added tables.
     *
     * @param reloadTables  reload tables
     * @param currentTables current tables
     * @return To be added table meta data
     */
    public static Map<String, ShardingSphereTable> getToBeAddedTables(final Map<String, ShardingSphereTable> reloadTables, final Map<String, ShardingSphereTable> currentTables) {
        return reloadTables.entrySet().stream().filter(entry -> !entry.getValue().equals(currentTables.get(entry.getKey()))).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
    
    /**
     * Get to be deleted tables by schemas.
     *
     * @param reloadSchemas reload schemas
     * @param currentSchemas current schemas
     * @return To be deleted table meta data
     */
    public static Map<String, ShardingSphereSchema> getToBeDeletedTablesBySchemas(final Map<String, ShardingSphereSchema> reloadSchemas, final Map<String, ShardingSphereSchema> currentSchemas) {
        Map<String, ShardingSphereSchema> result = new LinkedHashMap<>(currentSchemas.size(), 1);
        currentSchemas.entrySet().stream().filter(entry -> reloadSchemas.containsKey(entry.getKey())).collect(Collectors.toMap(Entry::getKey, Entry::getValue))
                .forEach((key, value) -> result.put(key, getToBeDeletedTablesBySchema(reloadSchemas.get(key), value)));
        return result;
    }
    
    private static ShardingSphereSchema getToBeDeletedTablesBySchema(final ShardingSphereSchema reloadSchema, final ShardingSphereSchema currentSchema) {
        return new ShardingSphereSchema(getToBeDeletedTables(reloadSchema.getTables(), currentSchema.getTables()), new LinkedHashMap<>());
    }
    
    /**
     * Get to be deleted tables.
     *
     * @param reloadTables reload tables
     * @param currentTables current tables
     * @return To be deleted table meta data
     */
    public static Map<String, ShardingSphereTable> getToBeDeletedTables(final Map<String, ShardingSphereTable> reloadTables, final Map<String, ShardingSphereTable> currentTables) {
        return currentTables.entrySet().stream().filter(entry -> !reloadTables.containsKey(entry.getKey())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
    
    /**
     * Get to be deleted schema names.
     *
     * @param reloadSchemas reload schemas
     * @param currentSchemas current schemas
     * @return To be deleted schema names
     */
    public static Map<String, ShardingSphereSchema> getToBeDeletedSchemaNames(final Map<String, ShardingSphereSchema> reloadSchemas, final Map<String, ShardingSphereSchema> currentSchemas) {
        return currentSchemas.entrySet().stream().filter(entry -> !reloadSchemas.containsKey(entry.getKey())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
}
