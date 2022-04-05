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

package org.apache.shardingsphere.scaling.distsql.handler.update;

import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RuleDisabledException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionAlterUpdater;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.scaling.distsql.statement.DisableShardingScalingRuleStatement;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;

/**
 * Disable sharding scaling rule statement updater.
 */
public final class DisableShardingScalingRuleStatementUpdater implements RuleDefinitionAlterUpdater<DisableShardingScalingRuleStatement, ShardingRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereMetaData shardingSphereMetaData, final DisableShardingScalingRuleStatement sqlStatement,
                                  final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        String schemaName = shardingSphereMetaData.getName();
        checkCurrentRuleConfiguration(schemaName, currentRuleConfig);
        checkExist(schemaName, sqlStatement, currentRuleConfig);
        checkDisabled(schemaName, sqlStatement, currentRuleConfig);
    }
    
    private void checkCurrentRuleConfiguration(final String schemaName, final ShardingRuleConfiguration currentRuleConfig) throws RequiredRuleMissedException {
        if (null == currentRuleConfig) {
            throw new RequiredRuleMissedException("Sharding", schemaName);
        }
    }
    
    private void checkExist(final String schemaName, final DisableShardingScalingRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        if (!currentRuleConfig.getScaling().containsKey(sqlStatement.getScalingName())) {
            throw new RequiredRuleMissedException("Scaling", schemaName, sqlStatement.getScalingName());
        }
    }
    
    private void checkDisabled(final String schemaName, final DisableShardingScalingRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        if (null == currentRuleConfig.getScalingName() || !currentRuleConfig.getScalingName().equals(sqlStatement.getScalingName())) {
            throw new RuleDisabledException("Scaling", schemaName, sqlStatement.getScalingName());
        }
    }
    
    @Override
    public ShardingRuleConfiguration buildToBeAlteredRuleConfiguration(final DisableShardingScalingRuleStatement sqlStatement) {
        return null;
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final ShardingRuleConfiguration currentRuleConfig, final ShardingRuleConfiguration toBeAlteredRuleConfig) {
        currentRuleConfig.setScalingName(null);
    }
    
    @Override
    public Class<ShardingRuleConfiguration> getRuleConfigurationClass() {
        return ShardingRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return DisableShardingScalingRuleStatement.class.getName();
    }
}
