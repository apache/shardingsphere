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
import org.apache.shardingsphere.dbdiscovery.distsql.handler.converter.DatabaseDiscoveryRuleStatementConverter;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.DatabaseDiscoveryHeartbeatSegment;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.AlterDatabaseDiscoveryHeartbeatStatement;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionAlterUpdater;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public final class AlterDatabaseDiscoveryHeartbeatStatementUpdater implements RuleDefinitionAlterUpdater<AlterDatabaseDiscoveryHeartbeatStatement, DatabaseDiscoveryRuleConfiguration> {
    
    private static final String RULE_TYPE = "database discovery";
    
    @Override
    public DatabaseDiscoveryRuleConfiguration buildToBeAlteredRuleConfiguration(final AlterDatabaseDiscoveryHeartbeatStatement sqlStatement) {
        return DatabaseDiscoveryRuleStatementConverter.convertDiscoveryHeartbeat(sqlStatement.getHeartbeats());
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final DatabaseDiscoveryRuleConfiguration currentRuleConfig, final DatabaseDiscoveryRuleConfiguration toBeCreatedRuleConfig) {
        if (null != currentRuleConfig) {
            currentRuleConfig.getDiscoveryHeartbeats().putAll(toBeCreatedRuleConfig.getDiscoveryHeartbeats());
        }
    }
    
    @Override
    public void checkSQLStatement(final ShardingSphereMetaData shardingSphereMetaData, final AlterDatabaseDiscoveryHeartbeatStatement sqlStatement,
                                  final DatabaseDiscoveryRuleConfiguration currentRuleConfig) throws DistSQLException {
        String schemaName = shardingSphereMetaData.getName();
        checkCurrentConfiguration(schemaName, currentRuleConfig);
        checkDuplicateHeartbeat(schemaName, sqlStatement);
        checkNotExistHeartbeat(schemaName, sqlStatement, currentRuleConfig);
    }
    
    private void checkCurrentConfiguration(final String schemaName, final DatabaseDiscoveryRuleConfiguration currentRuleConfig) throws DistSQLException {
        DistSQLException.predictionThrow(currentRuleConfig != null, () -> new RequiredRuleMissedException(RULE_TYPE, schemaName));
    }
    
    private void checkNotExistHeartbeat(final String schemaName, final AlterDatabaseDiscoveryHeartbeatStatement sqlStatement,
                                        final DatabaseDiscoveryRuleConfiguration currentRuleConfig) throws DistSQLException {
        Collection<String> currentHeartbeats = currentRuleConfig.getDiscoveryHeartbeats().keySet();
        Collection<String> notExistHeartbeats = sqlStatement.getHeartbeats().stream().map(DatabaseDiscoveryHeartbeatSegment::getHeartbeatName)
                .filter(each -> !currentHeartbeats.contains(each)).collect(Collectors.toSet());
        DistSQLException.predictionThrow(notExistHeartbeats.isEmpty(), () -> new RequiredRuleMissedException(RULE_TYPE, schemaName, notExistHeartbeats));
    }
    
    private void checkDuplicateHeartbeat(final String schemaName, final AlterDatabaseDiscoveryHeartbeatStatement sqlStatement) throws DistSQLException {
        Collection<String> duplicateRuleNames = getToBeCreatedDuplicateRuleNames(sqlStatement);
        DistSQLException.predictionThrow(duplicateRuleNames.isEmpty(), () -> new DuplicateRuleException(RULE_TYPE, schemaName, duplicateRuleNames));
    }
    
    private Collection<String> getToBeCreatedDuplicateRuleNames(final AlterDatabaseDiscoveryHeartbeatStatement sqlStatement) {
        return sqlStatement.getHeartbeats().stream().collect(Collectors.toMap(DatabaseDiscoveryHeartbeatSegment::getHeartbeatName, e -> 1, Integer::sum))
                .entrySet().stream().filter(entry -> entry.getValue() > 1).map(Entry::getKey).collect(Collectors.toSet());
    }
    
    @Override
    public Class<DatabaseDiscoveryRuleConfiguration> getRuleConfigurationClass() {
        return DatabaseDiscoveryRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return AlterDatabaseDiscoveryHeartbeatStatement.class.getName();
    }
}
