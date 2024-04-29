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

package org.apache.shardingsphere.traffic.distsql.handler.query;

import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorRuleAware;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.traffic.api.config.TrafficRuleConfiguration;
import org.apache.shardingsphere.traffic.api.config.TrafficStrategyConfiguration;
import org.apache.shardingsphere.traffic.distsql.statement.queryable.ShowTrafficRulesStatement;
import org.apache.shardingsphere.traffic.rule.TrafficRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Show traffic rule executor.
 */
@Setter
public final class ShowTrafficRuleExecutor implements DistSQLQueryExecutor<ShowTrafficRulesStatement>, DistSQLExecutorRuleAware<TrafficRule> {
    
    private TrafficRule rule;
    
    @Override
    public Collection<String> getColumnNames(final ShowTrafficRulesStatement sqlStatement) {
        return Arrays.asList("name", "labels", "algorithm_type", "algorithm_props", "load_balancer_type", "load_balancer_props");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowTrafficRulesStatement sqlStatement, final ContextManager contextManager) {
        TrafficRuleConfiguration ruleConfig = rule.getConfiguration();
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        ruleConfig.getTrafficStrategies().stream().filter(each -> null == sqlStatement.getRuleName() || each.getName().equals(sqlStatement.getRuleName()))
                .forEach(each -> result.add(buildRow(each, ruleConfig.getTrafficAlgorithms().get(each.getAlgorithmName()), ruleConfig.getLoadBalancers().get(each.getLoadBalancerName()))));
        return result;
    }
    
    private LocalDataQueryResultRow buildRow(final TrafficStrategyConfiguration strategy, final AlgorithmConfiguration trafficAlgorithm, final AlgorithmConfiguration loadBalancer) {
        return new LocalDataQueryResultRow(strategy.getName(), String.join(",", strategy.getLabels()), null == trafficAlgorithm ? null : trafficAlgorithm.getType(),
                null == trafficAlgorithm ? null : trafficAlgorithm.getProps(), null == loadBalancer ? null : loadBalancer.getType(), null == loadBalancer ? null : loadBalancer.getProps());
    }
    
    @Override
    public Class<TrafficRule> getRuleClass() {
        return TrafficRule.class;
    }
    
    @Override
    public Class<ShowTrafficRulesStatement> getType() {
        return ShowTrafficRulesStatement.class;
    }
}
