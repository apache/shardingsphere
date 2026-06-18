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

package org.apache.shardingsphere.database.exception.postgresql.vendor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.exception.postgresql.sqlstate.PostgreSQLState;
import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.SQLState;
import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.XOpenSQLState;
import org.apache.shardingsphere.infra.exception.external.sql.vendor.VendorError;

/**
 * PostgreSQL vendor error.
 *
 * @see <a href="https://www.postgresql.org/docs/12/errcodes-appendix.html">Appendix A. PostgreSQL Error Codes</a>
 */
@RequiredArgsConstructor
@Getter
public enum PostgreSQLVendorError implements VendorError {
    
    SUCCESSFUL_COMPLETION(XOpenSQLState.SUCCESSFUL_COMPLETION, "successful_completion"),
    
    PRIVILEGE_NOT_GRANTED(XOpenSQLState.PRIVILEGE_NOT_GRANTED, "Access denied for user '%s' to database '%s'"),
    
    PROTOCOL_VIOLATION(PostgreSQLState.PROTOCOL_VIOLATION, "expected %s response, got message type %s"),
    
    FEATURE_NOT_SUPPORTED(XOpenSQLState.FEATURE_NOT_SUPPORTED, "feature_not_supported"),
    
    DUPLICATE_DATABASE(PostgreSQLState.DUPLICATE_DATABASE, "Database '%s' already exists"),
    
    DUPLICATE_TABLE(PostgreSQLState.DUPLICATE_TABLE, "Table '%s' already exists"),
    
    INVALID_AUTHORIZATION_SPECIFICATION(XOpenSQLState.INVALID_AUTHORIZATION_SPECIFICATION, "unknown username: %s"),
    
    NO_USERNAME(XOpenSQLState.INVALID_AUTHORIZATION_SPECIFICATION, "no PostgreSQL user name specified in startup packet"),
    
    INVALID_PASSWORD(PostgreSQLState.INVALID_PASSWORD, "password authentication failed for user \"%s\""),
    
    INVALID_CATALOG_NAME(XOpenSQLState.INVALID_CATALOG_NAME, "database \"%s\" does not exist"),
    
    NO_SUCH_TABLE(PostgreSQLState.UNDEFINED_TABLE, "Table \"%s\" does not exist"),
    
    UNDEFINED_COLUMN(PostgreSQLState.UNDEFINED_COLUMN, "Column \"%s\" of table \"%s\" does not exist"),
    
    DATA_SOURCE_REJECTED_CONNECTION_ATTEMPT(XOpenSQLState.DATA_SOURCE_REJECTED_CONNECTION_ATTEMPT, "server rejected establishment of sql connection"),
    
    TRANSACTION_STATE_INVALID(XOpenSQLState.INVALID_TRANSACTION_STATE, "There is already a transaction in progress"),
    
    WRONG_VALUE_COUNT_ON_ROW(PostgreSQLState.SYNTAX_ERROR, "Column count doesn't match value count at row %d"),
    
    INVALID_PARAMETER_VALUE(XOpenSQLState.INVALID_PARAMETER_VALUE, "invalid value for parameter \"%s\": \"%s\""),
    
    SYSTEM_ERROR(PostgreSQLState.SYSTEM_ERROR, "system_error");
    
    private final SQLState sqlState;
    
    private final String reason;
    
    @Override
    public int getVendorCode() {
        return 0;
    }
}
