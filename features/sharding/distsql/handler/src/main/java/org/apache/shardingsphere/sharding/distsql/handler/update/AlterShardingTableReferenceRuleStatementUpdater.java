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

import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidRuleConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RuleDefinitionViolationException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionAlterUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.checker.ShardingTableRuleStatementChecker;
import org.apache.shardingsphere.sharding.distsql.parser.segment.table.TableReferenceRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.statement.AlterShardingTableReferenceRuleStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * Alter sharding table reference rule statement updater.
 */
public final class AlterShardingTableReferenceRuleStatementUpdater implements RuleDefinitionAlterUpdater<AlterShardingTableReferenceRuleStatement, ShardingRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database,
                                  final AlterShardingTableReferenceRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) throws RuleDefinitionViolationException {
        String databaseName = database.getName();
        checkCurrentRuleConfiguration(databaseName, currentRuleConfig);
        checkToBeAlertedBindingTables(databaseName, sqlStatement, currentRuleConfig);
        checkToBeAlteredDuplicateBindingTables(databaseName, sqlStatement);
        checkBindingTableGroups(sqlStatement, currentRuleConfig);
    }
    
    private void checkCurrentRuleConfiguration(final String databaseName, final ShardingRuleConfiguration currentRuleConfig) throws MissingRequiredRuleException {
        ShardingSpherePreconditions.checkNotNull(currentRuleConfig, () -> new MissingRequiredRuleException("Sharding", databaseName));
    }
    
    private void checkToBeAlertedBindingTables(final String databaseName, final AlterShardingTableReferenceRuleStatement sqlStatement,
                                               final ShardingRuleConfiguration currentRuleConfig) throws MissingRequiredRuleException {
        Collection<String> currentLogicTables = getCurrentLogicTables(currentRuleConfig);
        Collection<String> notExistedBindingTables = sqlStatement.getTableReferences().stream().filter(each -> !containsIgnoreCase(currentLogicTables, each)).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkState(notExistedBindingTables.isEmpty(), () -> new MissingRequiredRuleException("Sharding", databaseName, notExistedBindingTables));
    }
    
    private Collection<String> getCurrentLogicTables(final ShardingRuleConfiguration currentRuleConfig) {
        Collection<String> result = new HashSet<>();
        result.addAll(currentRuleConfig.getTables().stream().map(ShardingTableRuleConfiguration::getLogicTable).collect(Collectors.toSet()));
        result.addAll(currentRuleConfig.getAutoTables().stream().map(ShardingAutoTableRuleConfiguration::getLogicTable).collect(Collectors.toSet()));
        return result;
    }
    
    private void checkToBeAlteredDuplicateBindingTables(final String databaseName, final AlterShardingTableReferenceRuleStatement sqlStatement) throws DuplicateRuleException {
        Collection<String> toBeAlteredBindingTables = new HashSet<>();
        Collection<String> duplicateBindingTables = sqlStatement.getTableReferences().stream().filter(each -> !toBeAlteredBindingTables.add(each.toLowerCase())).collect(Collectors.toSet());
        Collection<String> duplicateBindingTablesForDisplay = sqlStatement.getTableReferences().stream().filter(each -> containsIgnoreCase(duplicateBindingTables, each)).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkState(duplicateBindingTablesForDisplay.isEmpty(),
                () -> new DuplicateRuleException("sharding table reference", databaseName, duplicateBindingTablesForDisplay));
    }
    
    private boolean containsIgnoreCase(final Collection<String> collection, final String str) {
        return collection.stream().anyMatch(each -> each.equalsIgnoreCase(str));
    }
    
    private void checkBindingTableGroups(final AlterShardingTableReferenceRuleStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        Collection<String> bindingTableGroups = ((ShardingRuleConfiguration) buildToBeAlteredRuleConfiguration(sqlStatement)).getBindingTableGroups();
        ShardingSpherePreconditions.checkState(ShardingTableRuleStatementChecker.isValidBindingTableGroups(bindingTableGroups, currentRuleConfig),
                () -> new InvalidRuleConfigurationException("sharding table", bindingTableGroups, Collections.singleton("invalid binding table configuration.")));
    }
    
    @Override
    public RuleConfiguration buildToBeAlteredRuleConfiguration(final AlterShardingTableReferenceRuleStatement sqlStatement) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        for (TableReferenceRuleSegment each : sqlStatement.getRules()) {
            result.getBindingTableGroups().add(each.getTableGroup());
        }
        return result;
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final ShardingRuleConfiguration currentRuleConfig, final ShardingRuleConfiguration toBeAlteredRuleConfig) {
        dropRuleConfiguration(currentRuleConfig);
        addRuleConfiguration(currentRuleConfig, toBeAlteredRuleConfig);
    }
    
    private void dropRuleConfiguration(final ShardingRuleConfiguration currentRuleConfig) {
        currentRuleConfig.getBindingTableGroups().clear();
    }
    
    private void addRuleConfiguration(final ShardingRuleConfiguration currentRuleConfig, final ShardingRuleConfiguration toBeAlteredRuleConfig) {
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
