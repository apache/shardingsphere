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
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.DatabaseDiscoveryProviderAlgorithmSegment;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.CreateDatabaseDiscoveryTypeStatement;
import org.apache.shardingsphere.dbdiscovery.factory.DatabaseDiscoveryProviderAlgorithmFactory;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionCreateUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Create database discovery type statement updater.
 */
public final class CreateDatabaseDiscoveryTypeStatementUpdater implements RuleDefinitionCreateUpdater<CreateDatabaseDiscoveryTypeStatement, DatabaseDiscoveryRuleConfiguration> {
    
    private static final String RULE_TYPE = "database discovery";
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database,
                                  final CreateDatabaseDiscoveryTypeStatement sqlStatement, final DatabaseDiscoveryRuleConfiguration currentRuleConfig) throws DistSQLException {
        String databaseName = database.getName();
        checkDuplicateDiscoveryType(databaseName, sqlStatement, currentRuleConfig);
        checkInvalidDiscoverType(sqlStatement);
    }
    
    private void checkDuplicateDiscoveryType(final String databaseName,
                                             final CreateDatabaseDiscoveryTypeStatement sqlStatement, final DatabaseDiscoveryRuleConfiguration currentRuleConfig) throws DistSQLException {
        if (null == currentRuleConfig) {
            return;
        }
        Collection<String> existRuleNames = currentRuleConfig.getDiscoveryTypes().keySet();
        Collection<String> duplicateRuleNames = sqlStatement.getProviders()
                .stream().map(DatabaseDiscoveryProviderAlgorithmSegment::getDiscoveryProviderName).filter(existRuleNames::contains).collect(Collectors.toSet());
        duplicateRuleNames.addAll(getToBeCreatedDuplicateRuleNames(sqlStatement));
        ShardingSpherePreconditions.checkState(duplicateRuleNames.isEmpty(), () -> new DuplicateRuleException(RULE_TYPE, databaseName, duplicateRuleNames));
    }
    
    private Collection<String> getToBeCreatedDuplicateRuleNames(final CreateDatabaseDiscoveryTypeStatement sqlStatement) {
        return sqlStatement.getProviders().stream().collect(Collectors.toMap(DatabaseDiscoveryProviderAlgorithmSegment::getDiscoveryProviderName, each -> 1, Integer::sum))
                .entrySet().stream().filter(entry -> entry.getValue() > 1).map(Entry::getKey).collect(Collectors.toSet());
    }
    
    private void checkInvalidDiscoverType(final CreateDatabaseDiscoveryTypeStatement sqlStatement) throws DistSQLException {
        Collection<String> invalidType = sqlStatement.getProviders().stream().map(each -> each.getAlgorithm().getName()).distinct()
                .filter(each -> !DatabaseDiscoveryProviderAlgorithmFactory.contains(each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(invalidType.isEmpty(), () -> new InvalidAlgorithmConfigurationException(RULE_TYPE, invalidType));
    }
    
    @Override
    public RuleConfiguration buildToBeCreatedRuleConfiguration(final CreateDatabaseDiscoveryTypeStatement sqlStatement) {
        return DatabaseDiscoveryRuleStatementConverter.convertDiscoveryProviderAlgorithm(sqlStatement.getProviders());
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final DatabaseDiscoveryRuleConfiguration currentRuleConfig, final DatabaseDiscoveryRuleConfiguration toBeCreatedRuleConfig) {
        if (null != currentRuleConfig) {
            currentRuleConfig.getDiscoveryTypes().putAll(toBeCreatedRuleConfig.getDiscoveryTypes());
        }
    }
    
    @Override
    public Class<DatabaseDiscoveryRuleConfiguration> getRuleConfigurationClass() {
        return DatabaseDiscoveryRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return CreateDatabaseDiscoveryTypeStatement.class.getName();
    }
}
