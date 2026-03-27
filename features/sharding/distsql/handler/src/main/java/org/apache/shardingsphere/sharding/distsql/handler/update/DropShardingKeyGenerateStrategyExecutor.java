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
import org.apache.shardingsphere.sharding.distsql.statement.DropShardingKeyGenerateStrategyStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Drop sharding key generate strategy executor.
 */
@DistSQLExecutorCurrentRuleRequired(ShardingRule.class)
@Setter
public final class DropShardingKeyGenerateStrategyExecutor
        implements
            DatabaseRuleDropExecutor<DropShardingKeyGenerateStrategyStatement, ShardingRule, ShardingRuleConfiguration> {
    
    private ShardingSphereDatabase database;
    
    private ShardingRule rule;
    
    @Override
    public void checkBeforeUpdate(final DropShardingKeyGenerateStrategyStatement sqlStatement) {
        if (!sqlStatement.isIfExists()) {
            Collection<String> notExistedRuleNames = sqlStatement.getNames().stream()
                    .filter(each -> !rule.getConfiguration().getKeyGenerateStrategies().containsKey(each)).collect(Collectors.toList());
            ShardingSpherePreconditions.checkMustEmpty(notExistedRuleNames,
                    () -> new MissingRequiredRuleException("Sharding key generate strategy", database.getName(), notExistedRuleNames));
        }
    }
    
    @Override
    public ShardingRuleConfiguration buildToBeDroppedRuleConfiguration(final DropShardingKeyGenerateStrategyStatement sqlStatement) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        for (String each : sqlStatement.getNames()) {
            if (rule.getConfiguration().getKeyGenerateStrategies().containsKey(each)) {
                result.getKeyGenerateStrategies().put(each, rule.getConfiguration().getKeyGenerateStrategies().get(each));
            }
        }
        return result;
    }
    
    @Override
    public boolean hasAnyOneToBeDropped(final DropShardingKeyGenerateStrategyStatement sqlStatement) {
        return !Collections.disjoint(rule.getConfiguration().getKeyGenerateStrategies().keySet(), sqlStatement.getNames());
    }
    
    @Override
    public Class<ShardingRule> getRuleClass() {
        return ShardingRule.class;
    }
    
    @Override
    public Class<DropShardingKeyGenerateStrategyStatement> getType() {
        return DropShardingKeyGenerateStrategyStatement.class;
    }
}
