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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.util.exception.sql.vendor.VendorError;
import org.apache.shardingsphere.infra.util.exception.sql.sqlstate.SQLState;
import org.apache.shardingsphere.infra.util.exception.sql.sqlstate.XOpenSQLState;

/**
 * Dist SQL vendor error.
 */
@RequiredArgsConstructor
@Getter
public enum DistSQLVendorError implements VendorError {
    
    UNSUPPORTED_VARIABLE(XOpenSQLState.GENERAL_ERROR, 11001, "Could not support variable `%s`"),
    
    INVALID_VALUE(XOpenSQLState.INVALID_DATA_TYPE, 11002, "Invalid value `%s`");
    
    private final SQLState sqlState;
    
    private final int vendorCode;
    
    private final String reason;
    
    /**
     * Value of dist SQL error code.
     * 
     * @param distSQLException dist SQL exception
     * @return dist SQL error code
     */
    public static DistSQLVendorError valueOf(final DistSQLException distSQLException) {
        if (distSQLException instanceof UnsupportedVariableException) {
            return UNSUPPORTED_VARIABLE;
        }
        if (distSQLException instanceof InvalidValueException) {
            return INVALID_VALUE;
        }
        throw new UnsupportedOperationException("Can not find DistSQL error code from exception: %s", distSQLException);
    }
}
