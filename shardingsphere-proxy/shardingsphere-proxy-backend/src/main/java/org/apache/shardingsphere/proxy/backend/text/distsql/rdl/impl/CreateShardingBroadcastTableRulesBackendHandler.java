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

import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingBroadcastTableRulesStatement;

/**
 * Create sharding broadcast table rule backend handler.
 */
public final class CreateShardingBroadcastTableRulesBackendHandler extends RDLBackendHandler<CreateShardingBroadcastTableRulesStatement, ShardingRuleConfiguration> {
    
    public CreateShardingBroadcastTableRulesBackendHandler(final CreateShardingBroadcastTableRulesStatement sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
    }
    
    @Override
    public void checkSQLStatement(final String schemaName, final CreateShardingBroadcastTableRulesStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final String schemaName, final CreateShardingBroadcastTableRulesStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
    }
}
