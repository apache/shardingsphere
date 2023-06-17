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

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * System schema builder rule.
 */
@RequiredArgsConstructor
@Getter
public enum SystemSchemaBuilderRule {
    
    MYSQL_INFORMATION_SCHEMA("MySQL", "information_schema", new HashSet<>(Arrays.asList("character_sets", "collation_character_set_applicability", "collations", "column_privileges", "columns",
            "engines", "events", "files", "global_status", "global_variables",
            "innodb_buffer_page", "innodb_buffer_page_lru", "innodb_buffer_pool_stats", "innodb_cmp", "innodb_cmp_per_index", "innodb_cmp_per_index_reset", "innodb_cmp_reset",
            "innodb_cmpmem", "innodb_cmpmem_reset", "innodb_ft_being_deleted",
            "innodb_ft_config", "innodb_ft_default_stopword", "innodb_ft_deleted", "innodb_ft_index_cache", "innodb_ft_index_table", "innodb_lock_waits", "innodb_locks",
            "innodb_metrics", "innodb_sys_columns", "innodb_sys_datafiles",
            "innodb_sys_fields", "innodb_sys_foreign", "innodb_sys_foreign_cols", "innodb_sys_indexes", "innodb_sys_tables", "innodb_sys_tablespaces",
            "innodb_sys_tablestats", "innodb_sys_virtual", "innodb_temp_table_info", "innodb_trx", "key_column_usage", "optimizer_trace", "parameters", "partitions", "plugins",
            "processlist", "profiling", "referential_constraints", "routines", "schema_privileges", "schemata", "session_status", "session_variables", "statistics",
            "table_constraints", "table_privileges", "tables", "tablespaces", "triggers", "user_privileges", "views"))),
    
    MYSQL_MYSQL("MySQL", "mysql", new HashSet<>(Arrays.asList("columns_priv", "db", "engine_cost", "event", "func", "general_log", "gtid_executed",
            "help_category", "help_keyword", "help_relation",
            "help_topic", "innodb_index_stats", "innodb_table_stats", "ndb_binlog_index", "plugin", "proc", "procs_priv", "proxies_priv", "server_cost", "servers",
            "slave_master_info", "slave_relay_log_info", "slave_worker_info", "slow_log", "tables_priv", "time_zone", "time_zone_leap_second",
            "time_zone_name", "time_zone_transition", "time_zone_transition_type", "user"))),
    
    MYSQL_PERFORMANCE_SCHEMA("MySQL", "performance_schema", new HashSet<>(Arrays.asList("accounts", "cond_instances", "events_stages_current", "events_stages_history", "events_stages_history_long",
            "events_stages_summary_by_account_by_event_name", "events_stages_summary_by_host_by_event_name", "events_stages_summary_by_thread_by_event_name",
            "events_stages_summary_by_user_by_event_name", "events_stages_summary_global_by_event_name", "events_statements_current", "events_statements_history", "events_statements_history_long",
            "events_statements_summary_by_account_by_event_name", "events_statements_summary_by_digest", "events_statements_summary_by_host_by_event_name", "events_statements_summary_by_program",
            "events_statements_summary_by_thread_by_event_name", "events_statements_summary_by_user_by_event_name", "events_statements_summary_global_by_event_name",
            "events_transactions_current", "events_transactions_history", "events_transactions_history_long", "events_transactions_summary_by_account_by_event_name",
            "events_transactions_summary_by_host_by_event_name", "events_transactions_summary_by_thread_by_event_name", "events_transactions_summary_by_user_by_event_name",
            "events_transactions_summary_global_by_event_name", "events_waits_current", "events_waits_history", "events_waits_history_long", "events_waits_summary_by_account_by_event_name",
            "events_waits_summary_by_host_by_event_name", "events_waits_summary_by_instance", "events_waits_summary_by_thread_by_event_name", "events_waits_summary_by_user_by_event_name",
            "events_waits_summary_global_by_event_name", "file_instances", "file_summary_by_event_name", "file_summary_by_instance", "global_status", "global_variables", "host_cache", "hosts",
            "memory_summary_by_account_by_event_name", "memory_summary_by_host_by_event_name", "memory_summary_by_thread_by_event_name", "memory_summary_by_user_by_event_name",
            "memory_summary_global_by_event_name", "metadata_locks", "mutex_instances", "objects_summary_global_by_type", "performance_timers", "prepared_statements_instances",
            "replication_applier_configuration", "replication_applier_status", "replication_applier_status_by_coordinator", "replication_applier_status_by_worker",
            "replication_connection_configuration", "replication_connection_status", "replication_group_member_stats", "replication_group_members", "rwlock_instances", "session_account_connect_attrs",
            "session_connect_attrs", "session_status", "session_variables", "setup_actors", "setup_consumers", "setup_instruments", "setup_objects", "setup_timers", "socket_instances",
            "socket_summary_by_event_name", "socket_summary_by_instance", "status_by_account", "status_by_host", "status_by_thread", "status_by_user", "table_handles",
            "table_io_waits_summary_by_index_usage", "table_io_waits_summary_by_table", "table_lock_waits_summary_by_table", "threads", "user_variables_by_thread", "users", "variables_by_thread"))),
    
