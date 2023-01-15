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

package org.apache.shardingsphere.agent.plugin.metrics.core.constant;

/**
 * Metric IDs.
 */
public final class MetricIds {
    
    public static final String BUILD_INFO = "build_info";
    
    public static final String PARSED_INSERT_SQL = "parsed_insert_sql_total";
    
    public static final String PARSED_UPDATE_SQL = "parsed_update_sql_total";
    
    public static final String PARSED_DELETE_SQL = "parsed_delete_sql_total";
    
    public static final String PARSED_SELECT_SQL = "parsed_select_sql_total";
    
    public static final String PARSED_DDL = "parsed_ddl_total";
    
    public static final String PARSED_DCL = "parsed_dcl_total";
    
    public static final String PARSED_DAL = "parsed_dal_total";
    
    public static final String PARSED_TCL = "parsed_tcl_total";
    
    public static final String PARSED_RQL = "parsed_rql_total";
    
    public static final String PARSED_RDL = "parsed_rdl_total";
    
    public static final String PARSED_RAL = "parsed_ral_total";
    
    public static final String ROUTED_INSERT_SQL = "routed_insert_sql_total";
    
    public static final String ROUTED_UPDATE_SQL = "routed_update_sql_total";
    
    public static final String ROUTED_DELETE_SQL = "routed_delete_sql_total";
    
    public static final String ROUTED_SELECT_SQL = "routed_select_sql_total";
    
    public static final String ROUTED_DATA_SOURCES = "routed_data_sources_total";
    
    public static final String ROUTED_TABLES = "routed_tables_total";
    
    public static final String PROXY_INFO = "proxy_info";
    
    public static final String PROXY_METADATA_INFO = "proxy_meta_data_info";
    
    public static final String PROXY_CURRENT_CONNECTIONS = "proxy_current_connections";
    
    public static final String PROXY_REQUESTS = "proxy_requests_total";
    
    public static final String PROXY_COMMIT_TRANSACTIONS = "proxy_commit_transactions_total";
    
    public static final String PROXY_ROLLBACK_TRANSACTIONS = "proxy_rollback_transactions_total";
    
    public static final String PROXY_EXECUTE_LATENCY_MILLIS = "proxy_execute_latency_millis";
    
    public static final String PROXY_EXECUTE_ERRORS = "proxy_execute_errors_total";
}
