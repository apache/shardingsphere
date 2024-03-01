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

package org.apache.shardingsphere.infra.metadata.database.schema.builder;

import com.cedarsoftware.util.CaseInsensitiveMap;
import com.cedarsoftware.util.CaseInsensitiveSet;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.infra.util.directory.ClasspathResourceDirectoryReader;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * System schema metadata.
 */
@RequiredArgsConstructor
@Getter
public final class SystemSchemaMetadata {
    
    private static final Map<String, Map<String, Collection<String>>> DATABASE_TYPE_SCHEMA_TABLE_MAP;
    
    static {
        try (Stream<String> resourceNameStream = ClasspathResourceDirectoryReader.read("schema")) {
            DATABASE_TYPE_SCHEMA_TABLE_MAP = resourceNameStream.map(Paths::get).collect(Collectors.groupingBy(path -> path.getName(1).toString(), CaseInsensitiveMap::new,
                    Collectors.groupingBy(path -> path.getName(2).toString(), CaseInsensitiveMap::new, Collectors.mapping(path -> StringUtils.removeEnd(path.getName(3).toString(), ".yaml"),
                            Collectors.toCollection(CaseInsensitiveSet::new)))));
        }
    }
    
    /**
     * Judge whether current table is system table or not.
     *
     * @param schema schema
     * @param tableName table name
     * @return whether current table is system table or not
     */
    public static boolean isSystemTable(final String schema, final String tableName) {
        for (final Entry<String, Map<String, Collection<String>>> each : DATABASE_TYPE_SCHEMA_TABLE_MAP.entrySet()) {
            if (Optional.ofNullable(each.getValue().get(schema)).map(tables -> tables.contains(tableName)).orElse(false)) {
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
        return Optional.ofNullable(DATABASE_TYPE_SCHEMA_TABLE_MAP.get(databaseType)).map(schemas -> schemas.get(schema)).map(tables -> tables.contains(tableName)).orElse(false);
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
        return Optional.ofNullable(DATABASE_TYPE_SCHEMA_TABLE_MAP.get(databaseType)).map(schemas -> schemas.get(schema)).map(tables -> tables.containsAll(tableNames)).orElse(false);
    }
    
    /**
     * Get tables.
     *
     * @param databaseType database type
     * @param schema schema
     * @return optional tables
     */
    public static Optional<Collection<String>> getTables(final String databaseType, final String schema) {
        return Optional.ofNullable(DATABASE_TYPE_SCHEMA_TABLE_MAP.get(databaseType)).map(schemas -> schemas.get(schema));
    }
}
