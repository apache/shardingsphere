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

import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingBindingTableRulesStatement;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.exception.ShardingBindingTableRuleNotExistsException;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;

import java.util.Optional;

/**
 * Drop sharding binding table rules backend handler.
 */
public final class DropShardingBindingTableRulesBackendHandler extends RDLBackendHandler<DropShardingBindingTableRulesStatement> {
    
    public DropShardingBindingTableRulesBackendHandler(final DropShardingBindingTableRulesStatement sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
    }
    
    @Override
    public void check(final String schemaName, final DropShardingBindingTableRulesStatement sqlStatement) {
        Optional<ShardingRuleConfiguration> ruleConfig = findCurrentRuleConfiguration(schemaName, ShardingRuleConfiguration.class);
        if (!ruleConfig.isPresent() || ruleConfig.get().getBindingTableGroups().isEmpty()) {
            throw new ShardingBindingTableRuleNotExistsException(schemaName);
        }
    }
    
    @Override
    public void doExecute(final String schemaName, final DropShardingBindingTableRulesStatement sqlStatement) {
        getCurrentRuleConfiguration(schemaName, ShardingRuleConfiguration.class).getBindingTableGroups().clear();
    }
}
