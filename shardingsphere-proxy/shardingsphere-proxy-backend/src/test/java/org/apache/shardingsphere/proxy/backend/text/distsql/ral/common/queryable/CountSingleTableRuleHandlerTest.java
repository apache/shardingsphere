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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.queryable;

import org.apache.shardingsphere.distsql.parser.statement.ral.common.queryable.CountSingleTableRuleStatement;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.apache.shardingsphere.singletable.rule.SingleTableRule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CountSingleTableRuleHandlerTest extends ProxyContextRestorer {
    
    @Mock
    private ShardingSphereDatabase database1;
    
    @Mock
    private ShardingSphereDatabase database2;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager contextManager;
    
    @Before
    public void before() {
        ShardingSphereRuleMetaData ruleMetaData = mock(ShardingSphereRuleMetaData.class);
        Collection<ShardingSphereRule> rules = Collections.singletonList(mockSingleTableRule());
        when(ruleMetaData.getRules()).thenReturn(rules);
        when(database1.getRuleMetaData()).thenReturn(ruleMetaData);
        when(database2.getRuleMetaData()).thenReturn(ruleMetaData);
        Map<String, ShardingSphereDatabase> databases = new HashMap<>(2, 1);
        databases.put("db_1", database1);
        databases.put("db_2", database2);
        when(contextManager.getMetaDataContexts().getMetaData().getDatabases()).thenReturn(databases);
        ProxyContext.init(contextManager);
    }
    
    private SingleTableRule mockSingleTableRule() {
        SingleTableRule result = mock(SingleTableRule.class);
        when(result.getAllTables()).thenReturn(Arrays.asList("single_table_1", "single_table_2"));
        return result;
    }
    
    @Test
    public void assertGetRowData() throws SQLException {
        CountSingleTableRuleHandler handler = new CountSingleTableRuleHandler();
        handler.init(new CountSingleTableRuleStatement(), null);
        handler.execute();
        boolean hasNext = handler.next();
        assertTrue(hasNext);
        Collection<Object> actual = handler.getRowData();
        assertThat(actual.size(), is(2));
        Iterator<Object> rowData = actual.iterator();
        assertThat(rowData.next(), is("single_table"));
        assertThat(rowData.next(), is(4));
        hasNext = handler.next();
        assertFalse(hasNext);
    }
}
