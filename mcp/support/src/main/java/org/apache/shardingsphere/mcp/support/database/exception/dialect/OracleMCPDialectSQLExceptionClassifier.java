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
 * MCP SQL exception classifier for Oracle.
 */
public final class OracleMCPDialectSQLExceptionClassifier implements MCPDialectSQLExceptionClassifier {
    
    private static final int INVALID_IDENTIFIER = 904;
    
    private static final int TABLE_OR_VIEW_DOES_NOT_EXIST = 942;
    
    private static final int INVALID_USERNAME_OR_PASSWORD = 1017;
    
    private static final int INSUFFICIENT_PRIVILEGES = 1031;
    
    private static final int OBJECT_DOES_NOT_EXIST = 4043;
    
    @Override
    public Optional<MCPJDBCErrorCategory> classify(final SQLException cause) {
        return switch (cause.getErrorCode()) {
            case INVALID_USERNAME_OR_PASSWORD -> Optional.of(MCPJDBCErrorCategory.AUTHENTICATION);
            case INSUFFICIENT_PRIVILEGES -> Optional.of(MCPJDBCErrorCategory.AUTHORIZATION);
            case INVALID_IDENTIFIER, TABLE_OR_VIEW_DOES_NOT_EXIST, OBJECT_DOES_NOT_EXIST -> Optional.of(MCPJDBCErrorCategory.OBJECT_NOT_VISIBLE);
            default -> Optional.empty();
        };
    }
    
    @Override
    public String getType() {
        return "Oracle";
    }
}
