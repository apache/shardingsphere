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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.drop;

import org.apache.shardingsphere.distsql.parser.statement.ral.common.drop.DropTrafficRuleStatement;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.traffic.api.config.TrafficRuleConfiguration;
import org.apache.shardingsphere.traffic.api.config.TrafficStrategyConfiguration;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DropTrafficRuleHandlerTest {
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertExecuteWithEmptyRuleConfigurationAndNotExistRule() throws DistSQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getGlobalRuleMetaData().findRuleConfiguration(any())).thenReturn(new LinkedList<>());
        ProxyContext.getInstance().init(contextManager);
        new DropTrafficRuleHandler(getSQLStatement(Collections.singletonList("rule_name"), false)).execute();
    }
    
    @Test
    public void assertExecuteWithEmptyRuleConfigurationAndNotExistRuleAndIfExists() throws DistSQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getGlobalRuleMetaData().findRuleConfiguration(any())).thenReturn(new LinkedList<>());
        ProxyContext.getInstance().init(contextManager);
        new DropTrafficRuleHandler(getSQLStatement(Collections.singletonList("rule_name"), true)).execute();
    }
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertExecuteWithNotExistRule() throws DistSQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getGlobalRuleMetaData().findRuleConfiguration(any())).thenReturn(createTrafficRule());
        ProxyContext.getInstance().init(contextManager);
        new DropTrafficRuleHandler(getSQLStatement(Collections.singletonList("rule_name"), false)).execute();
    }
    
    @Test
    public void assertExecute() throws DistSQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getGlobalRuleMetaData().findRuleConfiguration(any())).thenReturn(createTrafficRule());
        ProxyContext.getInstance().init(contextManager);
        new DropTrafficRuleHandler(getSQLStatement(Collections.singletonList("rule_name_1"), false)).execute();
        Optional<TrafficRuleConfiguration> ruleConfiguration = contextManager.getMetaDataContexts().getGlobalRuleMetaData().findRuleConfiguration(TrafficRuleConfiguration.class).stream().findAny();
        assertTrue(ruleConfiguration.isPresent());
        TrafficRuleConfiguration configuration = ruleConfiguration.get();
        assertEquals(1, configuration.getTrafficStrategies().size());
        assertEquals(1, configuration.getLoadBalancers().size());
        assertEquals(1, configuration.getTrafficAlgorithms().size());
        assertThat(new ArrayList<>(configuration.getTrafficStrategies()).get(0).getName(), is("rule_name_2"));
        assertNotNull(configuration.getTrafficAlgorithms().get("algorithm_2"));
        assertNotNull(configuration.getLoadBalancers().get("load_balancer_2"));
    }
    
    @Test
    public void assertExecuteWithIfExists() throws DistSQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getGlobalRuleMetaData().findRuleConfiguration(any())).thenReturn(createTrafficRule());
        ProxyContext.getInstance().init(contextManager);
        new DropTrafficRuleHandler(getSQLStatement(Collections.singletonList("rule_name_1"), false)).execute();
        Optional<TrafficRuleConfiguration> ruleConfiguration = contextManager.getMetaDataContexts().getGlobalRuleMetaData().findRuleConfiguration(TrafficRuleConfiguration.class).stream().findAny();
        assertTrue(ruleConfiguration.isPresent());
        TrafficRuleConfiguration configuration = ruleConfiguration.get();
        assertEquals(1, configuration.getTrafficStrategies().size());
        assertEquals(1, configuration.getLoadBalancers().size());
        assertEquals(1, configuration.getTrafficAlgorithms().size());
        assertThat(new ArrayList<>(configuration.getTrafficStrategies()).get(0).getName(), is("rule_name_2"));
        assertNotNull(configuration.getTrafficAlgorithms().get("algorithm_2"));
        assertNotNull(configuration.getLoadBalancers().get("load_balancer_2"));
    }
    
    @Test
    public void assertExecuteWithNotExistRuleAndIfExists() throws DistSQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getGlobalRuleMetaData().findRuleConfiguration(any())).thenReturn(createTrafficRule());
        ProxyContext.getInstance().init(contextManager);
        new DropTrafficRuleHandler(getSQLStatement(Collections.singletonList("rule_name_3"), true)).execute();
        Optional<TrafficRuleConfiguration> ruleConfiguration = contextManager.getMetaDataContexts().getGlobalRuleMetaData().findRuleConfiguration(TrafficRuleConfiguration.class).stream().findAny();
        assertTrue(ruleConfiguration.isPresent());
        TrafficRuleConfiguration configuration = ruleConfiguration.get();
        assertEquals(2, configuration.getTrafficStrategies().size());
        assertEquals(2, configuration.getLoadBalancers().size());
        assertEquals(2, configuration.getTrafficAlgorithms().size());
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
    
    private DropTrafficRuleStatement getSQLStatement(final Collection<String> ruleNames, final boolean containsIfExistClause) {
        DropTrafficRuleStatement result = new DropTrafficRuleStatement();
        result.setRuleNames(ruleNames);
        result.setContainsIfExistClause(containsIfExistClause);
        return result;
    }
}
