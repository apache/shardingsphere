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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.updatable;

import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.distsql.parser.segment.TrafficRuleSegment;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.updatable.CreateTrafficRuleStatement;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.apache.shardingsphere.traffic.api.config.TrafficRuleConfiguration;
import org.apache.shardingsphere.traffic.api.config.TrafficStrategyConfiguration;
import org.apache.shardingsphere.traffic.rule.TrafficRule;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class CreateTrafficRuleHandlerTest extends ProxyContextRestorer {
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertCheckWithInvalidAlgorithmType() throws SQLException {
        ContextManager contextManager = mockContextManager();
        ProxyContext.init(contextManager);
        TrafficRuleSegment trafficRuleSegment = new TrafficRuleSegment("input_rule_name", Arrays.asList("olap", "order_by"),
                new AlgorithmSegment("invalid", new Properties()), new AlgorithmSegment("invalid", new Properties()));
        CreateTrafficRuleHandler handler = new CreateTrafficRuleHandler();
        handler.init(new CreateTrafficRuleStatement(Collections.singletonList(trafficRuleSegment)), null);
        handler.execute();
    }
    
    @Test(expected = DuplicateRuleException.class)
    public void assertCheckWithDuplicatedRuleName() throws SQLException {
        ContextManager contextManager = mockContextManager();
        ProxyContext.init(contextManager);
        TrafficRuleSegment trafficRuleSegment = new TrafficRuleSegment("rule_name_1", Arrays.asList("olap", "order_by"),
                new AlgorithmSegment("DISTSQL.FIXTURE", new Properties()), new AlgorithmSegment("DISTSQL.FIXTURE", new Properties()));
        CreateTrafficRuleHandler handler = new CreateTrafficRuleHandler();
        handler.init(new CreateTrafficRuleStatement(Collections.singletonList(trafficRuleSegment)), null);
        handler.execute();
    }
    
    @Test
    public void assertCheckSuccess() throws SQLException {
        ContextManager contextManager = mockContextManager();
        ProxyContext.init(contextManager);
        TrafficRuleSegment trafficRuleSegment1 = new TrafficRuleSegment("rule_name_3", Arrays.asList("olap", "order_by"),
                new AlgorithmSegment("DISTSQL.FIXTURE", new Properties()), new AlgorithmSegment("DISTSQL.FIXTURE", new Properties()));
        TrafficRuleSegment trafficRuleSegment2 = new TrafficRuleSegment("rule_name_4", Collections.emptyList(),
                new AlgorithmSegment("DISTSQL.FIXTURE", new Properties()), null);
        CreateTrafficRuleHandler handler = new CreateTrafficRuleHandler();
        handler.init(new CreateTrafficRuleStatement(Arrays.asList(trafficRuleSegment1, trafficRuleSegment2)), null);
        handler.execute();
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        TrafficRule rule = mock(TrafficRule.class);
        when(rule.getConfiguration()).thenReturn(createTrafficRuleConfiguration());
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().findSingleRule(TrafficRule.class)).thenReturn(Optional.of(rule));
        return result;
    }
    
    private TrafficRuleConfiguration createTrafficRuleConfiguration() {
        TrafficRuleConfiguration result = new TrafficRuleConfiguration();
        result.getTrafficStrategies().add(new TrafficStrategyConfiguration("rule_name_1", Arrays.asList("olap", "order_by"), "algorithm_1", "load_balancer_1"));
        result.getTrafficStrategies().add(new TrafficStrategyConfiguration("rule_name_2", Collections.singletonList("oltp"), "algorithm_2", "load_balancer_2"));
        result.getTrafficAlgorithms().put("algorithm_1", new ShardingSphereAlgorithmConfiguration("SQL_MATCH", createProperties()));
        result.getTrafficAlgorithms().put("algorithm_2", new ShardingSphereAlgorithmConfiguration("SQL_HINT", new Properties()));
        result.getLoadBalancers().put("load_balancer_1", new ShardingSphereAlgorithmConfiguration("RANDOM", new Properties()));
        result.getLoadBalancers().put("load_balancer_2", new ShardingSphereAlgorithmConfiguration("ROBIN", new Properties()));
        return result;
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.put("sql", "select * from t_order");
        return result;
    }
}
