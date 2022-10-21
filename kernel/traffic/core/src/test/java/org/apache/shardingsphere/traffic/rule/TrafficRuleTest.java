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

package org.apache.shardingsphere.traffic.rule;

import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.CommentSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.traffic.algorithm.loadbalance.RandomTrafficLoadBalanceAlgorithm;
import org.apache.shardingsphere.traffic.algorithm.traffic.hint.SQLHintTrafficAlgorithm;
import org.apache.shardingsphere.traffic.algorithm.traffic.transaction.ProxyTrafficAlgorithm;
import org.apache.shardingsphere.traffic.api.config.TrafficRuleConfiguration;
import org.apache.shardingsphere.traffic.api.config.TrafficStrategyConfiguration;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class TrafficRuleTest {
    
    @Test
    public void assertFindMatchedStrategyRuleWhenSQLHintMatch() {
        TrafficRule trafficRule = new TrafficRule(createTrafficRuleConfig());
        Optional<TrafficStrategyRule> actual = trafficRule.findMatchedStrategyRule(createQueryContext(true), false);
        assertTrue(actual.isPresent());
        assertThat(actual.get().getName(), is("sql_hint_traffic"));
        assertThat(actual.get().getLabels(), is(new HashSet<>(Arrays.asList("OLTP", "OLAP"))));
        assertThat(actual.get().getTrafficAlgorithm(), instanceOf(SQLHintTrafficAlgorithm.class));
        assertThat(actual.get().getLoadBalancer(), instanceOf(RandomTrafficLoadBalanceAlgorithm.class));
    }
    
    @Test
    public void assertFindMatchedStrategyRuleWhenSQLHintNotMatch() {
        assertFalse(new TrafficRule(createTrafficRuleConfig()).findMatchedStrategyRule(createQueryContext(false), false).isPresent());
    }
    
    @Test
    public void assertFindMatchedStrategyRuleWhenInTransaction() {
        TrafficRule trafficRule = new TrafficRule(createTrafficRuleConfig());
        Optional<TrafficStrategyRule> actual = trafficRule.findMatchedStrategyRule(createQueryContext(false), true);
        assertTrue(actual.isPresent());
        assertThat(actual.get().getName(), is("transaction_traffic"));
        assertThat(actual.get().getLabels(), is(Collections.singleton("OLAP")));
        assertThat(actual.get().getTrafficAlgorithm(), instanceOf(ProxyTrafficAlgorithm.class));
        assertThat(actual.get().getLoadBalancer(), instanceOf(RandomTrafficLoadBalanceAlgorithm.class));
    }
    
    @Test
    public void assertGetLabels() {
        assertThat(new TrafficRule(createTrafficRuleConfig()).getLabels(), is(new HashSet<>(Arrays.asList("OLAP", "OLTP"))));
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private QueryContext createQueryContext(final boolean includeComments) {
        QueryContext result = mock(QueryContext.class);
        MySQLSelectStatement sqlStatement = mock(MySQLSelectStatement.class);
        when(sqlStatement.getCommentSegments()).thenReturn(includeComments ? Collections.singleton(new CommentSegment("/* SHARDINGSPHERE_HINT: USE_TRAFFIC=true */", 0, 0)) : Collections.emptyList());
        when(sqlStatement.getProjections()).thenReturn(new ProjectionsSegment(0, 0));
        SQLStatementContext statementContext = new SelectStatementContext(
                Collections.singletonMap("sharding_db", mock(ShardingSphereDatabase.class)), Collections.emptyList(), sqlStatement, "sharding_db");
        when(result.getSqlStatementContext()).thenReturn(statementContext);
        return result;
    }
    
    private TrafficRuleConfiguration createTrafficRuleConfig() {
        TrafficRuleConfiguration result = new TrafficRuleConfiguration();
        result.getTrafficStrategies().add(new TrafficStrategyConfiguration("sql_hint_traffic", Arrays.asList("OLTP", "OLAP"), "sql_hint_match", "random"));
        result.getTrafficStrategies().add(new TrafficStrategyConfiguration("transaction_traffic", Collections.singletonList("OLAP"), "transaction_algorithm", "random"));
        result.getTrafficAlgorithms().put("sql_hint_match", createSQLHintTrafficAlgorithm());
        result.getTrafficAlgorithms().put("transaction_algorithm", createTransactionTrafficAlgorithm());
        result.getLoadBalancers().put("random", createLoadBalancer());
        return result;
    }
    
    private AlgorithmConfiguration createSQLHintTrafficAlgorithm() {
        AlgorithmConfiguration result = mock(AlgorithmConfiguration.class);
        when(result.getType()).thenReturn("SQL_HINT");
        Properties props = new Properties();
        props.put("traffic", true);
        when(result.getProps()).thenReturn(props);
        return result;
    }
    
    private AlgorithmConfiguration createTransactionTrafficAlgorithm() {
        AlgorithmConfiguration result = mock(AlgorithmConfiguration.class);
        when(result.getType()).thenReturn("PROXY");
        return result;
    }
    
    private AlgorithmConfiguration createLoadBalancer() {
        AlgorithmConfiguration result = mock(AlgorithmConfiguration.class);
        when(result.getType()).thenReturn("RANDOM");
        return result;
    }
}
