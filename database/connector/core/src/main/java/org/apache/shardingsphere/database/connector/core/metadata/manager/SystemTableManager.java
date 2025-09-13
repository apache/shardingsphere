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

package org.apache.shardingsphere.database.connector.core.metadata.manager;

import com.cedarsoftware.util.CaseInsensitiveMap;
import com.cedarsoftware.util.CaseInsensitiveSet;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.infra.util.directory.ClasspathResourceDirectoryReader;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * System table manager.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SystemTableManager {
    
    private static final Map<String, Map<String, Collection<String>>> DATABASE_TYPE_SCHEMA_TABLE_MAP;
    
    private static final String COMMON = "common";
    
    static {
        List<String> resourceNames;
        try (Stream<String> resourceNameStream = ClasspathResourceDirectoryReader.read("schema")) {
            resourceNames = resourceNameStream.filter(each -> each.endsWith(".yaml")).collect(Collectors.toList());
        }
        DATABASE_TYPE_SCHEMA_TABLE_MAP = resourceNames.stream().map(resourceName -> resourceName.split("/")).filter(each -> each.length == 4)
                .collect(Collectors.groupingBy(path -> path[1], CaseInsensitiveMap::new, Collectors.groupingBy(path -> path[2], CaseInsensitiveMap::new,
                        Collectors.mapping(path -> StringUtils.removeEnd(path[3], ".yaml"), Collectors.toCollection(CaseInsensitiveSet::new)))));
    }
    
    /**
     * Judge whether current table is system table or not.
     *
     * @param schema schema
     * @param tableName table name
     * @return whether current table is system table or not
     */
    // TODO check if this function is needed, since it's only used in SimpleTableSegmentBinder and could be replaced by SystemTableManager.isSystemTable(databaseType). (issues/36462)
    public static boolean isSystemTable(final String schema, final String tableName) {
        for (Map.Entry<String, Map<String, Collection<String>>> entry : DATABASE_TYPE_SCHEMA_TABLE_MAP.entrySet()) {
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
     * @param schema       schema
     * @param tableName    table name
     * @return whether current table is system table or not
     */
    public static boolean isSystemTable(final String databaseType, final String schema, final String tableName) {
        Map<String, Collection<String>> schemaTableMap = DATABASE_TYPE_SCHEMA_TABLE_MAP.getOrDefault(databaseType, Collections.emptyMap());
        Map<String, Collection<String>> commonTableMap = DATABASE_TYPE_SCHEMA_TABLE_MAP.getOrDefault(COMMON, Collections.emptyMap());
        if (null == schema) {
            return schemaTableMap.values().stream().anyMatch(each -> each.contains(tableName)) || commonTableMap.values().stream().anyMatch(each -> each.contains(tableName));
        }
        return schemaTableMap.getOrDefault(schema, Collections.emptyList()).contains(tableName) || commonTableMap.getOrDefault(schema, Collections.emptyList()).contains(tableName);
    }
    
    /**
     * Judge whether current table is system table or not.
     *
     * @param databaseType database type
     * @param schema       schema
     * @param tableNames   table names
     * @return whether current table is system table or not
     */
    public static boolean isSystemTable(final String databaseType, final String schema, final Collection<String> tableNames) {
        Collection<String> databaseTypeTables = Optional.ofNullable(DATABASE_TYPE_SCHEMA_TABLE_MAP.get(databaseType)).map(schemas -> schemas.get(schema)).orElse(Collections.emptyList());
        Collection<String> commonTables = Optional.ofNullable(DATABASE_TYPE_SCHEMA_TABLE_MAP.get(COMMON)).map(schemas -> schemas.get(schema)).orElse(Collections.emptyList());
        for (String each : tableNames) {
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
     * @param schema       schema
     * @return optional tables
     */
    public static Collection<String> getTables(final String databaseType, final String schema) {
        Collection<String> result = new LinkedList<>();
        Optional.ofNullable(DATABASE_TYPE_SCHEMA_TABLE_MAP.get(databaseType)).map(schemas -> schemas.get(schema)).ifPresent(result::addAll);
        Optional.ofNullable(DATABASE_TYPE_SCHEMA_TABLE_MAP.get(COMMON)).map(schemas -> schemas.get(schema)).ifPresent(result::addAll);
        return result;
    }
}
