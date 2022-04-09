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
 * Create traffic rule handler.
 */
public final class CreateTrafficRuleHandler extends UpdatableRALBackendHandler<CreateTrafficRuleStatement, CreateTrafficRuleHandler> {
    
    @Override
    protected void update(final ContextManager contextManager, final CreateTrafficRuleStatement sqlStatement) throws DistSQLException {
        Optional<TrafficRuleConfiguration> trafficRuleConfiguration = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getGlobalRuleMetaData()
                .findRuleConfiguration(TrafficRuleConfiguration.class).stream().findAny();
        check(sqlStatement, trafficRuleConfiguration);
        TrafficRuleConfiguration toBeCreatedConfiguration = TrafficRuleConverter.convert(sqlStatement.getSegments());
        updateToRepository(toBeCreatedConfiguration, trafficRuleConfiguration);
    }
    
    private void check(final CreateTrafficRuleStatement sqlStatement, final Optional<TrafficRuleConfiguration> trafficRuleConfiguration) throws DistSQLException {
        if (trafficRuleConfiguration.isPresent()) {
            Collection<String> currentRuleNames = trafficRuleConfiguration.get().getTrafficStrategies().stream().map(TrafficStrategyConfiguration::getName).collect(Collectors.toSet());
            Set<String> duplicatedRuleNames = sqlStatement.getSegments().stream().map(TrafficRuleSegment::getName).filter(currentRuleNames::contains).collect(Collectors.toSet());
            DistSQLException.predictionThrow(duplicatedRuleNames.isEmpty(), () -> new DuplicateRuleException("traffic", duplicatedRuleNames));
        }
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
    
    private void updateToRepository(final TrafficRuleConfiguration toBeCreatedRuleConfiguration, final Optional<TrafficRuleConfiguration> currentRuleConfiguration) {
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        Collection<RuleConfiguration> globalRuleConfigurations = metaDataContexts.getGlobalRuleMetaData().getConfigurations();
        if (currentRuleConfiguration.isPresent()) {
            currentRuleConfiguration.get().getTrafficStrategies().addAll(toBeCreatedRuleConfiguration.getTrafficStrategies());
            currentRuleConfiguration.get().getTrafficAlgorithms().putAll(toBeCreatedRuleConfiguration.getTrafficAlgorithms());
            currentRuleConfiguration.get().getLoadBalancers().putAll(toBeCreatedRuleConfiguration.getLoadBalancers());
        } else {
            globalRuleConfigurations.add(toBeCreatedRuleConfiguration);
        }
        Optional<MetaDataPersistService> metaDataPersistService = metaDataContexts.getMetaDataPersistService();
        metaDataPersistService.ifPresent(op -> op.getGlobalRuleService().persist(globalRuleConfigurations, true));
    }
}
