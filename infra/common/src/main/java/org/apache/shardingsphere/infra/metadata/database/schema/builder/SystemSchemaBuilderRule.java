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
    
    MYSQL_SYS("MySQL", "sys",
            new HashSet<>(Arrays.asList("host_summary", "host_summary_by_file_io", "host_summary_by_file_io_type", "host_summary_by_stages", "host_summary_by_statement_latency",
                    "host_summary_by_statement_type", "innodb_buffer_stats_by_schema", "innodb_buffer_stats_by_table", "innodb_lock_waits",
                    "io_by_thread_by_latency", "io_global_by_file_by_bytes", "io_global_by_file_by_latency", "io_global_by_wait_by_bytes", "io_global_by_wait_by_latency", "latest_file_io",
                    "memory_by_host_by_current_bytes", "memory_by_thread_by_current_bytes", "memory_by_user_by_current_bytes",
                    "memory_global_by_current_bytes", "memory_global_total", "metrics", "processlist", "ps_check_lost_instrumentation", "schema_auto_increment_columns", "schema_index_statistics",
                    "schema_object_overview", "schema_redundant_indexes",
                    "schema_table_lock_waits", "schema_table_statistics", "schema_table_statistics_with_buffer", "schema_tables_with_full_table_scans", "schema_unused_indexes", "session",
                    "session_ssl_status", "statement_analysis", "statements_with_errors_or_warnings",
                    "statements_with_full_table_scans", "statements_with_runtimes_in_95th_percentile", "statements_with_sorting", "statements_with_temp_tables", "sys_config", "user_summary",
                    "user_summary_by_file_io", "user_summary_by_file_io_type", "user_summary_by_stages",
                    "user_summary_by_statement_latency", "user_summary_by_statement_type", "version", "wait_classes_global_by_avg_latency", "wait_classes_global_by_latency",
                    "waits_by_host_by_latency", "waits_by_user_by_latency", "waits_global_by_latency"))),
    
    MYSQL_SHARDING_SPHERE("MySQL", "shardingsphere", new HashSet<>(Arrays.asList("sharding_table_statistics", "cluster_information"))),
    
    POSTGRESQL_INFORMATION_SCHEMA("PostgreSQL", "information_schema",
            new HashSet<>(Arrays.asList("_pg_foreign_data_wrappers", "_pg_foreign_servers", "_pg_foreign_table_columns", "_pg_foreign_tables", "_pg_user_mappings", "administrable_role_authorizations",
                    "applicable_roles", "attributes", "character_sets",
                    "check_constraint_routine_usage", "check_constraints", "collation_character_set_applicability", "collations", "column_column_usage", "column_domain_usage", "column_options",
                    "column_privileges", "column_udt_usage",
                    "columns", "constraint_column_usage", "constraint_table_usage", "data_type_privileges", "domain_constraints", "domain_udt_usage", "domains", "element_types", "enabled_roles",
                    "foreign_data_wrapper_options", "foreign_data_wrappers", "foreign_server_options", "foreign_servers", "foreign_table_options", "foreign_tables", "information_schema_catalog_name",
                    "key_column_usage", "parameters",
                    "referential_constraints", "role_column_grants", "role_routine_grants", "role_table_grants", "role_udt_grants", "role_usage_grants", "routine_column_usage", "routine_privileges",
                    "routine_routine_usage",
                    "routine_sequence_usage", "routine_table_usage", "routines", "schemata", "sequences", "sql_features", "sql_implementation_info", "sql_parts", "sql_sizing",
                    "table_constraints", "table_privileges", "tables", "transforms", "triggered_update_columns", "triggers", "udt_privileges", "usage_privileges", "user_defined_types",
                    "user_mapping_options", "user_mappings", "view_column_usage", "view_routine_usage", "view_table_usage", "views"))),
    
    POSTGRESQL_PG_CATALOG("PostgreSQL", "pg_catalog",
            new HashSet<>(Arrays.asList("pg_aggregate", "pg_am", "pg_amop", "pg_amproc", "pg_attrdef", "pg_attribute", "pg_auth_members", "pg_authid", "pg_available_extension_versions",
                    "pg_available_extensions", "pg_backend_memory_contexts", "pg_cast", "pg_class", "pg_collation", "pg_config", "pg_constraint", "pg_conversion", "pg_cursors",
                    "pg_database", "pg_db_role_setting", "pg_default_acl", "pg_depend", "pg_description", "pg_enum", "pg_event_trigger", "pg_extension", "pg_file_settings",
                    "pg_foreign_data_wrapper", "pg_foreign_server", "pg_foreign_table", "pg_group", "pg_hba_file_rules", "pg_index", "pg_indexes", "pg_inherits", "pg_init_privs",
                    "pg_language", "pg_largeobject", "pg_largeobject_metadata", "pg_locks", "pg_matviews", "pg_namespace", "pg_opclass", "pg_operator", "pg_opfamily",
                    "pg_partitioned_table", "pg_policies", "pg_policy", "pg_prepared_statements", "pg_prepared_xacts", "pg_proc", "pg_publication", "pg_publication_rel", "pg_publication_tables",
                    "pg_range", "pg_replication_origin", "pg_replication_origin_status", "pg_replication_slots", "pg_rewrite", "pg_roles", "pg_rules", "pg_seclabel", "pg_seclabels",
                    "pg_sequence", "pg_sequences", "pg_settings", "pg_shadow", "pg_shdepend", "pg_shdescription", "pg_shmem_allocations", "pg_shseclabel", "pg_stat_activity",
                    "pg_stat_all_indexes", "pg_stat_all_tables", "pg_stat_archiver", "pg_stat_bgwriter", "pg_stat_database", "pg_stat_database_conflicts", "pg_stat_gssapi", "pg_stat_progress_analyze",
                    "pg_stat_progress_basebackup",
                    "pg_stat_progress_cluster", "pg_stat_progress_copy", "pg_stat_progress_create_index", "pg_stat_progress_vacuum", "pg_stat_replication", "pg_stat_replication_slots", "pg_stat_slru",
                    "pg_stat_ssl", "pg_stat_subscription",
                    "pg_stat_sys_indexes", "pg_stat_sys_tables", "pg_stat_user_functions", "pg_stat_user_indexes", "pg_stat_user_tables", "pg_stat_wal", "pg_stat_wal_receiver",
                    "pg_stat_xact_all_tables", "pg_stat_xact_sys_tables",
                    "pg_stat_xact_user_functions", "pg_stat_xact_user_tables", "pg_statio_all_indexes", "pg_statio_all_sequences", "pg_statio_all_tables", "pg_statio_sys_indexes",
                    "pg_statio_sys_sequences", "pg_statio_sys_tables", "pg_statio_user_indexes",
                    "pg_statio_user_sequences", "pg_statio_user_tables", "pg_statistic", "pg_statistic_ext", "pg_statistic_ext_data", "pg_stats", "pg_stats_ext", "pg_stats_ext_exprs",
                    "pg_subscription",
                    "pg_subscription_rel", "pg_tables", "pg_tablespace", "pg_timezone_abbrevs", "pg_timezone_names", "pg_transform", "pg_trigger", "pg_ts_config", "pg_ts_config_map",
                    "pg_ts_dict", "pg_ts_parser", "pg_ts_template", "pg_type", "pg_user", "pg_user_mapping", "pg_user_mappings", "pg_views"))),
    
    POSTGRESQL_SHARDING_SPHERE("PostgreSQL", "shardingsphere", new HashSet<>(Arrays.asList("sharding_table_statistics", "cluster_information"))),
    
    OPEN_GAUSS_INFORMATION_SCHEMA("openGauss", "information_schema",
            new HashSet<>(Arrays.asList("_pg_foreign_data_wrappers", "_pg_foreign_servers", "_pg_foreign_table_columns", "_pg_foreign_tables", "_pg_user_mappings", "administrable_role_authorizations",
                    "applicable_roles", "attributes", "character_sets",
                    "check_constraint_routine_usage", "check_constraints", "collation_character_set_applicability", "collations", "column_domain_usage", "column_options", "column_privileges",
                    "column_udt_usage", "columns",
                    "constraint_column_usage", "constraint_table_usage", "data_type_privileges", "domain_constraints", "domain_udt_usage", "domains", "element_types", "enabled_roles",
                    "foreign_data_wrapper_options",
                    "foreign_data_wrappers", "foreign_server_options", "foreign_servers", "foreign_table_options", "foreign_tables", "information_schema_catalog_name", "key_column_usage",
                    "parameters", "referential_constraints",
                    "role_column_grants", "role_routine_grants", "role_table_grants", "role_udt_grants", "role_usage_grants", "routine_privileges", "routines", "schemata", "sequences",
                    "sql_features", "sql_implementation_info", "sql_languages", "sql_packages", "sql_parts", "sql_sizing", "sql_sizing_profiles", "table_constraints", "table_privileges",
                    "tables", "triggered_update_columns", "triggers", "udt_privileges", "usage_privileges", "user_defined_types", "user_mapping_options", "user_mappings", "view_column_usage",
                    "view_routine_usage", "view_table_usage", "views"))),
    
    OPEN_GAUSS_PG_CATALOG("openGauss", "pg_catalog", new HashSet<>(Arrays.asList("get_global_prepared_xacts", "gs_all_control_group_info", "gs_asp", "gs_auditing", "gs_auditing_access",
            "gs_auditing_policy", "gs_auditing_policy_access", "gs_auditing_policy_filters", "gs_auditing_policy_privileges",
            "gs_auditing_privilege", "gs_client_global_keys", "gs_client_global_keys_args", "gs_cluster_resource_info", "gs_column_keys", "gs_column_keys_args", "gs_comm_proxy_thread_status",
            "gs_db_privilege", "gs_db_privileges",
            "gs_encrypted_columns", "gs_encrypted_proc", "gs_file_stat", "gs_get_control_group_info", "gs_global_chain", "gs_global_config", "gs_gsc_memory_detail", "gs_instance_time",
            "gs_job_argument",
            "gs_job_attribute", "gs_labels", "gs_lsc_memory_detail", "gs_masking", "gs_masking_policy", "gs_masking_policy_actions", "gs_masking_policy_filters", "gs_matview", "gs_matview_dependency",
            "gs_matviews", "gs_model_warehouse", "gs_obsscaninfo", "gs_opt_model", "gs_os_run_info", "gs_package", "gs_policy_label", "gs_recyclebin", "gs_redo_stat",
            "gs_session_cpu_statistics", "gs_session_memory", "gs_session_memory_context", "gs_session_memory_detail", "gs_session_memory_statistics", "gs_session_stat", "gs_session_time",
            "gs_shared_memory_detail", "gs_sql_count",
            "gs_stat_session_cu", "gs_thread_memory_context", "gs_total_memory_detail", "gs_total_nodegroup_memory_detail", "gs_txn_snapshot", "gs_uid", "gs_wlm_cgroup_info",
            "gs_wlm_ec_operator_history", "gs_wlm_ec_operator_info",
            "gs_wlm_ec_operator_statistics", "gs_wlm_instance_history", "gs_wlm_operator_history", "gs_wlm_operator_info", "gs_wlm_operator_statistics", "gs_wlm_plan_encoding_table",
            "gs_wlm_plan_operator_history", "gs_wlm_plan_operator_info", "gs_wlm_rebuild_user_resource_pool",
            "gs_wlm_resource_pool", "gs_wlm_session_history", "gs_wlm_session_info", "gs_wlm_session_info_all", "gs_wlm_session_query_info_all", "gs_wlm_session_statistics", "gs_wlm_user_info",
            "gs_wlm_user_resource_history", "gs_wlm_workload_records",
            "mpp_tables", "pg_aggregate", "pg_am", "pg_amop", "pg_amproc", "pg_app_workloadgroup_mapping", "pg_attrdef", "pg_attribute", "pg_auth_history",
            "pg_auth_members", "pg_authid", "pg_available_extension_versions", "pg_available_extensions", "pg_cast", "pg_class", "pg_collation", "pg_comm_delay", "pg_comm_recv_stream",
            "pg_comm_send_stream", "pg_comm_status", "pg_constraint", "pg_control_group_config", "pg_conversion", "pg_cursors", "pg_database", "pg_db_role_setting", "pg_default_acl",
            "pg_depend", "pg_description", "pg_directory", "pg_enum", "pg_ext_stats", "pg_extension", "pg_extension_data_source", "pg_foreign_data_wrapper", "pg_foreign_server",
            "pg_foreign_table", "pg_get_invalid_backends", "pg_get_senders_catchup_time", "pg_group", "pg_gtt_attached_pids", "pg_gtt_relstats", "pg_gtt_stats", "pg_hashbucket", "pg_index",
            "pg_indexes", "pg_inherits", "pg_job", "pg_job_proc", "pg_language", "pg_largeobject", "pg_largeobject_metadata", "pg_locks", "pg_namespace",
            "pg_node_env", "pg_object", "pg_obsscaninfo", "pg_opclass", "pg_operator", "pg_opfamily", "pg_os_threads", "pg_partition", "pg_pltemplate",
            "pg_prepared_statements", "pg_prepared_xacts", "pg_proc", "pg_publication", "pg_publication_rel", "pg_publication_tables", "pg_range", "pg_replication_origin",
            "pg_replication_origin_status",
            "pg_replication_slots", "pg_resource_pool", "pg_rewrite", "pg_rlspolicies", "pg_rlspolicy", "pg_roles", "pg_rules", "pg_running_xacts", "pg_seclabel",
            "pg_seclabels", "pg_session_iostat", "pg_session_wlmstat", "pg_settings", "pg_shadow", "pg_shdepend", "pg_shdescription", "pg_shseclabel", "pg_stat_activity",
            "pg_stat_activity_ng", "pg_stat_all_indexes", "pg_stat_all_tables", "pg_stat_bad_block", "pg_stat_bgwriter", "pg_stat_database", "pg_stat_database_conflicts", "pg_stat_replication",
            "pg_stat_subscription",
            "pg_stat_sys_indexes", "pg_stat_sys_tables", "pg_stat_user_functions", "pg_stat_user_indexes", "pg_stat_user_tables", "pg_stat_xact_all_tables", "pg_stat_xact_sys_tables",
            "pg_stat_xact_user_functions", "pg_stat_xact_user_tables",
            "pg_statio_all_indexes", "pg_statio_all_sequences", "pg_statio_all_tables", "pg_statio_sys_indexes", "pg_statio_sys_sequences", "pg_statio_sys_tables", "pg_statio_user_indexes",
            "pg_statio_user_sequences", "pg_statio_user_tables",
            "pg_statistic", "pg_statistic_ext", "pg_stats", "pg_subscription", "pg_synonym", "pg_tables", "pg_tablespace", "pg_tde_info", "pg_thread_wait_status",
            "pg_timezone_abbrevs", "pg_timezone_names", "pg_total_memory_detail", "pg_total_user_resource_info", "pg_total_user_resource_info_oid", "pg_trigger", "pg_ts_config", "pg_ts_config_map",
            "pg_ts_dict",
            "pg_ts_parser", "pg_ts_template", "pg_type", "pg_user", "pg_user_mapping", "pg_user_mappings", "pg_user_status", "pg_variable_info", "pg_views",
            "pg_wlm_statistics", "pg_workload_group", "pgxc_class", "pgxc_group", "pgxc_node", "pgxc_prepared_xacts", "pgxc_slice", "pgxc_thread_wait_status", "plan_table",
            "plan_table_data", "statement_history", "streaming_cont_query", "streaming_stream", "streaming_reaper_status", "sys_dummy"))),
    
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
