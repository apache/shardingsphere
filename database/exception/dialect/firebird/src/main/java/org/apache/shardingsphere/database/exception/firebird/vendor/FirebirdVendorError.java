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
 * <p>The vendor code carries the native Firebird GDSCODE, which the client (Jaybird) uses to look up both the message
 * template and the SQL state from its own catalog. The status vector transmits the GDSCODE plus a single string
 * argument, so the reason here holds only that argument. With Jaybird's current message rendering a {@code {0}}
 * placeholder is filled by the argument and a template without placeholders is followed by the argument; an empty reason
 * sends no argument, leaving the catalog message as-is. GDSCODEs whose templates require multiple arguments are avoided
 * until the status vector can carry them.</p>
 *
 * @see <a href="https://www.firebirdsql.org/file/documentation/chunk/en/refdocs/fblangref40/fblangref40-appx02-sqlcodes.html">SQLCODE and GDSCODE Error Codes</a>
 */
@RequiredArgsConstructor
@Getter
public enum FirebirdVendorError implements VendorError {
    
    UNAVAILABLE_DATABASE(FirebirdState.UNAVAILABLE_DATABASE, ISCConstants.isc_unavailable, "%s"),
    
    INVALID_BATCH_HANDLE(FirebirdState.INVALID_BATCH_HANDLE, ISCConstants.isc_bad_batch_handle, ""),
    
    BATCH_TOO_BIG(FirebirdState.BATCH_TOO_BIG, ISCConstants.isc_batch_too_big, ""),
    
    TABLE_EXISTS(XOpenSQLState.DUPLICATE, ISCConstants.isc_dyn_dup_table, "%s"),
    
    LOGIN_FAILED(XOpenSQLState.INVALID_AUTHORIZATION_SPECIFICATION, ISCConstants.isc_login, "");
    
    private final SQLState sqlState;
    
    private final int vendorCode;
    
    private final String reason;
}
