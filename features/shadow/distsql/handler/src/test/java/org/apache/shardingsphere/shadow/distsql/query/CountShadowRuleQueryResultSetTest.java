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

package org.apache.shardingsphere.shadow.distsql.query;

import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.shadow.distsql.handler.query.CountShadowRuleQueryResultSet;
import org.apache.shardingsphere.shadow.distsql.parser.statement.CountShadowRuleStatement;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class CountShadowRuleQueryResultSetTest {
    
    @Test
    public void assertGetRowData() {
        CountShadowRuleQueryResultSet resultSet = new CountShadowRuleQueryResultSet();
        resultSet.init(mockDatabase(), mock(CountShadowRuleStatement.class));
        assertTrue(resultSet.next());
        List<Object> actual = new ArrayList<>(resultSet.getRowData());
        assertThat(actual.size(), is(3));
        assertThat(actual.get(0), is("shadow"));
        assertThat(actual.get(1), is("db_1"));
        assertThat(actual.get(2), is(2));
        assertFalse(resultSet.next());
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getName()).thenReturn("db_1");
        ShardingSphereRuleMetaData ruleMetaData = mock(ShardingSphereRuleMetaData.class);
        ShadowRule shadowRule = mockShadowRule();
        when(ruleMetaData.findSingleRule(ShadowRule.class)).thenReturn(Optional.of(shadowRule));
        when(result.getRuleMetaData()).thenReturn(ruleMetaData);
        return result;
    }
    
    private ShadowRule mockShadowRule() {
        Map<String, Collection<String>> shadowDataSourceMappings = new HashMap<>();
        shadowDataSourceMappings.put("shadow-data-source-0", Arrays.asList("ds", "ds_shadow"));
        shadowDataSourceMappings.put("shadow-data-source-1", Arrays.asList("ds1", "ds1_shadow"));
        ShadowRule result = mock(ShadowRule.class);
        when(result.getDataSourceMapper()).thenReturn(shadowDataSourceMappings);
        return result;
    }
}
