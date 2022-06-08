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

import org.apache.shardingsphere.distsql.parser.statement.ral.common.updatable.DropTrafficRuleStatement;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.UpdatableRALBackendHandler;
import org.apache.shardingsphere.traffic.api.config.TrafficRuleConfiguration;
import org.apache.shardingsphere.traffic.api.config.TrafficStrategyConfiguration;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Drop traffic rule statement handler.
 */
public final class DropTrafficRuleHandler extends UpdatableRALBackendHandler<DropTrafficRuleStatement, DropTrafficRuleHandler> {
    
    @Override
    protected void update(final ContextManager contextManager, final DropTrafficRuleStatement sqlStatement) throws DistSQLException {
        Optional<TrafficRuleConfiguration> config = ProxyContext.getInstance()
                .getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().findSingleRuleConfiguration(TrafficRuleConfiguration.class);
        if (!sqlStatement.isContainsIfExistClause()) {
            DistSQLException.predictionThrow(config.isPresent(), () -> new RequiredRuleMissedException("Traffic"));
            checkTrafficRuleConfiguration(sqlStatement, config.get());
        }
        if (config.isPresent()) {
            config.get().getTrafficStrategies().removeIf(each -> sqlStatement.getRuleNames().contains(each.getName()));
            getUnusedAlgorithm(config.get()).forEach(each -> config.get().getTrafficAlgorithms().remove(each));
            getUnusedLoadBalancer(config.get()).forEach(each -> config.get().getLoadBalancers().remove(each));
            updateToRepository(config.get());
        }
    }
    
    private void checkTrafficRuleConfiguration(final DropTrafficRuleStatement sqlStatement, final TrafficRuleConfiguration config) throws DistSQLException {
        Set<String> currentTrafficStrategyNames = config.getTrafficStrategies().stream().map(TrafficStrategyConfiguration::getName).collect(Collectors.toSet());
        Set<String> notExistRuleNames = sqlStatement.getRuleNames().stream().filter(each -> !currentTrafficStrategyNames.contains(each)).collect(Collectors.toSet());
        DistSQLException.predictionThrow(notExistRuleNames.isEmpty(), () -> new RequiredRuleMissedException("Traffic"));
    }
    
    private Collection<String> getUnusedAlgorithm(final TrafficRuleConfiguration config) {
        Collection<String> currentlyInUse = config.getTrafficStrategies().stream().map(TrafficStrategyConfiguration::getAlgorithmName).collect(Collectors.toSet());
        return config.getTrafficAlgorithms().keySet().stream().filter(each -> !currentlyInUse.contains(each)).collect(Collectors.toSet());
    }
    
    private Collection<String> getUnusedLoadBalancer(final TrafficRuleConfiguration config) {
        Collection<String> currentlyInUse = config.getTrafficStrategies().stream().map(TrafficStrategyConfiguration::getLoadBalancerName).collect(Collectors.toSet());
        return config.getLoadBalancers().keySet().stream().filter(each -> !currentlyInUse.contains(each)).collect(Collectors.toSet());
    }
    
    private void updateToRepository(final TrafficRuleConfiguration config) {
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        Optional<MetaDataPersistService> metaDataPersistService = metaDataContexts.getPersistService();
        getUnusedLoadBalancer(config).forEach(each -> config.getLoadBalancers().remove(each));
        metaDataPersistService.ifPresent(optional -> optional.getGlobalRuleService().persist(metaDataContexts.getMetaData().getGlobalRuleMetaData().getConfigurations(), true));
    }
}
