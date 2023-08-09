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

import org.apache.shardingsphere.distsql.handler.query.RQLExecutor;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.distsql.handler.query.ShowDefaultShadowAlgorithmExecutor;
import org.apache.shardingsphere.shadow.distsql.parser.statement.ShowDefaultShadowAlgorithmStatement;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowDefaultShadowAlgorithmExecutorTest {
    
    @Test
    void assertGetRowData() {
        RQLExecutor<ShowDefaultShadowAlgorithmStatement> executor = new ShowDefaultShadowAlgorithmExecutor();
        Collection<LocalDataQueryResultRow> actual = executor.getRows(mockDatabase(), mock(ShowDefaultShadowAlgorithmStatement.class));
        assertThat(actual.size(), is(1));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        LocalDataQueryResultRow row = iterator.next();
        assertThat(row.getCell(1), is("shadowAlgorithmName"));
        assertThat(row.getCell(2), is("sql_hint"));
        assertThat(row.getCell(3), is("foo=bar"));
    }
    
    @Test
    void assertGetColumnNames() {
        RQLExecutor<ShowDefaultShadowAlgorithmStatement> executor = new ShowDefaultShadowAlgorithmExecutor();
        Collection<String> columns = executor.getColumnNames();
        assertThat(columns.size(), is(3));
        Iterator<String> iterator = columns.iterator();
        assertThat(iterator.next(), is("shadow_algorithm_name"));
        assertThat(iterator.next(), is("type"));
        assertThat(iterator.next(), is("props"));
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShadowRule rule = mock(ShadowRule.class);
        when(rule.getConfiguration()).thenReturn(createRuleConfiguration());
        when(result.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(rule)));
        return result;
    }
    
    private RuleConfiguration createRuleConfiguration() {
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        result.getTables().put("t_order", new ShadowTableConfiguration(Collections.emptyList(), Collections.singleton("shadowAlgorithmName")));
        result.getShadowAlgorithms().put("shadowAlgorithmName", new AlgorithmConfiguration("sql_hint", PropertiesBuilder.build(new Property("foo", "bar"))));
        result.setDefaultShadowAlgorithmName("shadowAlgorithmName");
        return result;
    }
}
