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
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.DropDatabaseDiscoveryHeartbeatStatement;
import org.apache.shardingsphere.infra.distsql.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RuleInUsedException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionDropUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Drop database discovery heartbeat statement updater.
 */
public final class DropDatabaseDiscoveryHeartbeatStatementUpdater implements RuleDefinitionDropUpdater<DropDatabaseDiscoveryHeartbeatStatement, DatabaseDiscoveryRuleConfiguration> {
    
    private static final String RULE_TYPE = "Database discovery";
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database, final DropDatabaseDiscoveryHeartbeatStatement sqlStatement, final DatabaseDiscoveryRuleConfiguration currentRuleConfig) {
        String databaseName = database.getName();
        checkCurrentRuleConfiguration(databaseName, sqlStatement, currentRuleConfig);
        checkIsInUse(databaseName, sqlStatement, currentRuleConfig);
    }
    
    private void checkCurrentRuleConfiguration(final String databaseName, final DropDatabaseDiscoveryHeartbeatStatement sqlStatement, final DatabaseDiscoveryRuleConfiguration currentRuleConfig) {
        if (sqlStatement.isIfExists()) {
            return;
        }
        ShardingSpherePreconditions.checkNotNull(currentRuleConfig, () -> new MissingRequiredRuleException(RULE_TYPE, databaseName));
        checkIsExist(databaseName, sqlStatement, currentRuleConfig);
    }
    
    private void checkIsExist(final String databaseName, final DropDatabaseDiscoveryHeartbeatStatement sqlStatement, final DatabaseDiscoveryRuleConfiguration currentRuleConfig) {
        Collection<String> currentRuleNames = currentRuleConfig.getDiscoveryHeartbeats().keySet();
        Collection<String> notExistedRuleNames = sqlStatement.getHeartbeatNames().stream().filter(each -> !currentRuleNames.contains(each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(notExistedRuleNames.isEmpty(), () -> new MissingRequiredRuleException(RULE_TYPE, databaseName, notExistedRuleNames));
    }
    
    private void checkIsInUse(final String databaseName, final DropDatabaseDiscoveryHeartbeatStatement sqlStatement, final DatabaseDiscoveryRuleConfiguration currentRuleConfig) {
        Collection<String> heartbeatInUse = currentRuleConfig.getDataSources().stream().map(DatabaseDiscoveryDataSourceRuleConfiguration::getDiscoveryHeartbeatName).collect(Collectors.toSet());
        Collection<String> invalid = sqlStatement.getHeartbeatNames().stream().filter(heartbeatInUse::contains).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(invalid.isEmpty(), () -> new RuleInUsedException(RULE_TYPE, databaseName, invalid));
    }
    
    @Override
    public boolean updateCurrentRuleConfiguration(final DropDatabaseDiscoveryHeartbeatStatement sqlStatement, final DatabaseDiscoveryRuleConfiguration currentRuleConfig) {
        for (String each : sqlStatement.getHeartbeatNames()) {
            dropRule(currentRuleConfig, each);
        }
        return false;
    }
    
    private void dropRule(final DatabaseDiscoveryRuleConfiguration currentRuleConfig, final String heartbeatName) {
        currentRuleConfig.getDiscoveryHeartbeats().remove(heartbeatName);
    }
    
    @Override
    public boolean hasAnyOneToBeDropped(final DropDatabaseDiscoveryHeartbeatStatement sqlStatement, final DatabaseDiscoveryRuleConfiguration currentRuleConfig) {
        return isExistRuleConfig(currentRuleConfig) && !getIdenticalData(currentRuleConfig.getDiscoveryHeartbeats().keySet(), sqlStatement.getHeartbeatNames()).isEmpty();
    }
    
    @Override
    public Class<DatabaseDiscoveryRuleConfiguration> getRuleConfigurationClass() {
        return DatabaseDiscoveryRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return DropDatabaseDiscoveryHeartbeatStatement.class.getName();
    }
}
