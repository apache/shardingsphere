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

import org.apache.shardingsphere.distsql.handler.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.distsql.handler.exception.rule.InvalidRuleConfigurationException;
import org.apache.shardingsphere.distsql.handler.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.distsql.handler.update.RuleDefinitionAlterUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableReferenceRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.checker.ShardingTableRuleStatementChecker;
import org.apache.shardingsphere.sharding.distsql.parser.segment.table.TableReferenceRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterShardingTableReferenceRuleStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * Alter sharding table reference rule statement updater.
 */
public final class AlterShardingTableReferenceRuleStatementUpdater implements RuleDefinitionAlterUpdater<AlterShardingTableReferenceRuleStatement, ShardingRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database, final AlterShardingTableReferenceRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        String databaseName = database.getName();
        checkCurrentRuleConfiguration(databaseName, currentRuleConfig);
        checkToBeAlteredRulesExisted(databaseName, sqlStatement, currentRuleConfig);
        checkDuplicatedTablesInShardingTableReferenceRules(databaseName, sqlStatement, currentRuleConfig);
        checkToBeReferencedShardingTablesExisted(databaseName, sqlStatement, currentRuleConfig);
        checkShardingTableReferenceRulesValid(sqlStatement, currentRuleConfig);
    }
    
    private void checkCurrentRuleConfiguration(final String databaseName, final ShardingRuleConfiguration currentRuleConfig) {
        ShardingSpherePreconditions.checkNotNull(currentRuleConfig, () -> new MissingRequiredRuleException("Sharding", databaseName));
    }
    
    private void checkToBeAlteredRulesExisted(final String databaseName, final AlterShardingTableReferenceRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        Collection<String> currentRuleNames = currentRuleConfig.getBindingTableGroups().stream().map(ShardingTableReferenceRuleConfiguration::getName).collect(Collectors.toSet());
        Collection<String> notExistedRuleNames = sqlStatement.getRules().stream().map(TableReferenceRuleSegment::getName).filter(each -> !currentRuleNames.contains(each)).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkState(notExistedRuleNames.isEmpty(), () -> new MissingRequiredRuleException("Sharding table reference", databaseName, notExistedRuleNames));
    }
    
    private void checkDuplicatedTablesInShardingTableReferenceRules(final String databaseName,
                                                                    final AlterShardingTableReferenceRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        Collection<String> currentReferencedTableNames = getReferencedTableNames(currentRuleConfig, getToBeAlteredRuleNames(sqlStatement));
        Collection<String> duplicatedTableNames = sqlStatement.getTableNames().stream().filter(currentReferencedTableNames::contains).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkState(duplicatedTableNames.isEmpty(), () -> new DuplicateRuleException("sharding table reference", databaseName, duplicatedTableNames));
    }
    
    private Collection<String> getToBeAlteredRuleNames(final AlterShardingTableReferenceRuleStatement sqlStatement) {
        return sqlStatement.getRules().stream().map(TableReferenceRuleSegment::getName).collect(Collectors.toSet());
    }
    
    private Collection<String> getToBeAlteredRuleNames(final ShardingRuleConfiguration ruleConfig) {
        return ruleConfig.getBindingTableGroups().stream().map(ShardingTableReferenceRuleConfiguration::getName).collect(Collectors.toSet());
    }
    
    private void checkToBeReferencedShardingTablesExisted(final String databaseName, final AlterShardingTableReferenceRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        Collection<String> existedShardingTables = getCurrentShardingTables(currentRuleConfig);
        Collection<String> notExistedShardingTables = sqlStatement.getTableNames().stream().filter(each -> !containsIgnoreCase(existedShardingTables, each)).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkState(notExistedShardingTables.isEmpty(), () -> new MissingRequiredRuleException("Sharding", databaseName, notExistedShardingTables));
    }
    
    private void checkShardingTableReferenceRulesValid(final AlterShardingTableReferenceRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        Collection<ShardingTableReferenceRuleConfiguration> toBeAlteredShardingTableReferenceRules = buildToBeAlteredRuleConfiguration(sqlStatement).getBindingTableGroups();
        Collection<String> ruleNames = toBeAlteredShardingTableReferenceRules.stream().map(ShardingTableReferenceRuleConfiguration::getName).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(ShardingTableRuleStatementChecker.isValidBindingTableGroups(toBeAlteredShardingTableReferenceRules, currentRuleConfig),
                () -> new InvalidRuleConfigurationException("sharding table", ruleNames, Collections.singleton("invalid sharding table reference.")));
    }
    
    private Collection<String> getReferencedTableNames(final ShardingRuleConfiguration currentRuleConfig, final Collection<String> getToBeAlteredRuleNames) {
        Collection<String> result = new HashSet<>();
        currentRuleConfig.getBindingTableGroups().forEach(each -> {
            if (!getToBeAlteredRuleNames.contains(each.getName())) {
                result.addAll(Arrays.stream(each.getReference().split(",")).map(String::trim).collect(Collectors.toSet()));
            }
        });
        return result;
    }
    
    private Collection<String> getCurrentShardingTables(final ShardingRuleConfiguration currentRuleConfig) {
        Collection<String> result = new HashSet<>();
        result.addAll(currentRuleConfig.getTables().stream().map(ShardingTableRuleConfiguration::getLogicTable).collect(Collectors.toSet()));
        result.addAll(currentRuleConfig.getAutoTables().stream().map(ShardingAutoTableRuleConfiguration::getLogicTable).collect(Collectors.toSet()));
        return result;
    }
    
    private boolean containsIgnoreCase(final Collection<String> currentRules, final String ruleName) {
        return currentRules.stream().anyMatch(each -> each.equalsIgnoreCase(ruleName));
    }
    
    @Override
    public ShardingRuleConfiguration buildToBeAlteredRuleConfiguration(final AlterShardingTableReferenceRuleStatement sqlStatement) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        sqlStatement.getRules().forEach(each -> result.getBindingTableGroups().add(new ShardingTableReferenceRuleConfiguration(each.getName(), each.getReference())));
        return result;
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final ShardingRuleConfiguration currentRuleConfig, final ShardingRuleConfiguration toBeAlteredRuleConfig) {
        Collection<String> toBeAlteredRuleNames = getToBeAlteredRuleNames(toBeAlteredRuleConfig);
        currentRuleConfig.getBindingTableGroups().removeIf(each -> toBeAlteredRuleNames.contains(each.getName()));
        currentRuleConfig.getBindingTableGroups().addAll(toBeAlteredRuleConfig.getBindingTableGroups());
    }
    
    @Override
    public Class<ShardingRuleConfiguration> getRuleConfigurationClass() {
        return ShardingRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return AlterShardingTableReferenceRuleStatement.class.getName();
    }
}
