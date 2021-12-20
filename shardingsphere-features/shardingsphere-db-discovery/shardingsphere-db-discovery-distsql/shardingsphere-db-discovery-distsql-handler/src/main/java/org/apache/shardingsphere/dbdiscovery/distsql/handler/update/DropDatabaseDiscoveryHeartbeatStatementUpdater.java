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
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RuleInUsedException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionDropUpdater;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Drop database discovery heartbeat statement updater.
 */
public final class DropDatabaseDiscoveryHeartbeatStatementUpdater implements RuleDefinitionDropUpdater<DropDatabaseDiscoveryHeartbeatStatement, DatabaseDiscoveryRuleConfiguration> {
    
    private static final String RULE_TYPE = "database discovery";
    
    @Override
    public void checkSQLStatement(final ShardingSphereMetaData shardingSphereMetaData, final DropDatabaseDiscoveryHeartbeatStatement sqlStatement, 
                                  final DatabaseDiscoveryRuleConfiguration currentRuleConfig) throws DistSQLException {
        String schemaName = shardingSphereMetaData.getName();
        checkCurrentRuleConfiguration(schemaName, currentRuleConfig);
        checkIsExist(schemaName, sqlStatement, currentRuleConfig);
        checkIsInUse(schemaName, sqlStatement, currentRuleConfig);
    }
    
    private void checkCurrentRuleConfiguration(final String schemaName, final DatabaseDiscoveryRuleConfiguration currentRuleConfig) throws DistSQLException {
        DistSQLException.predictionThrow(null != currentRuleConfig, new RequiredRuleMissedException(RULE_TYPE, schemaName));
    }
    
    private void checkIsExist(final String schemaName, final DropDatabaseDiscoveryHeartbeatStatement sqlStatement,
                              final DatabaseDiscoveryRuleConfiguration currentRuleConfig) throws DistSQLException {
        Collection<String> currentRuleNames = currentRuleConfig.getDiscoveryHeartbeats().keySet();
        Collection<String> notExistedRuleNames = sqlStatement.getHeartbeatNames().stream().filter(each -> !currentRuleNames.contains(each)).collect(Collectors.toList());
        DistSQLException.predictionThrow(notExistedRuleNames.isEmpty(), new RequiredRuleMissedException(RULE_TYPE, schemaName, notExistedRuleNames));
    }
    
    private void checkIsInUse(final String schemaName, final DropDatabaseDiscoveryHeartbeatStatement sqlStatement,
                              final DatabaseDiscoveryRuleConfiguration currentRuleConfig) throws DistSQLException {
        Set<String> heartbeatInUse = currentRuleConfig.getDataSources().stream().map(DatabaseDiscoveryDataSourceRuleConfiguration::getDiscoveryHeartbeatName).collect(Collectors.toSet());
        List<String> invalid = sqlStatement.getHeartbeatNames().stream().filter(heartbeatInUse::contains).collect(Collectors.toList());
        DistSQLException.predictionThrow(invalid.isEmpty(), new RuleInUsedException(RULE_TYPE, schemaName, invalid));
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
    public Class<DatabaseDiscoveryRuleConfiguration> getRuleConfigurationClass() {
        return DatabaseDiscoveryRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return DropDatabaseDiscoveryHeartbeatStatement.class.getCanonicalName();
    }
}
