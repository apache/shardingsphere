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

package org.apache.shardingsphere.readwritesplitting.distsql.handler.update;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.distsql.constant.ExportableConstants;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.resource.RequiredResourceMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionAlterUpdater;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.ExportableRule;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.handler.converter.ReadwriteSplittingRuleStatementConverter;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.segment.ReadwriteSplittingRuleSegment;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.AlterReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.readwritesplitting.spi.ReplicaLoadBalanceAlgorithm;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.typed.TypedSPIRegistry;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Alter readwrite-splitting rule statement updater.
 */
public final class AlterReadwriteSplittingRuleStatementUpdater implements RuleDefinitionAlterUpdater<AlterReadwriteSplittingRuleStatement, ReadwriteSplittingRuleConfiguration> {
    
    static {
        // TODO consider about register once only
        ShardingSphereServiceLoader.register(ReplicaLoadBalanceAlgorithm.class);
    }
    
    @Override
    public void checkSQLStatement(final ShardingSphereMetaData shardingSphereMetaData, final AlterReadwriteSplittingRuleStatement sqlStatement, 
                                  final ReadwriteSplittingRuleConfiguration currentRuleConfig) throws DistSQLException {
        String schemaName = shardingSphereMetaData.getName();
        checkCurrentRuleConfiguration(schemaName, currentRuleConfig);
        checkToBeAlteredRules(schemaName, sqlStatement, currentRuleConfig);
        checkToBeAlteredResources(schemaName, sqlStatement, shardingSphereMetaData);
        checkToBeAlteredLoadBalancer(sqlStatement);
    }
    
    private void checkCurrentRuleConfiguration(final String schemaName, final ReadwriteSplittingRuleConfiguration currentRuleConfig) throws RequiredRuleMissedException {
        if (null == currentRuleConfig) {
            throw new RequiredRuleMissedException("Readwrite splitting", schemaName);
        }
    }
    
    private void checkToBeAlteredRules(final String schemaName, final AlterReadwriteSplittingRuleStatement sqlStatement, 
                                       final ReadwriteSplittingRuleConfiguration currentRuleConfig) throws RequiredRuleMissedException {
        Collection<String> currentRuleNames = currentRuleConfig.getDataSources().stream().map(ReadwriteSplittingDataSourceRuleConfiguration::getName).collect(Collectors.toSet());
        Collection<String> notExistedRuleNames = getToBeAlteredRuleNames(sqlStatement).stream().filter(each -> !currentRuleNames.contains(each)).collect(Collectors.toList());
        if (!notExistedRuleNames.isEmpty()) {
            throw new RequiredRuleMissedException("Readwrite splitting", schemaName, notExistedRuleNames);
        }
    }
    
    private Collection<String> getToBeAlteredRuleNames(final AlterReadwriteSplittingRuleStatement sqlStatement) {
        return sqlStatement.getRules().stream().map(ReadwriteSplittingRuleSegment::getName).collect(Collectors.toSet());
    }
    
    private void checkToBeAlteredLoadBalancer(final AlterReadwriteSplittingRuleStatement sqlStatement) throws InvalidAlgorithmConfigurationException {
        Collection<String> invalidLoadBalancers = sqlStatement.getRules().stream().map(ReadwriteSplittingRuleSegment::getLoadBalancer).filter(Objects::nonNull).distinct()
                .filter(each -> !TypedSPIRegistry.findRegisteredService(ReplicaLoadBalanceAlgorithm.class, each, new Properties()).isPresent()).collect(Collectors.toList());
        if (!invalidLoadBalancers.isEmpty()) {
            throw new InvalidAlgorithmConfigurationException("Load balancers", invalidLoadBalancers);
        }
    }
    
