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
 * MCP SQL exception classifier for Hive.
 */
public final class HiveMCPDialectSQLExceptionClassifier implements MCPDialectSQLExceptionClassifier {
    
    private static final int INVALID_TABLE = 10001;
    
    private static final int INVALID_COLUMN = 10002;
    
    private static final int INVALID_TABLE_OR_COLUMN = 10004;
    
    private static final int AMBIGUOUS_COLUMN = 10007;
    
    private static final int INVALID_FUNCTION = 10011;
    
    private static final int DATABASE_NOT_EXISTS = 10072;
    
    private static final int ACCESS_DENIED = 20009;
    
    @Override
    public Optional<MCPJDBCErrorCategory> classify(final SQLException cause) {
        return switch (cause.getErrorCode()) {
            case INVALID_TABLE, INVALID_COLUMN, INVALID_TABLE_OR_COLUMN, INVALID_FUNCTION, DATABASE_NOT_EXISTS -> Optional.of(MCPJDBCErrorCategory.OBJECT_NOT_VISIBLE);
            case AMBIGUOUS_COLUMN -> Optional.of(MCPJDBCErrorCategory.QUERY_FAILED);
            case ACCESS_DENIED -> Optional.of(MCPJDBCErrorCategory.AUTHORIZATION);
            default -> Optional.empty();
        };
    }
    
    @Override
    public String getType() {
        return "Hive";
    }
}
