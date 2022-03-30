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

package org.apache.shardingsphere.infra.metadata.schema.builder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * System schema builder rule.
 */
@RequiredArgsConstructor
@Getter
public enum SystemSchemaBuilderRule {
    
    MYSQL_INFORMATION_SCHEMA("mysql.information_schema", Arrays.asList("columns", "tables", "views")),
    
    MYSQL_MYSQL("mysql.mysql", Collections.singletonList("db")),
    
    MYSQL_PERFORMANCE_SCHEMA("mysql.performance_schema", Collections.singletonList("accounts")),
    
    MYSQL_SYS("mysql.sys", Collections.singletonList("sys")),
    
    POSTGRESQL_INFORMATION_SCHEMA("postgresql.information_schema", Arrays.asList("columns", "tables", "views")),
    
    POSTGRESQL_PG_CATALOG("postgresql.pg_catalog", Arrays.asList("pg_database", "pg_tablespace"));
    
    private static final Map<String, SystemSchemaBuilderRule> SCHEMA_PATH_SYSTEM_SCHEMA_BUILDER_RULE_MAP = new HashMap<>();
    
    private final String schemaPath;
    
    private final Collection<String> tables;
    
    static {
        for (SystemSchemaBuilderRule each : values()) {
            SCHEMA_PATH_SYSTEM_SCHEMA_BUILDER_RULE_MAP.put(each.getSchemaPath(), each);
        }
    }

    /**
     * Value of builder rule.
     * 
     * @param databaseTypeName database type name
     * @param schema schema
     * @return builder rule
     */
    public static SystemSchemaBuilderRule valueOf(final String databaseTypeName, final String schema) {
        String schemaPath = databaseTypeName + "." + schema;
        SystemSchemaBuilderRule result = SCHEMA_PATH_SYSTEM_SCHEMA_BUILDER_RULE_MAP.get(schemaPath);
        if (null == result) {
            throw new IllegalArgumentException(String.format("Can not find builder rule: `%s`", schemaPath));
        }
        return result;
    }
}
