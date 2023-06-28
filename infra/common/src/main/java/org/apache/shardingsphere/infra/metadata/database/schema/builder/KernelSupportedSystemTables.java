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

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Kernel supported system tables.
 */
@RequiredArgsConstructor
@Getter
public enum KernelSupportedSystemTables {
    
    MYSQL_SHARDING_SPHERE("MySQL", "shardingsphere", new HashSet<>(Arrays.asList("sharding_table_statistics", "cluster_information"))),
    
    POSTGRESQL_PG_CATALOG("PostgreSQL", "pg_catalog", new HashSet<>(Arrays.asList("pg_class", "pg_namespace"))),
    
    POSTGRESQL_SHARDING_SPHERE("PostgreSQL", "shardingsphere", new HashSet<>(Arrays.asList("sharding_table_statistics", "cluster_information"))),
    
    OPEN_GAUSS_SHARDING_SPHERE("openGauss", "shardingsphere", new HashSet<>(Arrays.asList("sharding_table_statistics", "cluster_information")));
    
    private static final Map<String, KernelSupportedSystemTables> SCHEMA_PATH_SYSTEM_SCHEMA_BUILDER_RULE_MAP = new HashMap<>(values().length, 1F);
    
    private final String databaseType;
    
    private final String schema;
    
    private final Collection<String> tables;
    
    static {
        for (KernelSupportedSystemTables each : values()) {
            SCHEMA_PATH_SYSTEM_SCHEMA_BUILDER_RULE_MAP.put(each.getDatabaseType() + "." + each.getSchema(), each);
        }
    }
    
    /**
     * Judge whether current table is system table or not.
     *
     * @param schema schema
     * @param tableName table name
     * @return whether current table is system table or not
     */
    public static boolean isSupportedSystemTable(final String schema, final String tableName) {
        for (KernelSupportedSystemTables each : values()) {
            if (each.getSchema().equals(schema) && each.getTables().contains(tableName)) {
                return true;
            }
        }
        return false;
    }
}
