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

package org.apache.shardingsphere.infra.util.exception.sql.vendor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.util.exception.sql.sqlstate.SQLState;
import org.apache.shardingsphere.infra.util.exception.sql.sqlstate.XOpenSQLState;

/**
 * ShardingSphere vendor error.
 */
@RequiredArgsConstructor
@Getter
public enum ShardingSphereVendorError implements VendorError {
    
    SCALING_JOB_NOT_EXIST(XOpenSQLState.GENERAL_ERROR, 1201, "Scaling job `%s` does not exist"),
    
    DATABASE_WRITE_LOCKED(XOpenSQLState.GENERAL_ERROR, 1300, "The database `%s` is read-only"),
    
    TABLE_LOCK_WAIT_TIMEOUT(XOpenSQLState.GENERAL_ERROR, 1301, "The table `%s` of schema `%s` lock wait timeout of %s ms exceeded"),
    
    TABLE_LOCKED(XOpenSQLState.GENERAL_ERROR, 1302, "The table `%s` of schema `%s` is locked"),
    
    UNKNOWN_EXCEPTION(XOpenSQLState.SYNTAX_ERROR, 1999, "Unknown exception: %s"),
    
    UNSUPPORTED_SQL(XOpenSQLState.SYNTAX_ERROR, 1235, "Unsupported SQL: %s");
    
    private final SQLState sqlState;
    
    private final int vendorCode;
    
    private final String reason;
}
