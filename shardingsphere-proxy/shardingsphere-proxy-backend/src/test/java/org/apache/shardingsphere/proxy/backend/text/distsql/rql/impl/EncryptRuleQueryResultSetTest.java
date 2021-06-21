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

package org.apache.shardingsphere.proxy.backend.text.distsql.rql.impl;

import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.distsql.parser.statement.ShowEncryptRulesStatement;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.text.distsql.rql.RuleQueryResultSet;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class EncryptRuleQueryResultSetTest {
    
    @Before
    public void setUp() {
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class);
        when(metaDataContexts.getAllSchemaNames()).thenReturn(Collections.singleton("test"));
        ShardingSphereRuleMetaData ruleMetaData = mock(ShardingSphereRuleMetaData.class);
        when(ruleMetaData.getConfigurations()).thenReturn(Collections.singleton(buildEncryptRuleConfiguration()));
        ShardingSphereMetaData shardingSphereMetaData = mock(ShardingSphereMetaData.class);
        when(shardingSphereMetaData.getRuleMetaData()).thenReturn(ruleMetaData);
        when(metaDataContexts.getMetaData("test")).thenReturn(shardingSphereMetaData);
        ProxyContext.getInstance().init(metaDataContexts, mock(TransactionContexts.class));
    }
    
    private EncryptRuleConfiguration buildEncryptRuleConfiguration() {
        EncryptColumnRuleConfiguration encryptColumnRuleConfig = new EncryptColumnRuleConfiguration("user_id", "user_cipher", null, "user_plain", "test");
        EncryptTableRuleConfiguration encryptTableRuleConfig = new EncryptTableRuleConfiguration("t_encrypt", Collections.singleton(encryptColumnRuleConfig));
        ShardingSphereAlgorithmConfiguration shardingSphereAlgorithmConfig = new ShardingSphereAlgorithmConfiguration("md5", new Properties());
        Map<String, ShardingSphereAlgorithmConfiguration> encryptors = new HashMap<>();
        encryptors.put("test", shardingSphereAlgorithmConfig);
        return new EncryptRuleConfiguration(Collections.singleton(encryptTableRuleConfig), encryptors);
    }
    
    @Test
    public void assertGetRowData() {
        RuleQueryResultSet resultSet = new EncryptRuleQueryResultSet();
        resultSet.init("test", mock(ShowEncryptRulesStatement.class));
        Collection<Object> actual = resultSet.getRowData();
        assertThat(actual.size(), is(6));
        assertTrue(actual.contains("t_encrypt"));
        assertTrue(actual.contains("user_id"));
        assertTrue(actual.contains("user_cipher"));
        assertTrue(actual.contains("user_plain"));
        assertTrue(actual.contains("md5"));
    }
}
