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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.updatable;

import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.distsql.parser.segment.TrafficRuleSegment;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.AlterTrafficRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.CreateTrafficRuleStatement;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.apache.shardingsphere.traffic.api.config.TrafficRuleConfiguration;
import org.apache.shardingsphere.traffic.api.config.TrafficStrategyConfiguration;
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
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class AlterTrafficRuleHandlerTest extends ProxyContextRestorer {
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertExecuteWithNotExistRuleName() throws SQLException {
        mockContextManager();
        TrafficRuleSegment trafficRuleSegment = new TrafficRuleSegment(
                "rule_name_3", Arrays.asList("olap", "order_by"), new AlgorithmSegment("DISTSQL.FIXTURE", new Properties()), new AlgorithmSegment("DISTSQL.FIXTURE", new Properties()));
        AlterTrafficRuleHandler handler = new AlterTrafficRuleHandler();
        handler.init(new AlterTrafficRuleStatement(Collections.singleton(trafficRuleSegment)), null);
        handler.execute();
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertExecuteWithInvalidAlgorithmType() throws SQLException {
        mockContextManager();
        TrafficRuleSegment trafficRuleSegment = new TrafficRuleSegment(
                "rule_name_1", Arrays.asList("olap", "order_by"), new AlgorithmSegment("invalid", new Properties()), new AlgorithmSegment("invalid", new Properties()));
        AlterTrafficRuleHandler handler = new AlterTrafficRuleHandler();
        handler.init(new AlterTrafficRuleStatement(Collections.singleton(trafficRuleSegment)), null);
        handler.execute();
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertExecuteWithLoadBalancerCannotBeNull() throws SQLException {
        mockContextManager();
        TrafficRuleSegment trafficRuleSegment = new TrafficRuleSegment("input_rule_name", Arrays.asList("olap", "order_by"),
                new AlgorithmSegment("DISTSQL.FIXTURE", new Properties()), null);
        CreateTrafficRuleHandler handler = new CreateTrafficRuleHandler();
        handler.init(new CreateTrafficRuleStatement(Collections.singleton(trafficRuleSegment)), null);
        try {
            handler.execute();
        } catch (final IllegalStateException ex) {
            TrafficRule currentRule = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(TrafficRule.class);
            assertNotNull(currentRule);
            throw ex;
        }
    }
    
    @Test
    public void assertExecute() throws SQLException {
        ContextManager contextManager = mockContextManager();
        TrafficRuleSegment trafficRuleSegment1 = new TrafficRuleSegment(
                "rule_name_1", Arrays.asList("olap", "order_by"), new AlgorithmSegment("DISTSQL.FIXTURE", new Properties()), new AlgorithmSegment("DISTSQL.FIXTURE", new Properties()));
        TrafficRuleSegment trafficRuleSegment2 = new TrafficRuleSegment(
                "rule_name_2", Collections.emptyList(), new AlgorithmSegment("DISTSQL.FIXTURE", new Properties()), new AlgorithmSegment("DISTSQL.FIXTURE", new Properties()));
        AlterTrafficRuleHandler handler = new AlterTrafficRuleHandler();
        handler.init(new AlterTrafficRuleStatement(Arrays.asList(trafficRuleSegment1, trafficRuleSegment2)), null);
        handler.execute();
        TrafficRuleConfiguration alteredConfig = contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(TrafficRule.class).getConfiguration();
        assertThat(alteredConfig.getTrafficStrategies().size(), is(2));
        assertThat(alteredConfig.getLoadBalancers().size(), is(2));
        assertThat(alteredConfig.getTrafficAlgorithms().size(), is(2));
        assertThat(alteredConfig.getTrafficStrategies().iterator().next().getName(), is("rule_name_1"));
        assertNotNull(alteredConfig.getTrafficAlgorithms().get("rule_name_1_distsql.fixture"));
        assertNotNull(alteredConfig.getLoadBalancers().get("rule_name_2_distsql.fixture"));
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        TrafficRule rule = mockTrafficRule();
        when(rule.getConfiguration()).thenReturn(createTrafficRuleConfiguration());
        ShardingSphereRuleMetaData globalRuleMetaData = new ShardingSphereRuleMetaData(new LinkedList<>(Collections.singleton(rule)));
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        ProxyContext.init(result);
        return result;
    }
    
    private TrafficRule mockTrafficRule() {
        TrafficRule result = mock(TrafficRule.class);
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
        result.getTrafficAlgorithms().put("algorithm_1", new ShardingSphereAlgorithmConfiguration("SQL_MATCH", createProperties()));
        result.getTrafficAlgorithms().put("algorithm_2", new ShardingSphereAlgorithmConfiguration("SQL_HINT", new Properties()));
        result.getLoadBalancers().put("load_balancer_1", new ShardingSphereAlgorithmConfiguration("RANDOM", new Properties()));
        result.getLoadBalancers().put("load_balancer_2", new ShardingSphereAlgorithmConfiguration("ROUND_ROBIN", new Properties()));
        return result;
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.put("sql", "select * from t_order");
        return result;
    }
}
