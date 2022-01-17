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

import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.CommentSegment;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.traffic.api.config.TrafficRuleConfiguration;
import org.apache.shardingsphere.traffic.api.config.TrafficStrategyConfiguration;
import org.apache.shardingsphere.traffic.spi.TrafficLoadBalanceAlgorithm;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class TrafficRuleTest {
    
    @Test
    public void assertGetRuleType() {
        TrafficRule authorityRule = new TrafficRule(new TrafficRuleConfiguration());
        assertThat(authorityRule.getType(), is(TrafficRule.class.getSimpleName()));
    }
    
    @Test
    public void assertFindMatchedStrategyRule() {
        TrafficRule trafficRule = new TrafficRule(createTrafficRuleConfig());
        Optional<TrafficStrategyRule> actual = trafficRule.findMatchedStrategyRule(createLogicSQL(true));
        assertTrue(actual.isPresent());
        assertThat(actual.get().getName(), is("sql_hint_traffic"));
        assertThat(actual.get().getLabels(), is(Arrays.asList("OLTP", "OLAP")));
        assertThat(actual.get().getAlgorithmName(), is("sql_hint_match"));
        assertThat(actual.get().getLoadBalancerName(), is("random"));
    }
    
    @Test
    public void assertFindMatchedStrategyRuleWhenSQLHintNotMatch() {
        TrafficRule trafficRule = new TrafficRule(createTrafficRuleConfig());
        Optional<TrafficStrategyRule> actual = trafficRule.findMatchedStrategyRule(createLogicSQL(false));
        assertFalse(actual.isPresent());
    }
    
    @Test
    public void assertFindLoadBalancer() {
        TrafficRule trafficRule = new TrafficRule(createTrafficRuleConfig());
        TrafficLoadBalanceAlgorithm actual = trafficRule.findLoadBalancer("random");
        assertThat(actual.getType(), is("RANDOM"));
    }
    
    @Test
    public void assertGetLabels() {
        TrafficRule trafficRule = new TrafficRule(createTrafficRuleConfig());
        Collection<String> actual = trafficRule.getLabels();
        assertThat(actual, is(Arrays.asList("OLTP", "OLAP")));
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private LogicSQL createLogicSQL(final boolean includeComments) {
        LogicSQL result = mock(LogicSQL.class);
        MySQLSelectStatement sqlStatement = mock(MySQLSelectStatement.class);
        Collection<CommentSegment> comments = includeComments ? Collections.singletonList(
                new CommentSegment("/* ShardingSphere hint: traffic=true */", 0, 0)) : Collections.emptyList();
        when(sqlStatement.getCommentSegments()).thenReturn(comments);
        when(sqlStatement.getProjections()).thenReturn(new ProjectionsSegment(0, 0));
        SQLStatementContext statementContext = new SelectStatementContext(createMetaDataMap(), Collections.emptyList(), sqlStatement, "sharding_db");
        when(result.getSqlStatementContext()).thenReturn(statementContext);
        return result;
    }
    
    private Map<String, ShardingSphereMetaData> createMetaDataMap() {
        Map<String, ShardingSphereMetaData> result = new HashMap<>(1, 1);
        result.put("sharding_db", mock(ShardingSphereMetaData.class));
        return result;
    }
    
    private TrafficRuleConfiguration createTrafficRuleConfig() {
        TrafficRuleConfiguration result = new TrafficRuleConfiguration();
        result.getTrafficStrategies().add(new TrafficStrategyConfiguration("sql_hint_traffic", Arrays.asList("OLTP", "OLAP"), "sql_hint_match", "random"));
        result.getTrafficAlgorithms().put("sql_hint_match", createTrafficAlgorithm());
        result.getLoadBalancers().put("random", createLoadBalancer());
        return result;
    }
    
    private ShardingSphereAlgorithmConfiguration createTrafficAlgorithm() {
        ShardingSphereAlgorithmConfiguration result = mock(ShardingSphereAlgorithmConfiguration.class);
        when(result.getType()).thenReturn("SQL_HINT");
        Properties props = new Properties();
        props.put("traffic", true);
        when(result.getProps()).thenReturn(props);
        return result;
    }
    
    private ShardingSphereAlgorithmConfiguration createLoadBalancer() {
        ShardingSphereAlgorithmConfiguration result = mock(ShardingSphereAlgorithmConfiguration.class);
        when(result.getType()).thenReturn("RANDOM");
        return result;
    }
}
