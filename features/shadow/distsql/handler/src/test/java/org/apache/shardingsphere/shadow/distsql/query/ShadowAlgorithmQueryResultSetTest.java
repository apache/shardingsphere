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

package org.apache.shardingsphere.shadow.distsql.query;

import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.distsql.query.DatabaseDistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.distsql.handler.query.ShadowAlgorithmQueryResultSet;
import org.apache.shardingsphere.shadow.distsql.parser.statement.ShowShadowAlgorithmsStatement;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShadowAlgorithmQueryResultSetTest {
    
    @Test
    public void assertGetRowData() {
        DatabaseDistSQLResultSet resultSet = new ShadowAlgorithmQueryResultSet();
        resultSet.init(mockDatabase(), mock(ShowShadowAlgorithmsStatement.class));
        List<Object> actual = new ArrayList<>(resultSet.getRowData());
        assertThat(actual.size(), is(4));
        assertThat(actual.get(0), is("shadowAlgorithmName"));
        assertThat(actual.get(1), is("simple_hint"));
        assertThat(actual.get(2), is("foo=bar"));
        assertThat(actual.get(3), is("false"));
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShadowRule rule = mock(ShadowRule.class);
        when(rule.getConfiguration()).thenReturn(createRuleConfiguration());
        when(result.getRuleMetaData().findSingleRule(ShadowRule.class)).thenReturn(Optional.of(rule));
        return result;
    }
    
    private RuleConfiguration createRuleConfiguration() {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        Properties props = new Properties();
        props.setProperty("foo", "bar");
        result.getTables().put("t_order", new ShadowTableConfiguration(Collections.emptyList(), Collections.singleton("shadowAlgorithmName")));
        result.getShadowAlgorithms().put("shadowAlgorithmName", new AlgorithmConfiguration("simple_hint", props));
        return result;
    }
}
