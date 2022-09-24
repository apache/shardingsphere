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
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.distsql.constant.ExportableConstants;
import org.apache.shardingsphere.infra.distsql.exception.resource.MissingRequiredResourcesException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionAlterUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.identifier.type.exportable.ExportableRule;
import org.apache.shardingsphere.infra.rule.identifier.type.exportable.RuleExportEngine;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.handler.checker.ReadwriteSplittingRuleStatementChecker;
import org.apache.shardingsphere.readwritesplitting.distsql.handler.converter.ReadwriteSplittingRuleStatementConverter;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.segment.ReadwriteSplittingRuleSegment;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.AlterReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.readwritesplitting.factory.ReadQueryLoadBalanceAlgorithmFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Alter readwrite-splitting rule statement updater.
 */
public final class AlterReadwriteSplittingRuleStatementUpdater implements RuleDefinitionAlterUpdater<AlterReadwriteSplittingRuleStatement, ReadwriteSplittingRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database, final AlterReadwriteSplittingRuleStatement sqlStatement, final ReadwriteSplittingRuleConfiguration currentRuleConfig) {
        String databaseName = database.getName();
        checkCurrentRuleConfiguration(databaseName, currentRuleConfig);
        checkToBeAlteredRules(databaseName, sqlStatement, currentRuleConfig);
        checkToBeAlteredResources(databaseName, sqlStatement, database);
        // TODO move all check methods to checker
        ReadwriteSplittingRuleStatementChecker.checkDuplicateResourceNames(databaseName, sqlStatement.getRules(), currentRuleConfig, false);
        checkToBeAlteredLoadBalancer(sqlStatement);
    }
    
    private void checkCurrentRuleConfiguration(final String databaseName, final ReadwriteSplittingRuleConfiguration currentRuleConfig) throws MissingRequiredRuleException {
        if (null == currentRuleConfig) {
            throw new MissingRequiredRuleException("Readwrite splitting", databaseName);
        }
    }
    
    private void checkToBeAlteredRules(final String databaseName, final AlterReadwriteSplittingRuleStatement sqlStatement,
                                       final ReadwriteSplittingRuleConfiguration currentRuleConfig) throws MissingRequiredRuleException {
        Collection<String> currentRuleNames = currentRuleConfig.getDataSources().stream().map(ReadwriteSplittingDataSourceRuleConfiguration::getName).collect(Collectors.toSet());
        Collection<String> notExistedRuleNames = getToBeAlteredRuleNames(sqlStatement).stream().filter(each -> !currentRuleNames.contains(each)).collect(Collectors.toList());
        if (!notExistedRuleNames.isEmpty()) {
            throw new MissingRequiredRuleException("Readwrite splitting", databaseName, notExistedRuleNames);
        }
    }
    
    private Collection<String> getToBeAlteredRuleNames(final AlterReadwriteSplittingRuleStatement sqlStatement) {
        return sqlStatement.getRules().stream().map(ReadwriteSplittingRuleSegment::getName).collect(Collectors.toSet());
    }
    
    private void checkToBeAlteredLoadBalancer(final AlterReadwriteSplittingRuleStatement sqlStatement) throws InvalidAlgorithmConfigurationException {
        Collection<String> invalidLoadBalancers = sqlStatement.getRules().stream().map(ReadwriteSplittingRuleSegment::getLoadBalancer).filter(Objects::nonNull).distinct()
                .filter(each -> !ReadQueryLoadBalanceAlgorithmFactory.contains(each)).collect(Collectors.toList());
        if (!invalidLoadBalancers.isEmpty()) {
            throw new InvalidAlgorithmConfigurationException("Load balancers", invalidLoadBalancers);
        }
    }
    
    private void checkToBeAlteredResources(final String databaseName, final AlterReadwriteSplittingRuleStatement sqlStatement, final ShardingSphereDatabase database) {
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
        Collection<String> notExistResources = database.getResource().getNotExistedResources(requireResources);
        ShardingSpherePreconditions.checkState(notExistResources.isEmpty(), () -> new MissingRequiredResourcesException(databaseName, notExistResources));
        Collection<String> logicResources = getLogicResources(database);
        Set<String> notExistLogicResources = requireDiscoverableResources.stream().filter(each -> !logicResources.contains(each)).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkState(notExistLogicResources.isEmpty(), () -> new MissingRequiredResourcesException(databaseName, notExistLogicResources));
    }
    
    @SuppressWarnings("unchecked")
    private Collection<String> getLogicResources(final ShardingSphereDatabase database) {
        Collection<String> result = new LinkedHashSet<>();
        Optional<ExportableRule> exportableRule = database.getRuleMetaData().findRules(ExportableRule.class).stream()
                .filter(each -> new RuleExportEngine(each).containExportableKey(Collections.singletonList(ExportableConstants.EXPORT_DB_DISCOVERY_PRIMARY_DATA_SOURCES))).findAny();
        exportableRule.ifPresent(optional -> {
            Map<String, Object> exportData = new RuleExportEngine(optional).export(Collections.singletonList(ExportableConstants.EXPORT_DB_DISCOVERY_PRIMARY_DATA_SOURCES));
            Set<String> logicResources = ((Map<String, String>) exportData.getOrDefault(ExportableConstants.EXPORT_DB_DISCOVERY_PRIMARY_DATA_SOURCES, Collections.emptyMap())).keySet();
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
            Optional<ReadwriteSplittingDataSourceRuleConfiguration> toBeRemovedDataSourceRuleConfig =
                    currentRuleConfig.getDataSources().stream().filter(dataSource -> each.getName().equals(dataSource.getName())).findAny();
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
