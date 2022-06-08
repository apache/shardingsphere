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

package org.apache.shardingsphere.agent.metrics.api.constant;

public final class MetricIds {
    
    public static final String PROXY_REQUEST = "proxy_request_total";
    
    public static final String PROXY_COLLECTION = "proxy_connection_total";
    
    public static final String PROXY_EXECUTE_LATENCY_MILLIS = "proxy_execute_latency_millis";
    
    public static final String PROXY_EXECUTE_ERROR = "proxy_execute_error_total";
    
    public static final String ROUTE_SQL_SELECT = "route_sql_select_total";
    
    public static final String ROUTE_SQL_UPDATE = "route_sql_update_total";
    
    public static final String ROUTE_SQL_DELETE = "route_sql_delete_total";
    
    public static final String ROUTE_SQL_INSERT = "route_sql_insert_total";
    
    public static final String ROUTE_DATASOURCE = "route_datasource_total";
    
    public static final String ROUTE_TABLE = "route_table_total";
    
    public static final String TRANSACTION_COMMIT = "proxy_transaction_commit_total";
    
    public static final String TRANSACTION_ROLLBACK = "proxy_transaction_rollback_total";
    
    public static final String PROXY_INFO = "proxy_info";
    
    public static final String BUILD_INFO = "build_info";
    
    public static final String METADATA_INFO = "meta_data_info";
    
    public static final String PARSE_SQL_SELECT = "parse_sql_dml_select_total";
    
    public static final String PARSE_SQL_UPDATE = "parse_sql_dml_update_total";
    
    public static final String PARSE_SQL_DELETE = "parse_sql_dml_delete_total";
    
    public static final String PARSE_SQL_INSERT = "parse_sql_dml_insert_total";
    
    public static final String PARSE_SQL_DDL = "parse_sql_ddl_total";
    
    public static final String PARSE_SQL_DCL = "parse_sql_dcl_total";
    
    public static final String PARSE_SQL_DAL = "parse_sql_dal_total";
    
    public static final String PARSE_SQL_TCL = "parse_sql_tcl_total";
    
    public static final String PARSE_DIST_SQL_RQL = "parse_dist_sql_rql_total";
    
    public static final String PARSE_DIST_SQL_RDL = "parse_dist_sql_rdl_total";
    
    public static final String PARSE_DIST_SQL_RAL = "parse_dist_sql_ral_total";
}
