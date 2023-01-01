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

package org.apache.shardingsphere.mask.distsql.handler.query;

import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mask.distsql.parser.statement.CountMaskRuleStatement;
import org.apache.shardingsphere.mask.rule.MaskRule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class CountMaskRuleResultSetTest {
    
    @Test
    public void assertGetRowData() {
        CountMaskRuleResultSet resultSet = new CountMaskRuleResultSet();
        resultSet.init(mockDatabase(), mock(CountMaskRuleStatement.class));
        assertTrue(resultSet.next());
        assertTrue(resultSet.getColumnNames().containsAll(Arrays.asList("rule_name", "database", "count")));
        List<Object> actual = new ArrayList<>(resultSet.getRowData());
        assertThat(actual.size(), is(3));
        assertThat(actual.get(0), is("mask"));
        assertThat(actual.get(1), is("mask_db"));
        assertThat(actual.get(2), is(1));
        assertFalse(resultSet.next());
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getName()).thenReturn("mask_db");
        ShardingSphereRuleMetaData ruleMetaData = mock(ShardingSphereRuleMetaData.class);
        MaskRule maskRule = mockMaskRule();
        when(ruleMetaData.findSingleRule(MaskRule.class)).thenReturn(Optional.of(maskRule));
        when(result.getRuleMetaData()).thenReturn(ruleMetaData);
        return result;
    }
    
    private MaskRule mockMaskRule() {
        MaskRule result = mock(MaskRule.class);
        when(result.getTables()).thenReturn(Collections.singleton("mask_table"));
        return result;
    }
}
