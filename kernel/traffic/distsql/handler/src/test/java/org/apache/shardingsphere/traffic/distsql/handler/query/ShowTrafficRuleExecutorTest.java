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

package org.apache.shardingsphere.traffic.distsql.handler.query;

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.apache.shardingsphere.traffic.api.config.TrafficRuleConfiguration;
import org.apache.shardingsphere.traffic.api.config.TrafficStrategyConfiguration;
import org.apache.shardingsphere.traffic.distsql.statement.queryable.ShowTrafficRulesStatement;
import org.apache.shardingsphere.traffic.rule.TrafficRule;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowTrafficRuleExecutorTest {
    
    @Test
    void assertExecute() {
        ShowTrafficRuleExecutor executor = new ShowTrafficRuleExecutor();
        TrafficRule rule = mock(TrafficRule.class);
        when(rule.getConfiguration()).thenReturn(createTrafficRuleConfiguration());
        executor.setRule(rule);
        Collection<LocalDataQueryResultRow> actual = executor.getRows(mock(ShowTrafficRulesStatement.class), mock(ContextManager.class));
        assertThat(actual.size(), is(2));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        LocalDataQueryResultRow row = iterator.next();
        assertThat(row.getCell(1), is("rule_name_1"));
        assertThat(row.getCell(2), is("olap,order_by"));
        assertThat(row.getCell(3), is("SQL_MATCH"));
        assertThat(row.getCell(4), is("{\"sql\":\"select * from t_order\"}"));
        assertThat(row.getCell(5), is("RANDOM"));
        assertThat(row.getCell(6), is(""));
        row = iterator.next();
        assertThat(row.getCell(1), is("rule_name_2"));
        assertThat(row.getCell(2), is("oltp"));
        assertThat(row.getCell(3), is("SQL_HINT"));
        assertThat(row.getCell(4), is(""));
        assertThat(row.getCell(5), is("ROBIN"));
        assertThat(row.getCell(6), is(""));
    }
    
    private TrafficRuleConfiguration createTrafficRuleConfiguration() {
        TrafficRuleConfiguration result = new TrafficRuleConfiguration();
        result.getTrafficStrategies().add(new TrafficStrategyConfiguration("rule_name_1", Arrays.asList("olap", "order_by"), "algorithm_1", "load_balancer_1"));
        result.getTrafficStrategies().add(new TrafficStrategyConfiguration("rule_name_2", Collections.singletonList("oltp"), "algorithm_2", "load_balancer_2"));
        result.getLoadBalancers().put("load_balancer_1", new AlgorithmConfiguration("RANDOM", new Properties()));
        result.getLoadBalancers().put("load_balancer_2", new AlgorithmConfiguration("ROBIN", new Properties()));
        result.getTrafficAlgorithms().put("algorithm_1", new AlgorithmConfiguration("SQL_MATCH", PropertiesBuilder.build(new Property("sql", "select * from t_order"))));
        result.getTrafficAlgorithms().put("algorithm_2", new AlgorithmConfiguration("SQL_HINT", new Properties()));
        return result;
    }
}
