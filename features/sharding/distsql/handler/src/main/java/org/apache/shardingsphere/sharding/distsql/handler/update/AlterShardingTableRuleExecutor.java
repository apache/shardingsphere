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
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.type.DatabaseRuleAlterExecutor;
import org.apache.shardingsphere.distsql.handler.required.DistSQLExecutorCurrentRuleRequired;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.checker.ShardingTableRuleStatementChecker;
import org.apache.shardingsphere.sharding.distsql.handler.converter.ShardingTableRuleStatementConverter;
import org.apache.shardingsphere.sharding.distsql.statement.AlterShardingTableRuleStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Alter sharding table rule executor.
 */
@DistSQLExecutorCurrentRuleRequired(ShardingRule.class)
@Setter
public final class AlterShardingTableRuleExecutor implements DatabaseRuleAlterExecutor<AlterShardingTableRuleStatement, ShardingRule, ShardingRuleConfiguration> {
    
    private ShardingSphereDatabase database;
    
    private ShardingRule rule;
    
    @Override
    public void checkBeforeUpdate(final AlterShardingTableRuleStatement sqlStatement) {
        ShardingTableRuleStatementChecker.checkAlteration(database, sqlStatement.getRules(), rule.getConfiguration());
        checkUniqueActualDataNodes(sqlStatement);
    }
    
    private void checkUniqueActualDataNodes(final AlterShardingTableRuleStatement sqlStatement) {
        rule.getShardingRuleChecker().checkToBeAddedDataNodes(ShardingTableRuleStatementConverter.convertDataNodes(sqlStatement.getRules()), true);
    }
    
    @Override
    public ShardingRuleConfiguration buildToBeAlteredRuleConfiguration(final AlterShardingTableRuleStatement sqlStatement) {
        return ShardingTableRuleStatementConverter.convert(sqlStatement.getRules());
    }
    
    @Override
    public ShardingRuleConfiguration buildToBeDroppedRuleConfiguration(final ShardingRuleConfiguration toBeAlteredRuleConfig) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        Collection<String> toBeAlteredShardingTableNames = toBeAlteredRuleConfig.getTables().stream().map(ShardingTableRuleConfiguration::getLogicTable).collect(Collectors.toSet());
        for (ShardingAutoTableRuleConfiguration each : rule.getConfiguration().getAutoTables()) {
            if (toBeAlteredShardingTableNames.contains(each.getLogicTable())) {
                result.getAutoTables().add(each);
            }
        }
        Collection<String> toBeAlteredAutoTableNames = toBeAlteredRuleConfig.getAutoTables().stream().map(ShardingAutoTableRuleConfiguration::getLogicTable).collect(Collectors.toSet());
        for (ShardingTableRuleConfiguration each : rule.getConfiguration().getTables()) {
            if (toBeAlteredAutoTableNames.contains(each.getLogicTable())) {
                result.getTables().add(each);
            }
        }
        UnusedAlgorithmFinder.findUnusedShardingAlgorithm(rule.getConfiguration()).forEach(each -> result.getShardingAlgorithms().put(each, rule.getConfiguration().getShardingAlgorithms().get(each)));
        UnusedAlgorithmFinder.findUnusedKeyGenerator(rule.getConfiguration()).forEach(each -> result.getKeyGenerators().put(each, rule.getConfiguration().getKeyGenerators().get(each)));
        UnusedAlgorithmFinder.findUnusedAuditor(rule.getConfiguration()).forEach(each -> result.getAuditors().put(each, rule.getConfiguration().getAuditors().get(each)));
        return result;
    }
    
    @Override
    public Class<ShardingRule> getRuleClass() {
        return ShardingRule.class;
    }
    
    @Override
    public Class<AlterShardingTableRuleStatement> getType() {
        return AlterShardingTableRuleStatement.class;
    }
}
