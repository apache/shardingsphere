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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Kernel supported system tables.
 */
@RequiredArgsConstructor
@Getter
public enum KernelSupportedSystemTables {
    
    MYSQL_SYS("MySQL", "sys", new HashSet<>(Collections.singleton("sys_config"))),
    
    MYSQL_SHARDING_SPHERE("MySQL", "shardingsphere", new HashSet<>(Arrays.asList("sharding_table_statistics", "cluster_information"))),
    
    POSTGRESQL_INFORMATION_SCHEMA("PostgreSQL", "information_schema", new HashSet<>(Arrays.asList("columns", "tables", "views"))),
    
    POSTGRESQL_PG_CATALOG("PostgreSQL", "pg_catalog", new HashSet<>(Arrays.asList("pg_aggregate", "pg_class", "pg_database", "pg_tables", "pg_inherits",
            "pg_tablespace", "pg_trigger", "pg_namespace", "pg_roles"))),
    
    POSTGRESQL_SHARDING_SPHERE("PostgreSQL", "shardingsphere", new HashSet<>(Arrays.asList("sharding_table_statistics", "cluster_information"))),
    
    OPEN_GAUSS_INFORMATION_SCHEMA("openGauss", "information_schema", Collections.emptySet()),
    
    OPEN_GAUSS_PG_CATALOG("openGauss", "pg_catalog", new HashSet<>(Arrays.asList("pg_class", "pg_namespace", "pg_database", "pg_roles", "pg_tables", "pg_tablespace"))),
    
    OPEN_GAUSS_BLOCKCHAIN("openGauss", "blockchain", Collections.emptySet()),
    
    OPEN_GAUSS_CSTORE("openGauss", "cstore", Collections.emptySet()),
    
    OPEN_GAUSS_DB4AI("openGauss", "db4ai", Collections.emptySet()),
    
    OPEN_GAUSS_DBE_PERF("openGauss", "dbe_perf", Collections.emptySet()),
    
    OPEN_GAUSS_DBE_PLDEBUGGER("openGauss", "dbe_pldebugger", Collections.emptySet()),
    
    OPEN_GAUSS_GAUSSDB("openGauss", "gaussdb", Collections.emptySet()),
    
    OPEN_GAUSS_ORACLE("openGauss", "oracle", Collections.emptySet()),
    
    OPEN_GAUSS_PKG_SERVICE("openGauss", "pkg_service", Collections.emptySet()),
    
    OPEN_GAUSS_SNAPSHOT("openGauss", "snapshot", Collections.emptySet()),
    
    OPEN_GAUSS_PLDEVELOPER("openGauss", "dbe_pldeveloper", Collections.emptySet()),
    
    OPEN_GAUSS_PG_TOAST("openGauss", "pg_toast", Collections.emptySet()),
    
    OPEN_GAUSS_PKG_UTIL("openGauss", "pkg_util", Collections.emptySet()),
    
    OPEN_GAUSS_SQLADVISOR("openGauss", "sqladvisor", Collections.emptySet()),
    
    OPEN_GAUSS_SHARDING_SPHERE("openGauss", "shardingsphere", new HashSet<>(Arrays.asList("sharding_table_statistics", "cluster_information")));
    
    private static final Map<String, KernelSupportedSystemTables> SCHEMA_NAME_TO_TABLES = new HashMap<>(values().length, 1F);
    
    private final String databaseType;
    
    private final String schema;
    
    private final Collection<String> tables;
    
    static {
        for (KernelSupportedSystemTables each : values()) {
            SCHEMA_NAME_TO_TABLES.put(each.getDatabaseType() + "." + each.getSchema(), each);
        }
    }
    
    /**
     * Judge whether current table is kernel supported system table or not.
     *
     * @param schema schema
     * @param tableName table name
     * @return whether current table is kernel supported system table or not
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
