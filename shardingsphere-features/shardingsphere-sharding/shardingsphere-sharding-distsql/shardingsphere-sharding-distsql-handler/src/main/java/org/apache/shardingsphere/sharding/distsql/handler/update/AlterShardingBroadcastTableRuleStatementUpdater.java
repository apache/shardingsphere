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

import org.apache.shardingsphere.infra.distsql.update.RDLAlterUpdater;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RuleDefinitionViolationException;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterShardingBroadcastTableRulesStatement;

/**
 * Alter sharding broadcast table rule statement updater.
 */
public final class AlterShardingBroadcastTableRuleStatementUpdater implements RDLAlterUpdater<AlterShardingBroadcastTableRulesStatement, ShardingRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final String schemaName, final AlterShardingBroadcastTableRulesStatement sqlStatement, 
                                  final ShardingRuleConfiguration currentRuleConfig, final ShardingSphereResource resource) throws RuleDefinitionViolationException {
        checkCurrentRuleConfiguration(schemaName, currentRuleConfig);
    }
    
    private void checkCurrentRuleConfiguration(final String schemaName, final ShardingRuleConfiguration currentRuleConfig) throws RequiredRuleMissedException {
        if (null == currentRuleConfig) {
            throw new RequiredRuleMissedException("Sharding", schemaName);
        }
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final String schemaName, final AlterShardingBroadcastTableRulesStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        dropRuleConfiguration(currentRuleConfig);
        addRuleConfiguration(sqlStatement, currentRuleConfig);
    }
    
    private void dropRuleConfiguration(final ShardingRuleConfiguration currentRuleConfig) {
        currentRuleConfig.getBroadcastTables().clear();
    }
    
    private void addRuleConfiguration(final AlterShardingBroadcastTableRulesStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        currentRuleConfig.getBroadcastTables().addAll(sqlStatement.getTables());
    }
    
    @Override
    public Class<ShardingRuleConfiguration> getRuleConfigurationClass() {
        return ShardingRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return AlterShardingBroadcastTableRulesStatement.class.getCanonicalName();
    }
}
