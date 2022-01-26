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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.alter.excutor;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.distsql.parser.segment.TrafficRuleSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.AlterTrafficRuleStatement;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.alter.AlterStatementExecutor;
import org.apache.shardingsphere.spi.typed.TypedSPIRegistry;
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
@RequiredArgsConstructor
public final class AlterTrafficRuleExecutor implements AlterStatementExecutor {
    
    private final AlterTrafficRuleStatement sqlStatement;
    
    @Override
    public ResponseHeader execute() throws DistSQLException {
        Optional<TrafficRuleConfiguration> trafficRuleConfiguration = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getGlobalRuleMetaData()
                .findRuleConfiguration(TrafficRuleConfiguration.class).stream().findAny();
        check(sqlStatement, trafficRuleConfiguration);
        TrafficRuleConfiguration newTrafficRuleConfiguration = createTrafficRuleConfiguration(sqlStatement);
        updateToRepository(newTrafficRuleConfiguration, trafficRuleConfiguration.get());
        return new UpdateResponseHeader(sqlStatement);
    }
    
    private void check(final AlterTrafficRuleStatement sqlStatement, final Optional<TrafficRuleConfiguration> trafficRuleConfiguration) throws DistSQLException {
        DistSQLException.predictionThrow(trafficRuleConfiguration.isPresent(), new RequiredRuleMissedException("Traffic"));
        Collection<String> currentRuleNames = trafficRuleConfiguration.get().getTrafficStrategies().stream().map(TrafficStrategyConfiguration::getName).collect(Collectors.toSet());
        Set<String> notExistRuleNames = sqlStatement.getSegments().stream().map(TrafficRuleSegment::getName).filter(each -> !currentRuleNames.contains(each)).collect(Collectors.toSet());
        DistSQLException.predictionThrow(notExistRuleNames.isEmpty(), new RequiredRuleMissedException("Traffic", notExistRuleNames));
        Collection<String> invalidAlgorithmNames = getInvalidAlgorithmNames(sqlStatement.getSegments());
        DistSQLException.predictionThrow(invalidAlgorithmNames.isEmpty(), new InvalidAlgorithmConfigurationException("traffic", invalidAlgorithmNames));
    }
    
    private Collection<String> getInvalidAlgorithmNames(final Collection<TrafficRuleSegment> segments) {
        Collection<String> result = new ArrayList<>(segments.size());
        sqlStatement.getSegments().forEach(each -> {
            if (!TypedSPIRegistry.findRegisteredService(TrafficAlgorithm.class, each.getAlgorithm().getName(), new Properties()).isPresent()) {
                result.add(each.getAlgorithm().getName());
            }
            if (!TypedSPIRegistry.findRegisteredService(TrafficLoadBalanceAlgorithm.class, each.getLoadBalancer().getName(), new Properties()).isPresent()) {
                result.add(each.getLoadBalancer().getName());
            }
        });
        return result;
    }
    
    private TrafficRuleConfiguration createTrafficRuleConfiguration(final AlterTrafficRuleStatement sqlStatement) {
        TrafficRuleConfiguration result = new TrafficRuleConfiguration();
        sqlStatement.getSegments().forEach(each -> setConfigurationData(result, each));
        return result;
    }
    
    private void setConfigurationData(final TrafficRuleConfiguration result, final TrafficRuleSegment each) {
        ShardingSphereAlgorithmConfiguration trafficAlgorithm = createAlgorithmConfiguration(each.getAlgorithm());
        ShardingSphereAlgorithmConfiguration loadBalancer = createAlgorithmConfiguration(each.getLoadBalancer());
        String trafficAlgorithmName = createAlgorithmName(each.getName(), trafficAlgorithm);
        String loadBalancerName = createAlgorithmName(each.getName(), loadBalancer);
        TrafficStrategyConfiguration trafficStrategy = createTrafficStrategy(each, trafficAlgorithmName, loadBalancerName);
        result.getTrafficStrategies().add(trafficStrategy);
        result.getTrafficAlgorithms().put(trafficAlgorithmName, trafficAlgorithm);
        result.getLoadBalancers().put(loadBalancerName, loadBalancer);
    }
    
    private ShardingSphereAlgorithmConfiguration createAlgorithmConfiguration(final AlgorithmSegment segment) {
        return new ShardingSphereAlgorithmConfiguration(segment.getName(), segment.getProps());
    }
    
    private TrafficStrategyConfiguration createTrafficStrategy(final TrafficRuleSegment trafficRuleSegment, final String trafficAlgorithmName, final String loadBalancerName) {
        return new TrafficStrategyConfiguration(trafficRuleSegment.getName(), trafficRuleSegment.getLabels(), trafficAlgorithmName, loadBalancerName);
    }
    
    private void updateToRepository(final TrafficRuleConfiguration newTrafficRuleConfiguration, final TrafficRuleConfiguration trafficRuleConfiguration) {
        trafficRuleConfiguration.getTrafficStrategies().addAll(newTrafficRuleConfiguration.getTrafficStrategies());
        trafficRuleConfiguration.getTrafficAlgorithms().putAll(newTrafficRuleConfiguration.getTrafficAlgorithms());
        trafficRuleConfiguration.getLoadBalancers().putAll(newTrafficRuleConfiguration.getLoadBalancers());
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        Optional<MetaDataPersistService> metaDataPersistService = metaDataContexts.getMetaDataPersistService();
        metaDataPersistService.ifPresent(op -> op.getGlobalRuleService().persist(metaDataContexts.getGlobalRuleMetaData().getConfigurations(), true));
    }
    
    private String createAlgorithmName(final String ruleName, final ShardingSphereAlgorithmConfiguration algorithm) {
        return String.format("%s_%s", ruleName, algorithm.getType()).toLowerCase();
    }
}
