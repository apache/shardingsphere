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
import org.apache.shardingsphere.distsql.parser.statement.ral.common.updatable.AlterTrafficRuleStatement;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.UpdatableRALBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.convert.TrafficRuleConverter;
import org.apache.shardingsphere.traffic.api.config.TrafficRuleConfiguration;
import org.apache.shardingsphere.traffic.api.config.TrafficStrategyConfiguration;
import org.apache.shardingsphere.traffic.factory.TrafficAlgorithmFactory;
import org.apache.shardingsphere.traffic.factory.TrafficLoadBalanceAlgorithmFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Alter traffic rule handler.
 */
public final class AlterTrafficRuleHandler extends UpdatableRALBackendHandler<AlterTrafficRuleStatement, AlterTrafficRuleHandler> {
    
    @Override
    protected void update(final ContextManager contextManager, final AlterTrafficRuleStatement sqlStatement) throws DistSQLException {
        Optional<TrafficRuleConfiguration> currentConfig = findCurrentConfiguration();
        DistSQLException.predictionThrow(currentConfig.isPresent(), () -> new RequiredRuleMissedException("Traffic"));
        check(sqlStatement, currentConfig.get());
        TrafficRuleConfiguration toBeAlteredConfig = TrafficRuleConverter.convert(sqlStatement.getSegments());
        persistNewRuleConfigurations(toBeAlteredConfig, currentConfig.get());
    }
    
    private Optional<TrafficRuleConfiguration> findCurrentConfiguration() {
        return ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().findRuleConfigurations(TrafficRuleConfiguration.class).stream().findAny();
    }
    
    private void check(final AlterTrafficRuleStatement sqlStatement, final TrafficRuleConfiguration currentConfig) throws DistSQLException {
        Collection<String> currentRuleNames = currentConfig.getTrafficStrategies().stream().map(TrafficStrategyConfiguration::getName).collect(Collectors.toSet());
        Set<String> notExistRuleNames = sqlStatement.getSegments().stream().map(TrafficRuleSegment::getName).filter(each -> !currentRuleNames.contains(each)).collect(Collectors.toSet());
        DistSQLException.predictionThrow(notExistRuleNames.isEmpty(), () -> new RequiredRuleMissedException("Traffic", notExistRuleNames));
        Collection<String> invalidAlgorithmNames = getInvalidAlgorithmNames();
        DistSQLException.predictionThrow(invalidAlgorithmNames.isEmpty(), () -> new InvalidAlgorithmConfigurationException("traffic", invalidAlgorithmNames));
    }
    
    private Collection<String> getInvalidAlgorithmNames() {
        Collection<String> result = new LinkedList<>();
        for (TrafficRuleSegment each : sqlStatement.getSegments()) {
            if (!TrafficAlgorithmFactory.contains(each.getAlgorithm().getName())) {
                result.add(each.getAlgorithm().getName());
            }
            if (null != each.getLoadBalancer() && !TrafficLoadBalanceAlgorithmFactory.contains(each.getLoadBalancer().getName())) {
                result.add(each.getLoadBalancer().getName());
            }
        }
        return result;
    }
    
    private void persistNewRuleConfigurations(final TrafficRuleConfiguration toBeAlteredConfig, final TrafficRuleConfiguration currentConfig) {
        Collection<String> toBeAlteredConfigNames = toBeAlteredConfig.getTrafficStrategies().stream().map(TrafficStrategyConfiguration::getName).collect(Collectors.toSet());
        currentConfig.getTrafficStrategies().removeIf(each -> toBeAlteredConfigNames.contains(each.getName()));
        currentConfig.getTrafficStrategies().addAll(toBeAlteredConfig.getTrafficStrategies());
        currentConfig.getTrafficAlgorithms().putAll(toBeAlteredConfig.getTrafficAlgorithms());
        currentConfig.getLoadBalancers().putAll(toBeAlteredConfig.getLoadBalancers());
        getUnusedLoadBalancer(currentConfig).forEach(each -> currentConfig.getLoadBalancers().remove(each));
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        Optional<MetaDataPersistService> metaDataPersistService = metaDataContexts.getPersistService();
        metaDataPersistService.ifPresent(optional -> optional.getGlobalRuleService().persist(metaDataContexts.getMetaData().getGlobalRuleMetaData().getConfigurations(), true));
    }
    
    private Collection<String> getUnusedLoadBalancer(final TrafficRuleConfiguration currentConfig) {
        Collection<String> currentlyInUse = currentConfig.getTrafficStrategies().stream().map(TrafficStrategyConfiguration::getLoadBalancerName).collect(Collectors.toSet());
        return currentConfig.getLoadBalancers().keySet().stream().filter(each -> !currentlyInUse.contains(each)).collect(Collectors.toSet());
    }
}
