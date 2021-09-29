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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.db.protocol.error.SQLErrorCode;

/**
 * Common dist sql error code.
 */
@RequiredArgsConstructor
@Getter
public enum CommonDistSQLErrorCode implements SQLErrorCode {
    
    UNSUPPORTED_VARIABLE(11002, "S11002", "Could not support variable [%s].");
    
    private final int errorCode;
    
    private final String sqlState;
    
    private final String errorMessage;
    
    /**
     * Value of common dist sql error code.
     * 
     * @param exception exception
     * @return common dist sql error code
     */
    public static CommonDistSQLErrorCode valueOf(final CommonDistSQLException exception) {
        if (exception instanceof UnsupportedVariableException) {
            return UNSUPPORTED_VARIABLE;
        }
        throw new UnsupportedOperationException("Cannot find common dist sql error code from exception: %s", exception);
    }
}
