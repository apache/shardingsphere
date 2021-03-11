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

package org.apache.shardingsphere.infra.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Common error code.
 */
@RequiredArgsConstructor
@Getter
public enum CommonErrorCode implements SQLErrorCode {
    
    CIRCUIT_BREAK_MODE(10000, "C10000", "Circuit break mode is ON."),
    
    SHARDING_TABLE_RULES_NOT_EXISTED(11001, "C11001", "Sharding table rule %s is not exist."),
    
    TABLES_IN_USED(11002, "C11002", "Can not drop rule, tables %s in the rule are still in used."),

    RESOURCE_IN_USED(11003, "C11003", "Can not drop resources, resources %s in the rule are still in used."),
    
    RESOURCE_NOT_EXIST(11004, "C11004", "Can not drop resources, resources %s do not exist."),
    
    REPLICA_QUERY_RULE_NOT_EXIST(11005, "C11005", "Replica query rule does not exist."),
    
    REPLICA_QUERY_RULE_DATA_SOURCE_NOT_EXIST(11006, "C11006", "Data sources %s in replica query rule do not exist."),
    
    ADD_REPLICA_QUERY_RULE_DATA_SOURCE_EXIST(11007, "C11007", "Can not add replica query rule, data sources %s in replica query rule already exists."),
    
    REPLICA_QUERY_RULE_EXIST(11008, "C11008", "Replica query rule already exists."),
    
    SHARDING_RULE_NOT_EXIST(11009, "C11009", "Sharding rule does not exist."),
    
    SHARDING_TABLE_RULE_EXIST(11010, "C11010", "Sharding table rules: [%s] already exists."),

    // ShardingSphereException
    SHARDING_KEY_SCHEDULING_ALGORITHM_LENGTH_ERROR(11011, "C11011", "Key length has to be between %s and %s."),

    SHARDING_LOGIC_COLUMN_NOT_EXIST(11012, "C11012", "Can not find logic column by %s."),

    SHARDING_BETWEEN_AND_IN_ENCRYPT_RULE_NOT_UNSUPPORTED(11013, "C11013", "The SQL clause 'BETWEEN...AND...' is unsupported in encrypt rule."),

    SHARDING_HA_TYPE_CHECK_START_ERROR(11014, "C11014", "The HA type check and start error: %s."),

    SHARDING_DATABASE_TYPE_NOT_UNSUPPORTED(11015, "C11015", "Unsupported database:'%s'."),

    SHARDING_EVENTBUS_POST_EVENT_ERROR(11016, "C11016", "The eventbus post event %s error:'%s'."),

    SHARDING_GET_EXECUTOR_GROUP_RESULTS_ERROR(11017, "C11017", "Get executor group results error:'%s'."),

    SHARDING_SQL_EXECUTOR_ERROR(11018, "C11018", "The SQL executor error:'%s'."),

    SHARDING_DATABASE_TYPE_INIT_CALCITE_PROPERTIES_NO_MATCHING(11019, "C11019", "No matching DatabaseType found."),

    SHARDING_INIT_CALCITE_LOGIC_SCHEMA_FACTORY_ERROR(11020, "C11020", "Init calcite logic schema factory error:'%s'."),

    SHARDING_CREATE_CALCITE_LOGIC_SCHEMA_ERROR(11021, "C11021", "No `%s` schema."),

    SHARDING_CLOSE_CALCITE_ROW_ENUMERATOR_ERROR(11022, "C11022", "Close calcite row enumerator error:'%s'."),

    SHARDING_EXECUTE_CALCITE_ROW_ENUMERATOR_ERROR(11023, "C11023", "Execute calcite row enumerator error:'%s'."),

    SHARDING_SERVICE_LOCK_WAIT_TIMEOUT_ERROR(11024, "C11024", "Service lock wait timeout of %s ms exceeded."),

    SHARDING_SET_CHARACTER_STREAM_PARAMETER_ERROR(11025, "C11025", "Set character stream parameter error:'%s'."),

    SHARDING_UNSUPPORTED_DATA_TYPE_URL_ERROR(11026, "C11026", "Unsupported Data type: URL for value %s."),

    SHARDING_UNSUPPORTED_DATA_TYPE_BIG_DECIMAL_ERROR(11027, "C11027", "Unsupported Data type: BigDecimal for value %s."),

    SHARDING_UNSUPPORTED_DATA_TYPE_ERROR(11028, "C11028", "Unsupported data type: %s for value %s."),

