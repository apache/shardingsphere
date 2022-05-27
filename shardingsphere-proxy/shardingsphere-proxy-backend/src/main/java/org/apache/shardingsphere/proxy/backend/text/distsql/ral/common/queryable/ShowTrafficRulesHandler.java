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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.queryable;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.queryable.ShowTrafficRulesStatement;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.properties.PropertiesConverter;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.QueryableRALBackendHandler;
import org.apache.shardingsphere.traffic.api.config.TrafficRuleConfiguration;
import org.apache.shardingsphere.traffic.api.config.TrafficStrategyConfiguration;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Show traffic rules handler.
 */
@RequiredArgsConstructor
public final class ShowTrafficRulesHandler extends QueryableRALBackendHandler<ShowTrafficRulesStatement, ShowTrafficRulesHandler> {
    
    private static final String RULE_NAME = "name";
    
    private static final String LABELS = "labels";
    
    private static final String ALGORITHM_TYPE = "algorithm_type";
    
    private static final String ALGORITHM_PROPS = "algorithm_props";
    
    private static final String LOAD_BALANCER_TYPE = "load_balancer_type";
    
    private static final String LOAD_BALANCER_PROPS = "load_balancer_props";
    
    @Override
    protected Collection<String> getColumnNames() {
        return Arrays.asList(RULE_NAME, LABELS, ALGORITHM_TYPE, ALGORITHM_PROPS, LOAD_BALANCER_TYPE, LOAD_BALANCER_PROPS);
    }
    
    @Override
    protected Collection<List<Object>> getRows(final ContextManager contextManager) {
        Optional<TrafficRuleConfiguration> config = ProxyContext.getInstance().getContextManager().getMetaDataContexts()
                .getMetaData().getGlobalRuleMetaData().findRuleConfigurations(TrafficRuleConfiguration.class).stream().findAny();
        Collection<List<Object>> result = new LinkedList<>();
        Optional<String> ruleName = Optional.ofNullable(sqlStatement.getRuleName());
        config.ifPresent(optional -> {
            Map<String, ShardingSphereAlgorithmConfiguration> trafficAlgorithms = optional.getTrafficAlgorithms();
            Map<String, ShardingSphereAlgorithmConfiguration> loadBalancers = optional.getLoadBalancers();
            optional.getTrafficStrategies().stream().filter(each -> !ruleName.isPresent() || each.getName().equals(ruleName.get()))
                    .forEach(each -> result.add(buildRow(each, trafficAlgorithms.get(each.getAlgorithmName()), loadBalancers.get(each.getLoadBalancerName()))));
        });
        return result;
    }
    
    private List<Object> buildRow(final TrafficStrategyConfiguration strategy, final ShardingSphereAlgorithmConfiguration trafficAlgorithm,
                                  final ShardingSphereAlgorithmConfiguration loadBalancer) {
        List<Object> result = new LinkedList<>();
        result.add(strategy.getName());
        result.add(String.join(",", strategy.getLabels()));
        result.add(null != trafficAlgorithm ? trafficAlgorithm.getType() : "");
        result.add(null != trafficAlgorithm ? PropertiesConverter.convert(trafficAlgorithm.getProps()) : "");
        result.add(null != loadBalancer ? loadBalancer.getType() : "");
        result.add(null != loadBalancer ? PropertiesConverter.convert(loadBalancer.getProps()) : "");
        return result;
    }
}
