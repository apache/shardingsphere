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

package org.apache.shardingsphere.single.distsql.handler.query;

import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.single.distsql.statement.rql.ShowSingleTableStatement;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowSingleTableExecutorTest {
    
    @Test
    void assertGetRowData() {
        ShowSingleTableExecutor executor = new ShowSingleTableExecutor();
        executor.setRule(mockSingleRule());
        Collection<LocalDataQueryResultRow> actual = executor.getRows(mock(ShowSingleTableStatement.class), mock(ContextManager.class));
        assertThat(actual.size(), is(2));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        LocalDataQueryResultRow row = iterator.next();
        assertThat(row.getCell(1), is("t_order"));
        assertThat(row.getCell(2), is("ds_1"));
        row = iterator.next();
        assertThat(row.getCell(1), is("t_order_item"));
        assertThat(row.getCell(2), is("ds_2"));
    }
    
    @Test
    void assertGetSingleTableWithLikeLiteral() {
        ShowSingleTableExecutor executor = new ShowSingleTableExecutor();
        executor.setRule(mockSingleRule());
        ShowSingleTableStatement statement = new ShowSingleTableStatement(null, "%item", null);
        Collection<LocalDataQueryResultRow> actual = executor.getRows(statement, mock(ContextManager.class));
        assertThat(actual.size(), is(1));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        LocalDataQueryResultRow row = iterator.next();
        assertThat(row.getCell(1), is("t_order_item"));
        assertThat(row.getCell(2), is("ds_2"));
    }
    
    private SingleRule mockSingleRule() {
        SingleRule result = mock(SingleRule.class);
        Map<String, Collection<DataNode>> singleTableDataNodeMap = new HashMap<>();
        singleTableDataNodeMap.put("t_order", Collections.singleton(new DataNode("ds_1", "t_order")));
        singleTableDataNodeMap.put("t_order_item", Collections.singleton(new DataNode("ds_2", "t_order_item")));
        when(result.getSingleTableDataNodes()).thenReturn(singleTableDataNodeMap);
        return result;
    }
}
