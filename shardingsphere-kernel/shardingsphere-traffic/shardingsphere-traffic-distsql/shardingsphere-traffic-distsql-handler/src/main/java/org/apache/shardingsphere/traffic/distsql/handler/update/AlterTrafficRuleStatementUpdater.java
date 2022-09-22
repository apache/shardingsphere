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

package org.apache.shardingsphere.traffic.distsql.handler.update;

import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.distsql.update.GlobalRuleRALUpdater;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.traffic.api.config.TrafficRuleConfiguration;
import org.apache.shardingsphere.traffic.api.config.TrafficStrategyConfiguration;
import org.apache.shardingsphere.traffic.distsql.handler.convert.TrafficRuleConverter;
import org.apache.shardingsphere.traffic.distsql.parser.segment.TrafficRuleSegment;
import org.apache.shardingsphere.traffic.distsql.parser.statement.updatable.AlterTrafficRuleStatement;
import org.apache.shardingsphere.traffic.factory.TrafficAlgorithmFactory;
import org.apache.shardingsphere.traffic.factory.TrafficLoadBalanceAlgorithmFactory;
import org.apache.shardingsphere.traffic.rule.TrafficRule;
import org.apache.shardingsphere.traffic.rule.TrafficStrategyRule;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Alter traffic rule statement updater.
 */
public final class AlterTrafficRuleStatementUpdater implements GlobalRuleRALUpdater {
    
    @Override
    public void executeUpdate(final ShardingSphereMetaData metaData, final SQLStatement sqlStatement) throws DistSQLException {
        AlterTrafficRuleStatement statement = (AlterTrafficRuleStatement) sqlStatement;
        check(metaData.getGlobalRuleMetaData(), statement);
        replaceNewRule(metaData.getGlobalRuleMetaData(), statement);
    }
    
    private void check(final ShardingSphereRuleMetaData ruleMetaData, final AlterTrafficRuleStatement sqlStatement) throws DistSQLException {
        checkRuleNames(ruleMetaData, sqlStatement);
        checkAlgorithmNames(sqlStatement);
    }
    
    private void checkRuleNames(final ShardingSphereRuleMetaData ruleMetaData, final AlterTrafficRuleStatement sqlStatement) throws DistSQLException {
        Collection<String> notExistRuleNames = getNotExistRuleNames(ruleMetaData, sqlStatement);
        ShardingSpherePreconditions.checkState(notExistRuleNames.isEmpty(), () -> new RequiredRuleMissedException("Traffic", notExistRuleNames));
    }
    
    private Collection<String> getNotExistRuleNames(final ShardingSphereRuleMetaData ruleMetaData, final AlterTrafficRuleStatement sqlStatement) {
        TrafficRule currentRule = ruleMetaData.getSingleRule(TrafficRule.class);
        Collection<String> currentRuleNames = currentRule.getStrategyRules().stream().map(TrafficStrategyRule::getName).collect(Collectors.toSet());
        return sqlStatement.getSegments().stream().map(TrafficRuleSegment::getName).filter(each -> !currentRuleNames.contains(each)).collect(Collectors.toSet());
    }
    
    private void checkAlgorithmNames(final AlterTrafficRuleStatement sqlStatement) throws DistSQLException {
        Collection<String> invalidAlgorithmNames = getInvalidAlgorithmNames(sqlStatement);
        ShardingSpherePreconditions.checkState(invalidAlgorithmNames.isEmpty(), () -> new InvalidAlgorithmConfigurationException("Traffic", invalidAlgorithmNames));
    }
    
    private Collection<String> getInvalidAlgorithmNames(final AlterTrafficRuleStatement sqlStatement) {
        Collection<String> result = new LinkedList<>();
        for (TrafficRuleSegment each : sqlStatement.getSegments()) {
            if (!TrafficAlgorithmFactory.contains(each.getAlgorithm().getName())) {
                result.add(each.getAlgorithm().getName());
            }
            if (null != each.getLoadBalancer() && !TrafficLoadBalanceAlgorithmFactory.contains(each.getLoadBalancer().getName())) {
                result.add(each.getLoadBalancer().getName());
            }
        }
        return result;
    }
    
