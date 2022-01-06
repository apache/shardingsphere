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

package org.apache.shardingsphere.scaling.distsql.handler;

import org.apache.shardingsphere.infra.config.rulealtered.OnRuleAlteredActionConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionCreateUpdater;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.scaling.distsql.statement.CreateShardingScalingStatement;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Create sharding scaling statement updater.
 */
public final class CreateShardingScalingStatementUpdater implements RuleDefinitionCreateUpdater<CreateShardingScalingStatement, ShardingRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereMetaData shardingSphereMetaData, final CreateShardingScalingStatement sqlStatement,
                                  final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        String schemaName = shardingSphereMetaData.getName();
        checkCurrentRuleConfiguration(schemaName, currentRuleConfig);
        checkDuplicate(schemaName, sqlStatement, currentRuleConfig);
    }

    private void checkCurrentRuleConfiguration(final String schemaName, final ShardingRuleConfiguration currentRuleConfig) throws RequiredRuleMissedException {
        if (null == currentRuleConfig) {
            throw new RequiredRuleMissedException("Sharding", schemaName);
        }
    }
    
    private void checkDuplicate(final String schemaName, final CreateShardingScalingStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        if (currentRuleConfig.getScaling().containsKey(sqlStatement.getScalingName())) {
            throw new DuplicateRuleException("Scaling", schemaName, Arrays.asList(sqlStatement.getScalingName()));
        }
    }
    
    @Override
    public ShardingRuleConfiguration buildToBeCreatedRuleConfiguration(final CreateShardingScalingStatement sqlStatement) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        Map<String, OnRuleAlteredActionConfiguration> scalingConfigurationMap = new HashMap<>(1, 1);
        scalingConfigurationMap.put(sqlStatement.getScalingName(), buildNullScalingConfiguration());
        result.setScaling(scalingConfigurationMap);
        return result;
    }
    
    private OnRuleAlteredActionConfiguration buildNullScalingConfiguration() {
        return null;
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final ShardingRuleConfiguration currentRuleConfig, final ShardingRuleConfiguration toBeCreatedRuleConfig) {
        currentRuleConfig.getScaling().putAll(toBeCreatedRuleConfig.getScaling());
        if (null == currentRuleConfig.getScalingName()) {
            currentRuleConfig.setScalingName(toBeCreatedRuleConfig.getScaling().keySet().iterator().next());
        }
    }
    
    @Override
    public Class<ShardingRuleConfiguration> getRuleConfigurationClass() {
        return ShardingRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return CreateShardingScalingStatement.class.getCanonicalName();
    }
}
