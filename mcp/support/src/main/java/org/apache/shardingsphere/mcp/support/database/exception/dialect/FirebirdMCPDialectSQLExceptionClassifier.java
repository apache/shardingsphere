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

package org.apache.shardingsphere.mcp.support.database.exception.dialect;

import org.apache.shardingsphere.mcp.support.database.exception.MCPJDBCErrorCategory;
import org.apache.shardingsphere.mcp.support.database.spi.MCPDialectSQLExceptionClassifier;

import java.sql.SQLException;
import java.util.Optional;

/**
 * MCP SQL exception classifier for Firebird.
 */
public final class FirebirdMCPDialectSQLExceptionClassifier implements MCPDialectSQLExceptionClassifier {
    
    private static final int NO_PERMISSION = 335544352;
    
    private static final int LOGIN = 335544472;
    
    private static final int LOGIN_ERROR = 335545106;
    
    private static final int COLUMN_UNKNOWN = 335544578;
    
    private static final int TABLE_UNKNOWN = 335544580;
    
    private static final int PROCEDURE_UNKNOWN = 335544581;
    
    private static final int TOKEN_UNKNOWN = 335544634;
    
    @Override
    public Optional<MCPJDBCErrorCategory> classify(final SQLException cause) {
        switch (cause.getErrorCode()) {
            case NO_PERMISSION:
                return Optional.of(MCPJDBCErrorCategory.AUTHORIZATION);
            case LOGIN:
            case LOGIN_ERROR:
                return Optional.of(MCPJDBCErrorCategory.AUTHENTICATION);
            case COLUMN_UNKNOWN:
            case TABLE_UNKNOWN:
            case PROCEDURE_UNKNOWN:
                return Optional.of(MCPJDBCErrorCategory.OBJECT_NOT_VISIBLE);
            case TOKEN_UNKNOWN:
                return Optional.of(MCPJDBCErrorCategory.SYNTAX);
            default:
                return "42000".equals(cause.getSQLState()) ? Optional.of(MCPJDBCErrorCategory.QUERY_FAILED) : Optional.empty();
        }
    }
    
    @Override
    public String getType() {
        return "Firebird";
    }
}