    MYSQL_SYS("MySQL", "sys", new HashSet<>(Collections.singleton("sys_config"))),
    
    MYSQL_SHARDING_SPHERE("MySQL", "shardingsphere", new HashSet<>(Arrays.asList("sharding_table_statistics", "cluster_information"))),
    
    POSTGRESQL_INFORMATION_SCHEMA("PostgreSQL", "information_schema", new HashSet<>(Arrays.asList("columns", "tables", "views"))),
    
    POSTGRESQL_PG_CATALOG("PostgreSQL", "pg_catalog", new HashSet<>(Arrays.asList("pg_aggregate", "pg_am", "pg_amop", "pg_amproc", "pg_attrdef", "pg_class", "pg_database", "pg_tables", "pg_inherits",
            "pg_tablespace", "pg_trigger", "pg_namespace", "pg_roles"))),
    
    POSTGRESQL_SHARDING_SPHERE("PostgreSQL", "shardingsphere", new HashSet<>(Arrays.asList("sharding_table_statistics", "cluster_information"))),
    
    OPEN_GAUSS_INFORMATION_SCHEMA("openGauss", "information_schema", Collections.emptySet()),
    
    OPEN_GAUSS_PG_CATALOG("openGauss", "pg_catalog", new HashSet<>(Arrays.asList("pg_class", "pg_namespace"))),
    
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
    
    private static final Map<String, SystemSchemaBuilderRule> SCHEMA_PATH_SYSTEM_SCHEMA_BUILDER_RULE_MAP = new HashMap<>(values().length, 1F);
    
    private final String databaseType;
    
    private final String schema;
    
    private final Collection<String> tables;
    
    static {
        for (SystemSchemaBuilderRule each : values()) {
            SCHEMA_PATH_SYSTEM_SCHEMA_BUILDER_RULE_MAP.put(each.getDatabaseType() + "." + each.getSchema(), each);
        }
    }
    
    /**
     * Value of builder rule.
     *
     * @param databaseType database type
     * @param schema schema
     * @return builder rule
     */
    public static SystemSchemaBuilderRule valueOf(final String databaseType, final String schema) {
        String schemaPath = databaseType + "." + schema;
        SystemSchemaBuilderRule result = SCHEMA_PATH_SYSTEM_SCHEMA_BUILDER_RULE_MAP.get(schemaPath);
        Preconditions.checkNotNull(result, "Can not find builder rule: `%s`", schemaPath);
        return result;
    }
    
    /**
     * Judge whether current table is system table or not.
     *
     * @param schema schema
     * @param tableName table name
     * @return whether current table is system table or not
     */
    public static boolean isSystemTable(final String schema, final String tableName) {
        for (SystemSchemaBuilderRule each : values()) {
            if (each.getSchema().equals(schema) && each.getTables().contains(tableName)) {
                return true;
            }
        }
        return false;
    }
}
