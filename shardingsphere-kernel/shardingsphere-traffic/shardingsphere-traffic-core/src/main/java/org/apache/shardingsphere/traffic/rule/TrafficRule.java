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
import org.apache.shardingsphere.traffic.api.traffic.segment.SegmentTrafficAlgorithm;
import org.apache.shardingsphere.traffic.api.traffic.segment.SegmentTrafficValue;
import org.apache.shardingsphere.traffic.api.traffic.transaction.TransactionTrafficAlgorithm;
import org.apache.shardingsphere.traffic.api.traffic.transaction.TransactionTrafficValue;
import org.apache.shardingsphere.traffic.spi.TrafficAlgorithm;
import org.apache.shardingsphere.traffic.spi.TrafficLoadBalanceAlgorithm;
import org.apache.shardingsphere.transaction.TransactionHolder;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

/**
 * Traffic rule.
 */
public final class TrafficRule implements GlobalRule {
    
    static {
        ShardingSphereServiceLoader.register(TrafficAlgorithm.class);
        ShardingSphereServiceLoader.register(TrafficLoadBalanceAlgorithm.class);
    }
    
    private final Collection<TrafficStrategyRule> trafficStrategyRules = new LinkedList<>();
    
    private final Map<String, TrafficAlgorithm> trafficAlgorithms = new LinkedHashMap<>();
    
    private final Map<String, TrafficLoadBalanceAlgorithm> loadBalancers = new LinkedHashMap<>();
    
    public TrafficRule(final TrafficRuleConfiguration config) {
        config.getTrafficAlgorithms().forEach((key, value) -> trafficAlgorithms.put(key, ShardingSphereAlgorithmFactory.createAlgorithm(value, TrafficAlgorithm.class)));
        config.getLoadBalancers().forEach((key, value) -> loadBalancers.put(key, ShardingSphereAlgorithmFactory.createAlgorithm(value, TrafficLoadBalanceAlgorithm.class)));
        trafficStrategyRules.addAll(createTrafficStrategyRules(trafficAlgorithms, config));
    }
    
    private Collection<TrafficStrategyRule> createTrafficStrategyRules(final Map<String, TrafficAlgorithm> trafficAlgorithms, final TrafficRuleConfiguration config) {
        Optional<TrafficStrategyRule> transactionStrategyRule = Optional.empty();
        Collection<TrafficStrategyRule> noneTransactionStrategyRules = new LinkedList<>();
        Collection<TrafficStrategyRule> result = new LinkedList<>();
        for (TrafficStrategyConfiguration each : config.getTrafficStrategies()) {
            TrafficStrategyRule trafficStrategyRule = new TrafficStrategyRule(each.getName(), each.getLabels(), each.getAlgorithmName(), each.getLoadBalancerName());
            if (isTransactionTrafficStrategyRule(trafficAlgorithms, each.getAlgorithmName())) {
                Preconditions.checkState(!transactionStrategyRule.isPresent(), "Transaction traffic strategy rule configuration can only config once.");
                transactionStrategyRule = Optional.of(trafficStrategyRule);
            } else {
                noneTransactionStrategyRules.add(trafficStrategyRule);
            }
        }
        transactionStrategyRule.ifPresent(result::add);
        result.addAll(noneTransactionStrategyRules);
        return result;
    }
    
    private boolean isTransactionTrafficStrategyRule(final Map<String, TrafficAlgorithm> trafficAlgorithms, final String algorithmName) {
        TrafficAlgorithm trafficAlgorithm = getTrafficAlgorithm(trafficAlgorithms, algorithmName);
        return trafficAlgorithm instanceof TransactionTrafficAlgorithm;
    }
    
    /**
     * Find matched strategy rule.
     * 
     * @param logicSQL logic SQL
     * @return matched strategy rule
     */
    public Optional<TrafficStrategyRule> findMatchedStrategyRule(final LogicSQL logicSQL) {
        for (TrafficStrategyRule each : trafficStrategyRules) {
            TrafficAlgorithm trafficAlgorithm = getTrafficAlgorithm(trafficAlgorithms, each.getAlgorithmName());
            if (match(trafficAlgorithm, logicSQL)) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    private TrafficAlgorithm getTrafficAlgorithm(final Map<String, TrafficAlgorithm> trafficAlgorithms, final String algorithmName) {
        TrafficAlgorithm trafficAlgorithm = trafficAlgorithms.get(algorithmName);
        Preconditions.checkState(null != trafficAlgorithm, "Traffic strategy rule configuration must match traffic algorithm.");
        return trafficAlgorithm;
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
    
    /**
     * Find load balancer.
     * 
     * @param loadBalancerName load balancer name
     * @return load balancer
     */
    public TrafficLoadBalanceAlgorithm findLoadBalancer(final String loadBalancerName) {
        TrafficLoadBalanceAlgorithm loadBalanceAlgorithm = loadBalancers.get(loadBalancerName);
        Preconditions.checkState(null != loadBalanceAlgorithm, "Traffic load balance algorithm can not be null.");
        return loadBalanceAlgorithm;
    }
    
    /**
     * Get label collection.
     * 
     * @return label collection
     */
    public Collection<String> getLabels() {
        Collection<String> result = new LinkedList<>();
        for (TrafficStrategyRule each : trafficStrategyRules) {
            result.addAll(each.getLabels());
        }
        return result;
    }
    
    @Override
    public String getType() {
        return TrafficRule.class.getSimpleName();
    }
}
