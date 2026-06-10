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

package org.apache.shardingsphere.database.exception.firebird.vendor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.exception.firebird.sqlstate.FirebirdState;
import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.SQLState;
import org.apache.shardingsphere.infra.exception.external.sql.sqlstate.XOpenSQLState;
import org.apache.shardingsphere.infra.exception.external.sql.vendor.VendorError;
import org.firebirdsql.gds.ISCConstants;

/**
 * Firebird vendor error.
 *
 * <p>Kernel {@code ShardingSphereSQLException} types, such as metadata column-not-found errors from the binder, are
 * converted before the Firebird SQL dialect mapper is reached. They need a separate Firebird remapping path instead of
 * enum entries here.</p>
 *
 * @see <a href="https://www.firebirdsql.org/file/documentation/chunk/en/refdocs/fblangref40/fblangref40-appx02-sqlcodes.html">SQLCODE and GDSCODE Error Codes</a>
 */
@RequiredArgsConstructor
@Getter
public enum FirebirdVendorError implements VendorError {
    
    UNAVAILABLE_DATABASE(FirebirdState.UNAVAILABLE_DATABASE, ISCConstants.isc_unavailable, "%s"),
    
    INVALID_BLOB_HANDLE(XOpenSQLState.SYNTAX_ERROR, ISCConstants.isc_bad_segstr_handle, ""),
    
    INVALID_BATCH_HANDLE(FirebirdState.INVALID_BATCH_HANDLE, ISCConstants.isc_bad_batch_handle, ""),
    
    BATCH_TOO_BIG(FirebirdState.BATCH_TOO_BIG, ISCConstants.isc_batch_too_big, ""),
    
    DYNAMIC_SQL_ERROR(XOpenSQLState.SYNTAX_ERROR, ISCConstants.isc_dsql_error, "%s"),
    
    TABLE_ALREADY_EXISTS(XOpenSQLState.DUPLICATE, ISCConstants.isc_dyn_dup_table, "%s"),
    
    LOGIN_FAILED(XOpenSQLState.INVALID_AUTHORIZATION_SPECIFICATION, ISCConstants.isc_login, ""),
    
    CHARSET_NOT_FOUND(FirebirdState.CHARSET_NOT_FOUND, ISCConstants.isc_charset_not_found, "CHARACTER SET %s is not defined"),
    
    INVALID_STATEMENT_HANDLE(FirebirdState.INVALID_STATEMENT_HANDLE, ISCConstants.isc_bad_stmt_handle, ""),
    
    INVALID_TRANSACTION_HANDLE(FirebirdState.INVALID_TRANSACTION_HANDLE, ISCConstants.isc_bad_trans_handle, ""),
    
    BATCH_ALREADY_OPENED(FirebirdState.BATCH_ALREADY_OPENED, ISCConstants.isc_batch_open, ""),
    
    INVALID_BATCH_PARAMETER_VERSION(XOpenSQLState.DATA_EXCEPTION, ISCConstants.isc_batch_param_version, "%s"),
    
    SQLDA_ERROR(FirebirdState.SQLDA_ERROR, ISCConstants.isc_dsql_sqlda_err, ""),
    
    INVALID_BLOB_ID(XOpenSQLState.SYNTAX_ERROR, ISCConstants.isc_bad_segstr_id, "");
    
    private final SQLState sqlState;
    
    private final int vendorCode;
    
    private final String reason;
}
