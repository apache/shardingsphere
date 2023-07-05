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

package org.apache.shardingsphere.traffic.rule;

import com.google.common.base.Preconditions;
import lombok.Getter;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.rule.identifier.scope.GlobalRule;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.traffic.api.config.TrafficRuleConfiguration;
import org.apache.shardingsphere.traffic.api.config.TrafficStrategyConfiguration;
import org.apache.shardingsphere.traffic.api.traffic.hint.HintTrafficAlgorithm;
import org.apache.shardingsphere.traffic.api.traffic.hint.HintTrafficValue;
import org.apache.shardingsphere.traffic.api.traffic.identifier.SimplifiedTrafficAlgorithm;
import org.apache.shardingsphere.traffic.api.traffic.segment.SegmentTrafficAlgorithm;
import org.apache.shardingsphere.traffic.api.traffic.segment.SegmentTrafficValue;
import org.apache.shardingsphere.traffic.api.traffic.transaction.TransactionTrafficAlgorithm;
import org.apache.shardingsphere.traffic.api.traffic.transaction.TransactionTrafficValue;
import org.apache.shardingsphere.traffic.spi.TrafficAlgorithm;
import org.apache.shardingsphere.traffic.spi.TrafficLoadBalanceAlgorithm;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

/**
 * Traffic rule.
 */
@Getter
public final class TrafficRule implements GlobalRule {
    
    private final TrafficRuleConfiguration configuration;
    
    private final Collection<TrafficStrategyRule> strategyRules;
    
    public TrafficRule(final TrafficRuleConfiguration ruleConfig) {
        configuration = ruleConfig;
        Map<String, TrafficAlgorithm> trafficAlgorithms = createTrafficAlgorithms(ruleConfig.getTrafficAlgorithms());
        Map<String, TrafficLoadBalanceAlgorithm> loadBalancers = createTrafficLoadBalanceAlgorithms(ruleConfig.getLoadBalancers());
        strategyRules = createTrafficStrategyRules(ruleConfig.getTrafficStrategies(), trafficAlgorithms, loadBalancers);
    }
    
    private Map<String, TrafficAlgorithm> createTrafficAlgorithms(final Map<String, AlgorithmConfiguration> trafficAlgorithms) {
        Map<String, TrafficAlgorithm> result = new LinkedHashMap<>();
        for (TrafficStrategyConfiguration each : configuration.getTrafficStrategies()) {
            if (null == trafficAlgorithms.get(each.getAlgorithmName())) {
                break;
            }
            AlgorithmConfiguration algorithmConfig = trafficAlgorithms.get(each.getAlgorithmName());
            result.put(each.getName() + "." + each.getAlgorithmName(), TypedSPILoader.getService(TrafficAlgorithm.class, algorithmConfig.getType(), algorithmConfig.getProps()));
        }
        return result;
    }
    
    private Map<String, TrafficLoadBalanceAlgorithm> createTrafficLoadBalanceAlgorithms(final Map<String, AlgorithmConfiguration> loadBalancers) {
        Map<String, TrafficLoadBalanceAlgorithm> result = new LinkedHashMap<>();
        for (TrafficStrategyConfiguration each : configuration.getTrafficStrategies()) {
            if (null == loadBalancers.get(each.getLoadBalancerName())) {
                break;
            }
            AlgorithmConfiguration algorithmConfig = loadBalancers.get(each.getLoadBalancerName());
            result.put(each.getName() + "." + each.getLoadBalancerName(), TypedSPILoader.getService(TrafficLoadBalanceAlgorithm.class, algorithmConfig.getType(), algorithmConfig.getProps()));
        }
        return result;
    }
    
    private Collection<TrafficStrategyRule> createTrafficStrategyRules(final Collection<TrafficStrategyConfiguration> trafficStrategies,
                                                                       final Map<String, TrafficAlgorithm> trafficAlgorithms, final Map<String, TrafficLoadBalanceAlgorithm> loadBalancers) {
        Collection<TrafficStrategyRule> noneTransactionStrategyRules = new LinkedList<>();
        Collection<TrafficStrategyRule> result = new LinkedList<>();
        for (TrafficStrategyConfiguration each : trafficStrategies) {
            TrafficAlgorithm trafficAlgorithm = getTrafficAlgorithm(each, trafficAlgorithms);
            TrafficStrategyRule trafficStrategyRule = createTrafficStrategyRule(each, trafficAlgorithm, loadBalancers);
            if (isTransactionStrategyRule(trafficAlgorithm)) {
                result.add(trafficStrategyRule);
            } else {
                noneTransactionStrategyRules.add(trafficStrategyRule);
            }
        }
        result.addAll(noneTransactionStrategyRules);
        return result;
    }
    
