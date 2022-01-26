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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.show;

import org.apache.shardingsphere.distsql.parser.statement.ral.common.show.ShowTrafficRulesStatement;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.show.executor.ShowTrafficRulesExecutor;
import org.apache.shardingsphere.traffic.api.config.TrafficRuleConfiguration;
import org.apache.shardingsphere.traffic.api.config.TrafficStrategyConfiguration;
import org.junit.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ShowTrafficRulesExecutorTest {
    
    @Test
    public void assertExecutor() throws SQLException {
        ShowTrafficRulesStatement showTrafficRuleStatement = new ShowTrafficRulesStatement();
        showTrafficRuleStatement.setRuleName("rule_name_1");
        ShowTrafficRulesExecutor executor = new ShowTrafficRulesExecutor(showTrafficRuleStatement);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getGlobalRuleMetaData().findRuleConfiguration(any())).thenReturn(createTrafficRule());
        ProxyContext.getInstance().init(contextManager);
        executor.execute();
        executor.next();
        QueryResponseRow queryResponseRow = executor.getQueryResponseRow();
        ArrayList<Object> data = new ArrayList<>(queryResponseRow.getData());
        assertThat(data.size(), is(6));
        assertThat(data.get(0), is("rule_name_1"));
        assertThat(data.get(1), is("olap,order_by"));
        assertThat(data.get(2), is("SQL_MATCH"));
        assertThat(data.get(3), is("sql=select * from t_order"));
        assertThat(data.get(4), is("RANDOM"));
        assertThat(data.get(5), is(""));
    }
    
    private Collection<RuleConfiguration> createTrafficRule() {
        TrafficRuleConfiguration trafficRuleConfiguration = new TrafficRuleConfiguration();
        trafficRuleConfiguration.getTrafficStrategies().add(new TrafficStrategyConfiguration("rule_name_1", Arrays.asList("olap", "order_by"), "algorithm_1", "load_balancer_1"));
        trafficRuleConfiguration.getTrafficStrategies().add(new TrafficStrategyConfiguration("rule_name_2", Collections.singletonList("oltp"), "algorithm_2", "load_balancer_2"));
        trafficRuleConfiguration.getLoadBalancers().put("load_balancer_1", new ShardingSphereAlgorithmConfiguration("RANDOM", new Properties()));
        trafficRuleConfiguration.getLoadBalancers().put("load_balancer_2", new ShardingSphereAlgorithmConfiguration("ROBIN", new Properties()));
        Properties algorithmProperties = new Properties();
        algorithmProperties.put("sql", "select * from t_order");
        trafficRuleConfiguration.getTrafficAlgorithms().put("algorithm_1", new ShardingSphereAlgorithmConfiguration("SQL_MATCH", algorithmProperties));
        trafficRuleConfiguration.getTrafficAlgorithms().put("algorithm_2", new ShardingSphereAlgorithmConfiguration("SQL_HINT", new Properties()));
        return Collections.singletonList(trafficRuleConfiguration);
    }
}
