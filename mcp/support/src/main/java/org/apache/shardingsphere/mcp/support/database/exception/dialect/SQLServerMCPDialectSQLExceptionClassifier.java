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
 * MCP SQL exception classifier for SQL Server.
 */
public final class SQLServerMCPDialectSQLExceptionClassifier implements MCPDialectSQLExceptionClassifier {
    
    private static final int INCORRECT_SYNTAX = 102;
    
    private static final int INCORRECT_SYNTAX_NEAR_KEYWORD = 156;
    
    private static final int INVALID_COLUMN_NAME = 207;
    
    private static final int INVALID_OBJECT_NAME = 208;
    
    private static final int PERMISSION_DENIED = 229;
    
    private static final int STORED_PROCEDURE_NOT_FOUND = 2812;
    
    private static final int CANNOT_OPEN_DATABASE = 4060;
    
    private static final int LOGIN_UNTRUSTED_DOMAIN = 18452;
    
    private static final int LOGIN_FAILED = 18456;
    
    private static final int DATABASE_ACCESS_DENIED = 916;
    
    @Override
    public Optional<MCPJDBCErrorCategory> classify(final SQLException cause) {
        return switch (cause.getErrorCode()) {
            case LOGIN_UNTRUSTED_DOMAIN, LOGIN_FAILED -> Optional.of(MCPJDBCErrorCategory.AUTHENTICATION);
            case PERMISSION_DENIED, DATABASE_ACCESS_DENIED -> Optional.of(MCPJDBCErrorCategory.AUTHORIZATION);
            case INVALID_COLUMN_NAME, INVALID_OBJECT_NAME, STORED_PROCEDURE_NOT_FOUND, CANNOT_OPEN_DATABASE -> Optional.of(MCPJDBCErrorCategory.OBJECT_NOT_VISIBLE);
            case INCORRECT_SYNTAX, INCORRECT_SYNTAX_NEAR_KEYWORD -> Optional.of(MCPJDBCErrorCategory.SYNTAX);
            default -> Optional.empty();
        };
    }
    
    @Override
    public String getType() {
        return "SQLServer";
    }
}
