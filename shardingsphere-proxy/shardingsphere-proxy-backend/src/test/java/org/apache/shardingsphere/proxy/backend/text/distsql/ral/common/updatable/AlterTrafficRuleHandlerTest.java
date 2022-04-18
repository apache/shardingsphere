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
import org.apache.shardingsphere.distsql.parser.statement.ral.common.updatable.AlterTrafficRuleStatement;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.rule.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.traffic.api.config.TrafficRuleConfiguration;
import org.apache.shardingsphere.traffic.api.config.TrafficStrategyConfiguration;
import org.apache.shardingsphere.traffic.spi.TrafficAlgorithm;
import org.apache.shardingsphere.traffic.spi.TrafficLoadBalanceAlgorithm;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class AlterTrafficRuleHandlerTest {
    
    static {
        ShardingSphereServiceLoader.register(TrafficAlgorithm.class);
        ShardingSphereServiceLoader.register(TrafficLoadBalanceAlgorithm.class);
    }
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertCheckWithEmptyRule() throws SQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getGlobalRuleMetaData().findRuleConfiguration(any())).thenReturn(new LinkedList<>());
        ProxyContext.getInstance().init(contextManager);
        TrafficRuleSegment trafficRuleSegment = new TrafficRuleSegment("input_rule_name", Arrays.asList("olap", "order_by"),
                new AlgorithmSegment("invalid", new Properties()), new AlgorithmSegment("invalid", new Properties()));
        new AlterTrafficRuleHandler().initStatement(getSQLStatement(trafficRuleSegment)).execute();
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertCheckWithInvalidAlgorithmType() throws SQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getGlobalRuleMetaData().findRuleConfiguration(any())).thenReturn(createTrafficRule());
        ProxyContext.getInstance().init(contextManager);
        TrafficRuleSegment trafficRuleSegment = new TrafficRuleSegment("rule_name_1", Arrays.asList("olap", "order_by"), 
                new AlgorithmSegment("invalid", new Properties()), new AlgorithmSegment("invalid", new Properties()));
        new AlterTrafficRuleHandler().initStatement(getSQLStatement(trafficRuleSegment)).execute();
    }
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertCheckWithNotExistRuleName() throws SQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getGlobalRuleMetaData().findRuleConfiguration(any())).thenReturn(createTrafficRule());
        ProxyContext.getInstance().init(contextManager);
        TrafficRuleSegment trafficRuleSegment = new TrafficRuleSegment("rule_name_3", Arrays.asList("olap", "order_by"), 
                new AlgorithmSegment("TEST", new Properties()), new AlgorithmSegment("TEST", new Properties()));
        new AlterTrafficRuleHandler().initStatement(getSQLStatement(trafficRuleSegment)).execute();
    }
    
    @Test
    public void assertCheckSuccess() throws SQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getGlobalRuleMetaData().findRuleConfiguration(any())).thenReturn(createTrafficRule());
        ProxyContext.getInstance().init(contextManager);
        TrafficRuleSegment trafficRuleSegment1 = new TrafficRuleSegment("rule_name_1", Arrays.asList("olap", "order_by"),
                new AlgorithmSegment("TEST", new Properties()), new AlgorithmSegment("TEST", new Properties()));
        TrafficRuleSegment trafficRuleSegment2 = new TrafficRuleSegment("rule_name_2", Collections.emptyList(),
                new AlgorithmSegment("TEST", new Properties()), null);
        new AlterTrafficRuleHandler().initStatement(getSQLStatement(trafficRuleSegment1, trafficRuleSegment2)).execute();
    }
    
    private Collection<RuleConfiguration> createTrafficRule() {
        TrafficRuleConfiguration trafficRuleConfiguration = new TrafficRuleConfiguration();
        trafficRuleConfiguration.getTrafficStrategies().add(new TrafficStrategyConfiguration("rule_name_1", Arrays.asList("olap", "order_by"), "algorithm_1", "load_balancer_1"));
        trafficRuleConfiguration.getTrafficStrategies().add(new TrafficStrategyConfiguration("rule_name_2", Collections.singletonList("oltp"), "algorithm_2", "load_balancer_2"));
        Properties algorithmProperties = new Properties();
        algorithmProperties.put("sql", "select * from t_order");
        trafficRuleConfiguration.getTrafficAlgorithms().put("algorithm_1", new ShardingSphereAlgorithmConfiguration("SQL_MATCH", algorithmProperties));
        trafficRuleConfiguration.getTrafficAlgorithms().put("algorithm_2", new ShardingSphereAlgorithmConfiguration("SQL_HINT", new Properties()));
        trafficRuleConfiguration.getLoadBalancers().put("load_balancer_1", new ShardingSphereAlgorithmConfiguration("RANDOM", new Properties()));
        trafficRuleConfiguration.getLoadBalancers().put("load_balancer_2", new ShardingSphereAlgorithmConfiguration("ROBIN", new Properties()));
        return Collections.singletonList(trafficRuleConfiguration);
    }
    
    private AlterTrafficRuleStatement getSQLStatement(final TrafficRuleSegment... segments) {
        return new AlterTrafficRuleStatement(Arrays.asList(segments));
    }
}
