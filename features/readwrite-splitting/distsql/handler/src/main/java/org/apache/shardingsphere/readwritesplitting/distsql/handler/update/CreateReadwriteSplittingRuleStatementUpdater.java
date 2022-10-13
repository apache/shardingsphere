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
import org.apache.shardingsphere.infra.distsql.exception.resource.MissingRequiredResourcesException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidRuleConfigurationException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionCreateUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.exportable.ExportableRule;
import org.apache.shardingsphere.infra.rule.identifier.type.exportable.RuleExportEngine;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.handler.checker.ReadwriteSplittingRuleStatementChecker;
import org.apache.shardingsphere.readwritesplitting.distsql.handler.converter.ReadwriteSplittingRuleStatementConverter;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.segment.ReadwriteSplittingRuleSegment;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.CreateReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.readwritesplitting.factory.ReadQueryLoadBalanceAlgorithmFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Create readwrite-splitting rule statement updater.
 */
public final class CreateReadwriteSplittingRuleStatementUpdater implements RuleDefinitionCreateUpdater<CreateReadwriteSplittingRuleStatement, ReadwriteSplittingRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database, final CreateReadwriteSplittingRuleStatement sqlStatement, final ReadwriteSplittingRuleConfiguration currentRuleConfig) {
        String databaseName = database.getName();
        checkDuplicateRuleNames(databaseName, sqlStatement, currentRuleConfig, database.getResourceMetaData());
        checkToBeCreatedResources(databaseName, sqlStatement, database);
        // TODO move all check methods to checker
        ReadwriteSplittingRuleStatementChecker.checkDuplicateResourceNames(databaseName, sqlStatement.getRules(), currentRuleConfig, true);
        checkToBeCreatedLoadBalancers(sqlStatement);
    }
    
    private void checkDuplicateRuleNames(final String databaseName, final CreateReadwriteSplittingRuleStatement sqlStatement,
                                         final ReadwriteSplittingRuleConfiguration currentRuleConfig, final ShardingSphereResourceMetaData resourceMetaData) {
        Collection<String> currentRuleNames = new LinkedList<>();
        if (null != resourceMetaData && null != resourceMetaData.getDataSources()) {
            currentRuleNames.addAll(resourceMetaData.getDataSources().keySet());
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
            throw new DuplicateRuleException("readwrite splitting", databaseName, duplicateRuleNames);
        }
    }
    
    private void checkToBeCreatedResources(final String databaseName, final CreateReadwriteSplittingRuleStatement sqlStatement, final ShardingSphereDatabase database) {
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
        Collection<String> notExistResources = database.getResourceMetaData().getNotExistedResources(requireResources);
        ShardingSpherePreconditions.checkState(notExistResources.isEmpty(), () -> new MissingRequiredResourcesException(databaseName, notExistResources));
        Collection<String> logicResources = getLogicResources(database);
        Collection<String> notExistLogicResources = requireDiscoverableResources.stream().filter(each -> !logicResources.contains(each)).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkState(notExistLogicResources.isEmpty(), () -> new MissingRequiredResourcesException(databaseName, notExistLogicResources));
    }
    
    @SuppressWarnings("unchecked")
    private Collection<String> getLogicResources(final ShardingSphereDatabase database) {
        Collection<String> result = new LinkedHashSet<>();
        Optional<ExportableRule> exportableRule = database.getRuleMetaData().findRules(ExportableRule.class).stream()
                .filter(each -> new RuleExportEngine(each).containExportableKey(Collections.singletonList(ExportableConstants.EXPORT_DB_DISCOVERY_PRIMARY_DATA_SOURCES))).findAny();
        exportableRule.ifPresent(optional -> {
            Map<String, Object> exportData = new RuleExportEngine(optional).export(Collections.singletonList(ExportableConstants.EXPORT_DB_DISCOVERY_PRIMARY_DATA_SOURCES));
            Collection<String> logicResources = ((Map<String, String>) exportData.getOrDefault(ExportableConstants.EXPORT_DB_DISCOVERY_PRIMARY_DATA_SOURCES, Collections.emptyMap())).keySet();
            result.addAll(logicResources);
        });
        return result;
    }
    
    private void checkToBeCreatedLoadBalancers(final CreateReadwriteSplittingRuleStatement sqlStatement) throws InvalidAlgorithmConfigurationException {
        Collection<String> notExistedLoadBalancers = sqlStatement.getRules().stream().map(ReadwriteSplittingRuleSegment::getLoadBalancer).filter(Objects::nonNull).distinct()
                .filter(each -> !ReadQueryLoadBalanceAlgorithmFactory.contains(each)).collect(Collectors.toList());
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