    private void replaceNewRule(final ShardingSphereRuleMetaData ruleMetaData, final AlterTrafficRuleStatement sqlStatement) {
        TrafficRuleConfiguration toBeAlteredRuleConfig = createToBeAlteredRuleConfiguration(ruleMetaData, sqlStatement);
        Collection<ShardingSphereRule> globalRules = ruleMetaData.getRules();
        TrafficRule trafficRule = new TrafficRule(toBeAlteredRuleConfig);
        globalRules.removeIf(each -> each instanceof TrafficRule);
        globalRules.add(trafficRule);
    }
    
    private TrafficRuleConfiguration createToBeAlteredRuleConfiguration(final ShardingSphereRuleMetaData ruleMetaData, final AlterTrafficRuleStatement sqlStatement) {
        TrafficRuleConfiguration result = new TrafficRuleConfiguration();
        TrafficRuleConfiguration configFromSQLStatement = TrafficRuleConverter.convert(sqlStatement.getSegments());
        TrafficRuleConfiguration currentConfig = ruleMetaData.getSingleRule(TrafficRule.class).getConfiguration();
        result.getTrafficStrategies().addAll(createToBeAlteredStrategyConfigurations(currentConfig, configFromSQLStatement));
        result.getTrafficAlgorithms().putAll(createToBeAlteredTrafficAlgorithms(currentConfig, configFromSQLStatement, getInUsedTrafficAlgorithm(result)));
        result.getLoadBalancers().putAll(createToBeAlteredLoadBalancers(currentConfig, configFromSQLStatement, getInUsedLoadBalancer(result)));
        return result;
    }
    
    private Collection<TrafficStrategyConfiguration> createToBeAlteredStrategyConfigurations(final TrafficRuleConfiguration currentConfig, final TrafficRuleConfiguration configFromSQLStatement) {
        Collection<TrafficStrategyConfiguration> result = new LinkedList<>(currentConfig.getTrafficStrategies());
        Collection<String> toBeAlteredConfigNames = configFromSQLStatement.getTrafficStrategies().stream().map(TrafficStrategyConfiguration::getName).collect(Collectors.toSet());
        result.removeIf(each -> toBeAlteredConfigNames.contains(each.getName()));
        result.addAll(configFromSQLStatement.getTrafficStrategies());
        return result;
    }
    
    private Collection<String> getInUsedTrafficAlgorithm(final TrafficRuleConfiguration config) {
        return config.getTrafficStrategies().stream().map(TrafficStrategyConfiguration::getAlgorithmName).collect(Collectors.toSet());
    }
    
    private Map<String, AlgorithmConfiguration> createToBeAlteredTrafficAlgorithms(final TrafficRuleConfiguration currentConfig, final TrafficRuleConfiguration configFromSQLStatement,
                                                                                   final Collection<String> inUsedTrafficAlgorithm) {
        Map<String, AlgorithmConfiguration> result = new LinkedHashMap<>(currentConfig.getTrafficAlgorithms());
        result.putAll(configFromSQLStatement.getTrafficAlgorithms());
        for (String each : result.keySet().stream().filter(each -> !inUsedTrafficAlgorithm.contains(each)).collect(Collectors.toSet())) {
            result.remove(each);
        }
        return result;
    }
    
    private Collection<String> getInUsedLoadBalancer(final TrafficRuleConfiguration config) {
        return config.getTrafficStrategies().stream().map(TrafficStrategyConfiguration::getLoadBalancerName).collect(Collectors.toSet());
    }
    
    private Map<String, AlgorithmConfiguration> createToBeAlteredLoadBalancers(final TrafficRuleConfiguration currentConfig,
                                                                               final TrafficRuleConfiguration configFromSQLStatement, final Collection<String> inUsedLoadBalancer) {
        Map<String, AlgorithmConfiguration> result = new LinkedHashMap<>(currentConfig.getLoadBalancers());
        result.putAll(configFromSQLStatement.getLoadBalancers());
        for (String each : result.keySet().stream().filter(each -> !inUsedLoadBalancer.contains(each)).collect(Collectors.toSet())) {
            result.remove(each);
        }
        return result;
    }
    
    @Override
    public String getType() {
        return AlterTrafficRuleStatement.class.getName();
    }
}