    SHARDING_GET_RESULTSET_FROM_STATEMENT_ERROR(11029, "C11029", "Get resultset from statement error: %s."),

    SHARDING_DATA_SOURCE_TYPE_ERROR(11030, "C11030", "Can't find data source type."),

    SHARDING_JNDI_DATA_SOURCE_TYPE_ERROR(11031, "C11031", "Can't find JNDI data source type."),

    SHARDING_SWITCH_SCHEMA_ERROR(11032, "C11032", "Failed to switch schema, please terminate current transaction."),

    SHARDING_LOAD_JDBC_DRIVER_CLASS_ERROR(11033, "C11033", "Cannot load JDBC driver class `%s`, make sure it in ShardingSphere-Proxy's classpath."),

    SHARDING_RESOLVE_JDBC_URL_ERROR(11034, "C11034", "Cannot resolve JDBC url `%s`. Please implements `%s` and add to SPI."),

    SHARDING_GET_DATABASE_TYPE_METHOD_UNSUPPORTED_ERROR(11035, "C11035", "Unsupported getDatabaseType method."),

    SHARDING_SWITCH_TRANSACTION_TYPE_ERROR(11036, "C11036", "Failed to switch transaction type, please terminate current transaction."),

    SHARDING_SWITCH_TRANSACTION_TYPE_NOT_SELECT_DATABASE_ERROR(11037, "C11037", "Please select database, then switch transaction type."),

    SHARDING_LOAD_DATABASE_SERVER_INFO_ERROR(11038, "C11038", "Load database server info failed: %s."),

    SHARDING_SQL_CLAUSE_IN_UNSUPPORTED_IN_SHADOW_ERROR(11039, "C11039", "The SQL clause 'IN...' is unsupported in shadow rule."),

    SHARDING_SQL_CLAUSE_BETWEEN_AND_UNSUPPORTED_IN_SHADOW_ERROR(11040, "C11040", "The SQL clause 'BETWEEN...AND...' is unsupported in shadow rule."),

    SHARDING_CLASS_NOT_IMPLEMENT_ERROR(11041, "C11041", "Class %s should be implement %s."),

    SHARDING_TABLE_METAA_DATA_CHECK_UNIFORMED_ERROR(11042, "C11042", "Cannot get uniformed table structure for logic table `%s`, %s."),

    SHARDING_DATA_SOURCE_IN_SHARDING_RULE_NOT_FOUND_ERROR(11043, "C11043", "Cannot find data source in sharding rule, invalid actual data node is: '%s'."),

    SHARDING_INSERT_CLAUSE_SHARDING_COLUMN_IS_NULL_ERROR(11044, "C11044", "Insert clause sharding column can't be null."),

    SHARDING_DIFFERENT_TYPES_FOR_SHARDING_VALUE_FOUND_ERROR(11045, "C11045", "Found different types for sharding value `%s`."),

    SHARDING_TABLE_ROUTE_EMPTY_ERROR(11046, "C11046", "Cannot find table rule and default data source with logic tables: '%s'."),

    SHARDING_SINGLE_TABLE_DOES_NOT_EXIST_ERROR(11047, "C11047", "`%s` single table does not exist."),

    SHARDING_NOT_SUPPORT_SHARDING_TABLE_ERROR(11048, "C11048", "Can not support sharding table '%s'."),

    SHARDING_ACTUAL_TABLES_IN_USE_ERROR(11049, "C11049", "Actual Tables: [%s] are in use."),

    SHARDING_NOT_SUPPORT_MULTIPLE_TABLE_ERROR(11050, "C11050", "Cannot support Multiple-Table for '%s'."),

    SHARDING_NOT_SUPPORT_DELETE_LIMIT_IN_MULTIPLE_DATA_NODES_ERROR(11051, "C11051", "DELETE ... LIMIT can not support sharding route to multiple data nodes."),

    SHARDING_NOT_SUPPORT_INSERT_INTO_ON_DUPLICATE_KEY_UPDATE_UPDATE_FOR_SHARDING_COLUMN_ERROR(11052, "C11052", "INSERT INTO ... ON DUPLICATE KEY UPDATE can not support update for sharding column."),

    SHARDING_NOT_SUPPORT_INSERT_INTO_SELECT_APPLYING_KEY_GENERATOR_ERROR(11053, "C11053", "INSERT INTO ... SELECT can not support applying keyGenerator to absent generateKeyColumn."),

    SHARDING_TABLE_INSERTED_AND_SELECTED_MUST_BE_THE_SAME_OR_BIND_TABLES_ERROR(11054, "C11054", "The table inserted and the table selected must be the same or bind tables."),

