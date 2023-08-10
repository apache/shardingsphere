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
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.distsql.handler.query.ShowShadowRuleExecutor;
import org.apache.shardingsphere.shadow.distsql.parser.statement.ShowShadowRulesStatement;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowShadowRuleExecutorTest {
    
    @Test
    void assertGetRowData() {
        RQLExecutor<ShowShadowRulesStatement> executor = new ShowShadowRuleExecutor();
        Collection<LocalDataQueryResultRow> actual = executor.getRows(mockDatabase(), mock(ShowShadowRulesStatement.class));
        assertThat(actual.size(), is(2));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        LocalDataQueryResultRow row = iterator.next();
        assertThat(row.getCell(1), is("t_order"));
        assertThat(row.getCell(2), is("shadow_rule"));
        assertThat(row.getCell(3), is("source"));
        assertThat(row.getCell(4), is("shadow"));
        assertThat(row.getCell(5), is("REGEX_MATCH"));
        assertThat(row.getCell(6), is(""));
        row = iterator.next();
        assertThat(row.getCell(1), is("t_order_item"));
        assertThat(row.getCell(2), is("shadow_rule"));
        assertThat(row.getCell(3), is("source"));
        assertThat(row.getCell(4), is("shadow"));
        assertThat(row.getCell(5), is("REGEX_MATCH"));
        assertThat(row.getCell(6), is(""));
    }
    
    @Test
    void assertGetColumnNames() {
        RQLExecutor<ShowShadowRulesStatement> executor = new ShowShadowRuleExecutor();
        Collection<String> columns = executor.getColumnNames();
        assertThat(columns.size(), is(6));
        Iterator<String> iterator = columns.iterator();
        assertThat(iterator.next(), is("shadow_table"));
        assertThat(iterator.next(), is("rule_name"));
        assertThat(iterator.next(), is("source_name"));
        assertThat(iterator.next(), is("shadow_name"));
        assertThat(iterator.next(), is("algorithm_type"));
        assertThat(iterator.next(), is("algorithm_props"));
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
        result.getDataSources().add(new ShadowDataSourceConfiguration("shadow_rule", "source", "shadow"));
        result.getShadowAlgorithms().put("user_id_select_match_algorithm", new AlgorithmConfiguration("REGEX_MATCH", new Properties()));
        result.getTables().put("t_order", new ShadowTableConfiguration(Collections.singletonList("shadow_rule"), Collections.singletonList("user_id_select_match_algorithm")));
        result.getTables().put("t_order_item", new ShadowTableConfiguration(Collections.singletonList("shadow_rule"), Collections.singletonList("user_id_select_match_algorithm")));
        return result;
    }
}
