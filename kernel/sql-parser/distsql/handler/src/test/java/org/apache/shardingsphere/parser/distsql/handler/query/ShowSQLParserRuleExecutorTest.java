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

package org.apache.shardingsphere.parser.distsql.handler.query;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.distsql.parser.statement.queryable.ShowSQLParserRuleStatement;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
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

class ShowSQLParserRuleExecutorTest {
    
    @Test
    void assertSQLParserRule() {
        ShardingSphereMetaData metaData = mockMetaData();
        ShowSQLParserRuleExecutor executor = new ShowSQLParserRuleExecutor();
        Collection<LocalDataQueryResultRow> actual = executor.getRows(metaData, mock(ShowSQLParserRuleStatement.class));
        assertThat(actual.size(), is(1));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        LocalDataQueryResultRow row = iterator.next();
        assertThat(row.getCell(1), is("true"));
        assertThat(row.getCell(2), is("initialCapacity: 128, maximumSize: 1024"));
        assertThat(row.getCell(3), is("initialCapacity: 2000, maximumSize: 65535"));
    }
    
    @Test
    void assertGetColumnNames() {
        ShowSQLParserRuleExecutor executor = new ShowSQLParserRuleExecutor();
        Collection<String> columns = executor.getColumnNames();
        assertThat(columns.size(), is(3));
        Iterator<String> iterator = columns.iterator();
        assertThat(iterator.next(), is("sql_comment_parse_enabled"));
        assertThat(iterator.next(), is("parse_tree_cache"));
        assertThat(iterator.next(), is("sql_statement_cache"));
    }
    
    private ShardingSphereMetaData mockMetaData() {
        SQLParserRule sqlParserRule = mock(SQLParserRule.class);
        when(sqlParserRule.getConfiguration()).thenReturn(new SQLParserRuleConfiguration(true, new CacheOption(128, 1024), new CacheOption(2000, 65535)));
        return new ShardingSphereMetaData(new LinkedHashMap<>(), mock(ResourceMetaData.class),
                new RuleMetaData(Collections.singleton(sqlParserRule)), new ConfigurationProperties(new Properties()));
    }
}
