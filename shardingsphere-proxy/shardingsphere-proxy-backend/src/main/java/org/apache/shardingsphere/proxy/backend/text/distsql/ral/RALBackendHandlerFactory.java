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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.ral.AdvancedDistSQLStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.CommonDistSQLStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.QueryableRALStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.RALStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.UpdatableRALStatement;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.advanced.AdvancedDistSQLBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.query.QueryableRALBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.CommonDistSQLBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.update.UpdatableRALBackendHandlerFactory;

import java.sql.SQLException;

/**
 * RAL backend handler factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RALBackendHandlerFactory {
    
    /**
     * Create new instance of RAL backend handler.
     *
     * @param databaseType database type
     * @param sqlStatement RAL statement
     * @param backendConnection backend connection
     * @return RAL backend handler
     * @throws SQLException SQL exception
     */
    public static TextProtocolBackendHandler newInstance(final DatabaseType databaseType, final RALStatement sqlStatement, final BackendConnection backendConnection) throws SQLException {
        if (sqlStatement instanceof QueryableRALStatement) {
            return QueryableRALBackendHandlerFactory.newInstance((QueryableRALStatement) sqlStatement, backendConnection);
        }
        if (sqlStatement instanceof UpdatableRALStatement) {
            return UpdatableRALBackendHandlerFactory.newInstance((UpdatableRALStatement) sqlStatement);
        }
        if (sqlStatement instanceof CommonDistSQLStatement) {
            return CommonDistSQLBackendHandlerFactory.newInstance((CommonDistSQLStatement) sqlStatement, backendConnection);
        }
        if (sqlStatement instanceof AdvancedDistSQLStatement) {
            return AdvancedDistSQLBackendHandlerFactory.newInstance(databaseType, (AdvancedDistSQLStatement) sqlStatement, backendConnection);
        }
        throw new UnsupportedOperationException(sqlStatement.getClass().getCanonicalName());
    }
}