    private void checkToBeAlteredResources(final String schemaName, final AlterReadwriteSplittingRuleStatement sqlStatement,
                                           final ShardingSphereMetaData shardingSphereMetaData) throws DistSQLException {
        Collection<String> requireResources = new LinkedHashSet<>();
        Collection<String> requireDiscoverableResources = new LinkedHashSet<>();
        sqlStatement.getRules().forEach(each -> {
            if (Strings.isNullOrEmpty(each.getAutoAwareResource())) {
                requireResources.add(each.getWriteDataSource());
                requireResources.addAll(each.getReadDataSources());
            } else {
                requireDiscoverableResources.add(each.getAutoAwareResource());
            }
        });
        Collection<String> notExistResources = shardingSphereMetaData.getResource().getNotExistedResources(requireResources);
        DistSQLException.predictionThrow(notExistResources.isEmpty(), () -> new RequiredResourceMissedException(schemaName, notExistResources));
        Collection<String> logicResources = getLogicResources(shardingSphereMetaData);
        Set<String> notExistLogicResources = requireDiscoverableResources.stream().filter(each -> !logicResources.contains(each)).collect(Collectors.toSet());
        DistSQLException.predictionThrow(notExistLogicResources.isEmpty(), () -> new RequiredResourceMissedException(schemaName, notExistLogicResources));
    }
    
    private Collection<String> getLogicResources(final ShardingSphereMetaData shardingSphereMetaData) {
        Collection<String> result = new LinkedHashSet<>();
        Optional<ExportableRule> exportableRule = shardingSphereMetaData.getRuleMetaData().findRules(ExportableRule.class).stream()
                .filter(each -> each.containExportableKey(Collections.singletonList(ExportableConstants.EXPORTABLE_KEY_PRIMARY_DATA_SOURCE))).findAny();
        exportableRule.ifPresent(op -> {
            Map<String, Object> exportData = op.export(Collections.singletonList(ExportableConstants.EXPORTABLE_KEY_PRIMARY_DATA_SOURCE));
            Set<String> logicResources = ((Map<String, String>) exportData.getOrDefault(ExportableConstants.EXPORTABLE_KEY_PRIMARY_DATA_SOURCE, Collections.emptyMap())).keySet();
            result.addAll(logicResources);
        });
        return result;
    }
    
    @Override
    public RuleConfiguration buildToBeAlteredRuleConfiguration(final AlterReadwriteSplittingRuleStatement sqlStatement) {
        return ReadwriteSplittingRuleStatementConverter.convert(sqlStatement.getRules());
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final ReadwriteSplittingRuleConfiguration currentRuleConfig, final ReadwriteSplittingRuleConfiguration toBeAlteredRuleConfig) {
        dropRuleConfiguration(currentRuleConfig, toBeAlteredRuleConfig);
        addRuleConfiguration(currentRuleConfig, toBeAlteredRuleConfig);
    }
    
    private void dropRuleConfiguration(final ReadwriteSplittingRuleConfiguration currentRuleConfig, final ReadwriteSplittingRuleConfiguration toBeAlteredRuleConfig) {
        for (ReadwriteSplittingDataSourceRuleConfiguration each : toBeAlteredRuleConfig.getDataSources()) {
            Optional<ReadwriteSplittingDataSourceRuleConfiguration> toBeRemovedDataSourceRuleConfig
                    = currentRuleConfig.getDataSources().stream().filter(dataSource -> each.getName().equals(dataSource.getName())).findAny();
            Preconditions.checkState(toBeRemovedDataSourceRuleConfig.isPresent());
            currentRuleConfig.getDataSources().remove(toBeRemovedDataSourceRuleConfig.get());
            currentRuleConfig.getLoadBalancers().remove(toBeRemovedDataSourceRuleConfig.get().getLoadBalancerName());
        }
    }
    
    private void addRuleConfiguration(final ReadwriteSplittingRuleConfiguration currentRuleConfig, final ReadwriteSplittingRuleConfiguration toBeAlteredRuleConfig) {
        currentRuleConfig.getDataSources().addAll(toBeAlteredRuleConfig.getDataSources());
        currentRuleConfig.getLoadBalancers().putAll(toBeAlteredRuleConfig.getLoadBalancers());
    }
    
    @Override
    public Class<ReadwriteSplittingRuleConfiguration> getRuleConfigurationClass() {
        return ReadwriteSplittingRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return AlterReadwriteSplittingRuleStatement.class.getName();
    }
}
