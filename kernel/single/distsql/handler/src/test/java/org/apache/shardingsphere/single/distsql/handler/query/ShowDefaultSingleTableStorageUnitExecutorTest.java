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

import org.apache.shardingsphere.distsql.handler.query.RQLExecutor;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.single.api.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.distsql.statement.rql.ShowDefaultSingleTableStorageUnitStatement;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowDefaultSingleTableStorageUnitExecutorTest {
    
    @Test
    void assertGetRowData() {
        RQLExecutor<ShowDefaultSingleTableStorageUnitStatement> executor = new ShowDefaultSingleTableStorageUnitExecutor();
        Collection<LocalDataQueryResultRow> actual = executor.getRows(mockDatabase(), mock(ShowDefaultSingleTableStorageUnitStatement.class));
        assertThat(actual.size(), is(1));
        Iterator<LocalDataQueryResultRow> rowData = actual.iterator();
        String defaultSingleTableStorageUnit = (String) rowData.next().getCell(1);
        assertThat(defaultSingleTableStorageUnit, is("foo_ds"));
    }
    
    @Test
    void assertGetColumns() {
        RQLExecutor<ShowDefaultSingleTableStorageUnitStatement> executor = new ShowDefaultSingleTableStorageUnitExecutor();
        Collection<String> columns = executor.getColumnNames();
        assertThat(columns.size(), is(1));
        Iterator<String> iterator = columns.iterator();
        assertThat(iterator.next(), is("storage_unit_name"));
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        SingleRule singleRule = mock(SingleRule.class);
        when(singleRule.getConfiguration()).thenReturn(new SingleRuleConfiguration(Collections.emptyList(), "foo_ds"));
        when(result.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(singleRule)));
        return result;
    }
}
