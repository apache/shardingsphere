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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rql;

import org.apache.shardingsphere.distsql.handler.query.RQLExecutor;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.CountSingleTableStatement;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rql.rule.CountSingleTableExecutor;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class CountSingleTableExecutorTest {
    
    @Test
    public void assertGetRowData() {
        RQLExecutor<CountSingleTableStatement> executor = new CountSingleTableExecutor();
        Collection<LocalDataQueryResultRow> actual = executor.getRows(mockDatabase(), mock(CountSingleTableStatement.class));
        assertThat(actual.size(), is(1));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        LocalDataQueryResultRow row = iterator.next();
        assertThat(row.getCell(1), is("db_1"));
        assertThat(row.getCell(2), is(2));
    }
    
    @Test
    public void assertGetColumnNames() {
        RQLExecutor<CountSingleTableStatement> executor = new CountSingleTableExecutor();
        Collection<String> columns = executor.getColumnNames();
        assertThat(columns.size(), is(2));
        Iterator<String> iterator = columns.iterator();
        assertThat(iterator.next(), is("database"));
        assertThat(iterator.next(), is("count"));
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getName()).thenReturn("db_1");
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(Collections.singleton(mockSingleTableRule()));
        when(result.getRuleMetaData()).thenReturn(ruleMetaData);
        return result;
    }
    
    private SingleRule mockSingleTableRule() {
        SingleRule result = mock(SingleRule.class);
        when(result.getAllTables()).thenReturn(Arrays.asList("single_table_1", "single_table_2"));
        return result;
    }
}
