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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;
import org.apache.shardingsphere.database.connector.core.metadata.database.system.SystemDatabase;
import org.apache.shardingsphere.database.connector.core.metadata.database.schema.SystemSchemaProvider;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.directory.ClasspathResourceDirectoryReader;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * System schema manager.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
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
        return DATABASE_TYPE_AND_SYSTEM_SCHEMA_MANAGER_MAP.entrySet().stream().anyMatch(entry -> entry.getValue().getTables(schema).contains(tableName))
                || loadSystemTableNames(schema).contains(tableName);
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
        if (null == schema) {
            return isSystemTableWithNullSchema(databaseType, tableName);
        }
        Collection<String> systemTableNames = loadSystemTableNames(databaseType, schema);
        if (!systemTableNames.isEmpty()) {
            return systemTableNames.contains(tableName) || getDialectSystemSchemaManager(COMMON).isSystemTable(schema, tableName);
        }
        return getDialectSystemSchemaManager(databaseType).isSystemTable(schema, tableName)
                || getDialectSystemSchemaManager(COMMON).isSystemTable(schema, tableName);
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
        Collection<String> systemTableNames = loadSystemTableNames(databaseType, schema);
        if (!systemTableNames.isEmpty()) {
            return systemTableNames.containsAll(tableNames) || getDialectSystemSchemaManager(COMMON).isSystemTable(schema, tableNames);
        }
        return getDialectSystemSchemaManager(databaseType).isSystemTable(schema, tableNames)
                || getDialectSystemSchemaManager(COMMON).isSystemTable(schema, tableNames);
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
        Collection<String> systemTableNames = loadSystemTableNames(databaseType, schema);
        if (!systemTableNames.isEmpty()) {
            result.addAll(systemTableNames);
        } else {
            result.addAll(getDialectSystemSchemaManager(databaseType).getTables(schema));
        }
        result.addAll(getDialectSystemSchemaManager(COMMON).getTables(schema));
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
        result.addAll(getDialectSystemSchemaManager(databaseType).getAllInputStreams(schema));
        result.addAll(getDialectSystemSchemaManager(COMMON).getAllInputStreams(schema));
        return result;
    }
    
    /**
     * Get all input streams with optional live system schema support.
     *
     * @param databaseType database type
     * @param schema schema
     * @param systemSchemaMetadataEnabled system schema metadata enabled
     * @return input streams
     */
    public static Collection<InputStream> getAllInputStreams(final String databaseType, final String schema, final boolean systemSchemaMetadataEnabled) {
        if (!systemSchemaMetadataEnabled) {
            return getAllInputStreams(databaseType, schema);
        }
        Optional<Collection<InputStream>> liveSchema = loadSystemTableInputStreams(databaseType, schema);
        if (liveSchema.isPresent()) {
            return liveSchema.get();
        }
        return getAllInputStreams(databaseType, schema);
    }
    
    private static DialectSystemSchemaManager getDialectSystemSchemaManager(final String databaseType) {
        return DATABASE_TYPE_AND_SYSTEM_SCHEMA_MANAGER_MAP.getOrDefault(databaseType, new DialectSystemSchemaManager());
    }
    
    private static boolean isSystemTableWithNullSchema(final String databaseTypeName, final String tableName) {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, databaseTypeName);
        SystemDatabase systemDatabase = new SystemDatabase(databaseType);
        for (String schemaName : systemDatabase.getSystemSchemas()) {
            if (loadSystemTableNames(databaseTypeName, schemaName).contains(tableName)) {
                return true;
            }
        }
        return getDialectSystemSchemaManager(databaseTypeName).isSystemTable(null, tableName)
                || getDialectSystemSchemaManager(COMMON).isSystemTable(null, tableName);
    }
    
    private static Collection<String> loadSystemTableNames(final String schemaName) {
        if (null == schemaName) {
            return Collections.emptyList();
        }
        for (DialectSystemSchemaManager each : DATABASE_TYPE_AND_SYSTEM_SCHEMA_MANAGER_MAP.values()) {
            Collection<String> tables = each.getTables(schemaName);
            if (!tables.isEmpty()) {
                return tables;
            }
        }
        return Collections.emptyList();
    }
    
    private static Collection<String> loadSystemTableNames(final String databaseType, final String schemaName) {
        if (null == schemaName) {
            return Collections.emptyList();
        }
        Optional<SystemSchemaProvider> provider = findSystemSchemaProvider(databaseType);
        if (!provider.isPresent()) {
            return Collections.emptyList();
        }
        Optional<Collection<String>> systemTables = provider.get().getSystemTables(schemaName);
        return systemTables.isPresent() ? systemTables.get() : Collections.emptyList();
    }
    
    private static Optional<Collection<InputStream>> loadSystemTableInputStreams(final String databaseType, final String schemaName) {
        if (null == schemaName) {
            return Optional.empty();
        }
        Optional<SystemSchemaProvider> provider = findSystemSchemaProvider(databaseType);
        return provider.isPresent() ? provider.get().getSystemSchemaInputStreams(schemaName) : Optional.empty();
    }
    
    private static Optional<SystemSchemaProvider> findSystemSchemaProvider(final String databaseType) {
        DatabaseType type = TypedSPILoader.getService(DatabaseType.class, databaseType);
        return DatabaseTypedSPILoader.findService(SystemSchemaProvider.class, type);
    }
}
