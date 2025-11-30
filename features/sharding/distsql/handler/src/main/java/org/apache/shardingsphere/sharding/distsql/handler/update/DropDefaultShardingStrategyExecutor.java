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

import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.type.DatabaseRuleDropExecutor;
import org.apache.shardingsphere.distsql.handler.required.DistSQLExecutorCurrentRuleRequired;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.enums.ShardingStrategyLevelType;
import org.apache.shardingsphere.sharding.distsql.statement.DropDefaultShardingStrategyStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Optional;

/**
 * Drop default sharding strategy executor.
 */
@DistSQLExecutorCurrentRuleRequired(ShardingRule.class)
@Setter
public final class DropDefaultShardingStrategyExecutor implements DatabaseRuleDropExecutor<DropDefaultShardingStrategyStatement, ShardingRule, ShardingRuleConfiguration> {
    
    private ShardingSphereDatabase database;
    
    private ShardingRule rule;
    
    @Override
    public void checkBeforeUpdate(final DropDefaultShardingStrategyStatement sqlStatement) {
        if (!sqlStatement.isIfExists()) {
            checkExist(sqlStatement);
        }
    }
    
    private void checkExist(final DropDefaultShardingStrategyStatement sqlStatement) {
        Optional<ShardingStrategyConfiguration> shardingStrategyConfig = getStrategyConfiguration(sqlStatement.getDefaultType());
        ShardingSpherePreconditions.checkState(shardingStrategyConfig.isPresent(),
                () -> new MissingRequiredRuleException(String.format("Default sharding %s strategy", sqlStatement.getDefaultType().toLowerCase()), database.getName()));
    }
    
    private Optional<ShardingStrategyConfiguration> getStrategyConfiguration(final String type) {
        ShardingStrategyConfiguration result = type.equalsIgnoreCase(ShardingStrategyLevelType.TABLE.name())
                ? rule.getConfiguration().getDefaultTableShardingStrategy()
                : rule.getConfiguration().getDefaultDatabaseShardingStrategy();
        return Optional.ofNullable(result);
    }
    
    @Override
    public boolean hasAnyOneToBeDropped(final DropDefaultShardingStrategyStatement sqlStatement) {
        return sqlStatement.getDefaultType().equalsIgnoreCase(ShardingStrategyLevelType.TABLE.name())
                ? null != rule.getConfiguration().getDefaultTableShardingStrategy()
                : null != rule.getConfiguration().getDefaultDatabaseShardingStrategy();
    }
    
    @Override
    public ShardingRuleConfiguration buildToBeDroppedRuleConfiguration(final DropDefaultShardingStrategyStatement sqlStatement) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        if (sqlStatement.getDefaultType().equalsIgnoreCase(ShardingStrategyLevelType.TABLE.name())) {
            result.setDefaultTableShardingStrategy(rule.getConfiguration().getDefaultTableShardingStrategy());
            rule.getConfiguration().setDefaultTableShardingStrategy(null);
        } else {
            result.setDefaultDatabaseShardingStrategy(rule.getConfiguration().getDefaultDatabaseShardingStrategy());
            rule.getConfiguration().setDefaultDatabaseShardingStrategy(null);
        }
        UnusedAlgorithmFinder.findUnusedShardingAlgorithm(rule.getConfiguration()).forEach(each -> result.getShardingAlgorithms().put(each, rule.getConfiguration().getShardingAlgorithms().get(each)));
        return result;
    }
    
    @Override
    public Class<ShardingRule> getRuleClass() {
        return ShardingRule.class;
    }
    
    @Override
    public Class<DropDefaultShardingStrategyStatement> getType() {
        return DropDefaultShardingStrategyStatement.class;
    }
}
