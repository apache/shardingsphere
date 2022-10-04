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

import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.distsql.parser.statement.queryable.ShowSQLParserRuleStatement;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class SQLParserRuleQueryResultSetTest {
    
    @Test
    public void assertSQLParserRule() {
        ShardingSphereRuleMetaData ruleMetaData = mockGlobalRuleMetaData();
        SQLParserRuleQueryResultSet resultSet = new SQLParserRuleQueryResultSet();
        resultSet.init(ruleMetaData, mock(ShowSQLParserRuleStatement.class));
        assertTrue(resultSet.next());
        Collection<Object> actual = resultSet.getRowData();
        assertThat(actual.size(), is(3));
        Iterator<Object> rowData = actual.iterator();
        assertThat(rowData.next(), is(Boolean.TRUE.toString()));
        String parseTreeCache = (String) rowData.next();
        assertFalse(resultSet.next());
        assertThat(parseTreeCache, containsString("initialCapacity: 128"));
        assertThat(parseTreeCache, containsString("maximumSize: 1024"));
        String sqlStatementCache = (String) rowData.next();
        assertThat(sqlStatementCache, containsString("initialCapacity: 2000"));
        assertThat(sqlStatementCache, containsString("maximumSize: 65535"));
    }
    
    @Test
    public void assertGetColumnNames() {
        assertThat(new SQLParserRuleQueryResultSet().getColumnNames(), is(Arrays.asList("sql_comment_parse_enable", "parse_tree_cache", "sql_statement_cache")));
    }
    
    private ShardingSphereRuleMetaData mockGlobalRuleMetaData() {
        SQLParserRule sqlParserRule = mock(SQLParserRule.class);
        when(sqlParserRule.getConfiguration()).thenReturn(new SQLParserRuleConfiguration(true, new CacheOption(128, 1024), new CacheOption(2000, 65535)));
        ShardingSphereRuleMetaData result = mock(ShardingSphereRuleMetaData.class);
        when(result.findSingleRule(SQLParserRule.class)).thenReturn(Optional.of(sqlParserRule));
        return result;
    }
}
