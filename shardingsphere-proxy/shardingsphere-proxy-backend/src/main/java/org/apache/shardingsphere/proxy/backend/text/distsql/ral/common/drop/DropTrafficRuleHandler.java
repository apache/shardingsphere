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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.drop;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.drop.DropTrafficRuleStatement;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.traffic.api.config.TrafficRuleConfiguration;
import org.apache.shardingsphere.traffic.api.config.TrafficStrategyConfiguration;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Drop traffic rule statement handler.
 */
@RequiredArgsConstructor
public final class DropTrafficRuleHandler implements TextProtocolBackendHandler {
    
    private final DropTrafficRuleStatement sqlStatement;
    
    @Override
    public ResponseHeader execute() throws DistSQLException {
        Optional<TrafficRuleConfiguration> configuration = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getGlobalRuleMetaData()
                .findRuleConfiguration(TrafficRuleConfiguration.class).stream().findAny();
        check(sqlStatement, configuration);
        if (configuration.isPresent()) {
            configuration.get().getTrafficStrategies().removeIf(each -> sqlStatement.getRuleNames().contains(each.getName()));
            getUnusedAlgorithm(configuration.get()).forEach(each -> configuration.get().getTrafficAlgorithms().remove(each));
            getUnusedLoadBalancer(configuration.get()).forEach(each -> configuration.get().getLoadBalancers().remove(each));
        }
        return new UpdateResponseHeader(sqlStatement);
    }
    
    private void check(final DropTrafficRuleStatement sqlStatement, final Optional<TrafficRuleConfiguration> configuration) throws DistSQLException {
        if (!sqlStatement.isContainsIfExistClause()) {
            DistSQLException.predictionThrow(configuration.isPresent(), new RequiredRuleMissedException("Traffic"));
            Set<String> currentTrafficStrategyNames = configuration.get().getTrafficStrategies().stream().map(TrafficStrategyConfiguration::getName).collect(Collectors.toSet());
            Set<String> notExistRuleNames = sqlStatement.getRuleNames().stream().filter(each -> !currentTrafficStrategyNames.contains(each)).collect(Collectors.toSet());
            DistSQLException.predictionThrow(notExistRuleNames.isEmpty(), new RequiredRuleMissedException("Traffic"));
        }
    }
    
    private Collection<String> getUnusedAlgorithm(final TrafficRuleConfiguration configuration) {
        Set<String> currentlyInUse = configuration.getTrafficStrategies().stream().map(TrafficStrategyConfiguration::getAlgorithmName).collect(Collectors.toSet());
        return configuration.getTrafficAlgorithms().keySet().stream().filter(each -> !currentlyInUse.contains(each)).collect(Collectors.toSet());
    }
    
    private Collection<String> getUnusedLoadBalancer(final TrafficRuleConfiguration configuration) {
        Set<String> currentlyInUse = configuration.getTrafficStrategies().stream().map(TrafficStrategyConfiguration::getLoadBalancerName).collect(Collectors.toSet());
        return configuration.getLoadBalancers().keySet().stream().filter(each -> !currentlyInUse.contains(each)).collect(Collectors.toSet());
    }
}
