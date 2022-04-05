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
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionDropUpdater;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.scaling.distsql.statement.DropShardingScalingRuleStatement;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;

/**
 * Drop sharding scaling rule statement updater.
 */
public final class DropShardingScalingRuleStatementUpdater implements RuleDefinitionDropUpdater<DropShardingScalingRuleStatement, ShardingRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereMetaData shardingSphereMetaData, final DropShardingScalingRuleStatement sqlStatement,
                                  final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        String schemaName = shardingSphereMetaData.getName();
        if (!isExistRuleConfig(currentRuleConfig) && sqlStatement.isContainsExistClause()) {
            return;
        }
        checkCurrentRuleConfiguration(schemaName, currentRuleConfig);
        checkStatement(schemaName, sqlStatement, currentRuleConfig);
    }
    
    private void checkCurrentRuleConfiguration(final String schemaName, final ShardingRuleConfiguration currentRuleConfig) throws RequiredRuleMissedException {
        if (null == currentRuleConfig) {
            throw new RequiredRuleMissedException("Sharding", schemaName);
        }
    }
    
    private void checkStatement(final String schemaName, final DropShardingScalingRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        checkExist(schemaName, sqlStatement, currentRuleConfig);
        // TODO checkNotInUse
    }
    
    private void checkExist(final String schemaName, final DropShardingScalingRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        if (sqlStatement.isContainsExistClause()) {
            return;
        }
        if (!currentRuleConfig.getScaling().containsKey(sqlStatement.getScalingName())) {
            throw new RequiredRuleMissedException("Scaling", schemaName, sqlStatement.getScalingName());
        }
    }
    
    @Override
    public boolean hasAnyOneToBeDropped(final DropShardingScalingRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        return isExistRuleConfig(currentRuleConfig) && currentRuleConfig.getScaling().containsKey(sqlStatement.getScalingName());
    }
    
    @Override
    public boolean updateCurrentRuleConfiguration(final DropShardingScalingRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        currentRuleConfig.getScaling().remove(sqlStatement.getScalingName());
        if (null != currentRuleConfig.getScalingName() && currentRuleConfig.getScalingName().equalsIgnoreCase(sqlStatement.getScalingName())) {
            currentRuleConfig.setScalingName(null);
        }
        return false;
    }
    
    @Override
    public Class<ShardingRuleConfiguration> getRuleConfigurationClass() {
        return ShardingRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return DropShardingScalingRuleStatement.class.getName();
    }
}
