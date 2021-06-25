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

package org.apache.shardingsphere.proxy.backend.text.distsql.rdl.impl.updater;

import org.apache.shardingsphere.infra.distsql.RDLUpdater;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.ShardingBroadcastTableRuleExistedException;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingBroadcastTableRulesStatement;

/**
 * Create sharding broadcast table rule statement updater.
 */
public final class CreateShardingBroadcastTableRuleStatementUpdater implements RDLUpdater<CreateShardingBroadcastTableRulesStatement, ShardingRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final String schemaName, final CreateShardingBroadcastTableRulesStatement sqlStatement, 
                                  final ShardingRuleConfiguration currentRuleConfig, final ShardingSphereResource resource) {
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final String schemaName, final CreateShardingBroadcastTableRulesStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        if (null == currentRuleConfig) {
            ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfiguration();
            shardingRuleConfiguration.setBroadcastTables(sqlStatement.getTables());
            ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations().add(shardingRuleConfiguration);
        } else {
            if (!currentRuleConfig.getBroadcastTables().isEmpty()) {
                throw new ShardingBroadcastTableRuleExistedException(schemaName);
            }
            currentRuleConfig.getBroadcastTables().addAll(sqlStatement.getTables());
        }
    }
    
    @Override
    public String getType() {
        return CreateShardingBroadcastTableRulesStatement.class.getCanonicalName();
    }
}
