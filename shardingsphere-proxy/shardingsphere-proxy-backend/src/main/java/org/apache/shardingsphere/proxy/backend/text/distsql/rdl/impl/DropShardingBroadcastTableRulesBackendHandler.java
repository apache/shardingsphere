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

import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingBroadcastTableRulesStatement;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.exception.ShardingBroadcastTableRulesNotExistsException;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;

import java.util.Optional;

/**
 * Drop sharding broadcast table rules backend handler.
 */
public final class DropShardingBroadcastTableRulesBackendHandler extends RDLBackendHandler<DropShardingBroadcastTableRulesStatement> {
    
    public DropShardingBroadcastTableRulesBackendHandler(final DropShardingBroadcastTableRulesStatement sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
    }
    
    @Override
    public void before(final String schemaName, final DropShardingBroadcastTableRulesStatement sqlStatement) {
        Optional<ShardingRuleConfiguration> ruleConfig = findRuleConfiguration(schemaName, ShardingRuleConfiguration.class);
        if (!ruleConfig.isPresent() || ruleConfig.get().getBroadcastTables().isEmpty()) {
            throw new ShardingBroadcastTableRulesNotExistsException(schemaName);
        }
    }
    
    @Override
    public void doExecute(final String schemaName, final DropShardingBroadcastTableRulesStatement sqlStatement) {
        getRuleConfiguration(schemaName, ShardingRuleConfiguration.class).getBroadcastTables().clear();
    }
}
