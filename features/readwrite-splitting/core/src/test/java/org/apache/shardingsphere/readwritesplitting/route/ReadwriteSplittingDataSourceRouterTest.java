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

package org.apache.shardingsphere.readwritesplitting.route;

import org.apache.shardingsphere.infra.context.transaction.TransactionConnectionContext;
import org.apache.shardingsphere.readwritesplitting.algorithm.loadbalance.RandomReadQueryLoadBalanceAlgorithm;
import org.apache.shardingsphere.readwritesplitting.api.transaction.TransactionalReadQueryStrategy;
import org.apache.shardingsphere.readwritesplitting.strategy.type.StaticReadwriteSplittingStrategy;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SimpleSQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.context.ConnectionContext;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingDataSourceRule;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

public class ReadwriteSplittingDataSourceRouterTest {
    
    private ReadwriteSplittingDataSourceRouter router;
    
    @Mock
    private ReadwriteSplittingDataSourceRule rule;
    
    @Mock
    private ConnectionContext connectionContext;
    
    @Mock
    private SQLStatementContext sqlStatementContext;
    
    @Mock
    private RandomReadQueryLoadBalanceAlgorithm randomReadQueryLoadBalanceAlgorithm;
    
    @Mock
    private TransactionConnectionContext transactionConnectionContext;
    
    @Mock
    private StaticReadwriteSplittingStrategy staticReadwriteSplittingStrategy;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        router = new ReadwriteSplittingDataSourceRouter(rule, connectionContext);
    }
    
    @Test
    void testIsPrimaryRoute() {
        final String expectedResult = "primaryRoute";
        SimpleSQLStatement simpleSQLStatement = new SimpleSQLStatement();
        setupCommonMocks(expectedResult, Collections.emptyList(), false, null);
        when(sqlStatementContext.getSqlStatement()).thenReturn(simpleSQLStatement);
        String actualResult = router.route(sqlStatementContext);
        assertEquals(expectedResult, actualResult);
    }
    
    @Test
    void testDefaultRouteWithLoadBalancer() {
        final String expectedResult = "routeWithLoadBalancer";
        setupCommonMocks(expectedResult, Collections.emptyList(), false, expectedResult);
        String actualResult = router.route(sqlStatementContext);
        assertEquals(expectedResult, actualResult);
    }
    
    @Test
    void testPrimaryRouteInTransaction() {
        final String expectedResult = "routeInTransaction";
        setupCommonMocks(expectedResult, Collections.emptyList(), true, null);
        when(rule.getTransactionalReadQueryStrategy()).thenReturn(TransactionalReadQueryStrategy.PRIMARY);
        String actualResult = router.route(sqlStatementContext);
        assertEquals(expectedResult, actualResult);
    }
    
    @Test
    void testDynamicRouteInTransaction() {
        final String expectedResult = "routeInTransaction";
        setupCommonMocks(expectedResult, Collections.emptyList(), true, expectedResult);
        when(rule.getTransactionalReadQueryStrategy()).thenReturn(TransactionalReadQueryStrategy.DYNAMIC);
        String actualResult = router.route(sqlStatementContext);
        assertEquals(expectedResult, actualResult);
    }
    
    @Test
    void testFixedRouteInTransactionWhenReplicaRouteValueExists() {
        final String expectedResult = "routeInTransaction";
        setupCommonMocks(expectedResult, Collections.emptyList(), true, null);
        when(rule.getTransactionalReadQueryStrategy()).thenReturn(TransactionalReadQueryStrategy.FIXED);
        when(transactionConnectionContext.getReadWriteSplitReplicaRoute()).thenReturn(expectedResult);
        String actualResult = router.route(sqlStatementContext);
        assertEquals(expectedResult, actualResult);
    }
    
    @Test
    void testFixedRouteInTransactionWhenReplicaRouteIsNull() {
        final String expectedResult = "routeInTransaction";
        setupCommonMocks(expectedResult, Collections.emptyList(), true, expectedResult);
        when(rule.getTransactionalReadQueryStrategy()).thenReturn(TransactionalReadQueryStrategy.FIXED);
        doNothing().when(transactionConnectionContext).setReadWriteSplitReplicaRoute(expectedResult);
        assertNull(router.route(sqlStatementContext));
    }
    
    void setupCommonMocks(
                          final String expectedResult,
                          final List<String> emptyList,
                          final boolean inTransaction,
                          final String writeDataSourceName) {
        when(sqlStatementContext.getSqlStatement()).thenReturn(new MySQLSelectStatement());
        when(connectionContext.getTransactionContext()).thenReturn(transactionConnectionContext);
        when(transactionConnectionContext.isInTransaction()).thenReturn(inTransaction);
        when(rule.getLoadBalancer()).thenReturn(randomReadQueryLoadBalanceAlgorithm);
        when(rule.getReadwriteSplittingStrategy()).thenReturn(staticReadwriteSplittingStrategy);
        when(rule.getWriteDataSource()).thenReturn(expectedResult);
        when(staticReadwriteSplittingStrategy.getReadDataSources()).thenReturn(emptyList);
        when(randomReadQueryLoadBalanceAlgorithm
                .getDataSource(null, writeDataSourceName, emptyList))
                        .thenReturn(expectedResult);
    }
}
