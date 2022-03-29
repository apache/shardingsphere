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
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RuleInUsedException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionDropUpdater;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.ExportableRule;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Drop database discovery rule statement updater.
 */
public final class DropDatabaseDiscoveryRuleStatementUpdater implements RuleDefinitionDropUpdater<DropDatabaseDiscoveryRuleStatement, DatabaseDiscoveryRuleConfiguration> {
    
    private static final String RULE_TYPE = "Database discovery";
    
    @Override
    public void checkSQLStatement(final ShardingSphereMetaData shardingSphereMetaData, final DropDatabaseDiscoveryRuleStatement sqlStatement,
                                  final DatabaseDiscoveryRuleConfiguration currentRuleConfig) throws DistSQLException {
        String schemaName = shardingSphereMetaData.getName();
        checkCurrentRuleConfiguration(schemaName, sqlStatement, currentRuleConfig);
        checkIsInUse(schemaName, sqlStatement, shardingSphereMetaData);
    }
    
    private void checkCurrentRuleConfiguration(final String schemaName, final DropDatabaseDiscoveryRuleStatement sqlStatement,
                                               final DatabaseDiscoveryRuleConfiguration currentRuleConfig) throws DistSQLException {
        if (sqlStatement.isContainsExistClause()) {
            return;
        }
        DistSQLException.predictionThrow(null != currentRuleConfig, () -> new RequiredRuleMissedException(RULE_TYPE, schemaName));
        checkIsExist(schemaName, sqlStatement, currentRuleConfig);
    }
    
    private void checkIsExist(final String schemaName, final DropDatabaseDiscoveryRuleStatement sqlStatement,
                              final DatabaseDiscoveryRuleConfiguration currentRuleConfig) throws DistSQLException {
        Collection<String> currentRuleNames = currentRuleConfig.getDataSources().stream().map(DatabaseDiscoveryDataSourceRuleConfiguration::getGroupName).collect(Collectors.toList());
        Collection<String> notExistedRuleNames = sqlStatement.getRuleNames().stream().filter(each -> !currentRuleNames.contains(each)).collect(Collectors.toList());
        DistSQLException.predictionThrow(notExistedRuleNames.isEmpty(), () -> new RequiredRuleMissedException(RULE_TYPE, schemaName));
    }
    
    private void checkIsInUse(final String schemaName, final DropDatabaseDiscoveryRuleStatement sqlStatement, final ShardingSphereMetaData shardingSphereMetaData) throws DistSQLException {
        Collection<String> rulesInUse = shardingSphereMetaData.getRuleMetaData().findRules(ExportableRule.class).stream()
                .filter(each -> each.containExportableKey(Collections.singletonList(ExportableConstants.EXPORTABLE_KEY_AUTO_AWARE_DATA_SOURCE_NAME)))
                .map(each -> each.export(ExportableConstants.EXPORTABLE_KEY_AUTO_AWARE_DATA_SOURCE_NAME)).filter(Optional::isPresent)
                .map(each -> (Collection<String>) each.get()).flatMap(Collection::stream).collect(Collectors.toSet());
        Collection<String> invalid = sqlStatement.getRuleNames().stream().filter(rulesInUse::contains).collect(Collectors.toList());
        DistSQLException.predictionThrow(invalid.isEmpty(), () -> new RuleInUsedException(RULE_TYPE, schemaName, invalid));
    }
    
    @Override
    public boolean updateCurrentRuleConfiguration(final DropDatabaseDiscoveryRuleStatement sqlStatement, final DatabaseDiscoveryRuleConfiguration currentRuleConfig) {
        for (String each : sqlStatement.getRuleNames()) {
            dropRule(currentRuleConfig, each);
        }
        return false;
    }
    
    private void dropRule(final DatabaseDiscoveryRuleConfiguration currentRuleConfig, final String ruleName) {
        Optional<DatabaseDiscoveryDataSourceRuleConfiguration> dataSourceRuleConfig = currentRuleConfig.getDataSources().stream().filter(dataSource ->
                dataSource.getGroupName().equals(ruleName)).findAny();
        dataSourceRuleConfig.ifPresent(op -> {
            currentRuleConfig.getDataSources().remove(dataSourceRuleConfig.get());
        });
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