    private TrafficStrategyRule createTrafficStrategyRule(final TrafficStrategyConfiguration strategyConfig, final TrafficAlgorithm trafficAlgorithm,
                                                          final Map<String, TrafficLoadBalanceAlgorithm> loadBalancers) {
        TrafficStrategyRule result;
        if (trafficAlgorithm instanceof SimplifiedTrafficAlgorithm) {
            result = new TrafficStrategyRule(strategyConfig.getName(), Collections.emptyList(), trafficAlgorithm, null);
        } else {
            TrafficLoadBalanceAlgorithm loadBalancer = getLoadBalancer(strategyConfig, loadBalancers);
            result = new TrafficStrategyRule(strategyConfig.getName(), new LinkedHashSet<>(strategyConfig.getLabels()), trafficAlgorithm, loadBalancer);
        }
        return result;
    }
    
    private boolean isTransactionStrategyRule(final TrafficAlgorithm trafficAlgorithm) {
        return trafficAlgorithm instanceof TransactionTrafficAlgorithm;
    }
    
    /**
     * Find matched strategy rule.
     * 
     * @param queryContext query context
     * @param inTransaction is in transaction
     * @return matched strategy rule
     */
    public Optional<TrafficStrategyRule> findMatchedStrategyRule(final QueryContext queryContext, final boolean inTransaction) {
        for (TrafficStrategyRule each : strategyRules) {
            if (match(each.getTrafficAlgorithm(), queryContext, inTransaction)) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    private TrafficAlgorithm getTrafficAlgorithm(final TrafficStrategyConfiguration strategyConfig, final Map<String, TrafficAlgorithm> trafficAlgorithms) {
        TrafficAlgorithm result = trafficAlgorithms.get(strategyConfig.getName() + "." + strategyConfig.getAlgorithmName());
        Preconditions.checkState(null != result, "Traffic algorithm can not be null.");
        return result;
    }
    
    private boolean match(final TrafficAlgorithm trafficAlgorithm, final QueryContext queryContext, final boolean inTransaction) {
        if (trafficAlgorithm instanceof TransactionTrafficAlgorithm) {
            return matchTransactionTraffic((TransactionTrafficAlgorithm) trafficAlgorithm, inTransaction);
        }
        if (trafficAlgorithm instanceof HintTrafficAlgorithm) {
            return matchHintTraffic((HintTrafficAlgorithm) trafficAlgorithm, queryContext.getHintValueContext());
        }
        if (trafficAlgorithm instanceof SegmentTrafficAlgorithm) {
            SQLStatement sqlStatement = queryContext.getSqlStatementContext().getSqlStatement();
            return matchSegmentTraffic((SegmentTrafficAlgorithm) trafficAlgorithm, queryContext.getSql(), sqlStatement);
        }
        return false;
    }
    
    private boolean matchHintTraffic(final HintTrafficAlgorithm trafficAlgorithm, final HintValueContext hintValueContext) {
        HintTrafficValue hintTrafficValue = new HintTrafficValue(hintValueContext);
        return trafficAlgorithm.match(hintTrafficValue);
    }
    
    private boolean matchSegmentTraffic(final SegmentTrafficAlgorithm trafficAlgorithm, final String sql, final SQLStatement sqlStatement) {
        SegmentTrafficValue segmentTrafficValue = new SegmentTrafficValue(sqlStatement, sql);
        return trafficAlgorithm.match(segmentTrafficValue);
    }
    
    private boolean matchTransactionTraffic(final TransactionTrafficAlgorithm trafficAlgorithm, final boolean inTransaction) {
        TransactionTrafficValue transactionTrafficValue = new TransactionTrafficValue(inTransaction);
        return trafficAlgorithm.match(transactionTrafficValue);
    }
    
    private TrafficLoadBalanceAlgorithm getLoadBalancer(final TrafficStrategyConfiguration strategyConfig, final Map<String, TrafficLoadBalanceAlgorithm> loadBalancers) {
        TrafficLoadBalanceAlgorithm result = loadBalancers.get(strategyConfig.getName() + "." + strategyConfig.getLoadBalancerName());
        Preconditions.checkState(null != result, "Traffic load balance algorithm can not be null.");
        return result;
    }
    
    /**
     * Get label collection.
     * 
     * @return label collection
     */
    public Collection<String> getLabels() {
        Collection<String> result = new HashSet<>();
        for (TrafficStrategyRule each : strategyRules) {
            result.addAll(each.getLabels());
        }
        return result;
    }
    
    @Override
    public String getType() {
        return TrafficRule.class.getSimpleName();
    }
}
