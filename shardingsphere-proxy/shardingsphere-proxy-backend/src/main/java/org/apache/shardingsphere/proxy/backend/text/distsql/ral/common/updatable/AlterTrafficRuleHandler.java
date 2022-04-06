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
import org.apache.shardingsphere.spi.type.typed.TypedSPIRegistry;
import org.apache.shardingsphere.traffic.api.config.TrafficRuleConfiguration;
import org.apache.shardingsphere.traffic.api.config.TrafficStrategyConfiguration;
import org.apache.shardingsphere.traffic.spi.TrafficAlgorithm;
import org.apache.shardingsphere.traffic.spi.TrafficLoadBalanceAlgorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Alter traffic rule handler.
 */
public final class AlterTrafficRuleHandler extends UpdatableRALBackendHandler<AlterTrafficRuleStatement, AlterTrafficRuleHandler> {
    
    @Override
    protected void update(final ContextManager contextManager, final AlterTrafficRuleStatement sqlStatement) throws DistSQLException {
        Optional<TrafficRuleConfiguration> currentConfiguration = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getGlobalRuleMetaData()
                .findRuleConfiguration(TrafficRuleConfiguration.class).stream().findAny();
        check(sqlStatement, currentConfiguration);
        TrafficRuleConfiguration toBeAlteredConfiguration = TrafficRuleConverter.convert(sqlStatement.getSegments());
        updateToRepository(toBeAlteredConfiguration, currentConfiguration.get());
    }
    
    private void check(final AlterTrafficRuleStatement sqlStatement, final Optional<TrafficRuleConfiguration> currentConfiguration) throws DistSQLException {
        DistSQLException.predictionThrow(currentConfiguration.isPresent(), () -> new RequiredRuleMissedException("Traffic"));
        Collection<String> currentRuleNames = currentConfiguration.get().getTrafficStrategies().stream().map(TrafficStrategyConfiguration::getName).collect(Collectors.toSet());
        Set<String> notExistRuleNames = sqlStatement.getSegments().stream().map(TrafficRuleSegment::getName).filter(each -> !currentRuleNames.contains(each)).collect(Collectors.toSet());
        DistSQLException.predictionThrow(notExistRuleNames.isEmpty(), () -> new RequiredRuleMissedException("Traffic", notExistRuleNames));
        Collection<String> invalidAlgorithmNames = getInvalidAlgorithmNames(sqlStatement.getSegments());
        DistSQLException.predictionThrow(invalidAlgorithmNames.isEmpty(), () -> new InvalidAlgorithmConfigurationException("traffic", invalidAlgorithmNames));
    }
    
    private Collection<String> getInvalidAlgorithmNames(final Collection<TrafficRuleSegment> segments) {
        Collection<String> result = new ArrayList<>(segments.size());
        sqlStatement.getSegments().forEach(each -> {
            if (!TypedSPIRegistry.findRegisteredService(TrafficAlgorithm.class, each.getAlgorithm().getName(), new Properties()).isPresent()) {
                result.add(each.getAlgorithm().getName());
            }
            if (null != each.getLoadBalancer()
                    && !TypedSPIRegistry.findRegisteredService(TrafficLoadBalanceAlgorithm.class, each.getLoadBalancer().getName(), new Properties()).isPresent()) {
                result.add(each.getLoadBalancer().getName());
            }
        });
        return result;
    }
    
    private void updateToRepository(final TrafficRuleConfiguration toBeAlteredConfiguration, final TrafficRuleConfiguration currentConfiguration) {
        Collection<String> toBeAlteredConfigurationNames = toBeAlteredConfiguration.getTrafficStrategies().stream().map(TrafficStrategyConfiguration::getName).collect(Collectors.toSet());
        currentConfiguration.getTrafficStrategies().removeIf(each -> toBeAlteredConfigurationNames.contains(each.getName()));
        currentConfiguration.getTrafficStrategies().addAll(toBeAlteredConfiguration.getTrafficStrategies());
        currentConfiguration.getTrafficAlgorithms().putAll(toBeAlteredConfiguration.getTrafficAlgorithms());
        currentConfiguration.getLoadBalancers().putAll(toBeAlteredConfiguration.getLoadBalancers());
        getUnusedLoadBalancer(currentConfiguration).forEach(each -> currentConfiguration.getLoadBalancers().remove(each));
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        Optional<MetaDataPersistService> metaDataPersistService = metaDataContexts.getMetaDataPersistService();
        metaDataPersistService.ifPresent(op -> op.getGlobalRuleService().persist(metaDataContexts.getGlobalRuleMetaData().getConfigurations(), true));
    }
    
    private Collection<String> getUnusedLoadBalancer(final TrafficRuleConfiguration configuration) {
        Collection<String> currentlyInUse = configuration.getTrafficStrategies().stream().map(TrafficStrategyConfiguration::getLoadBalancerName).collect(Collectors.toSet());
        return configuration.getLoadBalancers().keySet().stream().filter(each -> !currentlyInUse.contains(each)).collect(Collectors.toSet());
    }
}
