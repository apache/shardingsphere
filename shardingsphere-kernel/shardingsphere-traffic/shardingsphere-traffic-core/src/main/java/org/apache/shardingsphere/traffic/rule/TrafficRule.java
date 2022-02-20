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
import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmFactory;
import org.apache.shardingsphere.infra.rule.identifier.scope.GlobalRule;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.CommentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.AbstractSQLStatement;
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
import org.apache.shardingsphere.transaction.TransactionHolder;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Traffic rule.
 */
public final class TrafficRule implements GlobalRule {
    
    static {
        ShardingSphereServiceLoader.register(TrafficAlgorithm.class);
        ShardingSphereServiceLoader.register(TrafficLoadBalanceAlgorithm.class);
    }
    
    private final Collection<TrafficStrategyRule> strategyRules;
    
    public TrafficRule(final TrafficRuleConfiguration config) {
        Map<String, TrafficAlgorithm> trafficAlgorithms = createTrafficAlgorithms(config.getTrafficAlgorithms());
        Map<String, TrafficLoadBalanceAlgorithm> loadBalancers = createTrafficLoadBalanceAlgorithms(config.getLoadBalancers());
        strategyRules = createTrafficStrategyRules(config.getTrafficStrategies(), trafficAlgorithms, loadBalancers);
    }
    
    private Map<String, TrafficAlgorithm> createTrafficAlgorithms(final Map<String, ShardingSphereAlgorithmConfiguration> trafficAlgorithms) {
        Map<String, TrafficAlgorithm> result = new LinkedHashMap<>();
        for (Entry<String, ShardingSphereAlgorithmConfiguration> entry : trafficAlgorithms.entrySet()) {
            result.put(entry.getKey(), ShardingSphereAlgorithmFactory.createAlgorithm(entry.getValue(), TrafficAlgorithm.class));
        }
        return result;
    }
    
    private Map<String, TrafficLoadBalanceAlgorithm> createTrafficLoadBalanceAlgorithms(final Map<String, ShardingSphereAlgorithmConfiguration> loadBalancers) {
        Map<String, TrafficLoadBalanceAlgorithm> result = new LinkedHashMap<>();
        for (Entry<String, ShardingSphereAlgorithmConfiguration> entry : loadBalancers.entrySet()) {
            result.put(entry.getKey(), ShardingSphereAlgorithmFactory.createAlgorithm(entry.getValue(), TrafficLoadBalanceAlgorithm.class));
        }
        return result;
    }
    
    private Collection<TrafficStrategyRule> createTrafficStrategyRules(final Collection<TrafficStrategyConfiguration> trafficStrategies, 
                                                                       final Map<String, TrafficAlgorithm> trafficAlgorithms, final Map<String, TrafficLoadBalanceAlgorithm> loadBalancers) {
        Collection<TrafficStrategyRule> noneTransactionStrategyRules = new LinkedList<>();
        Collection<TrafficStrategyRule> result = new LinkedList<>();
        for (TrafficStrategyConfiguration each : trafficStrategies) {
            TrafficAlgorithm trafficAlgorithm = getTrafficAlgorithm(trafficAlgorithms, each.getAlgorithmName());
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
            TrafficLoadBalanceAlgorithm loadBalancer = getLoadBalancer(loadBalancers, strategyConfig.getLoadBalancerName());
            result = new TrafficStrategyRule(strategyConfig.getName(), strategyConfig.getLabels(), trafficAlgorithm, loadBalancer);
        }
        return result;
    }
    
    private boolean isTransactionStrategyRule(final TrafficAlgorithm trafficAlgorithm) {
        return trafficAlgorithm instanceof TransactionTrafficAlgorithm;
    }
    
    /**
     * Find matched strategy rule.
     * 
     * @param logicSQL logic SQL
     * @return matched strategy rule
     */
    public Optional<TrafficStrategyRule> findMatchedStrategyRule(final LogicSQL logicSQL) {
        for (TrafficStrategyRule each : strategyRules) {
            if (match(each.getTrafficAlgorithm(), logicSQL)) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    private TrafficAlgorithm getTrafficAlgorithm(final Map<String, TrafficAlgorithm> trafficAlgorithms, final String algorithmName) {
        TrafficAlgorithm result = trafficAlgorithms.get(algorithmName);
        Preconditions.checkState(null != result, "Traffic algorithm can not be null.");
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private boolean match(final TrafficAlgorithm trafficAlgorithm, final LogicSQL logicSQL) {
        if (trafficAlgorithm instanceof TransactionTrafficAlgorithm) {
            return matchTransactionTraffic((TransactionTrafficAlgorithm) trafficAlgorithm);
        }
        SQLStatement sqlStatement = logicSQL.getSqlStatementContext().getSqlStatement();
        if (trafficAlgorithm instanceof HintTrafficAlgorithm) {
            return matchHintTraffic((HintTrafficAlgorithm<Comparable<?>>) trafficAlgorithm, sqlStatement);
        }
        if (trafficAlgorithm instanceof SegmentTrafficAlgorithm) {
            return matchSegmentTraffic((SegmentTrafficAlgorithm) trafficAlgorithm, logicSQL, sqlStatement);
        }
        return false;
    }
    
    private boolean matchHintTraffic(final HintTrafficAlgorithm<Comparable<?>> trafficAlgorithm, final SQLStatement sqlStatement) {
        for (HintTrafficValue<Comparable<?>> each : getHintTrafficValues(sqlStatement)) {
            if (trafficAlgorithm.match(each)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean matchSegmentTraffic(final SegmentTrafficAlgorithm trafficAlgorithm, final LogicSQL logicSQL, final SQLStatement sqlStatement) {
        SegmentTrafficValue segmentTrafficValue = new SegmentTrafficValue(sqlStatement, logicSQL.getSql());
        return trafficAlgorithm.match(segmentTrafficValue);
    }
    
    private boolean matchTransactionTraffic(final TransactionTrafficAlgorithm trafficAlgorithm) {
        TransactionTrafficValue transactionTrafficValue = new TransactionTrafficValue(TransactionHolder.isTransaction());
        return trafficAlgorithm.match(transactionTrafficValue);
    }
    
    private Collection<HintTrafficValue<Comparable<?>>> getHintTrafficValues(final SQLStatement sqlStatement) {
        Collection<HintTrafficValue<Comparable<?>>> result = new LinkedList<>();
        if (sqlStatement instanceof AbstractSQLStatement) {
            for (CommentSegment each : ((AbstractSQLStatement) sqlStatement).getCommentSegments()) {
                result.add(new HintTrafficValue<>(each.getText()));
            }
        }
        return result;
    }
    
    private TrafficLoadBalanceAlgorithm getLoadBalancer(final Map<String, TrafficLoadBalanceAlgorithm> loadBalancers, final String loadBalancerName) {
        TrafficLoadBalanceAlgorithm result = loadBalancers.get(loadBalancerName);
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
