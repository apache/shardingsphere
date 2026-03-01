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
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.connection.transaction.TransactionManager;
import org.apache.shardingsphere.readwritesplitting.config.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingDataSourceGroupRule;
import org.apache.shardingsphere.readwritesplitting.transaction.TransactionalReadQueryStrategy;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class QualifiedReadwriteSplittingTransactionalDataSourceRouterTest {
    
    @Test
    void assertIsQualified() {
        assertFalse(new QualifiedReadwriteSplittingTransactionalDataSourceRouter(new ConnectionContext(Collections::emptySet)).isQualified(null, null, null));
    }
    
    @Test
    void assertIsQualifiedInTransaction() {
        ConnectionContext connectionContext = new ConnectionContext(Collections::emptySet);
        connectionContext.getTransactionContext().beginTransaction("LOCAL", mock(TransactionManager.class));
        assertTrue(new QualifiedReadwriteSplittingTransactionalDataSourceRouter(connectionContext).isQualified(null, null, null));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("routeArguments")
    void assertRoute(final String name, final TransactionalReadQueryStrategy transactionalReadQueryStrategy, final String readWriteSplitReplicaRoute, final String expectedDataSourceName) {
        ConnectionContext connectionContext = new ConnectionContext(Collections::emptySet);
        if (null != readWriteSplitReplicaRoute) {
            connectionContext.getTransactionContext().setReadWriteSplitReplicaRoute(readWriteSplitReplicaRoute);
        }
        ReadwriteSplittingDataSourceGroupRule rule = new ReadwriteSplittingDataSourceGroupRule(
                new ReadwriteSplittingDataSourceGroupRuleConfiguration("foo_group", "write_ds", Arrays.asList("read_ds0", "read_ds1"), null), transactionalReadQueryStrategy,
                TransactionalReadQueryStrategy.PRIMARY == transactionalReadQueryStrategy ? null : new RoundRobinLoadBalanceAlgorithm());
        String actualDataSourceName = new QualifiedReadwriteSplittingTransactionalDataSourceRouter(connectionContext).route(rule);
        assertThat(actualDataSourceName, is(expectedDataSourceName));
    }
    
    private static Stream<Arguments> routeArguments() {
        return Stream.of(
                Arguments.of("fixed without route", TransactionalReadQueryStrategy.FIXED, null, "read_ds0"),
                Arguments.of("fixed with route", TransactionalReadQueryStrategy.FIXED, "read_ds1", "read_ds1"),
                Arguments.of("dynamic", TransactionalReadQueryStrategy.DYNAMIC, null, "read_ds0"),
                Arguments.of("primary", TransactionalReadQueryStrategy.PRIMARY, null, "write_ds"));
    }
}
