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

import org.apache.shardingsphere.distsql.handler.query.RQLExecutor;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.distsql.parser.statement.ShowEncryptRulesStatement;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShowEncryptRuleExecutorTest {
    
    @Test
    public void assertGetRowData() {
        ShardingSphereDatabase database = mockDatabase();
        RQLExecutor<ShowEncryptRulesStatement> executor = new ShowEncryptRuleExecutor();
        Collection<LocalDataQueryResultRow> actual = executor.getRows(database, mock(ShowEncryptRulesStatement.class));
        assertThat(actual.size(), is(1));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        LocalDataQueryResultRow row = iterator.next();
        assertThat(row.getCell(1), is("t_encrypt"));
        assertThat(row.getCell(2), is("user_id"));
        assertThat(row.getCell(3), is("user_cipher"));
        assertThat(row.getCell(4), is("user_plain"));
        assertThat(row.getCell(5), is("user_assisted"));
        assertThat(row.getCell(6), is("user_like"));
        assertThat(row.getCell(7), is("md5"));
        assertThat(row.getCell(8), is(""));
        assertThat(row.getCell(9), is(""));
        assertThat(row.getCell(10), is(""));
        assertThat(row.getCell(11), is(""));
        assertThat(row.getCell(12), is(""));
        assertThat(row.getCell(13), is("true"));
    }
    
    @Test
    public void assertGetColumnNames() {
        RQLExecutor<ShowEncryptRulesStatement> executor = new ShowEncryptRuleExecutor();
        Collection<String> columns = executor.getColumnNames();
        assertThat(columns.size(), is(13));
        Iterator<String> iterator = columns.iterator();
        assertThat(iterator.next(), is("table"));
        assertThat(iterator.next(), is("logic_column"));
        assertThat(iterator.next(), is("cipher_column"));
        assertThat(iterator.next(), is("plain_column"));
        assertThat(iterator.next(), is("assisted_query_column"));
        assertThat(iterator.next(), is("like_query_column"));
        assertThat(iterator.next(), is("encryptor_type"));
        assertThat(iterator.next(), is("encryptor_props"));
        assertThat(iterator.next(), is("assisted_query_type"));
        assertThat(iterator.next(), is("assisted_query_props"));
        assertThat(iterator.next(), is("like_query_type"));
        assertThat(iterator.next(), is("like_query_props"));
        assertThat(iterator.next(), is("query_with_cipher_column"));
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        EncryptRule rule = mock(EncryptRule.class);
        when(rule.getConfiguration()).thenReturn(getRuleConfiguration());
        when(result.getRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Collections.singleton(rule)));
        return result;
    }
    
    private RuleConfiguration getRuleConfiguration() {
        EncryptColumnRuleConfiguration encryptColumnRuleConfig = new EncryptColumnRuleConfiguration("user_id", "user_cipher", "user_assisted", "user_like", "user_plain", "test", null);
        EncryptTableRuleConfiguration encryptTableRuleConfig = new EncryptTableRuleConfiguration("t_encrypt", Collections.singleton(encryptColumnRuleConfig), null);
        AlgorithmConfiguration shardingSphereAlgorithmConfig = new AlgorithmConfiguration("md5", new Properties());
        return new EncryptRuleConfiguration(Collections.singleton(encryptTableRuleConfig), Collections.singletonMap("test", shardingSphereAlgorithmConfig));
    }
}
