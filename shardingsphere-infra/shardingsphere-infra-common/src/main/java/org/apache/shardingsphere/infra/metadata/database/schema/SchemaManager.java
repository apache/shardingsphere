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
     * Get to be deleted schema meta data.
     *
     * @param loadedSchema loaded schema
     * @param currentSchema current schema
     * @return To be deleted schema meta data
     */
    public static ShardingSphereSchema getToBeDeletedSchemaMetaData(final ShardingSphereSchema loadedSchema, final ShardingSphereSchema currentSchema) {
        return new ShardingSphereSchema(getToBeDeletedTables(loadedSchema.getTables(), currentSchema.getTables()), new LinkedHashMap<>());
    }
    
    /**
     * Get to be added table meta data.
     *
     * @param loadedTables loaded tables
     * @param currentTables current tables
     * @return To be added table meta data
     */
    public static Map<String, ShardingSphereTable> getToBeAddedTables(final Map<String, ShardingSphereTable> loadedTables, final Map<String, ShardingSphereTable> currentTables) {
        return loadedTables.entrySet().stream().filter(entry -> !entry.getValue().equals(currentTables.get(entry.getKey()))).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
    
    /**
     * Get to be deleted table meta data.
     *
     * @param loadedTables loaded tables
     * @param currentTables current tables
     * @return To be deleted table meta data
     */
    public static Map<String, ShardingSphereTable> getToBeDeletedTables(final Map<String, ShardingSphereTable> loadedTables, final Map<String, ShardingSphereTable> currentTables) {
        return currentTables.entrySet().stream().filter(entry -> !loadedTables.containsKey(entry.getKey())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
}
