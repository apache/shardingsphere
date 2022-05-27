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
import org.apache.shardingsphere.traffic.api.config.TrafficStrategyConfiguration;
import org.apache.shardingsphere.traffic.factory.TrafficAlgorithmFactory;
import org.apache.shardingsphere.traffic.factory.TrafficLoadBalanceAlgorithmFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Create traffic rule handler.
 */
public final class CreateTrafficRuleHandler extends UpdatableRALBackendHandler<CreateTrafficRuleStatement, CreateTrafficRuleHandler> {
    
    @Override
    protected void update(final ContextManager contextManager, final CreateTrafficRuleStatement sqlStatement) throws DistSQLException {
        Optional<TrafficRuleConfiguration> trafficRuleConfig = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData()
                .findRuleConfigurations(TrafficRuleConfiguration.class).stream().findAny();
        if (trafficRuleConfig.isPresent()) {
            checkTrafficRuleConfiguration(sqlStatement, trafficRuleConfig.get());
        }
        checkInvalidAlgorithmNames();
        updateToRepository(TrafficRuleConverter.convert(sqlStatement.getSegments()), trafficRuleConfig.orElse(null));
    }
    
    private void checkTrafficRuleConfiguration(final CreateTrafficRuleStatement sqlStatement, final TrafficRuleConfiguration trafficRuleConfig) throws DistSQLException {
        Collection<String> currentRuleNames = trafficRuleConfig.getTrafficStrategies().stream().map(TrafficStrategyConfiguration::getName).collect(Collectors.toSet());
        Set<String> duplicatedRuleNames = sqlStatement.getSegments().stream().map(TrafficRuleSegment::getName).filter(currentRuleNames::contains).collect(Collectors.toSet());
        DistSQLException.predictionThrow(duplicatedRuleNames.isEmpty(), () -> new DuplicateRuleException("traffic", duplicatedRuleNames));
    }
    
    private void checkInvalidAlgorithmNames() throws DistSQLException {
        Collection<String> invalidAlgorithmNames = new LinkedList<>();
        for (TrafficRuleSegment each : sqlStatement.getSegments()) {
            if (!TrafficAlgorithmFactory.contains(each.getAlgorithm().getName())) {
                invalidAlgorithmNames.add(each.getAlgorithm().getName());
            }
            if (null != each.getLoadBalancer() && !TrafficLoadBalanceAlgorithmFactory.contains(each.getLoadBalancer().getName())) {
                invalidAlgorithmNames.add(each.getLoadBalancer().getName());
            }
        }
        DistSQLException.predictionThrow(invalidAlgorithmNames.isEmpty(), () -> new InvalidAlgorithmConfigurationException("traffic", invalidAlgorithmNames));
    }
    
    private void updateToRepository(final TrafficRuleConfiguration toBeCreatedRuleConfig, final TrafficRuleConfiguration currentRuleConfig) {
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        Collection<RuleConfiguration> globalRuleConfigs = metaDataContexts.getMetaData().getGlobalRuleMetaData().getConfigurations();
        if (null == currentRuleConfig) {
            globalRuleConfigs.add(toBeCreatedRuleConfig);
        } else {
            setUpCurrentRuleConfiguration(toBeCreatedRuleConfig, currentRuleConfig);
        }
        Optional<MetaDataPersistService> metaDataPersistService = metaDataContexts.getPersistService();
        metaDataPersistService.ifPresent(optional -> optional.getGlobalRuleService().persist(globalRuleConfigs, true));
    }
    
    private void setUpCurrentRuleConfiguration(final TrafficRuleConfiguration toBeCreatedRuleConfig, final TrafficRuleConfiguration currentRuleConfig) {
        currentRuleConfig.getTrafficStrategies().addAll(toBeCreatedRuleConfig.getTrafficStrategies());
        currentRuleConfig.getTrafficAlgorithms().putAll(toBeCreatedRuleConfig.getTrafficAlgorithms());
        currentRuleConfig.getLoadBalancers().putAll(toBeCreatedRuleConfig.getLoadBalancers());
    }
}
