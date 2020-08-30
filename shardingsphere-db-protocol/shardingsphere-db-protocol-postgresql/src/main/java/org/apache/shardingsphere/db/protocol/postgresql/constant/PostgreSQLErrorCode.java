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

/**
 * PostgreSQL error code.
 *
 * @see <a href="https://www.postgresql.org/docs/12/errcodes-appendix.html">Appendix A. PostgreSQL Error Codes</a>
 */
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
    PROTOCOL_VIOLATION("08P01", "protocol_violation"),
    INVALID_AUTHORIZATION_SPECIFICATION("28000", "invalid_authorization_specification"),
    INVALID_PASSWORD("28P01", "invalid_password"),
    INVALID_CATALOG_NAME("3D000", "invalid_catalog_name"),
    INVALID_SCHEMA_NAME("3F000", "invalid_schema_name"),;
    
    private final String errorCode;
    
    private final String conditionName;
    
    PostgreSQLErrorCode(final String errorCode, final String conditionName) {
        this.errorCode = errorCode;
        this.conditionName = conditionName;
    }
    
    /**
     * Get error code.
     *
     * @return error code
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * Get condition name.
     *
     * @return condition name
     */
    public String getConditionName() {
        return conditionName;
    }
}
