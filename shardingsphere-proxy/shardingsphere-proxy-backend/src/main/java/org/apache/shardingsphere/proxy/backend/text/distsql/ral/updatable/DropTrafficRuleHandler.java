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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.updatable;

import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.DropTrafficRuleStatement;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.UpdatableRALBackendHandler;
import org.apache.shardingsphere.traffic.api.config.TrafficRuleConfiguration;
import org.apache.shardingsphere.traffic.api.config.TrafficStrategyConfiguration;
import org.apache.shardingsphere.traffic.rule.TrafficRule;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Drop traffic rule statement handler.
 */
public final class DropTrafficRuleHandler extends UpdatableRALBackendHandler<DropTrafficRuleStatement> {
    
    @Override
    protected void update(final ContextManager contextManager) throws DistSQLException {
        check();
        replaceNewRule();
        persistNewRuleConfigurations();
    }
    
    private void check() throws DistSQLException {
        checkRuleNames();
    }
    
    private void checkRuleNames() throws DistSQLException {
        if (getSqlStatement().isIfExists()) {
            return;
        }
        TrafficRule rule = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(TrafficRule.class);
        TrafficRuleConfiguration config = rule.getConfiguration();
        Collection<String> currentRuleNames = config.getTrafficStrategies().stream().map(TrafficStrategyConfiguration::getName).collect(Collectors.toSet());
        Collection<String> notExistRuleNames = getSqlStatement().getRuleNames().stream().filter(each -> !currentRuleNames.contains(each)).collect(Collectors.toSet());
        DistSQLException.predictionThrow(notExistRuleNames.isEmpty(), () -> new RequiredRuleMissedException("Traffic"));
    }
    
    private void replaceNewRule() {
        TrafficRuleConfiguration toBeAlteredRuleConfig = createToBeAlteredRuleConfiguration();
        Collection<ShardingSphereRule> globalRules = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getRules();
        globalRules.removeIf(each -> each instanceof TrafficRule);
        globalRules.add(new TrafficRule(toBeAlteredRuleConfig));
    }
    
    private TrafficRuleConfiguration createToBeAlteredRuleConfiguration() {
        TrafficRuleConfiguration result = new TrafficRuleConfiguration();
        DropTrafficRuleStatement statement = getSqlStatement();
        TrafficRuleConfiguration currentConfig = ProxyContext
                .getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(TrafficRule.class).getConfiguration();
        result.getTrafficStrategies().addAll(createToBeAlteredStrategyConfigurations(currentConfig, statement));
        result.getTrafficAlgorithms().putAll(createToBeAlteredTrafficAlgorithm(currentConfig, getInUsedTrafficAlgorithm(result)));
        result.getLoadBalancers().putAll(createToBeAlteredLoadBalancers(currentConfig, getInUsedLoadBalancer(result)));
        return result;
    }
    
    private Collection<TrafficStrategyConfiguration> createToBeAlteredStrategyConfigurations(final TrafficRuleConfiguration currentConfig, final DropTrafficRuleStatement statement) {
        return currentConfig.getTrafficStrategies().stream().filter(each -> !statement.getRuleNames().contains(each.getName())).collect(Collectors.toList());
    }
    
    private Collection<String> getInUsedTrafficAlgorithm(final TrafficRuleConfiguration config) {
        return config.getTrafficStrategies().stream().map(TrafficStrategyConfiguration::getAlgorithmName).collect(Collectors.toSet());
    }
    
    private Map<String, ShardingSphereAlgorithmConfiguration> createToBeAlteredTrafficAlgorithm(final TrafficRuleConfiguration currentConfig, final Collection<String> inUsedTrafficAlgorithm) {
        Map<String, ShardingSphereAlgorithmConfiguration> result = new LinkedHashMap<>(currentConfig.getTrafficAlgorithms());
        for (String each : result.keySet().stream().filter(each -> !inUsedTrafficAlgorithm.contains(each)).collect(Collectors.toSet())) {
            result.remove(each);
        }
        return result;
    }
    
    private Collection<String> getInUsedLoadBalancer(final TrafficRuleConfiguration config) {
        return config.getTrafficStrategies().stream().map(TrafficStrategyConfiguration::getLoadBalancerName).collect(Collectors.toSet());
    }
    
    private Map<String, ShardingSphereAlgorithmConfiguration> createToBeAlteredLoadBalancers(final TrafficRuleConfiguration currentConfig, final Collection<String> inUsedLoadBalancer) {
        Map<String, ShardingSphereAlgorithmConfiguration> result = new LinkedHashMap<>(currentConfig.getLoadBalancers());
        for (String each : result.keySet().stream().filter(each -> !inUsedLoadBalancer.contains(each)).collect(Collectors.toSet())) {
            result.remove(each);
        }
        return result;
    }
    
    private void persistNewRuleConfigurations() {
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        metaDataContexts.getPersistService().getGlobalRuleService().persist(metaDataContexts.getMetaData().getGlobalRuleMetaData().getConfigurations(), true);
    }
}
