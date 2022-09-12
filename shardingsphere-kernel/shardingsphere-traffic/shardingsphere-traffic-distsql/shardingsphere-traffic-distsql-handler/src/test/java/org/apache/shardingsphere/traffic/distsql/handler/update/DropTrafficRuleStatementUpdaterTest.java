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
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredRuleMissedException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.traffic.api.config.TrafficRuleConfiguration;
import org.apache.shardingsphere.traffic.api.config.TrafficStrategyConfiguration;
import org.apache.shardingsphere.traffic.distsql.parser.statement.updatable.DropTrafficRuleStatement;
import org.apache.shardingsphere.traffic.rule.TrafficRule;
import org.junit.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DropTrafficRuleStatementUpdaterTest {
    
    @Test(expected = RequiredRuleMissedException.class)
    public void assertExecuteForNotExistedRuleWithoutIfExists() throws SQLException {
        ShardingSphereMetaData metaData = createMetaData();
        DropTrafficRuleStatementUpdater updater = new DropTrafficRuleStatementUpdater();
        updater.executeUpdate(metaData, new DropTrafficRuleStatement(false, Collections.singleton("not_existed_rule")));
    }
    
    @Test
    public void assertExecuteForNotExistedRuleWithIfExists() throws SQLException {
        ShardingSphereMetaData metaData = createMetaData();
        DropTrafficRuleStatementUpdater updater = new DropTrafficRuleStatementUpdater();
        updater.executeUpdate(metaData, new DropTrafficRuleStatement(true, Collections.singleton("rule_name_3")));
        TrafficRuleConfiguration updatedConfig = metaData.getGlobalRuleMetaData().getSingleRule(TrafficRule.class).getConfiguration();
        assertThat(updatedConfig.getTrafficStrategies().size(), is(2));
        assertThat(updatedConfig.getLoadBalancers().size(), is(2));
        assertThat(updatedConfig.getTrafficAlgorithms().size(), is(2));
    }
    
    @Test
    public void assertExecute() throws SQLException {
        ShardingSphereMetaData metaData = createMetaData();
        DropTrafficRuleStatementUpdater updater = new DropTrafficRuleStatementUpdater();
        updater.executeUpdate(metaData, new DropTrafficRuleStatement(false, Collections.singleton("rule_name_1")));
        TrafficRuleConfiguration updatedConfig = metaData.getGlobalRuleMetaData().getSingleRule(TrafficRule.class).getConfiguration();
        assertThat(updatedConfig.getTrafficStrategies().size(), is(1));
        assertThat(updatedConfig.getLoadBalancers().size(), is(1));
        assertThat(updatedConfig.getTrafficAlgorithms().size(), is(1));
        assertThat(new ArrayList<>(updatedConfig.getTrafficStrategies()).get(0).getName(), is("rule_name_2"));
        assertNotNull(updatedConfig.getTrafficAlgorithms().get("algorithm_2"));
        assertNotNull(updatedConfig.getLoadBalancers().get("load_balancer_2"));
    }
    
    private ShardingSphereMetaData createMetaData() {
        TrafficRule trafficRule = mock(TrafficRule.class);
        when(trafficRule.getConfiguration()).thenReturn(createTrafficRuleConfiguration());
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(metaData.getGlobalRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(new LinkedList<>(Collections.singleton(trafficRule))));
        return metaData;
    }
    
    private TrafficRuleConfiguration createTrafficRuleConfiguration() {
        TrafficRuleConfiguration result = new TrafficRuleConfiguration();
        result.getTrafficStrategies().add(new TrafficStrategyConfiguration("rule_name_1", Arrays.asList("olap", "order_by"), "algorithm_1", "load_balancer_1"));
        result.getTrafficStrategies().add(new TrafficStrategyConfiguration("rule_name_2", Collections.singleton("oltp"), "algorithm_2", "load_balancer_2"));
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
