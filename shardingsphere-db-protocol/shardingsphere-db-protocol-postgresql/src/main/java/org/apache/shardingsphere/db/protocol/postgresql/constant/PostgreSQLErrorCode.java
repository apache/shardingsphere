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

package org.apache.shardingsphere.db.protocol.postgresql.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * PostgreSQL error code.
 *
 * @see <a href="https://www.postgresql.org/docs/12/errcodes-appendix.html">Appendix A. PostgreSQL Error Codes</a>
 */
@RequiredArgsConstructor
@Getter
public enum PostgreSQLErrorCode {
    
    SUCCESSFUL_COMPLETION("00000", "successful_completion"),
    
    WARNING("01000", "warning"),
    
    DYNAMIC_RESULT_SETS_RETURNED("0100C", "dynamic_result_sets_returned"),
    
    IMPLICIT_ZERO_BIT_PADDING("01008", "implicit_zero_bit_padding"),
    
    NULL_VALUE_ELIMINATED_IN_SET_FUNCTION("01003", "null_value_eliminated_in_set_function"),
    
    PRIVILEGE_NOT_GRANTED("01007", "privilege_not_granted"),
    
    PRIVILEGE_NOT_REVOKED("01006", "privilege_not_revoked"),
    
    STRING_DATA_RIGHT_TRUNCATION("01004", "string_data_right_truncation"),
    
    DEPRECATED_FEATURE("01P01", "deprecated_feature"),
    
    CONNECTION_EXCEPTION("08000", "connection_exception"),
    
    CONNECTION_DOES_NOT_EXIST("08003", "connection_does_not_exist"),
    
    CONNECTION_FAILURE("08006", "connection_failure"),
    
    SQLCLIENT_UNABLE_TO_ESTABLISH_SQLCONNECTION("08001", "sqlclient_unable_to_establish_sqlconnection"),
    
    SQLSERVER_REJECTED_ESTABLISHMENT_OF_SQLCONNECTION("08004", "sqlserver_rejected_establishment_of_sqlconnection"),
    
    TRANSACTION_RESOLUTION_UNKNOWN("08007", "transaction_resolution_unknown"),
    
    MODIFYING_SQL_DATA_NOT_PERMITTED("38002", "modifying_sql_data_not_permitted"),
    
    PROTOCOL_VIOLATION("08P01", "protocol_violation"),
    
    FEATURE_NOT_SUPPORTED("0A000", "feature_not_supported"),
    
    DUPLICATE_DATABASE("42P04", "Database '%s' already exists"),
    
    INVALID_AUTHORIZATION_SPECIFICATION("28000", "invalid_authorization_specification"),
    
    SYNTAX_ERROR("42601", "syntax_error"),
    
    INVALID_PARAMETER_VALUE("22023", "invalid_parameter_value"),
    
    INVALID_PASSWORD("28P01", "invalid_password"),
    
    INVALID_CATALOG_NAME("3D000", "invalid_catalog_name"),
    
    INVALID_SCHEMA_NAME("3F000", "invalid_schema_name"),
    
    UNDEFINED_COLUMN("42703", "undefined_column"),
    
    SYSTEM_ERROR("58000", "system_error");
    
    private final String errorCode;
    
    private final String conditionName;
}
