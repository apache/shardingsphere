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
import org.apache.shardingsphere.infra.algorithm.core.exception.InUsedAlgorithmException;
import org.apache.shardingsphere.infra.algorithm.core.exception.UnregisteredAlgorithmException;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.external.sql.identifier.SQLExceptionIdentifier;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.statement.DropShardingAlgorithmStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

/**
 * Drop sharding algorithm executor.
 */
@DistSQLExecutorCurrentRuleRequired(ShardingRule.class)
@Setter
public final class DropShardingAlgorithmExecutor implements DatabaseRuleDropExecutor<DropShardingAlgorithmStatement, ShardingRule, ShardingRuleConfiguration> {
    
    private ShardingSphereDatabase database;
    
    private ShardingRule rule;
    
    @Override
    public void checkBeforeUpdate(final DropShardingAlgorithmStatement sqlStatement) {
        if (!sqlStatement.isIfExists()) {
            checkToBeDroppedShardingAlgorithms(sqlStatement);
        }
        if (null != rule) {
            checkShardingAlgorithmsInUsed(sqlStatement);
        }
    }
    
    private void checkToBeDroppedShardingAlgorithms(final DropShardingAlgorithmStatement sqlStatement) {
        Collection<String> currentShardingAlgorithms = getCurrentShardingAlgorithms();
        Collection<String> notExistedAlgorithms = sqlStatement.getNames().stream().filter(each -> !currentShardingAlgorithms.contains(each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkMustEmpty(notExistedAlgorithms, () -> new UnregisteredAlgorithmException("Sharding", notExistedAlgorithms, new SQLExceptionIdentifier(database.getName())));
    }
    
    private void checkShardingAlgorithmsInUsed(final DropShardingAlgorithmStatement sqlStatement) {
        Collection<String> allInUsed = getAllOfAlgorithmsInUsed();
        Collection<String> usedAlgorithms = sqlStatement.getNames().stream().filter(allInUsed::contains).collect(Collectors.toList());
        ShardingSpherePreconditions.checkMustEmpty(usedAlgorithms, () -> new InUsedAlgorithmException("Sharding", database.getName(), usedAlgorithms));
    }
    
    private Collection<String> getAllOfAlgorithmsInUsed() {
        Collection<String> result = new LinkedHashSet<>();
        rule.getConfiguration().getTables().forEach(each -> {
            if (null != each.getDatabaseShardingStrategy()) {
                result.add(each.getDatabaseShardingStrategy().getShardingAlgorithmName());
            }
            if (null != each.getTableShardingStrategy()) {
                result.add(each.getTableShardingStrategy().getShardingAlgorithmName());
            }
        });
        rule.getConfiguration().getAutoTables().stream().filter(each -> null != each.getShardingStrategy()).forEach(each -> result.add(each.getShardingStrategy().getShardingAlgorithmName()));
        ShardingStrategyConfiguration tableShardingStrategy = rule.getConfiguration().getDefaultTableShardingStrategy();
        if (null != tableShardingStrategy && !tableShardingStrategy.getShardingAlgorithmName().isEmpty()) {
            result.add(tableShardingStrategy.getShardingAlgorithmName());
        }
        ShardingStrategyConfiguration databaseShardingStrategy = rule.getConfiguration().getDefaultDatabaseShardingStrategy();
        if (null != databaseShardingStrategy && !databaseShardingStrategy.getShardingAlgorithmName().isEmpty()) {
            result.add(databaseShardingStrategy.getShardingAlgorithmName());
        }
        return result;
    }
    
    private Collection<String> getCurrentShardingAlgorithms() {
        return rule.getConfiguration().getShardingAlgorithms().keySet();
    }
    
    @Override
    public ShardingRuleConfiguration buildToBeDroppedRuleConfiguration(final DropShardingAlgorithmStatement sqlStatement) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        for (String each : sqlStatement.getNames()) {
            result.getShardingAlgorithms().put(each, rule.getConfiguration().getShardingAlgorithms().get(each));
        }
        return result;
    }
    
    @Override
    public boolean hasAnyOneToBeDropped(final DropShardingAlgorithmStatement sqlStatement) {
        return !Collections.disjoint(getCurrentShardingAlgorithms(), sqlStatement.getNames());
    }
    
    @Override
    public Class<ShardingRule> getRuleClass() {
        return ShardingRule.class;
    }
    
    @Override
    public Class<DropShardingAlgorithmStatement> getType() {
        return DropShardingAlgorithmStatement.class;
    }
}
