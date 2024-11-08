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

package org.apache.shardingsphere.readwritesplitting.route.qualified.type;

import org.apache.shardingsphere.infra.algorithm.loadbalancer.round.robin.RoundRobinLoadBalanceAlgorithm;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.readwritesplitting.config.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingDataSourceGroupRule;
import org.apache.shardingsphere.readwritesplitting.transaction.TransactionalReadQueryStrategy;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;

class QualifiedReadwriteSplittingTransactionalDataSourceRouterTest {
    
    @Test
    void assertIsQualified() {
        assertFalse(new QualifiedReadwriteSplittingTransactionalDataSourceRouter(new ConnectionContext(Collections::emptySet)).isQualified(null, null, mock(HintValueContext.class)));
    }
    
    @Test
    void assertRouteWithFixedAndWithoutReadWriteSplitReplicaRoute() {
        ReadwriteSplittingDataSourceGroupRuleConfiguration dataSourceGroupConfig = createDataSourceGroupRuleConfiguration();
        ReadwriteSplittingDataSourceGroupRule rule = new ReadwriteSplittingDataSourceGroupRule(dataSourceGroupConfig, TransactionalReadQueryStrategy.FIXED, new RoundRobinLoadBalanceAlgorithm());
        assertThat(new QualifiedReadwriteSplittingTransactionalDataSourceRouter(new ConnectionContext(Collections::emptySet)).route(rule), is("read_ds0"));
    }
    
    @Test
    void assertRouteWithFixedAndWithReadWriteSplitReplicaRoute() {
        ReadwriteSplittingDataSourceGroupRuleConfiguration dataSourceGroupConfig = createDataSourceGroupRuleConfiguration();
        ReadwriteSplittingDataSourceGroupRule rule = new ReadwriteSplittingDataSourceGroupRule(dataSourceGroupConfig, TransactionalReadQueryStrategy.FIXED, new RoundRobinLoadBalanceAlgorithm());
        ConnectionContext connectionContext = new ConnectionContext(Collections::emptySet);
        connectionContext.getTransactionContext().setReadWriteSplitReplicaRoute("read_ds1");
        assertThat(new QualifiedReadwriteSplittingTransactionalDataSourceRouter(connectionContext).route(rule), is("read_ds1"));
    }
    
    @Test
    void assertRouteWithDynamic() {
        ReadwriteSplittingDataSourceGroupRuleConfiguration dataSourceGroupConfig = createDataSourceGroupRuleConfiguration();
        ReadwriteSplittingDataSourceGroupRule rule = new ReadwriteSplittingDataSourceGroupRule(dataSourceGroupConfig, TransactionalReadQueryStrategy.DYNAMIC, new RoundRobinLoadBalanceAlgorithm());
        assertThat(new QualifiedReadwriteSplittingTransactionalDataSourceRouter(new ConnectionContext(Collections::emptySet)).route(rule), is("read_ds0"));
    }
    
    @Test
    void assertRouteWithPrimary() {
        ReadwriteSplittingDataSourceGroupRule rule = new ReadwriteSplittingDataSourceGroupRule(createDataSourceGroupRuleConfiguration(), TransactionalReadQueryStrategy.PRIMARY, null);
        assertThat(new QualifiedReadwriteSplittingTransactionalDataSourceRouter(new ConnectionContext(Collections::emptySet)).route(rule), is("write_ds"));
    }
    
    private ReadwriteSplittingDataSourceGroupRuleConfiguration createDataSourceGroupRuleConfiguration() {
        return new ReadwriteSplittingDataSourceGroupRuleConfiguration("foo_group", "write_ds", Arrays.asList("read_ds0", "read_ds1"), null);
    }
}
