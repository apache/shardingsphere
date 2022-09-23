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

import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.traffic.api.config.TrafficRuleConfiguration;
import org.apache.shardingsphere.traffic.api.config.TrafficStrategyConfiguration;
import org.apache.shardingsphere.traffic.distsql.parser.segment.TrafficRuleSegment;
import org.apache.shardingsphere.traffic.distsql.parser.statement.updatable.AlterTrafficRuleStatement;
import org.apache.shardingsphere.traffic.rule.TrafficRule;
import org.apache.shardingsphere.traffic.rule.TrafficStrategyRule;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class AlterTrafficRuleStatementUpdaterTest {
    
    @Test(expected = MissingRequiredRuleException.class)
    public void assertExecuteWithNotExistRuleName() {
        ShardingSphereMetaData metaData = createMetaData();
        TrafficRuleSegment trafficRuleSegment = new TrafficRuleSegment(
                "rule_name_3", Arrays.asList("olap", "order_by"), new AlgorithmSegment("DISTSQL.FIXTURE", new Properties()), new AlgorithmSegment("DISTSQL.FIXTURE", new Properties()));
        AlterTrafficRuleStatementUpdater updater = new AlterTrafficRuleStatementUpdater();
        updater.executeUpdate(metaData, new AlterTrafficRuleStatement(Collections.singleton(trafficRuleSegment)));
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertExecuteWithInvalidAlgorithmType() {
        ShardingSphereMetaData metaData = createMetaData();
        TrafficRuleSegment trafficRuleSegment = new TrafficRuleSegment(
                "rule_name_1", Arrays.asList("olap", "order_by"), new AlgorithmSegment("invalid", new Properties()), new AlgorithmSegment("invalid", new Properties()));
        AlterTrafficRuleStatementUpdater updater = new AlterTrafficRuleStatementUpdater();
        updater.executeUpdate(metaData, new AlterTrafficRuleStatement(Collections.singleton(trafficRuleSegment)));
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertExecuteWithLoadBalancerCannotBeNull() {
        ShardingSphereMetaData metaData = createMetaData();
        TrafficRuleSegment trafficRuleSegment = new TrafficRuleSegment("rule_name_1", Arrays.asList("olap", "order_by"),
                new AlgorithmSegment("DISTSQL.FIXTURE", new Properties()), null);
        AlterTrafficRuleStatementUpdater updater = new AlterTrafficRuleStatementUpdater();
        try {
            updater.executeUpdate(metaData, new AlterTrafficRuleStatement(Collections.singleton(trafficRuleSegment)));
        } catch (final IllegalStateException ex) {
            TrafficRule currentRule = metaData.getGlobalRuleMetaData().getSingleRule(TrafficRule.class);
            assertNotNull(currentRule);
            throw ex;
        }
    }
    
    @Test
    public void assertExecute() throws SQLException {
        ShardingSphereMetaData metaData = createMetaData();
        TrafficRuleSegment trafficRuleSegment1 = new TrafficRuleSegment(
                "rule_name_1", Arrays.asList("olap", "order_by"), new AlgorithmSegment("DISTSQL.FIXTURE", new Properties()), new AlgorithmSegment("DISTSQL.FIXTURE", new Properties()));
        TrafficRuleSegment trafficRuleSegment2 = new TrafficRuleSegment(
                "rule_name_2", Collections.emptyList(), new AlgorithmSegment("DISTSQL.FIXTURE", new Properties()), new AlgorithmSegment("DISTSQL.FIXTURE", new Properties()));
        AlterTrafficRuleStatementUpdater updater = new AlterTrafficRuleStatementUpdater();
        updater.executeUpdate(metaData, new AlterTrafficRuleStatement(Arrays.asList(trafficRuleSegment1, trafficRuleSegment2)));
        TrafficRuleConfiguration alteredConfig = metaData.getGlobalRuleMetaData().getSingleRule(TrafficRule.class).getConfiguration();
        assertThat(alteredConfig.getTrafficStrategies().size(), is(2));
        assertThat(alteredConfig.getLoadBalancers().size(), is(2));
        assertThat(alteredConfig.getTrafficAlgorithms().size(), is(2));
        assertThat(alteredConfig.getTrafficStrategies().iterator().next().getName(), is("rule_name_1"));
        assertNotNull(alteredConfig.getTrafficAlgorithms().get("rule_name_1_distsql.fixture"));
        assertNotNull(alteredConfig.getLoadBalancers().get("rule_name_2_distsql.fixture"));
    }
    
    private ShardingSphereMetaData createMetaData() {
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(new LinkedList<>(Collections.singleton(mockTrafficRule())));
        when(metaData.getGlobalRuleMetaData()).thenReturn(ruleMetaData);
        return metaData;
    }
    
    private TrafficRule mockTrafficRule() {
        TrafficRule result = mock(TrafficRule.class);
        when(result.getConfiguration()).thenReturn(createTrafficRuleConfiguration());
        TrafficStrategyRule strategyRule1 = mock(TrafficStrategyRule.class);
        when(strategyRule1.getName()).thenReturn("rule_name_1");
        TrafficStrategyRule strategyRule2 = mock(TrafficStrategyRule.class);
        when(strategyRule2.getName()).thenReturn("rule_name_2");
        when(result.getStrategyRules()).thenReturn(Arrays.asList(strategyRule1, strategyRule2));
        return result;
    }
    
    private TrafficRuleConfiguration createTrafficRuleConfiguration() {
        TrafficRuleConfiguration result = new TrafficRuleConfiguration();
        result.getTrafficStrategies().add(new TrafficStrategyConfiguration("rule_name_1", Arrays.asList("olap", "order_by"), "algorithm_1", "load_balancer_1"));
        result.getTrafficStrategies().add(new TrafficStrategyConfiguration("rule_name_2", Collections.singletonList("oltp"), "algorithm_2", "load_balancer_2"));
        result.getTrafficAlgorithms().put("algorithm_1", new AlgorithmConfiguration("SQL_MATCH", createProperties()));
        result.getTrafficAlgorithms().put("algorithm_2", new AlgorithmConfiguration("SQL_HINT", new Properties()));
        result.getLoadBalancers().put("load_balancer_1", new AlgorithmConfiguration("RANDOM", new Properties()));
        result.getLoadBalancers().put("load_balancer_2", new AlgorithmConfiguration("ROUND_ROBIN", new Properties()));
        return result;
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.put("sql", "select * from t_order");
        return result;
    }
}
