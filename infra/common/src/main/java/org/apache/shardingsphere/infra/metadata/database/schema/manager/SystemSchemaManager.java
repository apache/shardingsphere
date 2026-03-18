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
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.Strings;
import org.apache.shardingsphere.infra.util.directory.ClasspathResourceDirectoryReader;

import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * System schema manager.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SystemSchemaManager {
    
    private static final Map<String, DialectSystemSchemaManager> DATABASE_TYPE_AND_SYSTEM_SCHEMA_MANAGER_MAP;
    
    private static final String COMMON = "common";
    
    static {
        List<String> resourceNames;
        try (Stream<String> resourceNameStream = ClasspathResourceDirectoryReader.read("schema")) {
            resourceNames = resourceNameStream.filter(each -> each.endsWith(".yaml")).collect(Collectors.toList());
        }
        DATABASE_TYPE_AND_SYSTEM_SCHEMA_MANAGER_MAP = new CaseInsensitiveMap<>();
        for (String each : resourceNames) {
            String[] pathParts = each.split("/");
            if (4 == pathParts.length) {
                String databaseType = pathParts[1];
                String schemaName = pathParts[2];
                String tableName = Strings.CS.removeEnd(pathParts[3], ".yaml");
                String resourcePath = String.join("/", pathParts);
                DialectSystemSchemaManager dialectSystemSchemaManager = DATABASE_TYPE_AND_SYSTEM_SCHEMA_MANAGER_MAP.computeIfAbsent(databaseType, key -> new DialectSystemSchemaManager());
                dialectSystemSchemaManager.putTable(schemaName, tableName);
                dialectSystemSchemaManager.putResource(schemaName, resourcePath);
            }
        }
    }
    
    /**
     * Judge whether the current table is system table.
     *
     * @param schema schema
     * @param tableName table name
     * @return is system table or not
     */
    public static boolean isSystemTable(final String schema, final String tableName) {
        return DATABASE_TYPE_AND_SYSTEM_SCHEMA_MANAGER_MAP.entrySet().stream().anyMatch(entry -> entry.getValue().getTables(schema).contains(tableName));
    }
    
    /**
     * Judge whether the current table is system table.
     *
     * @param databaseType database type
     * @param schema schema
     * @param tableName table name
     * @return is system table or not
     */
    public static boolean isSystemTable(final String databaseType, final String schema, final String tableName) {
        return DATABASE_TYPE_AND_SYSTEM_SCHEMA_MANAGER_MAP.containsKey(databaseType) && DATABASE_TYPE_AND_SYSTEM_SCHEMA_MANAGER_MAP.get(databaseType).isSystemTable(schema, tableName)
                || DATABASE_TYPE_AND_SYSTEM_SCHEMA_MANAGER_MAP.containsKey(COMMON) && DATABASE_TYPE_AND_SYSTEM_SCHEMA_MANAGER_MAP.get(COMMON).isSystemTable(schema, tableName);
    }
    
    /**
     * Judge whether the current table is system table.
     *
     * @param databaseType database type
     * @param schema schema
     * @param tableNames table names
     * @return is system table or not
     */
    public static boolean isSystemTable(final String databaseType, final String schema, final Collection<String> tableNames) {
        return DATABASE_TYPE_AND_SYSTEM_SCHEMA_MANAGER_MAP.containsKey(databaseType) && DATABASE_TYPE_AND_SYSTEM_SCHEMA_MANAGER_MAP.get(databaseType).isSystemTable(schema, tableNames)
                || DATABASE_TYPE_AND_SYSTEM_SCHEMA_MANAGER_MAP.containsKey(COMMON) && DATABASE_TYPE_AND_SYSTEM_SCHEMA_MANAGER_MAP.get(COMMON).isSystemTable(schema, tableNames);
    }
    
    /**
     * Get tables.
     *
     * @param databaseType database type
     * @param schema schema
     * @return optional tables
     */
    public static Collection<String> getTables(final String databaseType, final String schema) {
        Collection<String> result = new LinkedList<>();
        result.addAll(DATABASE_TYPE_AND_SYSTEM_SCHEMA_MANAGER_MAP.get(databaseType).getTables(schema));
        result.addAll(DATABASE_TYPE_AND_SYSTEM_SCHEMA_MANAGER_MAP.get(COMMON).getTables(schema));
        return result;
    }
    
    /**
     * Get all input streams.
     *
     * @param databaseType database type
     * @param schema schema
     * @return input streams
     */
    public static Collection<InputStream> getAllInputStreams(final String databaseType, final String schema) {
        Collection<InputStream> result = new LinkedList<>();
        result.addAll(DATABASE_TYPE_AND_SYSTEM_SCHEMA_MANAGER_MAP.get(databaseType).getAllInputStreams(schema));
        result.addAll(DATABASE_TYPE_AND_SYSTEM_SCHEMA_MANAGER_MAP.get(COMMON).getAllInputStreams(schema));
        return result;
    }
}
