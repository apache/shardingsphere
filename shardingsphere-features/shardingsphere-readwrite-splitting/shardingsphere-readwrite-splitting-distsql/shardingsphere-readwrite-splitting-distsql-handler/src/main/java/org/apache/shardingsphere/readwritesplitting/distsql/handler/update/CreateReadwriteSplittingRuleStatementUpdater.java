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

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.distsql.constant.ExportableConstants;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.resource.RequiredResourceMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidRuleConfigurationException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionCreateUpdater;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.rule.identifier.type.ExportableRule;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.handler.converter.ReadwriteSplittingRuleStatementConverter;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.segment.ReadwriteSplittingRuleSegment;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.CreateReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.readwritesplitting.spi.ReplicaLoadBalanceAlgorithm;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.type.typed.TypedSPIRegistry;

import java.util.ArrayList;
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
 * Create readwrite-splitting rule statement updater.
 */
public final class CreateReadwriteSplittingRuleStatementUpdater implements RuleDefinitionCreateUpdater<CreateReadwriteSplittingRuleStatement, ReadwriteSplittingRuleConfiguration> {
    
    static {
        // TODO consider about register once only
        ShardingSphereServiceLoader.register(ReplicaLoadBalanceAlgorithm.class);
    }
    
    @Override
    public void checkSQLStatement(final ShardingSphereMetaData shardingSphereMetaData, final CreateReadwriteSplittingRuleStatement sqlStatement, 
                                  final ReadwriteSplittingRuleConfiguration currentRuleConfig) throws DistSQLException {
        String schemaName = shardingSphereMetaData.getName();
        checkDuplicateRuleNames(schemaName, sqlStatement, currentRuleConfig, shardingSphereMetaData.getResource());
        checkToBeCreatedResources(schemaName, sqlStatement, shardingSphereMetaData);
        checkToBeCreatedLoadBalancers(sqlStatement);
    }
    
    private void checkDuplicateRuleNames(final String schemaName, final CreateReadwriteSplittingRuleStatement sqlStatement,
                                         final ReadwriteSplittingRuleConfiguration currentRuleConfig, final ShardingSphereResource resource) throws DistSQLException {
        Collection<String> currentRuleNames = new ArrayList<>();
        if (null != resource && null != resource.getDataSources()) {
            currentRuleNames.addAll(resource.getDataSources().keySet());
        }
        Collection<String> duplicateRuleNames = sqlStatement.getRules().stream().map(ReadwriteSplittingRuleSegment::getName).filter(currentRuleNames::contains).collect(Collectors.toList());
        if (!duplicateRuleNames.isEmpty()) {
            throw new InvalidRuleConfigurationException("readwrite splitting", duplicateRuleNames, Collections.singleton(String.format("%s already exists in resource", duplicateRuleNames)));
        }
        if (null != currentRuleConfig) {
            currentRuleNames.addAll(currentRuleConfig.getDataSources().stream().map(ReadwriteSplittingDataSourceRuleConfiguration::getName).collect(Collectors.toList()));
        }
        duplicateRuleNames = sqlStatement.getRules().stream().map(ReadwriteSplittingRuleSegment::getName).filter(currentRuleNames::contains).collect(Collectors.toList());
        if (!duplicateRuleNames.isEmpty()) {
            throw new DuplicateRuleException("readwrite splitting", schemaName, duplicateRuleNames);
        }
    }
    
    private void checkToBeCreatedResources(final String schemaName, final CreateReadwriteSplittingRuleStatement sqlStatement,
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
        exportableRule.ifPresent(optional -> {
            Map<String, Object> exportData = optional.export(Collections.singletonList(ExportableConstants.EXPORTABLE_KEY_PRIMARY_DATA_SOURCE));
            Set<String> logicResources = ((Map<String, String>) exportData.getOrDefault(ExportableConstants.EXPORTABLE_KEY_PRIMARY_DATA_SOURCE, Collections.emptyMap())).keySet();
            result.addAll(logicResources);
        });
        return result;
    }
    
    private void checkToBeCreatedLoadBalancers(final CreateReadwriteSplittingRuleStatement sqlStatement) throws InvalidAlgorithmConfigurationException {
        Collection<String> notExistedLoadBalancers = sqlStatement.getRules().stream().map(ReadwriteSplittingRuleSegment::getLoadBalancer).filter(Objects::nonNull).distinct()
                .filter(each -> !TypedSPIRegistry.findRegisteredService(ReplicaLoadBalanceAlgorithm.class, each, new Properties()).isPresent()).collect(Collectors.toList());
        if (!notExistedLoadBalancers.isEmpty()) {
            throw new InvalidAlgorithmConfigurationException("Load balancers", notExistedLoadBalancers);
        }
    }
    
    @Override
    public ReadwriteSplittingRuleConfiguration buildToBeCreatedRuleConfiguration(final CreateReadwriteSplittingRuleStatement sqlStatement) {
        return ReadwriteSplittingRuleStatementConverter.convert(sqlStatement.getRules());
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final ReadwriteSplittingRuleConfiguration currentRuleConfig, final ReadwriteSplittingRuleConfiguration toBeCreatedRuleConfig) {
        if (null != currentRuleConfig) {
            currentRuleConfig.getDataSources().addAll(toBeCreatedRuleConfig.getDataSources());
            currentRuleConfig.getLoadBalancers().putAll(toBeCreatedRuleConfig.getLoadBalancers());
        }
    }
    
    @Override
    public Class<ReadwriteSplittingRuleConfiguration> getRuleConfigurationClass() {
        return ReadwriteSplittingRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return CreateReadwriteSplittingRuleStatement.class.getName();
    }
}
