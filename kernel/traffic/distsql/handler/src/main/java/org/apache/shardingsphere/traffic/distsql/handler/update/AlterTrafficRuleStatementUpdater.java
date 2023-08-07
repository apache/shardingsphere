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

package org.apache.shardingsphere.traffic.distsql.handler.update;

import org.apache.shardingsphere.distsql.handler.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.distsql.handler.ral.update.GlobalRuleRALUpdater;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.traffic.api.config.TrafficRuleConfiguration;
import org.apache.shardingsphere.traffic.api.config.TrafficStrategyConfiguration;
import org.apache.shardingsphere.traffic.distsql.handler.convert.TrafficRuleConverter;
import org.apache.shardingsphere.traffic.distsql.parser.segment.TrafficRuleSegment;
import org.apache.shardingsphere.traffic.distsql.parser.statement.updatable.AlterTrafficRuleStatement;
import org.apache.shardingsphere.traffic.spi.TrafficAlgorithm;
import org.apache.shardingsphere.traffic.spi.TrafficLoadBalanceAlgorithm;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Alter traffic rule statement updater.
 */
public final class AlterTrafficRuleStatementUpdater implements GlobalRuleRALUpdater<AlterTrafficRuleStatement, TrafficRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final TrafficRuleConfiguration currentRuleConfig, final AlterTrafficRuleStatement sqlStatement) {
        checkRuleNames(currentRuleConfig, sqlStatement);
        checkAlgorithmNames(sqlStatement);
    }
    
    private void checkRuleNames(final TrafficRuleConfiguration currentRuleConfig, final AlterTrafficRuleStatement sqlStatement) {
        Collection<String> notExistRuleNames = getNotExistRuleNames(currentRuleConfig, sqlStatement);
        ShardingSpherePreconditions.checkState(notExistRuleNames.isEmpty(), () -> new MissingRequiredRuleException("Traffic", notExistRuleNames));
    }
    
    private Collection<String> getNotExistRuleNames(final TrafficRuleConfiguration currentRuleConfig, final AlterTrafficRuleStatement sqlStatement) {
        Collection<String> currentRuleNames = currentRuleConfig.getTrafficStrategies().stream().map(TrafficStrategyConfiguration::getName).collect(Collectors.toSet());
        return sqlStatement.getSegments().stream().map(TrafficRuleSegment::getName).filter(each -> !currentRuleNames.contains(each)).collect(Collectors.toSet());
    }
    
    private void checkAlgorithmNames(final AlterTrafficRuleStatement sqlStatement) {
        sqlStatement.getSegments().forEach(each -> {
            TypedSPILoader.checkService(TrafficAlgorithm.class, each.getAlgorithm().getName(), each.getAlgorithm().getProps());
            if (null != each.getLoadBalancer()) {
                TypedSPILoader.checkService(TrafficLoadBalanceAlgorithm.class, each.getLoadBalancer().getName(), each.getLoadBalancer().getProps());
            }
        });
    }
    
    @Override
    public TrafficRuleConfiguration buildAlteredRuleConfiguration(final TrafficRuleConfiguration currentRuleConfig, final AlterTrafficRuleStatement sqlStatement) {
        TrafficRuleConfiguration result = new TrafficRuleConfiguration();
        TrafficRuleConfiguration configFromSQLStatement = TrafficRuleConverter.convert(sqlStatement.getSegments());
        result.getTrafficStrategies().addAll(createToBeAlteredStrategyConfigurations(currentRuleConfig, configFromSQLStatement));
        result.getTrafficAlgorithms().putAll(createToBeAlteredTrafficAlgorithms(currentRuleConfig, configFromSQLStatement, getInUsedTrafficAlgorithm(result)));
        result.getLoadBalancers().putAll(createToBeAlteredLoadBalancers(currentRuleConfig, configFromSQLStatement, getInUsedLoadBalancer(result)));
        return result;
    }
    
    private Collection<TrafficStrategyConfiguration> createToBeAlteredStrategyConfigurations(final TrafficRuleConfiguration currentConfig, final TrafficRuleConfiguration configFromSQLStatement) {
        Collection<TrafficStrategyConfiguration> result = new LinkedList<>(currentConfig.getTrafficStrategies());
        Collection<String> toBeAlteredConfigNames = configFromSQLStatement.getTrafficStrategies().stream().map(TrafficStrategyConfiguration::getName).collect(Collectors.toSet());
        result.removeIf(each -> toBeAlteredConfigNames.contains(each.getName()));
        result.addAll(configFromSQLStatement.getTrafficStrategies());
        return result;
    }
    
    private Collection<String> getInUsedTrafficAlgorithm(final TrafficRuleConfiguration config) {
        return config.getTrafficStrategies().stream().map(TrafficStrategyConfiguration::getAlgorithmName).collect(Collectors.toSet());
    }
    
    private Map<String, AlgorithmConfiguration> createToBeAlteredTrafficAlgorithms(final TrafficRuleConfiguration currentConfig, final TrafficRuleConfiguration configFromSQLStatement,
                                                                                   final Collection<String> inUsedTrafficAlgorithm) {
        Map<String, AlgorithmConfiguration> result = new LinkedHashMap<>(currentConfig.getTrafficAlgorithms());
        result.putAll(configFromSQLStatement.getTrafficAlgorithms());
        for (String each : result.keySet().stream().filter(each -> !inUsedTrafficAlgorithm.contains(each)).collect(Collectors.toSet())) {
            result.remove(each);
        }
        return result;
    }
    
    private Collection<String> getInUsedLoadBalancer(final TrafficRuleConfiguration config) {
        return config.getTrafficStrategies().stream().map(TrafficStrategyConfiguration::getLoadBalancerName).collect(Collectors.toSet());
    }
    
    private Map<String, AlgorithmConfiguration> createToBeAlteredLoadBalancers(final TrafficRuleConfiguration currentConfig,
                                                                               final TrafficRuleConfiguration configFromSQLStatement, final Collection<String> inUsedLoadBalancer) {
        Map<String, AlgorithmConfiguration> result = new LinkedHashMap<>(currentConfig.getLoadBalancers());
        result.putAll(configFromSQLStatement.getLoadBalancers());
        for (String each : result.keySet().stream().filter(each -> !inUsedLoadBalancer.contains(each)).collect(Collectors.toSet())) {
            result.remove(each);
        }
        return result;
    }
    
    @Override
    public Class<TrafficRuleConfiguration> getRuleConfigurationClass() {
        return TrafficRuleConfiguration.class;
    }
    
    @Override
    public Class<AlterTrafficRuleStatement> getType() {
        return AlterTrafficRuleStatement.class;
    }
}
