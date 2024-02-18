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
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnItemRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.distsql.statement.ShowEncryptRulesStatement;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShowEncryptRuleExecutorTest {
    
    @Test
    void assertGetRowData() {
        ShowEncryptRuleExecutor executor = new ShowEncryptRuleExecutor();
        EncryptRule rule = mock(EncryptRule.class);
        when(rule.getConfiguration()).thenReturn(getRuleConfiguration());
        executor.setRule(rule);
        Collection<LocalDataQueryResultRow> actual = executor.getRows(mock(ShowEncryptRulesStatement.class), mock(ContextManager.class));
        assertThat(actual.size(), is(1));
        Iterator<LocalDataQueryResultRow> iterator = actual.iterator();
        LocalDataQueryResultRow row = iterator.next();
        assertThat(row.getCell(1), is("t_encrypt"));
        assertThat(row.getCell(2), is("user_id"));
        assertThat(row.getCell(3), is("user_cipher"));
        assertThat(row.getCell(4), is("user_assisted"));
        assertThat(row.getCell(5), is("user_like"));
        assertThat(row.getCell(6), is("md5"));
        assertThat(row.getCell(7), is(""));
        assertThat(row.getCell(8), is(""));
        assertThat(row.getCell(9), is(""));
        assertThat(row.getCell(10), is(""));
        assertThat(row.getCell(11), is(""));
    }
    
    private EncryptRuleConfiguration getRuleConfiguration() {
        EncryptColumnRuleConfiguration encryptColumnRuleConfig = new EncryptColumnRuleConfiguration("user_id", new EncryptColumnItemRuleConfiguration("user_cipher", "test"));
        encryptColumnRuleConfig.setAssistedQuery(new EncryptColumnItemRuleConfiguration("user_assisted", "foo_assist_query_encryptor"));
        encryptColumnRuleConfig.setLikeQuery(new EncryptColumnItemRuleConfiguration("user_like", "foo_like_encryptor"));
        EncryptTableRuleConfiguration encryptTableRuleConfig = new EncryptTableRuleConfiguration("t_encrypt", Collections.singleton(encryptColumnRuleConfig));
        AlgorithmConfiguration shardingSphereAlgorithmConfig = new AlgorithmConfiguration("md5", new Properties());
        return new EncryptRuleConfiguration(Collections.singleton(encryptTableRuleConfig), Collections.singletonMap("test", shardingSphereAlgorithmConfig));
    }
}