    SHARDING_UPDATE_SHARDING_KEY_ERROR(11055, "C11055", "Can not update sharding key, logic table: [%s], column: [%s]."),

    SHARDING_NOT_SUPPORT_UPDATE_LIMIT_IN_MULTIPLE_DATA_NODES_ERROR(11056, "C11056", "UPDATE ... LIMIT can not support sharding route to multiple data nodes."),

    SHARDING_ROUTE_TABLE_NOT_EXIST_ERROR(11057, "C11057", "Route table %s does not exist, available actual table: %s."),

    SHARDING_SHARDING_STRATEGY_TYPE_TAG_ERROR(11058, "C11058", "Cannot support sharding strategy tag type: %s."),

    SHARDING_CREATE_XA_DATASOURCE_ERROR(11059, "C11059", "Failed to create [%s] XA DataSource."),

    SHARDING_LOAD_XA_DATASOURCE_ERROR(11060, "C11060", "Failed to load [%s] XA DataSource."),

    SHARDING_INSTANCE_XA_DATASOURCE_ERROR(11061, "C11061", "Failed to instance [%s] XA DataSource."),

    SHARDING_SWAP_DATASOURCE_TYPE_ERROR(11062, "C11062", "Cannot swap data source type: `%s`, please provide an implementation from SPI `%s`."),

    SHARDING_SHARDING_VALUE_ERROR(11063, "C11063", "Failed to shard value %s, and availableTables %s."),

    // SCALING
    SCALING_JOB_NOT_EXIST(12001, "C12001", "Scaling job %s does not exist."),

    SCALING_DATA_CHECK_FAIL(12002, "C12002", "Scaling job: table %s count check failed."),

    SCALING_PREPARE_DATA_SOURCES_FAIL(12003, "C12003", "Scaling job: Data sources can't connected."),

    SCALING_PREPARE_CHECK_TARGET_TABLE_FAIL(12004, "C12004", "Scaling job: Check target table failed."),

    SCALING_PREPARE_CHECK_TARGET_TABLE_EMPTY(12005, "C12005", "Scaling job: Target table [%s] is not empty."),

    SCALING_PREPARE_FAIL(12006, "C12006", "Scaling job: preparing failed."),

    SCALING_SPLIT_TASK_FAIL(12007, "C12007", "Scaling job: split task for table %s by primary key %s error."),

    SCALING_SOURCE_DATA_SOURCE_CHECK_PRIVILEGES_FAIL(12008, "C12008", "Scaling job: source data source check privileges failed. "
            + "Source data source is lack of REPLICATION SLAVE, REPLICATION CLIENT ON *.* privileges."),

    SCALING_OPERATE_FAILED(12009, "C12009", "Scaling Operate Failed: [%s]"),

    SCALING_SOURCE_DATA_SOURCE_CHECK_VARIABLES_FAIL(12010, "C12010", "Scaling Job: source data source check variables failed."),

    SCALING_SOURCE_DATA_SOURCE_CHECK_VARIABLES_REQUIRED_FAIL(12011, "C12011", "Scaling Job: source data source required %s = %s, now is %s."),

    SCALING_SOURCE_DATA_SOURCE_NO_TABLES_FIND(12012, "C12012", "Scaling Job: no tables find in the source data source."),

    SCALING_TASK_EXECUTE_READ_CHARACTER_VARYING_FAIL(12013, "C12013", "Scaling Job: read character varying data unexpected exception."),

    SCALING_TASK_EXECUTE_READ_TABLE_EVENT_FAIL(12014, "C12014", "Scaling Job: read table event unexpected exception."),

    SCALING_TASK_EXECUTE_DUMP_FAIL(12015, "C12015", "Scaling Job: Dump unexpected exception."),

    SCALING_TASK_EXECUTE_WAIT_FOR_RESULT_FAIL(12016, "C12016", "Scaling Job: task %s execute failed."),

    SCALING_TASK_EXECUTE_WRITE_FAIL(12017, "C12017", "Scaling Job: write failed."),

    SCALING_UNEXPECTED_DATA_RECORD_ORDER_EXCEPTION(12018, "C12018", "Scaling Job: unexpected data record order exception."),

    UNSUPPORTED_COMMAND(19998, "C19998", "Unsupported command: [%s]"),
    
    UNKNOWN_EXCEPTION(19999, "C19999", "Unknown exception: [%s]");

    private final int errorCode;
    
    private final String sqlState;
    
    private final String errorMessage;
}
