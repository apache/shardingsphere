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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.rdl.AlterReadWriteSplittingRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.AlterShardingRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.AddResourceStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateReadWriteSplittingRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateShardingBindingTableRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateShardingTableRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.impl.DropReplicaQueryRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.impl.DropResourceStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.impl.DropShardingRuleStatement;
import org.apache.shardingsphere.infra.context.metadata.impl.StandardMetaDataContexts;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.rdl.impl.AddResourceBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.rdl.impl.AlterReadWriteSplittingRuleBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.rdl.impl.AlterShardingRuleBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.rdl.impl.CreateDatabaseBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.rdl.impl.CreateReadWriteSplittingRuleBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.rdl.impl.CreateShardingBindingTableRulesBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.rdl.impl.CreateShardingTableRuleBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.rdl.impl.DropDatabaseBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.rdl.impl.DropReadWriteSplittingRuleBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.rdl.impl.DropResourceBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.rdl.impl.DropShardingRuleBackendHandler;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropDatabaseStatement;

import java.sql.SQLException;
import java.util.Optional;

/**
 * RDL backend handler factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RDLBackendHandlerFactory {
    
    /**
     * Create new instance of RDL backend handler.
     * 
     * @param databaseType database type
     * @param sqlStatement SQL statement
     * @param backendConnection backend connection
     * @return RDL backend handler
     * @throws SQLException SQL exception
     */
    public static Optional<TextProtocolBackendHandler> newInstance(final DatabaseType databaseType, final SQLStatement sqlStatement, final BackendConnection backendConnection) throws SQLException {
        Optional<TextProtocolBackendHandler> result = createRDLBackendHandler(databaseType, sqlStatement, backendConnection);
        if (result.isPresent()) {
            checkRegistryCenterExisted(sqlStatement);
        }
        return result;
    }
    
    private static void checkRegistryCenterExisted(final SQLStatement sqlStatement) throws SQLException {
        if (ProxyContext.getInstance().getMetaDataContexts() instanceof StandardMetaDataContexts) {
            throw new SQLException(String.format("No Registry center to execute `%s` SQL", sqlStatement.getClass().getSimpleName()));
        }
    }
    
    private static Optional<TextProtocolBackendHandler> createRDLBackendHandler(final DatabaseType databaseType, final SQLStatement sqlStatement, final BackendConnection backendConnection) {
        if (sqlStatement instanceof AddResourceStatement) {
            return Optional.of(new AddResourceBackendHandler(databaseType, (AddResourceStatement) sqlStatement, backendConnection));
        }
        if (sqlStatement instanceof DropResourceStatement) {
            return Optional.of(new DropResourceBackendHandler((DropResourceStatement) sqlStatement, backendConnection));
        }
        if (sqlStatement instanceof CreateDatabaseStatement) {
            return Optional.of(new CreateDatabaseBackendHandler((CreateDatabaseStatement) sqlStatement));
        }
        if (sqlStatement instanceof CreateShardingTableRuleStatement) {
            return Optional.of(new CreateShardingTableRuleBackendHandler((CreateShardingTableRuleStatement) sqlStatement, backendConnection));
        }
        if (sqlStatement instanceof CreateShardingTableRuleStatement) {
            return Optional.of(new CreateShardingTableRuleBackendHandler((CreateShardingTableRuleStatement) sqlStatement, backendConnection));
        }
        if (sqlStatement instanceof CreateShardingBindingTableRulesStatement) {
            return Optional.of(new CreateShardingBindingTableRulesBackendHandler((CreateShardingBindingTableRulesStatement) sqlStatement, backendConnection));
        }
        if (sqlStatement instanceof AlterShardingRuleStatement) {
            return Optional.of(new AlterShardingRuleBackendHandler((AlterShardingRuleStatement) sqlStatement, backendConnection));
        }
        if (sqlStatement instanceof CreateReadWriteSplittingRuleStatement) {
            return Optional.of(new CreateReadWriteSplittingRuleBackendHandler((CreateReadWriteSplittingRuleStatement) sqlStatement, backendConnection));
        }
        if (sqlStatement instanceof AlterReadWriteSplittingRuleStatement) {
            return Optional.of(new AlterReadWriteSplittingRuleBackendHandler((AlterReadWriteSplittingRuleStatement) sqlStatement, backendConnection));
        }
        if (sqlStatement instanceof DropReplicaQueryRuleStatement) {
            return Optional.of(new DropReadWriteSplittingRuleBackendHandler((DropReplicaQueryRuleStatement) sqlStatement, backendConnection));
        }
        if (sqlStatement instanceof DropDatabaseStatement) {
            return Optional.of(new DropDatabaseBackendHandler((DropDatabaseStatement) sqlStatement));
        }
        if (sqlStatement instanceof DropShardingRuleStatement) {
            return Optional.of(new DropShardingRuleBackendHandler((DropShardingRuleStatement) sqlStatement, backendConnection));
        }
        return Optional.empty();
    }
}
