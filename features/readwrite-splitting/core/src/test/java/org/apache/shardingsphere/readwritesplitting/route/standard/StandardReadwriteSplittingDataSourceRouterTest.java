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

package org.apache.shardingsphere.readwritesplitting.route.standard;

import org.apache.shardingsphere.infra.algorithm.loadbalancer.round.robin.RoundRobinLoadBalanceAlgorithm;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.transaction.TransactionalReadQueryStrategy;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingDataSourceRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class StandardReadwriteSplittingDataSourceRouterTest {
    
    private ReadwriteSplittingDataSourceRule rule;
    
    @BeforeEach
    void setUp() {
        rule = new ReadwriteSplittingDataSourceRule(
                new ReadwriteSplittingDataSourceRuleConfiguration("test_config", "write_ds", Arrays.asList("read_ds_0", "read_ds_1"), null),
                TransactionalReadQueryStrategy.DYNAMIC, new RoundRobinLoadBalanceAlgorithm());
    }
    
    @Test
    void assertRoute() {
        assertThat(new StandardReadwriteSplittingDataSourceRouter().route(rule), is("read_ds_0"));
    }
    
    @Test
    void assertRouteWithFilter() {
        rule.disableDataSource("read_ds_0");
        assertThat(new StandardReadwriteSplittingDataSourceRouter().route(rule), is("read_ds_1"));
    }
}
