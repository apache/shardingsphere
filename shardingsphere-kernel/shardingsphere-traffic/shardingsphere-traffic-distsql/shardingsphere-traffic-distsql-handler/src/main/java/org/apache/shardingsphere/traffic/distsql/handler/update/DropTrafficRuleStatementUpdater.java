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
import org.apache.shardingsphere.infra.distsql.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.distsql.update.GlobalRuleRALUpdater;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.traffic.api.config.TrafficRuleConfiguration;
import org.apache.shardingsphere.traffic.api.config.TrafficStrategyConfiguration;
import org.apache.shardingsphere.traffic.distsql.parser.statement.updatable.DropTrafficRuleStatement;
import org.apache.shardingsphere.traffic.rule.TrafficRule;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Drop traffic rule statement updater.
 */
public final class DropTrafficRuleStatementUpdater implements GlobalRuleRALUpdater {
    
    @Override
    public void executeUpdate(final ShardingSphereMetaData metaData, final SQLStatement sqlStatement) throws DistSQLException {
        DropTrafficRuleStatement statement = (DropTrafficRuleStatement) sqlStatement;
        check(metaData.getGlobalRuleMetaData(), statement);
        replaceNewRule(metaData.getGlobalRuleMetaData(), statement);
    }
    
    private void check(final ShardingSphereRuleMetaData ruleMetaData, final DropTrafficRuleStatement sqlStatement) throws DistSQLException {
        checkRuleNames(ruleMetaData, sqlStatement);
    }
    
    private void checkRuleNames(final ShardingSphereRuleMetaData ruleMetaData, final DropTrafficRuleStatement sqlStatement) throws DistSQLException {
        if (sqlStatement.isIfExists()) {
            return;
        }
        TrafficRule rule = ruleMetaData.getSingleRule(TrafficRule.class);
        TrafficRuleConfiguration config = rule.getConfiguration();
        Collection<String> currentRuleNames = config.getTrafficStrategies().stream().map(TrafficStrategyConfiguration::getName).collect(Collectors.toSet());
        Collection<String> notExistRuleNames = sqlStatement.getRuleNames().stream().filter(each -> !currentRuleNames.contains(each)).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkState(notExistRuleNames.isEmpty(), () -> new MissingRequiredRuleException("Traffic"));
    }
    
    private void replaceNewRule(final ShardingSphereRuleMetaData ruleMetaData, final DropTrafficRuleStatement sqlStatement) {
        TrafficRuleConfiguration toBeAlteredRuleConfig = createToBeAlteredRuleConfiguration(ruleMetaData, sqlStatement);
        Collection<ShardingSphereRule> globalRules = ruleMetaData.getRules();
        globalRules.removeIf(each -> each instanceof TrafficRule);
        globalRules.add(new TrafficRule(toBeAlteredRuleConfig));
    }
    
    private TrafficRuleConfiguration createToBeAlteredRuleConfiguration(final ShardingSphereRuleMetaData ruleMetaData, final DropTrafficRuleStatement sqlStatement) {
        TrafficRuleConfiguration result = new TrafficRuleConfiguration();
        TrafficRuleConfiguration currentConfig = ruleMetaData.getSingleRule(TrafficRule.class).getConfiguration();
        result.getTrafficStrategies().addAll(createToBeAlteredStrategyConfigurations(currentConfig, sqlStatement));
        result.getTrafficAlgorithms().putAll(createToBeAlteredTrafficAlgorithm(currentConfig, getInUsedTrafficAlgorithm(result)));
        result.getLoadBalancers().putAll(createToBeAlteredLoadBalancers(currentConfig, getInUsedLoadBalancer(result)));
        return result;
    }
    
    private Collection<TrafficStrategyConfiguration> createToBeAlteredStrategyConfigurations(final TrafficRuleConfiguration currentConfig, final DropTrafficRuleStatement statement) {
        return currentConfig.getTrafficStrategies().stream().filter(each -> !statement.getRuleNames().contains(each.getName())).collect(Collectors.toList());
    }
    
    private Collection<String> getInUsedTrafficAlgorithm(final TrafficRuleConfiguration config) {
        return config.getTrafficStrategies().stream().map(TrafficStrategyConfiguration::getAlgorithmName).collect(Collectors.toSet());
    }
    
    private Map<String, AlgorithmConfiguration> createToBeAlteredTrafficAlgorithm(final TrafficRuleConfiguration currentConfig, final Collection<String> inUsedTrafficAlgorithm) {
        Map<String, AlgorithmConfiguration> result = new LinkedHashMap<>(currentConfig.getTrafficAlgorithms());
        for (String each : result.keySet().stream().filter(each -> !inUsedTrafficAlgorithm.contains(each)).collect(Collectors.toSet())) {
            result.remove(each);
        }
        return result;
    }
    
    private Collection<String> getInUsedLoadBalancer(final TrafficRuleConfiguration config) {
        return config.getTrafficStrategies().stream().map(TrafficStrategyConfiguration::getLoadBalancerName).collect(Collectors.toSet());
    }
    
    private Map<String, AlgorithmConfiguration> createToBeAlteredLoadBalancers(final TrafficRuleConfiguration currentConfig, final Collection<String> inUsedLoadBalancer) {
        Map<String, AlgorithmConfiguration> result = new LinkedHashMap<>(currentConfig.getLoadBalancers());
        for (String each : result.keySet().stream().filter(each -> !inUsedLoadBalancer.contains(each)).collect(Collectors.toSet())) {
            result.remove(each);
        }
        return result;
    }
    
    @Override
    public String getType() {
        return DropTrafficRuleStatement.class.getName();
    }
}
