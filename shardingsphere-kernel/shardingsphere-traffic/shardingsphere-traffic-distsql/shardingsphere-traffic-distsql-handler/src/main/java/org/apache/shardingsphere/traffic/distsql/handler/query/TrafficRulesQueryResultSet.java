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

import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.query.GlobalRuleDistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.util.props.PropertiesConverter;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.traffic.api.config.TrafficRuleConfiguration;
import org.apache.shardingsphere.traffic.api.config.TrafficStrategyConfiguration;
import org.apache.shardingsphere.traffic.distsql.parser.statement.queryable.ShowTrafficRulesStatement;
import org.apache.shardingsphere.traffic.rule.TrafficRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Query result set for traffic rule.
 */
public final class TrafficRulesQueryResultSet implements GlobalRuleDistSQLResultSet {
    
    private static final String RULE_NAME = "name";
    
    private static final String LABELS = "labels";
    
    private static final String ALGORITHM_TYPE = "algorithm_type";
    
    private static final String ALGORITHM_PROPS = "algorithm_props";
    
    private static final String LOAD_BALANCER_TYPE = "load_balancer_type";
    
    private static final String LOAD_BALANCER_PROPS = "load_balancer_props";
    
    private Iterator<Collection<Object>> data = Collections.emptyIterator();
    
    @Override
    public void init(final ShardingSphereRuleMetaData ruleMetaData, final SQLStatement sqlStatement) {
        Optional<TrafficRule> rule = ruleMetaData.findSingleRule(TrafficRule.class);
        ShowTrafficRulesStatement statement = (ShowTrafficRulesStatement) sqlStatement;
        Optional<String> ruleName = Optional.ofNullable(statement.getRuleName());
        rule.ifPresent(optional -> data = buildData(optional.getConfiguration(), ruleName).iterator());
    }
    
    private Collection<Collection<Object>> buildData(final TrafficRuleConfiguration ruleConfig, final Optional<String> ruleName) {
        Collection<Collection<Object>> result = new LinkedList<>();
        ruleConfig.getTrafficStrategies().stream().filter(each -> !ruleName.isPresent() || each.getName().equals(ruleName.get()))
                .forEach(each -> result.add(buildRow(each, ruleConfig.getTrafficAlgorithms().get(each.getAlgorithmName()), ruleConfig.getLoadBalancers().get(each.getLoadBalancerName()))));
        return result;
    }
    
    private Collection<Object> buildRow(final TrafficStrategyConfiguration strategy, final AlgorithmConfiguration trafficAlgorithm, final AlgorithmConfiguration loadBalancer) {
        return Arrays.asList(strategy.getName(), String.join(",", strategy.getLabels()), null != trafficAlgorithm ? trafficAlgorithm.getType() : "",
                null != trafficAlgorithm ? PropertiesConverter.convert(trafficAlgorithm.getProps()) : "", null != loadBalancer ? loadBalancer.getType() : "",
                null != loadBalancer ? PropertiesConverter.convert(loadBalancer.getProps()) : "");
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList(RULE_NAME, LABELS, ALGORITHM_TYPE, ALGORITHM_PROPS, LOAD_BALANCER_TYPE, LOAD_BALANCER_PROPS);
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        return data.next();
    }
    
    @Override
    public String getType() {
        return ShowTrafficRulesStatement.class.getName();
    }
}
