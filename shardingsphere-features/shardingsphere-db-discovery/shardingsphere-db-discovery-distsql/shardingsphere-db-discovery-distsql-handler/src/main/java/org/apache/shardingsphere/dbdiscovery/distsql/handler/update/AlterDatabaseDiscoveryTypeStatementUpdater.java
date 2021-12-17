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
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.AlterDatabaseDiscoveryTypeStatement;
import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryType;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionAlterUpdater;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.typed.TypedSPIRegistry;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Alter database discovery type statement updater.
 */
public final class AlterDatabaseDiscoveryTypeStatementUpdater implements RuleDefinitionAlterUpdater<AlterDatabaseDiscoveryTypeStatement, DatabaseDiscoveryRuleConfiguration> {
    
    private static final String RULE_TYPE = "database discovery";
    
    static {
        // TODO consider about register once only
        ShardingSphereServiceLoader.register(DatabaseDiscoveryType.class);
    }
    
    @Override
    public void checkSQLStatement(final ShardingSphereMetaData shardingSphereMetaData, final AlterDatabaseDiscoveryTypeStatement sqlStatement,
                                  final DatabaseDiscoveryRuleConfiguration currentRuleConfig) throws DistSQLException {
        String schemaName = shardingSphereMetaData.getName();
        checkCurrentRuleConfiguration(schemaName, currentRuleConfig);
        checkDuplicateDiscoveryType(schemaName, sqlStatement);
        checkNotExistDiscoveryType(schemaName, sqlStatement, currentRuleConfig);
        checkInvalidDiscoverType(sqlStatement);
    }
    
    private void checkNotExistDiscoveryType(final String schemaName, final AlterDatabaseDiscoveryTypeStatement sqlStatement,
                                            final DatabaseDiscoveryRuleConfiguration currentRuleConfig) throws DistSQLException {
        Collection<String> existTypes = currentRuleConfig.getDiscoveryTypes().keySet();
        Collection<String> notExistTypes = sqlStatement.getTypes().stream().map(DatabaseDiscoveryTypeSegment::getDiscoveryTypeName)
                .filter(each -> !existTypes.contains(each)).collect(Collectors.toSet());
        DistSQLException.predictionThrow(notExistTypes.isEmpty(), new RequiredRuleMissedException(RULE_TYPE, schemaName));
        
    }
    
    private void checkCurrentRuleConfiguration(final String schemaName, final DatabaseDiscoveryRuleConfiguration currentRuleConfig) throws DistSQLException {
        DistSQLException.predictionThrow(null != currentRuleConfig, new RequiredRuleMissedException(RULE_TYPE, schemaName));
    }
    
    private void checkDuplicateDiscoveryType(final String schemaName, final AlterDatabaseDiscoveryTypeStatement sqlStatement) throws DistSQLException {
        Collection<String> duplicateTypeNames = getToBeAlteredDuplicateTypeNames(sqlStatement);
        DistSQLException.predictionThrow(duplicateTypeNames.isEmpty(), new DuplicateRuleException(RULE_TYPE, schemaName, duplicateTypeNames));
    }
    
    private Collection<String> getToBeAlteredDuplicateTypeNames(final AlterDatabaseDiscoveryTypeStatement sqlStatement) {
        return sqlStatement.getTypes().stream().collect(Collectors.toMap(DatabaseDiscoveryTypeSegment::getDiscoveryTypeName, e -> 1, Integer::sum))
                .entrySet().stream().filter(entry -> entry.getValue() > 1).map(Entry::getKey).collect(Collectors.toSet());
    }
    
    private void checkInvalidDiscoverType(final AlterDatabaseDiscoveryTypeStatement sqlStatement) throws DistSQLException {
        List<String> invalidType = sqlStatement.getTypes().stream().map(each -> each.getAlgorithmSegment().getName()).distinct()
                .filter(each -> !TypedSPIRegistry.findRegisteredService(DatabaseDiscoveryType.class, each, new Properties()).isPresent()).collect(Collectors.toList());
        DistSQLException.predictionThrow(invalidType.isEmpty(), new InvalidAlgorithmConfigurationException(RULE_TYPE, invalidType));
    }
    
    @Override
    public RuleConfiguration buildToBeAlteredRuleConfiguration(final AlterDatabaseDiscoveryTypeStatement sqlStatement) {
        return DatabaseDiscoveryRuleStatementConverter.convertDiscoveryType(sqlStatement.getTypes());
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final DatabaseDiscoveryRuleConfiguration currentRuleConfig, final DatabaseDiscoveryRuleConfiguration toBeAlteredRuleConfig) {
        if (null != currentRuleConfig) {
            currentRuleConfig.getDiscoveryTypes().putAll(toBeAlteredRuleConfig.getDiscoveryTypes());
        }
    }
    
    @Override
    public Class<DatabaseDiscoveryRuleConfiguration> getRuleConfigurationClass() {
        return DatabaseDiscoveryRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return AlterDatabaseDiscoveryTypeStatement.class.getCanonicalName();
    }
}
