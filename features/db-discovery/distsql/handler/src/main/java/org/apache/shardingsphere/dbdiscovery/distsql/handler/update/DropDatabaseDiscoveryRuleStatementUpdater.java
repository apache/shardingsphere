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

package org.apache.shardingsphere.dbdiscovery.distsql.handler.update;

import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.DropDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.infra.distsql.constant.ExportableConstants;
import org.apache.shardingsphere.infra.distsql.constant.ExportableItemConstants;
import org.apache.shardingsphere.infra.distsql.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RuleInUsedException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionDropUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.identifier.type.exportable.ExportableRule;
import org.apache.shardingsphere.infra.rule.identifier.type.exportable.RuleExportEngine;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Drop database discovery rule statement updater.
 */
public final class DropDatabaseDiscoveryRuleStatementUpdater implements RuleDefinitionDropUpdater<DropDatabaseDiscoveryRuleStatement, DatabaseDiscoveryRuleConfiguration> {
    
    private static final String RULE_TYPE = "Database discovery";
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database, final DropDatabaseDiscoveryRuleStatement sqlStatement, final DatabaseDiscoveryRuleConfiguration currentRuleConfig) {
        String databaseName = database.getName();
        checkCurrentRuleConfiguration(databaseName, sqlStatement, currentRuleConfig);
        checkIsInUse(databaseName, sqlStatement, database);
    }
    
    private void checkCurrentRuleConfiguration(final String databaseName, final DropDatabaseDiscoveryRuleStatement sqlStatement, final DatabaseDiscoveryRuleConfiguration currentRuleConfig) {
        if (sqlStatement.isIfExists()) {
            return;
        }
        ShardingSpherePreconditions.checkNotNull(currentRuleConfig, () -> new MissingRequiredRuleException(RULE_TYPE, databaseName));
        checkIsExist(databaseName, sqlStatement, currentRuleConfig);
    }
    
    private void checkIsExist(final String databaseName, final DropDatabaseDiscoveryRuleStatement sqlStatement, final DatabaseDiscoveryRuleConfiguration currentRuleConfig) {
        Collection<String> currentRuleNames = currentRuleConfig.getDataSources().stream().map(DatabaseDiscoveryDataSourceRuleConfiguration::getGroupName).collect(Collectors.toList());
        Collection<String> notExistedRuleNames = sqlStatement.getRuleNames().stream().filter(each -> !currentRuleNames.contains(each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(notExistedRuleNames.isEmpty(), () -> new MissingRequiredRuleException(RULE_TYPE, databaseName));
    }
    
    private void checkIsInUse(final String databaseName, final DropDatabaseDiscoveryRuleStatement sqlStatement, final ShardingSphereDatabase database) {
        Optional<ExportableRule> exportableRule = database.getRuleMetaData().findRules(ExportableRule.class).stream()
                .filter(each -> new RuleExportEngine(each).containExportableKey(Collections.singletonList(ExportableConstants.EXPORT_DYNAMIC_READWRITE_SPLITTING_RULE))).findFirst();
        Collection<String> rulesInUse = new LinkedList<>();
        exportableRule.ifPresent(optional -> {
            Map<String, Map<String, String>> readwriteRuleMap = new RuleExportEngine(optional).export(ExportableConstants.EXPORT_DYNAMIC_READWRITE_SPLITTING_RULE)
                    .map(each -> (Map<String, Map<String, String>>) each).orElse(Collections.emptyMap());
            readwriteRuleMap.values().stream().map(each -> each.get(ExportableItemConstants.AUTO_AWARE_DATA_SOURCE_NAME)).forEach(rulesInUse::add);
        });
        Collection<String> invalid = sqlStatement.getRuleNames().stream().filter(rulesInUse::contains).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(invalid.isEmpty(), () -> new RuleInUsedException(RULE_TYPE, databaseName, invalid));
    }
    
    @Override
    public boolean updateCurrentRuleConfiguration(final DropDatabaseDiscoveryRuleStatement sqlStatement, final DatabaseDiscoveryRuleConfiguration currentRuleConfig) {
        for (String each : sqlStatement.getRuleNames()) {
            dropRule(currentRuleConfig, each);
        }
        return false;
    }
    
    private void dropRule(final DatabaseDiscoveryRuleConfiguration currentRuleConfig, final String ruleName) {
        Optional<DatabaseDiscoveryDataSourceRuleConfiguration> dataSourceRuleConfig = currentRuleConfig.getDataSources().stream().filter(each -> each.getGroupName().equals(ruleName)).findAny();
        dataSourceRuleConfig.ifPresent(optional -> currentRuleConfig.getDataSources().remove(dataSourceRuleConfig.get()));
    }
    
    @Override
    public boolean hasAnyOneToBeDropped(final DropDatabaseDiscoveryRuleStatement sqlStatement, final DatabaseDiscoveryRuleConfiguration currentRuleConfig) {
        return isExistRuleConfig(currentRuleConfig)
                && !getIdenticalData(currentRuleConfig.getDataSources().stream().map(DatabaseDiscoveryDataSourceRuleConfiguration::getGroupName).collect(Collectors.toSet()),
                        sqlStatement.getRuleNames()).isEmpty();
    }
    
    @Override
    public Class<DatabaseDiscoveryRuleConfiguration> getRuleConfigurationClass() {
        return DatabaseDiscoveryRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return DropDatabaseDiscoveryRuleStatement.class.getName();
    }
}
