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

import org.apache.shardingsphere.distsql.handler.ral.query.MetaDataRequiredQueryableRALExecutor;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.props.PropertiesConverter;
import org.apache.shardingsphere.traffic.api.config.TrafficRuleConfiguration;
import org.apache.shardingsphere.traffic.api.config.TrafficStrategyConfiguration;
import org.apache.shardingsphere.traffic.distsql.parser.statement.queryable.ShowTrafficRulesStatement;
import org.apache.shardingsphere.traffic.rule.TrafficRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Show traffic rule executor.
 */
public final class ShowTrafficRuleExecutor implements MetaDataRequiredQueryableRALExecutor<ShowTrafficRulesStatement> {
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShardingSphereMetaData metaData, final ShowTrafficRulesStatement sqlStatement) {
        TrafficRule rule = metaData.getGlobalRuleMetaData().getSingleRule(TrafficRule.class);
        return buildData(rule.getConfiguration(), sqlStatement.getRuleName());
    }
    
    private Collection<LocalDataQueryResultRow> buildData(final TrafficRuleConfiguration ruleConfig, final String ruleName) {
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        ruleConfig.getTrafficStrategies().stream().filter(each -> null == ruleName || each.getName().equals(ruleName))
                .forEach(each -> result.add(buildRow(each, ruleConfig.getTrafficAlgorithms().get(each.getAlgorithmName()), ruleConfig.getLoadBalancers().get(each.getLoadBalancerName()))));
        return result;
    }
    
    private LocalDataQueryResultRow buildRow(final TrafficStrategyConfiguration strategy, final AlgorithmConfiguration trafficAlgorithm, final AlgorithmConfiguration loadBalancer) {
        return new LocalDataQueryResultRow(strategy.getName(), String.join(",", strategy.getLabels()), null != trafficAlgorithm ? trafficAlgorithm.getType() : "",
                null != trafficAlgorithm ? PropertiesConverter.convert(trafficAlgorithm.getProps()) : "", null != loadBalancer ? loadBalancer.getType() : "",
                null != loadBalancer ? PropertiesConverter.convert(loadBalancer.getProps()) : "");
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("name", "labels", "algorithm_type", "algorithm_props", "load_balancer_type", "load_balancer_props");
    }
    
    @Override
    public Class<ShowTrafficRulesStatement> getType() {
        return ShowTrafficRulesStatement.class;
    }
}
