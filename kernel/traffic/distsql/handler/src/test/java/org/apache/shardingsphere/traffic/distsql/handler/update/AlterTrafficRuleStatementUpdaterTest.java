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

import org.apache.shardingsphere.distsql.handler.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.spi.exception.ServiceProviderNotFoundException;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.apache.shardingsphere.traffic.api.config.TrafficRuleConfiguration;
import org.apache.shardingsphere.traffic.api.config.TrafficStrategyConfiguration;
import org.apache.shardingsphere.traffic.distsql.parser.segment.TrafficRuleSegment;
import org.apache.shardingsphere.traffic.distsql.parser.statement.updatable.AlterTrafficRuleStatement;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AlterTrafficRuleStatementUpdaterTest {
    
    @Test
    void assertExecuteWithNotExistRuleName() {
        TrafficRuleSegment trafficRuleSegment = new TrafficRuleSegment(
                "rule_name_3", Arrays.asList("olap", "order_by"), new AlgorithmSegment("DISTSQL.FIXTURE", new Properties()), new AlgorithmSegment("DISTSQL.FIXTURE", new Properties()));
        AlterTrafficRuleStatementUpdater updater = new AlterTrafficRuleStatementUpdater();
        assertThrows(MissingRequiredRuleException.class,
                () -> updater.checkSQLStatement(createTrafficRuleConfiguration(), new AlterTrafficRuleStatement(Collections.singleton(trafficRuleSegment))));
    }
    
    @Test
    void assertExecuteWithInvalidAlgorithmType() {
        TrafficRuleSegment trafficRuleSegment = new TrafficRuleSegment(
                "rule_name_1", Arrays.asList("olap", "order_by"), new AlgorithmSegment("invalid", new Properties()), new AlgorithmSegment("invalid", new Properties()));
        AlterTrafficRuleStatementUpdater updater = new AlterTrafficRuleStatementUpdater();
        assertThrows(ServiceProviderNotFoundException.class,
                () -> updater.checkSQLStatement(createTrafficRuleConfiguration(), new AlterTrafficRuleStatement(Collections.singleton(trafficRuleSegment))));
    }
    
    @Test
    void assertExecuteWithLoadBalancerCannotBeNull() {
        TrafficRuleSegment trafficRuleSegment = new TrafficRuleSegment("rule_name_1", Arrays.asList("olap", "order_by"),
                new AlgorithmSegment("DISTSQL.FIXTURE", new Properties()), null);
        AlterTrafficRuleStatementUpdater updater = new AlterTrafficRuleStatementUpdater();
        TrafficRuleConfiguration actual = updater.buildAlteredRuleConfiguration(createTrafficRuleConfiguration(), new AlterTrafficRuleStatement(Collections.singleton(trafficRuleSegment)));
        assertThat(actual.getTrafficStrategies().size(), is(2));
        assertThat(actual.getTrafficAlgorithms().size(), is(2));
        assertThat(actual.getLoadBalancers().size(), is(1));
    }
    
    @Test
    void assertExecute() {
        TrafficRuleSegment trafficRuleSegment1 = new TrafficRuleSegment(
                "rule_name_1", Arrays.asList("olap", "order_by"), new AlgorithmSegment("DISTSQL.FIXTURE", new Properties()), new AlgorithmSegment("DISTSQL.FIXTURE", new Properties()));
        TrafficRuleSegment trafficRuleSegment2 = new TrafficRuleSegment(
                "rule_name_2", Collections.emptyList(), new AlgorithmSegment("DISTSQL.FIXTURE", new Properties()), new AlgorithmSegment("DISTSQL.FIXTURE", new Properties()));
        AlterTrafficRuleStatementUpdater updater = new AlterTrafficRuleStatementUpdater();
        TrafficRuleConfiguration actual =
                updater.buildAlteredRuleConfiguration(createTrafficRuleConfiguration(), new AlterTrafficRuleStatement(Arrays.asList(trafficRuleSegment1, trafficRuleSegment2)));
        assertThat(actual.getTrafficStrategies().size(), is(2));
        assertThat(actual.getTrafficAlgorithms().size(), is(2));
        assertThat(actual.getLoadBalancers().size(), is(2));
        assertThat(actual.getTrafficStrategies().iterator().next().getName(), is("rule_name_1"));
        assertNotNull(actual.getTrafficAlgorithms().get("rule_name_1_distsql.fixture"));
        assertNotNull(actual.getLoadBalancers().get("rule_name_2_distsql.fixture"));
    }
    
    private TrafficRuleConfiguration createTrafficRuleConfiguration() {
        TrafficRuleConfiguration result = new TrafficRuleConfiguration();
        result.getTrafficStrategies().add(new TrafficStrategyConfiguration("rule_name_1", Arrays.asList("olap", "order_by"), "algorithm_1", "load_balancer_1"));
        result.getTrafficStrategies().add(new TrafficStrategyConfiguration("rule_name_2", Collections.singletonList("oltp"), "algorithm_2", "load_balancer_2"));
        result.getTrafficAlgorithms().put("algorithm_1", new AlgorithmConfiguration("SQL_MATCH", PropertiesBuilder.build(new Property("sql", "select * from t_order"))));
        result.getTrafficAlgorithms().put("algorithm_2", new AlgorithmConfiguration("SQL_HINT", new Properties()));
        result.getLoadBalancers().put("load_balancer_1", new AlgorithmConfiguration("RANDOM", new Properties()));
        result.getLoadBalancers().put("load_balancer_2", new AlgorithmConfiguration("ROUND_ROBIN", new Properties()));
        return result;
    }
}
