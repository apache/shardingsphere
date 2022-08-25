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

package org.apache.shardingsphere.encrypt.distsql.handler.query;

import org.apache.shardingsphere.encrypt.distsql.parser.statement.CountEncryptRuleStatement;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class CountEncryptRuleQueryResultSetTest {
    
    @Test
    public void assertGetRowData() {
        CountEncryptRuleQueryResultSet resultSet = new CountEncryptRuleQueryResultSet();
        resultSet.init(mockDatabase(), mock(CountEncryptRuleStatement.class));
        assertTrue(resultSet.next());
        List<Object> actual = new ArrayList<>(resultSet.getRowData());
        assertThat(actual.size(), is(3));
        assertThat(actual.get(0), is("encrypt"));
        assertThat(actual.get(1), is("db_1"));
        assertThat(actual.get(2), is(1));
        assertFalse(resultSet.next());
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getName()).thenReturn("db_1");
        ShardingSphereRuleMetaData ruleMetaData = mock(ShardingSphereRuleMetaData.class);
        EncryptRule encryptRule = mockEncryptRule();
        when(ruleMetaData.findSingleRule(EncryptRule.class)).thenReturn(Optional.of(encryptRule));
        when(result.getRuleMetaData()).thenReturn(ruleMetaData);
        return result;
    }
    
    private EncryptRule mockEncryptRule() {
        EncryptRule result = mock(EncryptRule.class);
        when(result.getTables()).thenReturn(Collections.singleton("encrypt_table"));
        return result;
    }
}
