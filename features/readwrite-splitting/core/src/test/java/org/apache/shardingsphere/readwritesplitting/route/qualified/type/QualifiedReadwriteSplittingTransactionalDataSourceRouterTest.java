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
import org.apache.shardingsphere.infra.session.connection.transaction.TransactionConnectionContext;
import org.apache.shardingsphere.readwritesplitting.config.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.transaction.TransactionalReadQueryStrategy;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingDataSourceGroupRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QualifiedReadwriteSplittingTransactionalDataSourceRouterTest {
    
    @Mock
    private HintValueContext hintValueContext;
    
    @Test
    void assertWriteRouteTransaction() {
        ConnectionContext connectionContext = mock(ConnectionContext.class);
        TransactionConnectionContext transactionConnectionContext = mock(TransactionConnectionContext.class);
        when(connectionContext.getTransactionContext()).thenReturn(transactionConnectionContext);
        when(connectionContext.getTransactionContext().isInTransaction()).thenReturn(Boolean.TRUE);
        assertTrue(new QualifiedReadwriteSplittingTransactionalDataSourceRouter(connectionContext).isQualified(null, null, hintValueContext));
        when(connectionContext.getTransactionContext().isInTransaction()).thenReturn(Boolean.FALSE);
        assertFalse(new QualifiedReadwriteSplittingTransactionalDataSourceRouter(connectionContext).isQualified(null, null, hintValueContext));
    }
    
    @Test
    void assertRoute() {
        ReadwriteSplittingDataSourceGroupRuleConfiguration dataSourceGroupConfig = new ReadwriteSplittingDataSourceGroupRuleConfiguration(
                "test_config", "write_ds", Arrays.asList("read_ds_0", "read_ds_1"), null);
        ReadwriteSplittingDataSourceGroupRule rule;
        rule = new ReadwriteSplittingDataSourceGroupRule(dataSourceGroupConfig, TransactionalReadQueryStrategy.PRIMARY, null);
        assertThat(new QualifiedReadwriteSplittingTransactionalDataSourceRouter(new ConnectionContext(Collections::emptySet)).route(rule), is("write_ds"));
        rule = new ReadwriteSplittingDataSourceGroupRule(dataSourceGroupConfig, TransactionalReadQueryStrategy.FIXED, new RoundRobinLoadBalanceAlgorithm());
        assertThat(new QualifiedReadwriteSplittingTransactionalDataSourceRouter(new ConnectionContext(Collections::emptySet)).route(rule), is("read_ds_0"));
        rule = new ReadwriteSplittingDataSourceGroupRule(dataSourceGroupConfig, TransactionalReadQueryStrategy.DYNAMIC, new RoundRobinLoadBalanceAlgorithm());
        assertThat(new QualifiedReadwriteSplittingTransactionalDataSourceRouter(new ConnectionContext(Collections::emptySet)).route(rule), is("read_ds_0"));
    }
}
