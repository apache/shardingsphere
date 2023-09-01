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

package org.apache.shardingsphere.globalclock.distsql.handler.query;

import org.apache.shardingsphere.globalclock.core.rule.GlobalClockRule;
import org.apache.shardingsphere.globalclock.core.rule.builder.DefaultGlobalClockRuleConfigurationBuilder;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.globalclock.distsql.parser.statement.queryable.ShowGlobalClockRuleStatement;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowGlobalClockRuleExecutorTest {
    
    @Test
    void assertGlobalClockRule() {
        ShardingSphereMetaData metaData = mockMetaData();
        ShowGlobalClockRuleExecutor executor = new ShowGlobalClockRuleExecutor();
        Collection<LocalDataQueryResultRow> actual = executor.getRows(metaData, mock(ShowGlobalClockRuleStatement.class));
        assertThat(actual.size(), is(1));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        LocalDataQueryResultRow row = iterator.next();
        assertThat(row.getCell(1), is("TSO"));
        assertThat(row.getCell(2), is("local"));
        assertThat(row.getCell(3), is("false"));
        assertThat(row.getCell(4), is("{}"));
    }
    
    @Test
    void assertGetColumnNames() {
        ShowGlobalClockRuleExecutor executor = new ShowGlobalClockRuleExecutor();
        Collection<String> columns = executor.getColumnNames();
        assertThat(columns.size(), is(4));
        Iterator<String> iterator = columns.iterator();
        assertThat(iterator.next(), is("type"));
        assertThat(iterator.next(), is("provider"));
        assertThat(iterator.next(), is("enable"));
        assertThat(iterator.next(), is("props"));
    }
    
    private ShardingSphereMetaData mockMetaData() {
        GlobalClockRule sqlParserRule = mock(GlobalClockRule.class);
        when(sqlParserRule.getConfiguration()).thenReturn(new DefaultGlobalClockRuleConfigurationBuilder().build());
        return new ShardingSphereMetaData(new LinkedHashMap<>(), mock(ResourceMetaData.class),
                new RuleMetaData(Collections.singleton(sqlParserRule)), new ConfigurationProperties(new Properties()));
    }
}
