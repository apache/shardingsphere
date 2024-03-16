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

import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.global.GlobalRuleDefinitionExecutor;
import org.apache.shardingsphere.distsql.handler.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.loadbalancer.core.LoadBalanceAlgorithm;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.traffic.api.config.TrafficRuleConfiguration;
import org.apache.shardingsphere.traffic.api.config.TrafficStrategyConfiguration;
import org.apache.shardingsphere.traffic.distsql.handler.convert.TrafficRuleConverter;
import org.apache.shardingsphere.traffic.distsql.segment.TrafficRuleSegment;
import org.apache.shardingsphere.traffic.distsql.statement.updatable.AlterTrafficRuleStatement;
import org.apache.shardingsphere.traffic.rule.TrafficRule;
import org.apache.shardingsphere.traffic.spi.TrafficAlgorithm;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Alter traffic rule executor.
 */
@Setter
public final class AlterTrafficRuleExecutor implements GlobalRuleDefinitionExecutor<AlterTrafficRuleStatement, TrafficRule> {
    
    private TrafficRule rule;
    
    @Override
    public void checkBeforeUpdate(final AlterTrafficRuleStatement sqlStatement) {
        checkRuleNames(sqlStatement);
        checkAlgorithmNames(sqlStatement);
    }
    
    private void checkRuleNames(final AlterTrafficRuleStatement sqlStatement) {
        Collection<String> notExistRuleNames = getNotExistRuleNames(sqlStatement);
        ShardingSpherePreconditions.checkState(notExistRuleNames.isEmpty(), () -> new MissingRequiredRuleException("Traffic", notExistRuleNames));
    }
    
    private Collection<String> getNotExistRuleNames(final AlterTrafficRuleStatement sqlStatement) {
        Collection<String> currentRuleNames = rule.getConfiguration().getTrafficStrategies().stream().map(TrafficStrategyConfiguration::getName).collect(Collectors.toSet());
        return sqlStatement.getSegments().stream().map(TrafficRuleSegment::getName).filter(each -> !currentRuleNames.contains(each)).collect(Collectors.toSet());
    }
    
    private void checkAlgorithmNames(final AlterTrafficRuleStatement sqlStatement) {
        sqlStatement.getSegments().forEach(each -> {
            TypedSPILoader.checkService(TrafficAlgorithm.class, each.getAlgorithm().getName(), each.getAlgorithm().getProps());
            if (null != each.getLoadBalancer()) {
                TypedSPILoader.checkService(LoadBalanceAlgorithm.class, each.getLoadBalancer().getName(), each.getLoadBalancer().getProps());
            }
        });
    }
    
    @Override
    public TrafficRuleConfiguration buildToBeAlteredRuleConfiguration(final AlterTrafficRuleStatement sqlStatement) {
        TrafficRuleConfiguration result = new TrafficRuleConfiguration();
        TrafficRuleConfiguration configFromSQLStatement = TrafficRuleConverter.convert(sqlStatement.getSegments());
        result.getTrafficStrategies().addAll(createToBeAlteredStrategyConfigurations(configFromSQLStatement));
        result.getTrafficAlgorithms().putAll(createToBeAlteredTrafficAlgorithms(configFromSQLStatement, getInUsedTrafficAlgorithm(result)));
        result.getLoadBalancers().putAll(createToBeAlteredLoadBalancers(configFromSQLStatement, getInUsedLoadBalancer(result)));
        return result;
    }
    
    private Collection<TrafficStrategyConfiguration> createToBeAlteredStrategyConfigurations(final TrafficRuleConfiguration configFromSQLStatement) {
        Collection<TrafficStrategyConfiguration> result = new LinkedList<>(rule.getConfiguration().getTrafficStrategies());
        Collection<String> toBeAlteredConfigNames = configFromSQLStatement.getTrafficStrategies().stream().map(TrafficStrategyConfiguration::getName).collect(Collectors.toSet());
        result.removeIf(each -> toBeAlteredConfigNames.contains(each.getName()));
        result.addAll(configFromSQLStatement.getTrafficStrategies());
        return result;
    }
    
    private Collection<String> getInUsedTrafficAlgorithm(final TrafficRuleConfiguration config) {
        return config.getTrafficStrategies().stream().map(TrafficStrategyConfiguration::getAlgorithmName).collect(Collectors.toSet());
    }
    
    private Map<String, AlgorithmConfiguration> createToBeAlteredTrafficAlgorithms(final TrafficRuleConfiguration configFromSQLStatement, final Collection<String> inUsedTrafficAlgorithm) {
        Map<String, AlgorithmConfiguration> result = new LinkedHashMap<>(rule.getConfiguration().getTrafficAlgorithms());
        result.putAll(configFromSQLStatement.getTrafficAlgorithms());
        for (String each : result.keySet().stream().filter(each -> !inUsedTrafficAlgorithm.contains(each)).collect(Collectors.toSet())) {
            result.remove(each);
        }
        return result;
    }
    
    private Collection<String> getInUsedLoadBalancer(final TrafficRuleConfiguration config) {
        return config.getTrafficStrategies().stream().map(TrafficStrategyConfiguration::getLoadBalancerName).collect(Collectors.toSet());
    }
    
    private Map<String, AlgorithmConfiguration> createToBeAlteredLoadBalancers(final TrafficRuleConfiguration configFromSQLStatement, final Collection<String> inUsedLoadBalancer) {
        Map<String, AlgorithmConfiguration> result = new LinkedHashMap<>(rule.getConfiguration().getLoadBalancers());
        result.putAll(configFromSQLStatement.getLoadBalancers());
        for (String each : result.keySet().stream().filter(each -> !inUsedLoadBalancer.contains(each)).collect(Collectors.toSet())) {
            result.remove(each);
        }
        return result;
    }
    
    @Override
    public Class<TrafficRule> getRuleClass() {
        return TrafficRule.class;
    }
    
    @Override
    public Class<AlterTrafficRuleStatement> getType() {
        return AlterTrafficRuleStatement.class;
    }
}
