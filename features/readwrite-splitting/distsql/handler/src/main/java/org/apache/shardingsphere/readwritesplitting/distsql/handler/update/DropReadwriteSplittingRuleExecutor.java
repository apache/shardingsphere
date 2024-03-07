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

import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.DatabaseRuleDropExecutor;
import org.apache.shardingsphere.distsql.handler.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.distsql.handler.exception.rule.RuleInUsedException;
import org.apache.shardingsphere.distsql.handler.required.DistSQLExecutorCurrentRuleRequired;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.datanode.DataNodeRule;
import org.apache.shardingsphere.infra.rule.identifier.type.datasource.DataSourceMapperRule;
import org.apache.shardingsphere.infra.rule.identifier.type.datasource.StaticDataSourceRule;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.statement.DropReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;
import org.apache.shardingsphere.single.rule.SingleRule;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Drop readwrite-splitting rule executor.
 */
@DistSQLExecutorCurrentRuleRequired(ReadwriteSplittingRule.class)
@Setter
public final class DropReadwriteSplittingRuleExecutor implements DatabaseRuleDropExecutor<DropReadwriteSplittingRuleStatement, ReadwriteSplittingRule, ReadwriteSplittingRuleConfiguration> {
    
    private ShardingSphereDatabase database;
    
    private ReadwriteSplittingRule rule;
    
    @Override
    public void checkBeforeUpdate(final DropReadwriteSplittingRuleStatement sqlStatement) {
        if (!sqlStatement.isIfExists()) {
            checkToBeDroppedRuleNames(sqlStatement);
        }
        checkToBeDroppedInUsed(sqlStatement);
    }
    
    private void checkToBeDroppedRuleNames(final DropReadwriteSplittingRuleStatement sqlStatement) {
        Collection<String> currentRuleNames = rule.getConfiguration().getDataSources().stream().map(ReadwriteSplittingDataSourceRuleConfiguration::getName).collect(Collectors.toList());
        Collection<String> notExistedRuleNames = sqlStatement.getNames().stream().filter(each -> !currentRuleNames.contains(each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(notExistedRuleNames.isEmpty(), () -> new MissingRequiredRuleException("Readwrite-splitting", database.getName(), sqlStatement.getNames()));
    }
    
    private void checkToBeDroppedInUsed(final DropReadwriteSplittingRuleStatement sqlStatement) {
        Collection<String> resourceBeUsed = getInUsedResources();
        Collection<String> ruleInUsed = sqlStatement.getNames().stream().filter(resourceBeUsed::contains).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkState(ruleInUsed.isEmpty(), () -> new RuleInUsedException("Readwrite-splitting", database.getName(), ruleInUsed));
    }
    
    private Collection<String> getInUsedResources() {
        Collection<String> result = new HashSet<>();
        for (ShardingSphereRule each : database.getRuleMetaData().getRules()) {
            if (each instanceof ReadwriteSplittingRule) {
                continue;
            }
            Optional<DataSourceMapperRule> dataSourceMapperRule = each.getRuleIdentifiers().findIdentifier(DataSourceMapperRule.class);
            if (!dataSourceMapperRule.isPresent()) {
                continue;
            }
            Collection<String> actualDataSources = new HashSet<>();
            
            dataSourceMapperRule.get().getDataSourceMapper().values().forEach(actualDataSources::addAll);
            result.addAll(actualDataSources);
        }
        for (ShardingSphereRule each : database.getRuleMetaData().getRules()) {
            if (each instanceof SingleRule) {
                continue;
            }
            Collection<DataNode> actualDataNodes = new HashSet<>();
            Optional<DataNodeRule> dataNodeRule = each.getRuleIdentifiers().findIdentifier(DataNodeRule.class);
            if (dataNodeRule.isPresent()) {
                dataNodeRule.get().getAllDataNodes().values().forEach(actualDataNodes::addAll);
                result.addAll(actualDataNodes.stream().map(DataNode::getDataSourceName).collect(Collectors.toSet()));
            }
        }
        return result;
    }
    
    @Override
    public ReadwriteSplittingRuleConfiguration buildToBeDroppedRuleConfiguration(final DropReadwriteSplittingRuleStatement sqlStatement) {
        Collection<ReadwriteSplittingDataSourceRuleConfiguration> toBeDroppedDataSources = new LinkedList<>();
        Map<String, AlgorithmConfiguration> toBeDroppedLoadBalancers = new HashMap<>();
        for (String each : sqlStatement.getNames()) {
            toBeDroppedDataSources.add(new ReadwriteSplittingDataSourceRuleConfiguration(each, null, null, null));
            dropRule(each);
        }
        findUnusedLoadBalancers().forEach(each -> toBeDroppedLoadBalancers.put(each, rule.getConfiguration().getLoadBalancers().get(each)));
        return new ReadwriteSplittingRuleConfiguration(toBeDroppedDataSources, toBeDroppedLoadBalancers);
    }
    
    @Override
    public boolean updateCurrentRuleConfiguration(final DropReadwriteSplittingRuleStatement sqlStatement, final ReadwriteSplittingRuleConfiguration currentRuleConfig) {
        for (String each : sqlStatement.getNames()) {
            dropRule(each);
        }
        dropUnusedLoadBalancer(currentRuleConfig);
        return currentRuleConfig.isEmpty();
    }
    
    private void dropRule(final String ruleName) {
        Optional<ReadwriteSplittingDataSourceRuleConfiguration> dataSourceRuleConfig = rule.getConfiguration().getDataSources().stream().filter(each -> ruleName.equals(each.getName())).findAny();
        dataSourceRuleConfig.ifPresent(optional -> rule.getConfiguration().getDataSources().remove(optional));
    }
    
    private void dropUnusedLoadBalancer(final ReadwriteSplittingRuleConfiguration currentRuleConfig) {
        findUnusedLoadBalancers().forEach(each -> currentRuleConfig.getLoadBalancers().remove(each));
    }
    
    private Collection<String> findUnusedLoadBalancers() {
        Collection<String> inUsedAlgorithms = rule.getConfiguration().getDataSources().stream().map(ReadwriteSplittingDataSourceRuleConfiguration::getLoadBalancerName).collect(Collectors.toSet());
        return rule.getConfiguration().getLoadBalancers().keySet().stream().filter(each -> !inUsedAlgorithms.contains(each)).collect(Collectors.toSet());
    }
    
    @Override
    public boolean hasAnyOneToBeDropped(final DropReadwriteSplittingRuleStatement sqlStatement) {
        return !Collections.disjoint(
                rule.getConfiguration().getDataSources().stream().map(ReadwriteSplittingDataSourceRuleConfiguration::getName).collect(Collectors.toSet()), sqlStatement.getNames());
    }
    
    @Override
    public void operate(final DropReadwriteSplittingRuleStatement sqlStatement, final ShardingSphereDatabase database) {
        for (ShardingSphereRule each : database.getRuleMetaData().getRules()) {
            each.getRuleIdentifiers().findIdentifier(StaticDataSourceRule.class).ifPresent(optional -> sqlStatement.getNames().forEach(optional::cleanStorageNodeDataSource));
        }
    }
    
    @Override
    public Class<ReadwriteSplittingRule> getRuleClass() {
        return ReadwriteSplittingRule.class;
    }
    
    @Override
    public Class<DropReadwriteSplittingRuleStatement> getType() {
        return DropReadwriteSplittingRuleStatement.class;
    }
}
