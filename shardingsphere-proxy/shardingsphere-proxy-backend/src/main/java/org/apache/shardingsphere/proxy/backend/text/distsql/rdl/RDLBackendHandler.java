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

package org.apache.shardingsphere.proxy.backend.text.distsql.rdl;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.AddResourceStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateShardingRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.impl.DropShardingRuleStatement;
import org.apache.shardingsphere.infra.context.metadata.impl.StandardMetaDataContexts;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.rdl.detail.AddResourceBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.rdl.detail.CreateDatabaseBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.rdl.detail.CreateShardingRuleBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.rdl.detail.DropDatabaseBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.rdl.detail.DropShardingRuleBackendHandler;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropDatabaseStatement;

import java.sql.SQLException;

/**
 * RDL backend handler.
 */
@RequiredArgsConstructor
public final class RDLBackendHandler implements TextProtocolBackendHandler {
    
    private final SQLStatement sqlStatement;
    
    private final BackendConnection backendConnection;
    
    @Override
    public ResponseHeader execute() throws SQLException {
        if (!isRegistryCenterExisted()) {
            throw new SQLException(String.format("No Registry center to execute `%s` SQL", sqlStatement.getClass().getSimpleName()));
        }
        return getResponseHeader(sqlStatement);
    }
    
    private boolean isRegistryCenterExisted() {
        return !(ProxyContext.getInstance().getMetaDataContexts() instanceof StandardMetaDataContexts);
    }
    
    private ResponseHeader getResponseHeader(final SQLStatement sqlStatement) {
        DatabaseType databaseType = ProxyContext.getInstance().getMetaDataContexts().getMetaData(backendConnection.getSchemaName()).getResource().getDatabaseType();
        if (sqlStatement instanceof AddResourceStatement) {
            return new AddResourceBackendHandler().execute(databaseType, backendConnection, (AddResourceStatement) sqlStatement);
        }
        if (sqlStatement instanceof CreateDatabaseStatement) {
            return new CreateDatabaseBackendHandler().execute(databaseType, backendConnection, (CreateDatabaseStatement) sqlStatement);
        }
        if (sqlStatement instanceof CreateShardingRuleStatement) {
            return new CreateShardingRuleBackendHandler().execute(databaseType, backendConnection, (CreateShardingRuleStatement) sqlStatement);
        }
        if (sqlStatement instanceof DropDatabaseStatement) {
            return new DropDatabaseBackendHandler().execute(databaseType, backendConnection, (DropDatabaseStatement) sqlStatement);
        }
        if (sqlStatement instanceof DropShardingRuleStatement) {
            new DropShardingRuleBackendHandler().execute(databaseType, backendConnection, (DropShardingRuleStatement) sqlStatement);
        }
        throw new UnsupportedOperationException(sqlStatement.getClass().getName());
    }
}
