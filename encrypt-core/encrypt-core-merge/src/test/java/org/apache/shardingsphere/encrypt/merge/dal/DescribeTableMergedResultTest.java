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

package org.apache.shardingsphere.encrypt.merge.dal;

import com.google.common.base.Optional;
import org.apache.shardingsphere.underlying.execute.QueryResult;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.core.strategy.encrypt.EncryptTable;
import org.apache.shardingsphere.sql.parser.relation.segment.table.TablesContext;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DescribeTableMergedResultTest {
    
    @Test
    public void assertNextForEmptyQueryResult() throws SQLException {
        EncryptRule encryptRule = mock(EncryptRule.class);
        DescribeTableMergedResult actual = new DescribeTableMergedResult(encryptRule, Collections.<QueryResult>emptyList(), createSQLStatementContext());
        assertFalse(actual.next());
    }
    
    @Test
    public void assertFieldWithEncryptRule() throws SQLException {
        EncryptRule encryptRule = createEncryptRule();
        DescribeTableMergedResult actual = new DescribeTableMergedResult(encryptRule, Collections.singletonList(createQueryResult()), createSQLStatementContext());
        assertTrue(actual.next());
        assertThat(actual.getValue(1, String.class).toString(), is("id"));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, String.class).toString(), is("logic_name"));
        assertTrue(actual.next());
        assertThat(actual.getValue(1, String.class).toString(), is("pre_name"));
        assertFalse(actual.next());
    }
    
    @Test
    public void assertAllWithEncryptRule() throws SQLException {
        EncryptRule encryptRule = createEncryptRule();
        DescribeTableMergedResult actual = new DescribeTableMergedResult(encryptRule, Collections.singletonList(createQueryResult()), createSQLStatementContext());
        assertTrue(actual.next());
        assertThat(actual.getValue(1, String.class).toString(), is("id"));
        assertThat(actual.getValue(2, String.class).toString(), is("int(11) unsigned"));
        assertThat(actual.getValue(3, String.class).toString(), is("NO"));
        assertThat(actual.getValue(4, String.class).toString(), is("PRI"));
        assertThat(actual.getValue(5, String.class).toString(), is(""));
        assertThat(actual.getValue(6, String.class).toString(), is("auto_increment"));
    }
    
    private EncryptRule createEncryptRule() {
        EncryptRule result = mock(EncryptRule.class);
        EncryptTable encryptTable = mock(EncryptTable.class);
        when(result.findEncryptTable("user")).thenReturn(Optional.of(encryptTable));
        when(encryptTable.getAssistedQueryColumns()).thenReturn(Collections.singletonList("name_assisted"));
        when(encryptTable.getPlainColumns()).thenReturn(Collections.singletonList("name_plain"));
        when(encryptTable.getCipherColumns()).thenReturn(Collections.singletonList("name"));
        when(encryptTable.getLogicColumnOfCipher("name")).thenReturn("logic_name");
        return result;
    }
    
    private QueryResult createQueryResult() throws SQLException {
        QueryResult result = mock(QueryResult.class);
        when(result.getColumnCount()).thenReturn(6);
        when(result.next()).thenReturn(true, true, true, true, true, false);
        when(result.getValue(1, Object.class)).thenReturn("id", "name", "pre_name", "name_assisted", "name_plain");
        when(result.getValue(2, Object.class)).thenReturn("int(11) unsigned", "varchar(100)");
        when(result.getValue(3, Object.class)).thenReturn("NO", "YES");
        when(result.getValue(4, Object.class)).thenReturn("PRI", "");
        when(result.getValue(5, Object.class)).thenReturn("");
        when(result.getValue(6, Object.class)).thenReturn("auto_increment", "");
        return result;
    }
    
    private SQLStatementContext createSQLStatementContext() {
        SQLStatementContext result = mock(SQLStatementContext.class);
        TablesContext tablesContext = mock(TablesContext.class);
        when(tablesContext.getSingleTableName()).thenReturn("user");
        when(result.getTablesContext()).thenReturn(tablesContext);
        return result;
    }
}
