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

package org.apache.shardingsphere.sqltranslator.distsql.handler;

import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.sqltranslator.api.config.SQLTranslatorRuleConfiguration;
import org.apache.shardingsphere.sqltranslator.distsql.parser.statement.ShowSQLTranslatorRuleStatement;
import org.apache.shardingsphere.sqltranslator.rule.SQLTranslatorRule;
import org.junit.Test;

import java.util.Collection;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class SQLTranslatorRuleQueryResultSetTest {
    
    @Test
    public void assertExecute() {
        ShardingSphereRuleMetaData ruleMetaData = mockGlobalRuleMetaData();
        SQLTranslatorRuleQueryResultSet resultSet = new SQLTranslatorRuleQueryResultSet();
        resultSet.init(ruleMetaData, mock(ShowSQLTranslatorRuleStatement.class));
        Collection<Object> actual = resultSet.getRowData();
        assertThat(actual.size(), is(2));
        assertTrue(actual.contains("NATIVE"));
        assertTrue(actual.contains(Boolean.TRUE.toString()));
    }
    
    private ShardingSphereRuleMetaData mockGlobalRuleMetaData() {
        SQLTranslatorRule authorityRule = mock(SQLTranslatorRule.class);
        when(authorityRule.getConfiguration()).thenReturn(createSQLTranslatorRuleConfiguration());
        ShardingSphereRuleMetaData result = mock(ShardingSphereRuleMetaData.class);
        when(result.findSingleRule(SQLTranslatorRule.class)).thenReturn(Optional.of(authorityRule));
        return result;
    }
    
    private SQLTranslatorRuleConfiguration createSQLTranslatorRuleConfiguration() {
        return new SQLTranslatorRuleConfiguration("NATIVE", true);
    }
}
