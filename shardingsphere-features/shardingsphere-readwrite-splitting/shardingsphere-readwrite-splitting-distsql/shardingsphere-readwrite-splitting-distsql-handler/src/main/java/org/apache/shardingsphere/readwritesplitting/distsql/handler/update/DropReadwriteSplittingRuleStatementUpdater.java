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

import org.apache.shardingsphere.infra.config.function.ResourceRequiredRuleConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RuleDefinitionViolationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RuleInUsedException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionDropUpdater;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.DropReadwriteSplittingRuleStatement;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Drop readwrite-splitting rule statement updater.
 */
public final class DropReadwriteSplittingRuleStatementUpdater implements RuleDefinitionDropUpdater<DropReadwriteSplittingRuleStatement, ReadwriteSplittingRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereMetaData shardingSphereMetaData, final DropReadwriteSplittingRuleStatement sqlStatement,
                                  final ReadwriteSplittingRuleConfiguration currentRuleConfig) throws RuleDefinitionViolationException {
        String schemaName = shardingSphereMetaData.getName();
        if (!isExistRuleConfig(currentRuleConfig) && sqlStatement.isContainsExistClause()) {
            return;
        }
        checkCurrentRuleConfiguration(schemaName, currentRuleConfig);
        checkToBeDroppedRuleNames(schemaName, sqlStatement, currentRuleConfig);
        checkToBeDroppedInUsed(schemaName, sqlStatement, shardingSphereMetaData);
    }
    
    private void checkCurrentRuleConfiguration(final String schemaName, final ReadwriteSplittingRuleConfiguration currentRuleConfig) throws RequiredRuleMissedException {
        if (null == currentRuleConfig) {
            throw new RequiredRuleMissedException("Readwrite splitting", schemaName);
        }
    }
    
    private void checkToBeDroppedRuleNames(final String schemaName, final DropReadwriteSplittingRuleStatement sqlStatement,
                                           final ReadwriteSplittingRuleConfiguration currentRuleConfig) throws RequiredRuleMissedException {
        if (sqlStatement.isContainsExistClause()) {
            return;
        }
        Collection<String> currentRuleNames = currentRuleConfig.getDataSources().stream().map(ReadwriteSplittingDataSourceRuleConfiguration::getName).collect(Collectors.toList());
        Collection<String> notExistedRuleNames = sqlStatement.getRuleNames().stream().filter(each -> !currentRuleNames.contains(each)).collect(Collectors.toList());
        if (!notExistedRuleNames.isEmpty()) {
            throw new RequiredRuleMissedException("Readwrite splitting", schemaName, sqlStatement.getRuleNames());
        }
    }
    
    private void checkToBeDroppedInUsed(final String schemaName, final DropReadwriteSplittingRuleStatement sqlStatement, 
                                        final ShardingSphereMetaData shardingSphereMetaData) throws RuleInUsedException {
        Collection<String> resourceBeUsed = shardingSphereMetaData.getRuleMetaData().findRuleConfiguration(ResourceRequiredRuleConfiguration.class).stream()
                .map(ResourceRequiredRuleConfiguration::getRequiredResource).flatMap(Collection::stream).collect(Collectors.toSet());
        Collection<String> ruleInUsed = sqlStatement.getRuleNames().stream().filter(resourceBeUsed::contains).collect(Collectors.toSet());
        if (!ruleInUsed.isEmpty()) {
            throw new RuleInUsedException("Readwrite splitting", schemaName, ruleInUsed);
        }
    }
    
    @Override
    public boolean updateCurrentRuleConfiguration(final DropReadwriteSplittingRuleStatement sqlStatement, final ReadwriteSplittingRuleConfiguration currentRuleConfig) {
        for (String each : sqlStatement.getRuleNames()) {
            dropRule(currentRuleConfig, each);
        }
        return currentRuleConfig.getDataSources().isEmpty();
    }
    
    private void dropRule(final ReadwriteSplittingRuleConfiguration currentRuleConfig, final String ruleName) {
        Optional<ReadwriteSplittingDataSourceRuleConfiguration> dataSourceRuleConfig
                = currentRuleConfig.getDataSources().stream().filter(dataSource -> ruleName.equals(dataSource.getName())).findAny();
        dataSourceRuleConfig.ifPresent(op -> {
            currentRuleConfig.getDataSources().remove(op);
            if (isLoadBalancerNotInUse(currentRuleConfig, op.getLoadBalancerName())) {
                currentRuleConfig.getLoadBalancers().remove(op.getLoadBalancerName());
            }
        });
    }
    
    private boolean isLoadBalancerNotInUse(final ReadwriteSplittingRuleConfiguration currentRuleConfig, final String toBeDroppedLoadBalancerName) {
        return currentRuleConfig.getDataSources().stream().map(ReadwriteSplittingDataSourceRuleConfiguration::getLoadBalancerName)
                .filter(Objects::nonNull).noneMatch(toBeDroppedLoadBalancerName::equals);
    }
    
    @Override
    public boolean hasAnyOneToBeDropped(final DropReadwriteSplittingRuleStatement sqlStatement, final ReadwriteSplittingRuleConfiguration currentRuleConfig) {
        return null != currentRuleConfig
                && !getIdenticalData(currentRuleConfig.getDataSources().stream().map(each -> each.getName()).collect(Collectors.toSet()), sqlStatement.getRuleNames()).isEmpty();
    }
    
    @Override
    public Class<ReadwriteSplittingRuleConfiguration> getRuleConfigurationClass() {
        return ReadwriteSplittingRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return DropReadwriteSplittingRuleStatement.class.getName();
    }
}
