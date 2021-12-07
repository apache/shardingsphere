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

package org.apache.shardingsphere.proxy.backend.text.database;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropDatabaseStatement;

import java.sql.SQLException;

/**
 * Database operate backend handler factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseOperateBackendHandlerFactory {
    
    /**
     * Create new instance of database operate backend handler.
     * 
     * @param sqlStatement SQL statement
     * @param connectionSession connection session
     * @return database operate backend handler
     * @throws SQLException SQL exception
     */
    public static TextProtocolBackendHandler newInstance(final SQLStatement sqlStatement, final ConnectionSession connectionSession) throws SQLException {
        return createBackendHandler(sqlStatement, connectionSession);
    }
    
    private static TextProtocolBackendHandler createBackendHandler(final SQLStatement sqlStatement, final ConnectionSession connectionSession) {
        if (sqlStatement instanceof CreateDatabaseStatement) {
            return new CreateDatabaseBackendHandler((CreateDatabaseStatement) sqlStatement);
        }
        if (sqlStatement instanceof DropDatabaseStatement) {
            return new DropDatabaseBackendHandler((DropDatabaseStatement) sqlStatement, connectionSession);
        }
        throw new UnsupportedOperationException(sqlStatement.getClass().getCanonicalName());
    }
}
