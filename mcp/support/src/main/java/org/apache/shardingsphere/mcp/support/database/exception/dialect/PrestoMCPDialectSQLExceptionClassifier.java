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
 * MCP SQL exception classifier for Presto.
 */
public final class PrestoMCPDialectSQLExceptionClassifier implements MCPDialectSQLExceptionClassifier {
    
    private static final int SYNTAX_ERROR = 1;
    
    private static final int PERMISSION_DENIED = 4;
    
    private static final int NOT_FOUND = 5;
    
    private static final int FUNCTION_NOT_FOUND = 6;
    
    private static final int NOT_SUPPORTED = 13;
    
    private static final int PROCEDURE_NOT_FOUND = 29;
    
    private static final int VIEW_NOT_FOUND = 48;
    
    private static final int COLUMN_NOT_FOUND = 50;
    
    private static final int EXCEEDED_TIME_LIMIT = 131075;
    
    @Override
    public Optional<MCPJDBCErrorCategory> classify(final SQLException cause) {
        return switch (cause.getErrorCode()) {
            case SYNTAX_ERROR -> Optional.of(MCPJDBCErrorCategory.SYNTAX);
            case PERMISSION_DENIED -> Optional.of(MCPJDBCErrorCategory.AUTHORIZATION);
            case NOT_FOUND, FUNCTION_NOT_FOUND, PROCEDURE_NOT_FOUND, VIEW_NOT_FOUND, COLUMN_NOT_FOUND -> Optional.of(MCPJDBCErrorCategory.OBJECT_NOT_VISIBLE);
            case NOT_SUPPORTED -> Optional.of(MCPJDBCErrorCategory.FEATURE_NOT_SUPPORTED);
            case EXCEEDED_TIME_LIMIT -> Optional.of(MCPJDBCErrorCategory.TIMEOUT);
            default -> Optional.empty();
        };
    }
    
    @Override
    public String getType() {
        return "Presto";
    }
}
