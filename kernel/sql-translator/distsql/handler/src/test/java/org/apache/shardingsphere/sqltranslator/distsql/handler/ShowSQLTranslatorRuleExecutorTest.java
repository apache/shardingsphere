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

package org.apache.shardingsphere.sqltranslator.distsql.handler;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.sqltranslator.api.config.SQLTranslatorRuleConfiguration;
import org.apache.shardingsphere.sqltranslator.distsql.parser.statement.ShowSQLTranslatorRuleStatement;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;
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

class ShowSQLTranslatorRuleExecutorTest {
    
    @Test
    void assertExecute() {
        ShardingSphereMetaData metaData = mockMetaData();
        ShowSQLTranslatorRuleExecutor executor = new ShowSQLTranslatorRuleExecutor();
        Collection<LocalDataQueryResultRow> actual = executor.getRows(metaData, mock(ShowSQLTranslatorRuleStatement.class));
        assertThat(actual.size(), is(1));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        LocalDataQueryResultRow row = iterator.next();
        assertThat(row.getCell(1), is("NATIVE"));
        assertThat(row.getCell(2), is("true"));
    }
    
    @Test
    void assertGetColumnNames() {
        ShowSQLTranslatorRuleExecutor executor = new ShowSQLTranslatorRuleExecutor();
        Collection<String> columns = executor.getColumnNames();
        assertThat(columns.size(), is(2));
        Iterator<String> iterator = columns.iterator();
        assertThat(iterator.next(), is("type"));
        assertThat(iterator.next(), is("use_original_sql_when_translating_failed"));
    }
    
    private ShardingSphereMetaData mockMetaData() {
        SQLTranslatorRule sqlTranslatorRule = mock(SQLTranslatorRule.class);
        when(sqlTranslatorRule.getConfiguration()).thenReturn(createSQLTranslatorRuleConfiguration());
        return new ShardingSphereMetaData(new LinkedHashMap<>(), mock(ShardingSphereResourceMetaData.class),
                new ShardingSphereRuleMetaData(Collections.singleton(sqlTranslatorRule)), new ConfigurationProperties(new Properties()));
    }
    
    private SQLTranslatorRuleConfiguration createSQLTranslatorRuleConfiguration() {
        return new SQLTranslatorRuleConfiguration("NATIVE", true);
    }
}
