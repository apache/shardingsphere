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

import org.apache.shardingsphere.distsql.parser.statement.rql.show.CountSingleTableRuleStatement;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rql.rule.CountSingleTableRuleQueryResultSet;
import org.apache.shardingsphere.singletable.rule.SingleTableRule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class CountSingleTableRuleQueryResultSetTest {
    
    @Test
    public void assertGetRowData() {
        CountSingleTableRuleQueryResultSet resultSet = new CountSingleTableRuleQueryResultSet();
        resultSet.init(mockDatabase(), mock(CountSingleTableRuleStatement.class));
        assertTrue(resultSet.next());
        List<Object> actual = new ArrayList<>(resultSet.getRowData());
        assertThat(actual.size(), is(3));
        assertThat(actual.get(0), is("single_table"));
        assertThat(actual.get(1), is("db_1"));
        assertThat(actual.get(2), is(2));
        assertFalse(resultSet.next());
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getName()).thenReturn("db_1");
        ShardingSphereRuleMetaData ruleMetaData = mock(ShardingSphereRuleMetaData.class);
        SingleTableRule singleTableRule = mockSingleTableRule();
        when(ruleMetaData.findSingleRule(SingleTableRule.class)).thenReturn(Optional.of(singleTableRule));
        when(result.getRuleMetaData()).thenReturn(ruleMetaData);
        return result;
    }
    
    private SingleTableRule mockSingleTableRule() {
        SingleTableRule result = mock(SingleTableRule.class);
        when(result.getAllTables()).thenReturn(Arrays.asList("single_table_1", "single_table_2"));
        return result;
    }
}
