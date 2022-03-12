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
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.DropDatabaseDiscoveryTypeStatement;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RuleInUsedException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionDropUpdater;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Drop database discovery type statement updater.
 */
public final class DropDatabaseDiscoveryTypeStatementUpdater implements RuleDefinitionDropUpdater<DropDatabaseDiscoveryTypeStatement, DatabaseDiscoveryRuleConfiguration> {
    
    private static final String RULE_TYPE = "Database discovery";
    
    @Override
    public void checkSQLStatement(final ShardingSphereMetaData shardingSphereMetaData, final DropDatabaseDiscoveryTypeStatement sqlStatement,
                                  final DatabaseDiscoveryRuleConfiguration currentRuleConfig) throws DistSQLException {
        String schemaName = shardingSphereMetaData.getName();
        checkCurrentRuleConfiguration(schemaName, sqlStatement, currentRuleConfig);
        checkIsInUse(schemaName, sqlStatement, currentRuleConfig);
    }
    
    private void checkCurrentRuleConfiguration(final String schemaName, final DropDatabaseDiscoveryTypeStatement sqlStatement,
                                               final DatabaseDiscoveryRuleConfiguration currentRuleConfig) throws DistSQLException {
        if (sqlStatement.isContainsExistClause()) {
            return;
        }
        DistSQLException.predictionThrow(null != currentRuleConfig, () -> new RequiredRuleMissedException(RULE_TYPE, schemaName));
        checkIsExist(schemaName, sqlStatement, currentRuleConfig);
    }
    
    private void checkIsExist(final String schemaName, final DropDatabaseDiscoveryTypeStatement sqlStatement,
                              final DatabaseDiscoveryRuleConfiguration currentRuleConfig) throws DistSQLException {
        Collection<String> currentRuleNames = currentRuleConfig.getDiscoveryTypes().keySet();
        Collection<String> notExistedRuleNames = sqlStatement.getTypes().stream().filter(each -> !currentRuleNames.contains(each)).collect(Collectors.toList());
        DistSQLException.predictionThrow(notExistedRuleNames.isEmpty(), () -> new RequiredRuleMissedException(RULE_TYPE, schemaName, notExistedRuleNames));
    }
    
    private void checkIsInUse(final String schemaName, final DropDatabaseDiscoveryTypeStatement sqlStatement,
                              final DatabaseDiscoveryRuleConfiguration currentRuleConfig) throws DistSQLException {
        Collection<String> heartbeatInUse = currentRuleConfig.getDataSources().stream().map(DatabaseDiscoveryDataSourceRuleConfiguration::getDiscoveryTypeName).collect(Collectors.toSet());
        Collection<String> invalid = sqlStatement.getTypes().stream().filter(heartbeatInUse::contains).collect(Collectors.toList());
        DistSQLException.predictionThrow(invalid.isEmpty(), () -> new RuleInUsedException(RULE_TYPE, schemaName, invalid));
    }
    
    @Override
    public boolean updateCurrentRuleConfiguration(final DropDatabaseDiscoveryTypeStatement sqlStatement, final DatabaseDiscoveryRuleConfiguration currentRuleConfig) {
        for (String each : sqlStatement.getTypes()) {
            dropRule(currentRuleConfig, each);
        }
        return false;
    }
    
    private void dropRule(final DatabaseDiscoveryRuleConfiguration currentRuleConfig, final String typeName) {
        if (isNotInUse(currentRuleConfig, typeName)) {
            currentRuleConfig.getDiscoveryTypes().remove(typeName);
        }
    }
    
    @Override
    public boolean hasAnyOneToBeDropped(final DropDatabaseDiscoveryTypeStatement sqlStatement, final DatabaseDiscoveryRuleConfiguration currentRuleConfig) {
        return isExistRuleConfig(currentRuleConfig) && !getIdenticalData(currentRuleConfig.getDiscoveryTypes().keySet(), sqlStatement.getTypes()).isEmpty();
    }
    
    private boolean isNotInUse(final DatabaseDiscoveryRuleConfiguration currentRuleConfig, final String toBeDroppedDiscoveryTypeName) {
        return currentRuleConfig.getDataSources().stream().noneMatch(each -> each.getDiscoveryTypeName().equals(toBeDroppedDiscoveryTypeName));
    }
    
    @Override
    public Class<DatabaseDiscoveryRuleConfiguration> getRuleConfigurationClass() {
        return DatabaseDiscoveryRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return DropDatabaseDiscoveryTypeStatement.class.getName();
    }
}
