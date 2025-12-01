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
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Generic schema manager.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GenericSchemaManager {
    
    /**
     * Get to be altered schemas with tables added.
     *
     * @param reloadDatabase reload database
     * @param currentDatabase current database
     * @return to be altered schemas with tables added
     */
    public static Collection<ShardingSphereSchema> getToBeAlteredSchemasWithTablesAdded(final ShardingSphereDatabase reloadDatabase, final ShardingSphereDatabase currentDatabase) {
        Collection<ShardingSphereSchema> result = new LinkedList<>();
        reloadDatabase.getAllSchemas().stream().filter(each -> !currentDatabase.containsSchema(each.getName())).forEach(result::add);
        reloadDatabase.getAllSchemas().stream().filter(each -> currentDatabase.containsSchema(each.getName())).collect(Collectors.toList())
                .forEach(each -> result.add(getToBeAlteredSchemaWithTablesAdded(each, currentDatabase.getSchema(each.getName()))));
        return result;
    }
    
    private static ShardingSphereSchema getToBeAlteredSchemaWithTablesAdded(final ShardingSphereSchema reloadSchema, final ShardingSphereSchema currentSchema) {
        return new ShardingSphereSchema(currentSchema.getName(), getToBeAddedTables(reloadSchema, currentSchema), new LinkedList<>());
    }
    
    /**
     * Get to be added tables.
     *
     * @param reloadSchema reload schema
     * @param currentSchema current schema
     * @return to be added tables
     */
    public static Collection<ShardingSphereTable> getToBeAddedTables(final ShardingSphereSchema reloadSchema, final ShardingSphereSchema currentSchema) {
        return reloadSchema.getAllTables().stream().filter(each -> !currentSchema.containsTable(each.getName())
                || !each.toString().equals(currentSchema.getTable(each.getName()).toString())).collect(Collectors.toList());
    }
    
    /**
     * Get to be altered schemas with tables dropped.
     *
     * @param reloadDatabase reload database
     * @param currentDatabase current database
     * @return to be altered schemas with tables dropped
     */
    public static Collection<ShardingSphereSchema> getToBeAlteredSchemasWithTablesDropped(final ShardingSphereDatabase reloadDatabase, final ShardingSphereDatabase currentDatabase) {
        Collection<ShardingSphereSchema> result = new LinkedList<>();
        currentDatabase.getAllSchemas().stream().filter(each -> reloadDatabase.containsSchema(each.getName())).collect(Collectors.toMap(ShardingSphereSchema::getName, each -> each))
                .forEach((key, value) -> result.add(getToBeAlteredSchemaWithTablesDropped(reloadDatabase.getSchema(key), value)));
        return result;
    }
    
    private static ShardingSphereSchema getToBeAlteredSchemaWithTablesDropped(final ShardingSphereSchema reloadSchema, final ShardingSphereSchema currentSchema) {
        return new ShardingSphereSchema(currentSchema.getName(), getToBeDroppedTables(reloadSchema, currentSchema), new LinkedList<>());
    }
    
    /**
     * Get to be drop tables.
     *
     * @param reloadSchema reload schema
     * @param currentSchema current schema
     * @return to be dropped table
     */
    public static Collection<ShardingSphereTable> getToBeDroppedTables(final ShardingSphereSchema reloadSchema, final ShardingSphereSchema currentSchema) {
        return currentSchema.getAllTables().stream().filter(each -> !reloadSchema.containsTable(each.getName())).collect(Collectors.toList());
    }
    
    /**
     * Get to be dropped schema names.
     *
     * @param reloadDatabase reload database
     * @param currentDatabase current database
     * @return to be dropped schema names
     */
    public static Collection<String> getToBeDroppedSchemaNames(final ShardingSphereDatabase reloadDatabase, final ShardingSphereDatabase currentDatabase) {
        return currentDatabase.getAllSchemas().stream().map(ShardingSphereSchema::getName).filter(each -> !reloadDatabase.containsSchema(each)).collect(Collectors.toSet());
    }
}
