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

package org.apache.shardingsphere.sharding.distsql.handler.update;

import org.apache.shardingsphere.infra.distsql.update.RDLCreateUpdater;
import org.apache.shardingsphere.infra.exception.rule.RuleDefinitionViolationException;
import org.apache.shardingsphere.infra.exception.rule.RuleInUsedException;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.statement.CreateShardingBroadcastTableRulesStatement;

/**
 * Create sharding broadcast table rule statement updater.
 */
public final class CreateShardingBroadcastTableRuleStatementUpdater implements RDLCreateUpdater<CreateShardingBroadcastTableRulesStatement, ShardingRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final String schemaName, final CreateShardingBroadcastTableRulesStatement sqlStatement, 
                                  final ShardingRuleConfiguration currentRuleConfig, final ShardingSphereResource resource) throws RuleDefinitionViolationException {
    }
    
    @Override
    public ShardingRuleConfiguration buildToBeCreatedRuleConfiguration(final String schemaName, final CreateShardingBroadcastTableRulesStatement sqlStatement) {
        return new ShardingRuleConfiguration();
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final String schemaName, final CreateShardingBroadcastTableRulesStatement sqlStatement, 
                                               final ShardingRuleConfiguration currentRuleConfig, final ShardingRuleConfiguration toBeCreatedRuleConfig) throws RuleDefinitionViolationException {
        if (null == currentRuleConfig) {
            toBeCreatedRuleConfig.setBroadcastTables(sqlStatement.getTables());
        } else {
            if (!currentRuleConfig.getBroadcastTables().isEmpty()) {
                throw new RuleInUsedException("Broadcast", schemaName);
            }
            currentRuleConfig.getBroadcastTables().addAll(sqlStatement.getTables());
        }
    }
    
    @Override
    public Class<ShardingRuleConfiguration> getRuleConfigurationClass() {
        return ShardingRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return CreateShardingBroadcastTableRulesStatement.class.getCanonicalName();
    }
}
