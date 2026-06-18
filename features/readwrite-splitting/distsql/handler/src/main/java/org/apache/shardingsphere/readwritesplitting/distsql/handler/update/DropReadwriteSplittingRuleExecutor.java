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
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.type.DatabaseRuleDropExecutor;
import org.apache.shardingsphere.distsql.handler.required.DistSQLExecutorCurrentRuleRequired;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.InUsedRuleException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.infra.rule.attribute.datasource.DataSourceMapperRuleAttribute;
import org.apache.shardingsphere.readwritesplitting.config.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.config.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;
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
        Collection<String> currentRuleNames = rule.getConfiguration().getDataSourceGroups().stream().map(ReadwriteSplittingDataSourceGroupRuleConfiguration::getName).collect(Collectors.toList());
        Collection<String> notExistedRuleNames = sqlStatement.getNames().stream().filter(each -> !currentRuleNames.contains(each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkMustEmpty(notExistedRuleNames, () -> new MissingRequiredRuleException("Readwrite-splitting", database.getName(), sqlStatement.getNames()));
    }
    
    private void checkToBeDroppedInUsed(final DropReadwriteSplittingRuleStatement sqlStatement) {
        Collection<String> resourceBeUsed = getInUsedResources();
        Collection<String> ruleInUsed = sqlStatement.getNames().stream().filter(resourceBeUsed::contains).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkMustEmpty(ruleInUsed, () -> new InUsedRuleException("Readwrite-splitting", database.getName(), ruleInUsed));
    }
    
    private Collection<String> getInUsedResources() {
        Collection<String> result = new HashSet<>();
        for (ShardingSphereRule each : database.getRuleMetaData().getRules()) {
            if (each instanceof ReadwriteSplittingRule) {
                continue;
            }
            Optional<DataSourceMapperRuleAttribute> ruleAttribute = each.getAttributes().findAttribute(DataSourceMapperRuleAttribute.class);
            if (!ruleAttribute.isPresent()) {
                continue;
            }
            Collection<String> actualDataSources = new HashSet<>();
            ruleAttribute.get().getDataSourceMapper().values().forEach(actualDataSources::addAll);
            result.addAll(actualDataSources);
        }
        for (ShardingSphereRule each : database.getRuleMetaData().getRules()) {
            if (each instanceof SingleRule) {
                continue;
            }
            Collection<DataNode> actualDataNodes = new HashSet<>();
            Optional<DataNodeRuleAttribute> ruleAttribute = each.getAttributes().findAttribute(DataNodeRuleAttribute.class);
            if (ruleAttribute.isPresent()) {
                ruleAttribute.get().getAllDataNodes().values().forEach(actualDataNodes::addAll);
                result.addAll(actualDataNodes.stream().map(DataNode::getDataSourceName).collect(Collectors.toSet()));
            }
        }
        return result;
    }
    
    @Override
    public ReadwriteSplittingRuleConfiguration buildToBeDroppedRuleConfiguration(final DropReadwriteSplittingRuleStatement sqlStatement) {
        Collection<ReadwriteSplittingDataSourceGroupRuleConfiguration> toBeDroppedDataSourceGroups = new LinkedList<>();
        Map<String, AlgorithmConfiguration> toBeDroppedLoadBalancers = new HashMap<>();
        for (String each : sqlStatement.getNames()) {
            toBeDroppedDataSourceGroups.add(new ReadwriteSplittingDataSourceGroupRuleConfiguration(each, null, null, null));
            dropRule(each);
        }
        findUnusedLoadBalancers().forEach(each -> toBeDroppedLoadBalancers.put(each, rule.getConfiguration().getLoadBalancers().get(each)));
        return new ReadwriteSplittingRuleConfiguration(toBeDroppedDataSourceGroups, toBeDroppedLoadBalancers);
    }
    
    private void dropRule(final String ruleName) {
        Optional<ReadwriteSplittingDataSourceGroupRuleConfiguration> dataSourceGroupRuleConfig = rule.getConfiguration().getDataSourceGroups().stream()
                .filter(each -> ruleName.equals(each.getName())).findAny();
        dataSourceGroupRuleConfig.ifPresent(optional -> rule.getConfiguration().getDataSourceGroups().remove(optional));
    }
    
    private Collection<String> findUnusedLoadBalancers() {
        Collection<String> inUsedAlgorithms = rule.getConfiguration().getDataSourceGroups().stream()
                .map(ReadwriteSplittingDataSourceGroupRuleConfiguration::getLoadBalancerName).collect(Collectors.toSet());
        return rule.getConfiguration().getLoadBalancers().keySet().stream().filter(each -> !inUsedAlgorithms.contains(each)).collect(Collectors.toSet());
    }
    
    @Override
    public boolean hasAnyOneToBeDropped(final DropReadwriteSplittingRuleStatement sqlStatement) {
        return !Collections.disjoint(
                rule.getConfiguration().getDataSourceGroups().stream().map(ReadwriteSplittingDataSourceGroupRuleConfiguration::getName).collect(Collectors.toSet()), sqlStatement.getNames());
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
