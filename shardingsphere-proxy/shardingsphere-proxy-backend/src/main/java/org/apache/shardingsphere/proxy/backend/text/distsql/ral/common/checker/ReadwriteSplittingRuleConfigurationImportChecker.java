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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.checker;

import org.apache.shardingsphere.infra.config.TypedSPIConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.resource.RequiredResourceMissedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.spi.ReplicaLoadBalanceAlgorithm;
import org.apache.shardingsphere.sharding.support.InlineExpressionParser;
import org.apache.shardingsphere.spi.type.typed.TypedSPIRegistry;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Readwrite-splitting rule configuration import checker.
 */
public final class ReadwriteSplittingRuleConfigurationImportChecker {
    
    /**
     * Check readwrite-splitting rule configuration.
     *
     * @param shardingSphereMetaData ShardingSphere meta data
     * @param currentRuleConfig current rule configuration
     * @throws DistSQLException definition violation exception
     */
    public void check(final ShardingSphereMetaData shardingSphereMetaData, final ReadwriteSplittingRuleConfiguration currentRuleConfig) throws DistSQLException {
        if (null == shardingSphereMetaData || null == currentRuleConfig) {
            return;
        }
        String schemaName = shardingSphereMetaData.getName();
        checkResources(schemaName, shardingSphereMetaData, currentRuleConfig);
        checkLoadBalancers(currentRuleConfig);
    }
    
    private void checkResources(final String schemaName, final ShardingSphereMetaData shardingSphereMetaData, final ReadwriteSplittingRuleConfiguration currentRuleConfig) throws DistSQLException {
        Collection<String> requireResources = new LinkedHashSet<>();
        Collection<String> requireDiscoverableResources = new LinkedHashSet<>();
        currentRuleConfig.getDataSources().forEach(each -> {
            if (each.getAutoAwareDataSourceName().isPresent()) {
                requireDiscoverableResources.add(each.getAutoAwareDataSourceName().get());
            }
            if (each.getWriteDataSourceName().isPresent()) {
                requireResources.add(each.getWriteDataSourceName().get());
            }
            if (each.getReadDataSourceNames().isPresent()) {
                requireResources.addAll(new InlineExpressionParser(each.getReadDataSourceNames().get()).splitAndEvaluate());
            }
        });
        Collection<String> notExistResources = shardingSphereMetaData.getResource().getNotExistedResources(requireResources);
        DistSQLException.predictionThrow(notExistResources.isEmpty(), () -> new RequiredResourceMissedException(schemaName, notExistResources));
        Collection<String> logicResources = getLogicResources(shardingSphereMetaData);
        Collection<String> notExistLogicResources = requireDiscoverableResources.stream().filter(each -> !logicResources.contains(each)).collect(Collectors.toSet());
        DistSQLException.predictionThrow(notExistLogicResources.isEmpty(), () -> new RequiredResourceMissedException(schemaName, notExistLogicResources));
    }
    
    private Collection<String> getLogicResources(final ShardingSphereMetaData shardingSphereMetaData) {
        return shardingSphereMetaData.getRuleMetaData().getRules().stream().filter(each -> each instanceof DataSourceContainedRule)
                .map(each -> ((DataSourceContainedRule) each).getDataSourceMapper().keySet()).flatMap(Collection::stream).collect(Collectors.toCollection(LinkedHashSet::new));
    }
    
    private void checkLoadBalancers(final ReadwriteSplittingRuleConfiguration currentRuleConfig) throws DistSQLException {
        Collection<String> notExistedLoadBalancers = currentRuleConfig.getLoadBalancers().values().stream().map(TypedSPIConfiguration::getType)
                .filter(each -> !TypedSPIRegistry.findRegisteredService(ReplicaLoadBalanceAlgorithm.class, each, new Properties()).isPresent()).collect(Collectors.toList());
        DistSQLException.predictionThrow(notExistedLoadBalancers.isEmpty(), () -> new InvalidAlgorithmConfigurationException("Load balancers", notExistedLoadBalancers));
    }
}
