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

import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.shadow.distsql.handler.query.CountShadowRuleExecutor;
import org.apache.shardingsphere.shadow.distsql.parser.statement.CountShadowRuleStatement;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CountShadowRuleExecutorTest {
    
    @Test
    void assertGetRowData() {
        Collection<LocalDataQueryResultRow> actual = new CountShadowRuleExecutor().getRows(mockDatabase(), mock(CountShadowRuleStatement.class));
        assertThat(actual.size(), is(1));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        LocalDataQueryResultRow row = iterator.next();
        assertThat(row.getCell(1), is("shadow"));
        assertThat(row.getCell(2), is("db_1"));
        assertThat(row.getCell(3), is(2));
    }
    
    @Test
    void assertGetColumnNames() {
        Collection<String> columns = new CountShadowRuleExecutor().getColumnNames();
        assertThat(columns.size(), is(3));
        Iterator<String> iterator = columns.iterator();
        assertThat(iterator.next(), is("rule_name"));
        assertThat(iterator.next(), is("database"));
        assertThat(iterator.next(), is("count"));
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getName()).thenReturn("db_1");
        RuleMetaData ruleMetaData = new RuleMetaData(Collections.singleton(mockShadowRule()));
        when(result.getRuleMetaData()).thenReturn(ruleMetaData);
        return result;
    }
    
    private ShadowRule mockShadowRule() {
        ShadowRule result = mock(ShadowRule.class);
        Map<String, Collection<String>> dataSourceMapper = new LinkedHashMap<>();
        dataSourceMapper.put("shadow-data-source-0", Arrays.asList("ds", "ds_shadow"));
        dataSourceMapper.put("shadow-data-source-1", Arrays.asList("ds1", "ds1_shadow"));
        when(result.getDataSourceMapper()).thenReturn(dataSourceMapper);
        return result;
    }
}
