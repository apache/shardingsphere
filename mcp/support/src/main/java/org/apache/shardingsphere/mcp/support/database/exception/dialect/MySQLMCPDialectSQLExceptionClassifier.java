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
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * MCP SQL exception classifier for MySQL protocol databases.
 */
public final class MySQLMCPDialectSQLExceptionClassifier implements MCPDialectSQLExceptionClassifier {
    
    private static final int ER_DBACCESS_DENIED_ERROR = 1044;
    
    private static final int ER_BAD_DB_ERROR = 1049;
    
    private static final int ER_PARSE_ERROR = 1064;
    
    private static final int ER_SYNTAX_ERROR = 1149;
    
    private static final int ER_SP_DOES_NOT_EXIST = 1305;
    
    @Override
    public Optional<MCPJDBCErrorCategory> classify(final SQLException cause) {
        if (!"42000".equals(cause.getSQLState())) {
            return Optional.empty();
        }
        switch (cause.getErrorCode()) {
            case ER_DBACCESS_DENIED_ERROR:
                return Optional.of(MCPJDBCErrorCategory.AUTHORIZATION);
            case ER_BAD_DB_ERROR:
            case ER_SP_DOES_NOT_EXIST:
                return Optional.of(MCPJDBCErrorCategory.OBJECT_NOT_VISIBLE);
            case ER_PARSE_ERROR:
            case ER_SYNTAX_ERROR:
                return Optional.of(MCPJDBCErrorCategory.SYNTAX);
            default:
                return Optional.of(MCPJDBCErrorCategory.QUERY_FAILED);
        }
    }
    
    @Override
    public String getType() {
        return "MySQL";
    }
    
    @Override
    public Collection<Object> getTypeAliases() {
        return List.of("MariaDB");
    }
}
