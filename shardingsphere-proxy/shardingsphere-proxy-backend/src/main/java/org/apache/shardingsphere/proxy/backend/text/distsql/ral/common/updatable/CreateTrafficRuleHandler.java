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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.updatable;

import org.apache.shardingsphere.distsql.parser.segment.TrafficRuleSegment;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.updatable.CreateTrafficRuleStatement;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.UpdatableRALBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.convert.TrafficRuleConverter;
import org.apache.shardingsphere.traffic.api.config.TrafficRuleConfiguration;
import org.apache.shardingsphere.traffic.factory.TrafficAlgorithmFactory;
import org.apache.shardingsphere.traffic.factory.TrafficLoadBalanceAlgorithmFactory;
import org.apache.shardingsphere.traffic.rule.TrafficRule;
import org.apache.shardingsphere.traffic.rule.TrafficStrategyRule;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Create traffic rule handler.
 */
public final class CreateTrafficRuleHandler extends UpdatableRALBackendHandler<CreateTrafficRuleStatement> {
    
    @Override
    protected void update(final ContextManager contextManager) throws DistSQLException {
        TrafficRule trafficRule = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(TrafficRule.class);
        check();
        updateToRepository(TrafficRuleConverter.convert(getSqlStatement().getSegments()), trafficRule.getConfiguration());
    }
    
    private void check() throws DistSQLException {
        checkRuleNames();
        checkAlgorithmNames();
    }
    
    private void checkRuleNames() throws DistSQLException {
        Collection<String> inUsedRuleNames = getInUsedRuleNames();
        DistSQLException.predictionThrow(inUsedRuleNames.isEmpty(), () -> new DuplicateRuleException("Traffic", inUsedRuleNames));
    }
    
    private Collection<String> getInUsedRuleNames() {
        TrafficRule currentRule = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(TrafficRule.class);
        Collection<String> currentRuleNames = currentRule.getStrategyRules().stream().map(TrafficStrategyRule::getName).collect(Collectors.toSet());
        return getSqlStatement().getSegments().stream().map(TrafficRuleSegment::getName).filter(currentRuleNames::contains).collect(Collectors.toSet());
    }
    
    private void checkAlgorithmNames() throws DistSQLException {
        Collection<String> invalidAlgorithmNames = getInvalidAlgorithmNames();
        DistSQLException.predictionThrow(invalidAlgorithmNames.isEmpty(), () -> new InvalidAlgorithmConfigurationException("Traffic", invalidAlgorithmNames));
    }
    
    private Collection<String> getInvalidAlgorithmNames() {
        Collection<String> result = new LinkedList<>();
        for (TrafficRuleSegment each : getSqlStatement().getSegments()) {
            if (!TrafficAlgorithmFactory.contains(each.getAlgorithm().getName())) {
                result.add(each.getAlgorithm().getName());
            }
            if (null != each.getLoadBalancer() && !TrafficLoadBalanceAlgorithmFactory.contains(each.getLoadBalancer().getName())) {
                result.add(each.getLoadBalancer().getName());
            }
        }
        return result;
    }
    
    private void updateToRepository(final TrafficRuleConfiguration toBeCreatedRuleConfig, final TrafficRuleConfiguration currentRuleConfig) {
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        Collection<RuleConfiguration> globalRuleConfigs = metaDataContexts.getMetaData().getGlobalRuleMetaData().getConfigurations();
        setUpCurrentRuleConfiguration(toBeCreatedRuleConfig, currentRuleConfig);
        Optional<MetaDataPersistService> metaDataPersistService = metaDataContexts.getPersistService();
        metaDataPersistService.ifPresent(optional -> optional.getGlobalRuleService().persist(globalRuleConfigs, true));
    }
    
    private void setUpCurrentRuleConfiguration(final TrafficRuleConfiguration toBeCreatedRuleConfig, final TrafficRuleConfiguration currentRuleConfig) {
        currentRuleConfig.getTrafficStrategies().addAll(toBeCreatedRuleConfig.getTrafficStrategies());
        currentRuleConfig.getTrafficAlgorithms().putAll(toBeCreatedRuleConfig.getTrafficAlgorithms());
        currentRuleConfig.getLoadBalancers().putAll(toBeCreatedRuleConfig.getLoadBalancers());
    }
}
