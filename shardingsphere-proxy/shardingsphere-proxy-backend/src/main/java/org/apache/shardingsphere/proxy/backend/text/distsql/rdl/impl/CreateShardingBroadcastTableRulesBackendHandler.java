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

package org.apache.shardingsphere.proxy.backend.text.distsql.rdl.impl;

import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingBroadcastTableRulesStatement;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.ShardingBroadcastTableRulesExistsException;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;

import java.util.Optional;

/**
 * Create sharding broadcast table rule backend handler.
 */
public final class CreateShardingBroadcastTableRulesBackendHandler extends RDLBackendHandler<CreateShardingBroadcastTableRulesStatement> {
    
    public CreateShardingBroadcastTableRulesBackendHandler(final CreateShardingBroadcastTableRulesStatement sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
    }
    
    @Override
    public void before(final String schemaName, final CreateShardingBroadcastTableRulesStatement sqlStatement) {
    }
    
    @Override
    public void doExecute(final String schemaName, final CreateShardingBroadcastTableRulesStatement sqlStatement) {
        Optional<ShardingRuleConfiguration> shardingRuleConfig = getShardingRuleConfiguration(schemaName);
        if (!shardingRuleConfig.isPresent()) {
            ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfiguration();
            shardingRuleConfiguration.setBroadcastTables(sqlStatement.getTables());
            ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations().add(shardingRuleConfiguration);
        } else {
            if (!shardingRuleConfig.get().getBroadcastTables().isEmpty()) {
                throw new ShardingBroadcastTableRulesExistsException(schemaName);
            }
            shardingRuleConfig.get().getBroadcastTables().addAll(sqlStatement.getTables());
        }
    }
}
