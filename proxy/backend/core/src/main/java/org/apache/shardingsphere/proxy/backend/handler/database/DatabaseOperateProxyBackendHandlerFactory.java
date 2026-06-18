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

package org.apache.shardingsphere.proxy.backend.handler.database;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.database.type.CreateDatabaseProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.database.type.DropDatabaseProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.database.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.database.DropDatabaseStatement;

/**
 * Database operate proxy backend handler factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseOperateProxyBackendHandlerFactory {
    
    /**
     * Create new instance of database operate backend handler.
     *
     * @param sqlStatement SQL statement
     * @param connectionSession connection session
     * @return created instance
     */
    public static ProxyBackendHandler newInstance(final SQLStatement sqlStatement, final ConnectionSession connectionSession) {
        return createProxyBackendHandler(sqlStatement, connectionSession);
    }
    
    private static ProxyBackendHandler createProxyBackendHandler(final SQLStatement sqlStatement, final ConnectionSession connectionSession) {
        ContextManager contextManager = ProxyContext.getInstance().getContextManager();
        if (sqlStatement instanceof CreateDatabaseStatement) {
            return new CreateDatabaseProxyBackendHandler((CreateDatabaseStatement) sqlStatement, contextManager);
        }
        if (sqlStatement instanceof DropDatabaseStatement) {
            return new DropDatabaseProxyBackendHandler((DropDatabaseStatement) sqlStatement, contextManager, connectionSession);
        }
        throw new UnsupportedSQLOperationException(sqlStatement.getClass().getName());
    }
}
