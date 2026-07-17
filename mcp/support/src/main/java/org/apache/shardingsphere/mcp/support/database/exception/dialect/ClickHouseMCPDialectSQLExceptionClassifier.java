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
 * MCP SQL exception classifier for ClickHouse.
 */
public final class ClickHouseMCPDialectSQLExceptionClassifier implements MCPDialectSQLExceptionClassifier {
    
    private static final int UNKNOWN_IDENTIFIER = 47;
    
    private static final int NOT_IMPLEMENTED = 48;
    
    private static final int UNKNOWN_TABLE = 60;
    
    private static final int SYNTAX_ERROR = 62;
    
    private static final int UNKNOWN_DATABASE = 81;
    
    private static final int TIMEOUT_EXCEEDED = 159;
    
    private static final int DATABASE_ACCESS_DENIED = 291;
    
    private static final int ACCESS_DENIED = 497;
    
    private static final int AUTHENTICATION_FAILED = 516;
    
    @Override
    public Optional<MCPJDBCErrorCategory> classify(final SQLException cause) {
        return switch (cause.getErrorCode()) {
            case TIMEOUT_EXCEEDED -> Optional.of(MCPJDBCErrorCategory.TIMEOUT);
            case NOT_IMPLEMENTED -> Optional.of(MCPJDBCErrorCategory.FEATURE_NOT_SUPPORTED);
            case AUTHENTICATION_FAILED -> Optional.of(MCPJDBCErrorCategory.AUTHENTICATION);
            case DATABASE_ACCESS_DENIED, ACCESS_DENIED -> Optional.of(MCPJDBCErrorCategory.AUTHORIZATION);
            case UNKNOWN_IDENTIFIER, UNKNOWN_TABLE, UNKNOWN_DATABASE -> Optional.of(MCPJDBCErrorCategory.OBJECT_NOT_VISIBLE);
            case SYNTAX_ERROR -> Optional.of(MCPJDBCErrorCategory.SYNTAX);
            default -> Optional.empty();
        };
    }
    
    @Override
    public String getType() {
        return "ClickHouse";
    }
}
