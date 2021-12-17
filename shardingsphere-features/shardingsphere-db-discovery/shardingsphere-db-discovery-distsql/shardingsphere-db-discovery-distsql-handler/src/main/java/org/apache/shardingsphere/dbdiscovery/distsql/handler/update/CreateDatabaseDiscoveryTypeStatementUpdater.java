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
import org.apache.shardingsphere.dbdiscovery.distsql.parser.segment.DatabaseDiscoveryTypeSegment;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.CreateDatabaseDiscoveryTypeStatement;
import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryType;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionCreateUpdater;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.typed.TypedSPIRegistry;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Create database discovery type statement updater.
 */
public final class CreateDatabaseDiscoveryTypeStatementUpdater implements RuleDefinitionCreateUpdater<CreateDatabaseDiscoveryTypeStatement, DatabaseDiscoveryRuleConfiguration> {
    
    private static final String RULE_TYPE = "database discovery";
    
    static {
        // TODO consider about register once only
        ShardingSphereServiceLoader.register(DatabaseDiscoveryType.class);
    }
    
    @Override
    public void checkSQLStatement(final ShardingSphereMetaData shardingSphereMetaData, final CreateDatabaseDiscoveryTypeStatement sqlStatement,
                                  final DatabaseDiscoveryRuleConfiguration currentRuleConfig) throws DistSQLException {
        String schemaName = shardingSphereMetaData.getName();
        checkDuplicateDiscoveryType(schemaName, sqlStatement, currentRuleConfig);
        checkInvalidDiscoverType(sqlStatement);
    }
    
    private void checkDuplicateDiscoveryType(final String schemaName, final CreateDatabaseDiscoveryTypeStatement sqlStatement,
                                             final DatabaseDiscoveryRuleConfiguration currentRuleConfig) throws DistSQLException {
        if (null == currentRuleConfig) {
            return;
        }
        Collection<String> existRuleNames = currentRuleConfig.getDiscoveryTypes().keySet();
        Collection<String> duplicateRuleNames = sqlStatement.getTypes().stream().map(DatabaseDiscoveryTypeSegment::getDiscoveryTypeName).filter(existRuleNames::contains).collect(Collectors.toSet());
        duplicateRuleNames.addAll(getToBeCreatedDuplicateRuleNames(sqlStatement));
        DistSQLException.predictionThrow(duplicateRuleNames.isEmpty(), new DuplicateRuleException(RULE_TYPE, schemaName, duplicateRuleNames));
    }
    
    private Collection<String> getToBeCreatedDuplicateRuleNames(final CreateDatabaseDiscoveryTypeStatement sqlStatement) {
        return sqlStatement.getTypes().stream().collect(Collectors.toMap(DatabaseDiscoveryTypeSegment::getDiscoveryTypeName, e -> 1, Integer::sum))
                .entrySet().stream().filter(entry -> entry.getValue() > 1).map(Entry::getKey).collect(Collectors.toSet());
    }
    
    private void checkInvalidDiscoverType(final CreateDatabaseDiscoveryTypeStatement sqlStatement) throws DistSQLException {
        List<String> invalidType = sqlStatement.getTypes().stream().map(each -> each.getAlgorithmSegment().getName()).distinct()
                .filter(each -> !TypedSPIRegistry.findRegisteredService(DatabaseDiscoveryType.class, each, new Properties()).isPresent()).collect(Collectors.toList());
        DistSQLException.predictionThrow(invalidType.isEmpty(), new InvalidAlgorithmConfigurationException(RULE_TYPE, invalidType));
    }
    
    @Override
    public RuleConfiguration buildToBeCreatedRuleConfiguration(final CreateDatabaseDiscoveryTypeStatement sqlStatement) {
        return DatabaseDiscoveryRuleStatementConverter.convertDiscoveryType(sqlStatement.getTypes());
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
        return CreateDatabaseDiscoveryTypeStatement.class.getCanonicalName();
    }
}
