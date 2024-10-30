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

import org.apache.shardingsphere.broadcast.rule.BroadcastRule;
import org.apache.shardingsphere.distsql.handler.executor.rql.rule.CountResultRowBuilder;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BroadcastCountResultRowBuilderTest {
    
    @SuppressWarnings("unchecked")
    private final CountResultRowBuilder<BroadcastRule> builder = TypedSPILoader.getService(CountResultRowBuilder.class, "BROADCAST");
    
    @Test
    void assertGenerateRows() {
        List<LocalDataQueryResultRow> actual = new ArrayList<>(builder.generateRows(mockRule(), "foo_db"));
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getCell(1), is("broadcast_table"));
        assertThat(actual.get(0).getCell(2), is("foo_db"));
        assertThat(actual.get(0).getCell(3), is("1"));
    }
    
    private BroadcastRule mockRule() {
        BroadcastRule result = mock(BroadcastRule.class, RETURNS_DEEP_STUBS);
        when(result.getConfiguration().getTables().size()).thenReturn(1);
        return result;
    }
    
    @Test
    void assertGetRuleClass() {
        assertThat(builder.getRuleClass(), is(BroadcastRule.class));
    }
}
