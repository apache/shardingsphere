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

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.DropDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionDropUpdater;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RuleDefinitionViolationException;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Drop database discovery rule statement updater.
 */
public final class DropDatabaseDiscoveryRuleStatementUpdater implements RuleDefinitionDropUpdater<DropDatabaseDiscoveryRuleStatement, DatabaseDiscoveryRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final String schemaName, final DropDatabaseDiscoveryRuleStatement sqlStatement, 
                                  final DatabaseDiscoveryRuleConfiguration currentRuleConfig, final ShardingSphereResource resource) throws RuleDefinitionViolationException {
        checkCurrentRuleConfiguration(schemaName, currentRuleConfig);
        checkToBeDroppedRuleNames(schemaName, sqlStatement, currentRuleConfig);
    }
    
    private void checkCurrentRuleConfiguration(final String schemaName, final DatabaseDiscoveryRuleConfiguration currentRuleConfig) throws RequiredRuleMissedException {
        if (null == currentRuleConfig) {
            throw new RequiredRuleMissedException("Database discovery", schemaName);
        }
    }
    
    private void checkToBeDroppedRuleNames(final String schemaName, final DropDatabaseDiscoveryRuleStatement sqlStatement, 
                                           final DatabaseDiscoveryRuleConfiguration currentRuleConfig) throws RequiredRuleMissedException {
        Collection<String> currentRuleNames = currentRuleConfig.getDataSources().stream().map(DatabaseDiscoveryDataSourceRuleConfiguration::getName).collect(Collectors.toList());
        Collection<String> notExistedRuleNames = sqlStatement.getRuleNames().stream().filter(each -> !currentRuleNames.contains(each)).collect(Collectors.toList());
        if (!notExistedRuleNames.isEmpty()) {
            throw new RequiredRuleMissedException("Database discovery", schemaName, notExistedRuleNames);
        }
    }
    
    @Override
    public boolean updateCurrentRuleConfiguration(final DropDatabaseDiscoveryRuleStatement sqlStatement, final DatabaseDiscoveryRuleConfiguration currentRuleConfig) {
        for (String each : sqlStatement.getRuleNames()) {
            dropRule(currentRuleConfig, each);
        }
        return currentRuleConfig.getDataSources().isEmpty();
    }
    
    private void dropRule(final DatabaseDiscoveryRuleConfiguration currentRuleConfig, final String ruleName) {
        Optional<DatabaseDiscoveryDataSourceRuleConfiguration> dataSourceRuleConfig = currentRuleConfig.getDataSources().stream().filter(dataSource -> dataSource.getName().equals(ruleName)).findAny();
        Preconditions.checkState(dataSourceRuleConfig.isPresent());
        currentRuleConfig.getDataSources().remove(dataSourceRuleConfig.get());
        if (isDiscoveryTypeNotInUse(currentRuleConfig, dataSourceRuleConfig.get().getDiscoveryTypeName())) {
            currentRuleConfig.getDiscoveryTypes().remove(dataSourceRuleConfig.get().getDiscoveryTypeName());
        }
    }
    
    private boolean isDiscoveryTypeNotInUse(final DatabaseDiscoveryRuleConfiguration currentRuleConfig, final String toBeDroppedDiscoveryTypeName) {
        return !currentRuleConfig.getDataSources().stream().filter(each -> each.getDiscoveryTypeName().equals(toBeDroppedDiscoveryTypeName)).findAny().isPresent();
    }
    
    @Override
    public Class<DatabaseDiscoveryRuleConfiguration> getRuleConfigurationClass() {
        return DatabaseDiscoveryRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return DropDatabaseDiscoveryRuleStatement.class.getCanonicalName();
    }
}
