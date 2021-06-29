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
import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.AlterDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.CreateDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.DropDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.AddResourceStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.DropResourceStatement;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.distsql.parser.statement.AlterEncryptRuleStatement;
import org.apache.shardingsphere.encrypt.distsql.parser.statement.CreateEncryptRuleStatement;
import org.apache.shardingsphere.encrypt.distsql.parser.statement.DropEncryptRuleStatement;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.context.metadata.impl.StandardMetaDataContexts;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.rdl.impl.AddResourceBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.rdl.impl.CreateDatabaseBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.rdl.impl.DropDatabaseBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.rdl.impl.DropResourceBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.rdl.impl.RDLBackendHandler;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.AlterReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.CreateReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.DropReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterShardingBindingTableRulesStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterShardingBroadcastTableRulesStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterShardingTableRuleStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingBindingTableRulesStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingBroadcastTableRulesStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingTableRuleStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingBindingTableRulesStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingBroadcastTableRulesStatement;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingTableRuleStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropDatabaseStatement;

import java.sql.SQLException;
import java.util.Optional;

/**
 * RDL backend handler factory.
 */
@SuppressWarnings("unchecked")
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
        if (sqlStatement instanceof DropDatabaseStatement) {
            return Optional.of(new DropDatabaseBackendHandler((DropDatabaseStatement) sqlStatement, backendConnection));
        }
        return Optional.of(new RDLBackendHandler<>(sqlStatement, backendConnection, getRuleConfigurationClass(sqlStatement)));
    }
    
    private static Class<? extends RuleConfiguration> getRuleConfigurationClass(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof CreateShardingTableRuleStatement || sqlStatement instanceof AlterShardingTableRuleStatement || sqlStatement instanceof DropShardingTableRuleStatement
                || sqlStatement instanceof CreateShardingBindingTableRulesStatement || sqlStatement instanceof AlterShardingBindingTableRulesStatement
                || sqlStatement instanceof DropShardingBindingTableRulesStatement
                || sqlStatement instanceof CreateShardingBroadcastTableRulesStatement || sqlStatement instanceof AlterShardingBroadcastTableRulesStatement
                || sqlStatement instanceof DropShardingBroadcastTableRulesStatement) {
            return ShardingRuleConfiguration.class;
        }
        if (sqlStatement instanceof CreateReadwriteSplittingRuleStatement
                || sqlStatement instanceof AlterReadwriteSplittingRuleStatement || sqlStatement instanceof DropReadwriteSplittingRuleStatement) {
            return ReadwriteSplittingRuleConfiguration.class;
        }
        if (sqlStatement instanceof CreateDatabaseDiscoveryRuleStatement
                || sqlStatement instanceof AlterDatabaseDiscoveryRuleStatement || sqlStatement instanceof DropDatabaseDiscoveryRuleStatement) {
            return DatabaseDiscoveryRuleConfiguration.class;
        }
        if (sqlStatement instanceof CreateEncryptRuleStatement || sqlStatement instanceof AlterEncryptRuleStatement || sqlStatement instanceof DropEncryptRuleStatement) {
            return EncryptRuleConfiguration.class;
        }
        throw new UnsupportedOperationException(sqlStatement.getClass().getCanonicalName());
    }
}
