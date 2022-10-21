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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.checker;

import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.resource.MissingRequiredResourcesException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.factory.ReadQueryLoadBalanceAlgorithmFactory;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

/**
 * Readwrite-splitting rule configuration import checker.
 */
public final class ReadwriteSplittingRuleConfigurationImportChecker {
    
    /**
     * Check readwrite-splitting rule configuration.
     *
     * @param database database
     * @param currentRuleConfig current rule configuration
     */
    public void check(final ShardingSphereDatabase database, final ReadwriteSplittingRuleConfiguration currentRuleConfig) {
        if (null == database || null == currentRuleConfig) {
            return;
        }
        String databaseName = database.getName();
        checkResources(databaseName, database, currentRuleConfig);
        checkLoadBalancers(currentRuleConfig);
    }
    
    private void checkResources(final String databaseName, final ShardingSphereDatabase database, final ReadwriteSplittingRuleConfiguration currentRuleConfig) {
        Collection<String> requireResources = new LinkedHashSet<>();
        Collection<String> requireDiscoverableResources = new LinkedHashSet<>();
        currentRuleConfig.getDataSources().forEach(each -> {
            if (null != each.getDynamicStrategy()) {
                requireDiscoverableResources.add(each.getDynamicStrategy().getAutoAwareDataSourceName());
            }
            if (null != each.getStaticStrategy()) {
                if (null != each.getStaticStrategy().getWriteDataSourceName()) {
                    requireResources.add(each.getStaticStrategy().getWriteDataSourceName());
                }
                if (!each.getStaticStrategy().getReadDataSourceNames().isEmpty()) {
                    requireResources.addAll(each.getStaticStrategy().getReadDataSourceNames());
                }
            }
        });
        Collection<String> notExistResources = database.getResourceMetaData().getNotExistedResources(requireResources);
        ShardingSpherePreconditions.checkState(notExistResources.isEmpty(), () -> new MissingRequiredResourcesException(databaseName, notExistResources));
        Collection<String> logicResources = getLogicResources(database);
        Collection<String> notExistLogicResources = requireDiscoverableResources.stream().filter(each -> !logicResources.contains(each)).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkState(notExistLogicResources.isEmpty(), () -> new MissingRequiredResourcesException(databaseName, notExistLogicResources));
    }
    
    private Collection<String> getLogicResources(final ShardingSphereDatabase database) {
        return database.getRuleMetaData().getRules().stream().filter(each -> each instanceof DataSourceContainedRule)
                .map(each -> ((DataSourceContainedRule) each).getDataSourceMapper().keySet()).flatMap(Collection::stream).collect(Collectors.toCollection(LinkedHashSet::new));
    }
    
    private void checkLoadBalancers(final ReadwriteSplittingRuleConfiguration currentRuleConfig) {
        Collection<String> notExistedLoadBalancers = currentRuleConfig.getLoadBalancers().values().stream().map(AlgorithmConfiguration::getType)
                .filter(each -> !ReadQueryLoadBalanceAlgorithmFactory.contains(each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(notExistedLoadBalancers.isEmpty(), () -> new InvalidAlgorithmConfigurationException("Load balancers", notExistedLoadBalancers));
    }
}
