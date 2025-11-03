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

import com.cedarsoftware.util.CaseInsensitiveMap;
import com.cedarsoftware.util.CaseInsensitiveSet;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Dialect system schema manager.
 */
public final class DialectSystemSchemaManager {
    
    private final Map<String, Collection<String>> schemaAndTableMap = new CaseInsensitiveMap<>();
    
    private final Map<String, Collection<String>> schemaAndResourceMap = new CaseInsensitiveMap<>();
    
    /**
     * Put the table.
     *
     * @param schemaName schema name
     * @param tableName table name
     */
    public void putTable(final String schemaName, final String tableName) {
        schemaAndTableMap.computeIfAbsent(schemaName, key -> new CaseInsensitiveSet<>()).add(tableName);
    }
    
    /**
     * Put resource.
     *
     * @param schemaName schema name
     * @param resourcePath resource path
     */
    public void putResource(final String schemaName, final String resourcePath) {
        schemaAndResourceMap.computeIfAbsent(schemaName, key -> new CaseInsensitiveSet<>()).add(resourcePath);
    }
    
    /**
     * Judge whether the current table is system table.
     *
     * @param schemaName schema name
     * @param tableName table name
     * @return is system table or not
     */
    public boolean isSystemTable(final String schemaName, final String tableName) {
        return null == schemaName
                ? schemaAndTableMap.values().stream().anyMatch(each -> each.contains(tableName))
                : schemaAndTableMap.getOrDefault(schemaName, Collections.emptyList()).contains(tableName);
    }
    
    /**
     * Judge whether the current table is system table.
     *
     * @param schemaName schema name
     * @param tableNames table names
     * @return is system table or not
     */
    public boolean isSystemTable(final String schemaName, final Collection<String> tableNames) {
        Collection<String> tables = schemaAndTableMap.getOrDefault(schemaName, Collections.emptyList());
        for (String each : tableNames) {
            if (!tables.contains(each)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Get tables.
     *
     * @param schemaName schema
     * @return got tables
     */
    public Collection<String> getTables(final String schemaName) {
        return schemaAndTableMap.getOrDefault(schemaName, Collections.emptyList());
    }
    
    /**
     * Get all input streams.
     *
     * @param schemaName schema name
     * @return input streams
     */
    public Collection<InputStream> getAllInputStreams(final String schemaName) {
        return schemaAndResourceMap.getOrDefault(schemaName, Collections.emptyList())
                .stream().map(each -> DialectSystemSchemaManager.class.getClassLoader().getResourceAsStream(each)).collect(Collectors.toList());
    }
}
