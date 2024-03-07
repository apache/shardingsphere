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
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.infra.util.directory.ClasspathResourceDirectoryReader;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * System schema manager.
 */
@RequiredArgsConstructor
@Getter
public final class SystemSchemaManager {
    
    private static final Map<String, Map<String, Collection<String>>> DATABASE_TYPE_SCHEMA_TABLE_MAP;
    
    private static final Map<String, Map<String, Collection<String>>> DATABASE_TYPE_SCHEMA_RESOURCE_MAP;
    
    private static final String COMMON = "common";
    
    static {
        List<String> resourceNames;
        try (Stream<String> resourceNameStream = ClasspathResourceDirectoryReader.read("schema")) {
            resourceNames = resourceNameStream.filter(each -> each.endsWith(".yaml")).collect(Collectors.toList());
        }
        DATABASE_TYPE_SCHEMA_TABLE_MAP = resourceNames.stream().map(resourceName -> resourceName.split("/")).filter(each -> each.length == 4)
                .collect(Collectors.groupingBy(path -> path[1], CaseInsensitiveMap::new, Collectors.groupingBy(path -> path[2], CaseInsensitiveMap::new,
                        Collectors.mapping(path -> StringUtils.removeEnd(path[3], ".yaml"), Collectors.toCollection(CaseInsensitiveSet::new)))));
        DATABASE_TYPE_SCHEMA_RESOURCE_MAP = resourceNames.stream().map(resourceName -> resourceName.split("/")).filter(each -> each.length == 4)
                .collect(Collectors.groupingBy(path -> path[1], CaseInsensitiveMap::new, Collectors.groupingBy(path -> path[2], CaseInsensitiveMap::new,
                        Collectors.mapping(path -> String.join("/", path), Collectors.toCollection(CaseInsensitiveSet::new)))));
    }
    
    /**
     * Judge whether current table is system table or not.
     *
     * @param schema schema
     * @param tableName table name
     * @return whether current table is system table or not
     */
    public static boolean isSystemTable(final String schema, final String tableName) {
        for (Entry<String, Map<String, Collection<String>>> entry : DATABASE_TYPE_SCHEMA_TABLE_MAP.entrySet()) {
            if (Optional.ofNullable(entry.getValue().get(schema)).map(tables -> tables.contains(tableName)).orElse(false)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Judge whether current table is system table or not.
     *
     * @param databaseType database type
     * @param schema schema
     * @param tableName table name
     * @return whether current table is system table or not
     */
    public static boolean isSystemTable(final String databaseType, final String schema, final String tableName) {
        return Optional.ofNullable(DATABASE_TYPE_SCHEMA_TABLE_MAP.get(databaseType)).map(schemas -> schemas.get(schema)).map(tables -> tables.contains(tableName)).orElse(false)
                || Optional.ofNullable(DATABASE_TYPE_SCHEMA_TABLE_MAP.get(COMMON)).map(schemas -> schemas.get(schema)).map(tables -> tables.contains(tableName)).orElse(false);
    }
    
    /**
     * Judge whether current table is system table or not.
     *
     * @param databaseType database type
     * @param schema schema
     * @param tableNames table names
     * @return whether current table is system table or not
     */
    public static boolean isSystemTable(final String databaseType, final String schema, final Collection<String> tableNames) {
        Collection<String> databaseTypeTables = Optional.ofNullable(DATABASE_TYPE_SCHEMA_TABLE_MAP.get(databaseType)).map(schemas -> schemas.get(schema)).orElse(Collections.emptyList());
        Collection<String> commonTables = Optional.ofNullable(DATABASE_TYPE_SCHEMA_TABLE_MAP.get(COMMON)).map(schemas -> schemas.get(schema)).orElse(Collections.emptyList());
        for (final String each : tableNames) {
            if (!databaseTypeTables.contains(each) && !commonTables.contains(each)) {
                return false;
            }
        }
        return true;
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
        Optional.ofNullable(DATABASE_TYPE_SCHEMA_TABLE_MAP.get(databaseType)).map(schemas -> schemas.get(schema)).ifPresent(result::addAll);
        Optional.ofNullable(DATABASE_TYPE_SCHEMA_TABLE_MAP.get(COMMON)).map(schemas -> schemas.get(schema)).ifPresent(result::addAll);
        return result;
    }
    
    /**
     * Get all input streams.
     *
     * @param databaseType database type
     * @param schema schema
     * @return inputStream collection
     */
    public static Collection<InputStream> getAllInputStreams(final String databaseType, final String schema) {
        Collection<InputStream> result = new LinkedList<>();
        result.addAll(getInputStreams(databaseType, schema));
        result.addAll(getInputStreams(COMMON, schema));
        return result;
    }
    
    private static Collection<InputStream> getInputStreams(final String databaseType, final String schema) {
        if (!DATABASE_TYPE_SCHEMA_RESOURCE_MAP.containsKey(databaseType) || !DATABASE_TYPE_SCHEMA_RESOURCE_MAP.get(databaseType).containsKey(schema)) {
            return Collections.emptyList();
        }
        Collection<InputStream> result = new LinkedList<>();
        for (String each : DATABASE_TYPE_SCHEMA_RESOURCE_MAP.get(databaseType).get(schema)) {
            result.add(SystemSchemaManager.class.getClassLoader().getResourceAsStream(each));
        }
        return result;
    }
}
