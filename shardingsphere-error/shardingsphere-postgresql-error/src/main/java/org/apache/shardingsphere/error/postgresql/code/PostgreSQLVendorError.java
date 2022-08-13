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

package org.apache.shardingsphere.error.postgresql.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.error.postgresql.sqlstate.PostgreSQLState;
import org.apache.shardingsphere.error.sqlstate.SQLState;
import org.apache.shardingsphere.error.sqlstate.XOpenSQLState;
import org.apache.shardingsphere.error.vendor.VendorError;

/**
 * PostgreSQL vendor error.
 *
 * @see <a href="https://www.postgresql.org/docs/12/errcodes-appendix.html">Appendix A. PostgreSQL Error Codes</a>
 */
@RequiredArgsConstructor
@Getter
public enum PostgreSQLVendorError implements VendorError {
    
    SUCCESSFUL_COMPLETION(XOpenSQLState.SUCCESSFUL_COMPLETION, "successful_completion"),
    
    PRIVILEGE_NOT_GRANTED(XOpenSQLState.PRIVILEGE_NOT_GRANTED, "privilege_not_granted"),
    
    PROTOCOL_VIOLATION(PostgreSQLState.PROTOCOL_VIOLATION, "protocol_violation"),
    
    FEATURE_NOT_SUPPORTED(XOpenSQLState.FEATURE_NOT_SUPPORTED, "feature_not_supported"),
    
    DUPLICATE_DATABASE(PostgreSQLState.DUPLICATE_DATABASE, "Database '%s' already exists"),
    
    INVALID_AUTHORIZATION_SPECIFICATION(XOpenSQLState.INVALID_AUTHORIZATION_SPECIFICATION, "invalid_authorization_specification"),
    
    INVALID_PASSWORD(PostgreSQLState.INVALID_PASSWORD, "invalid_password"),
    
    INVALID_CATALOG_NAME(XOpenSQLState.INVALID_CATALOG_NAME, "invalid_catalog_name"),
    
    UNDEFINED_COLUMN(PostgreSQLState.UNDEFINED_COLUMN, "undefined_column"),
    
    TOO_MANY_CONNECTIONS(PostgreSQLState.TOO_MANY_CONNECTIONS, "too_many_connections"),
    
    SYSTEM_ERROR(PostgreSQLState.SYSTEM_ERROR, "system_error");
    
    private final SQLState sqlState;
    
    private final String reason;
    
    @Override
    public int getVendorCode() {
        return 0;
    }
}
