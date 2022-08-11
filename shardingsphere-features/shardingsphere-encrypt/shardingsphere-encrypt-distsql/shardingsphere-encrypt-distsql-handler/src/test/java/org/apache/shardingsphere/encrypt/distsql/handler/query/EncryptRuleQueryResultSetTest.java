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

import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.distsql.parser.statement.ShowEncryptRulesStatement;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.distsql.query.DatabaseDistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class EncryptRuleQueryResultSetTest {
    
    @Test
    public void assertGetRowData() {
        ShardingSphereDatabase database = mockDatabase();
        DatabaseDistSQLResultSet resultSet = new EncryptRuleQueryResultSet();
        resultSet.init(database, mock(ShowEncryptRulesStatement.class));
        Collection<Object> actual = resultSet.getRowData();
        assertThat(actual.size(), is(12));
        assertTrue(actual.contains("t_encrypt"));
        assertTrue(actual.contains("user_id"));
        assertTrue(actual.contains("user_cipher"));
        assertTrue(actual.contains("user_plain"));
        assertTrue(actual.contains("md5"));
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        EncryptRule rule = mock(EncryptRule.class);
        when(rule.getConfiguration()).thenReturn(getRuleConfiguration());
        when(result.getRuleMetaData().findSingleRule(EncryptRule.class)).thenReturn(Optional.of(rule));
        return result;
    }
    
    private RuleConfiguration getRuleConfiguration() {
        EncryptColumnRuleConfiguration encryptColumnRuleConfig = new EncryptColumnRuleConfiguration("user_id", "user_cipher", null, "user_plain", "test", null);
        EncryptTableRuleConfiguration encryptTableRuleConfig = new EncryptTableRuleConfiguration("t_encrypt", Collections.singleton(encryptColumnRuleConfig), null);
        AlgorithmConfiguration shardingSphereAlgorithmConfig = new AlgorithmConfiguration("md5", new Properties());
        return new EncryptRuleConfiguration(Collections.singleton(encryptTableRuleConfig), Collections.singletonMap("test", shardingSphereAlgorithmConfig));
    }
}
