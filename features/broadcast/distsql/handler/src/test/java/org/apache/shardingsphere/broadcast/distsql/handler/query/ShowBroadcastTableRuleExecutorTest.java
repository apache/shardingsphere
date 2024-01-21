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

package org.apache.shardingsphere.broadcast.distsql.handler.query;

import org.apache.shardingsphere.broadcast.api.config.BroadcastRuleConfiguration;
import org.apache.shardingsphere.broadcast.distsql.statement.ShowBroadcastTableRulesStatement;
import org.apache.shardingsphere.broadcast.rule.BroadcastRule;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowBroadcastTableRuleExecutorTest {
    
    @Test
    void assertGetRowData() {
        ShowBroadcastTableRuleExecutor executor = new ShowBroadcastTableRuleExecutor();
        executor.setRule(mockBroadcastRule());
        Collection<LocalDataQueryResultRow> actual = executor.getRows(mock(ShowBroadcastTableRulesStatement.class));
        assertThat(actual.size(), is(1));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        LocalDataQueryResultRow row = iterator.next();
        assertThat(row.getCell(1), is("t_address"));
    }
    
    private BroadcastRule mockBroadcastRule() {
        BroadcastRule result = mock(BroadcastRule.class);
        BroadcastRuleConfiguration config = mock(BroadcastRuleConfiguration.class);
        when(config.getTables()).thenReturn(Collections.singleton("t_address"));
        when(result.getConfiguration()).thenReturn(config);
        return result;
    }
}
