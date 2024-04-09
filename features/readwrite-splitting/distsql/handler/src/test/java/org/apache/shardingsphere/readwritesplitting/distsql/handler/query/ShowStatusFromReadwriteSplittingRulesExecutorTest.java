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

package org.apache.shardingsphere.readwritesplitting.distsql.handler.query;

import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.readwritesplitting.distsql.statement.ShowStatusFromReadwriteSplittingRulesStatement;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingDataSourceRule;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowStatusFromReadwriteSplittingRulesExecutorTest {
    
    // TODO Replace With DistSQLQueryExecuteEngine
    @Test
    void assertGetRowData() {
        ShowStatusFromReadwriteSplittingRulesExecutor executor = new ShowStatusFromReadwriteSplittingRulesExecutor();
        executor.setRule(mockRule());
        Collection<LocalDataQueryResultRow> actual = executor.getRows(mock(ShowStatusFromReadwriteSplittingRulesStatement.class), mock(ContextManager.class, RETURNS_DEEP_STUBS));
        assertThat(actual.size(), is(2));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        LocalDataQueryResultRow row = iterator.next();
        assertThat(row.getCell(1), is("read_ds_0"));
        assertThat(row.getCell(2), is("ENABLED"));
        row = iterator.next();
        assertThat(row.getCell(1), is("read_ds_1"));
        assertThat(row.getCell(2), is("DISABLED"));
    }
    
    private ReadwriteSplittingRule mockRule() {
        ReadwriteSplittingRule result = mock(ReadwriteSplittingRule.class);
        Map<String, ReadwriteSplittingDataSourceRule> dataSourceRules = Collections.singletonMap("group_0", mockReadwriteSplittingDataSourceRule());
        when(result.getDataSourceRules()).thenReturn(dataSourceRules);
        return result;
    }
    
    private ReadwriteSplittingDataSourceRule mockReadwriteSplittingDataSourceRule() {
        ReadwriteSplittingDataSourceRule result = mock(ReadwriteSplittingDataSourceRule.class, RETURNS_DEEP_STUBS);
        when(result.getReadwriteSplittingGroup().getReadDataSources()).thenReturn(Arrays.asList("read_ds_0", "read_ds_1"));
        when(result.getDisabledDataSourceNames()).thenReturn(Collections.singleton("read_ds_1"));
        return result;
    }
}
