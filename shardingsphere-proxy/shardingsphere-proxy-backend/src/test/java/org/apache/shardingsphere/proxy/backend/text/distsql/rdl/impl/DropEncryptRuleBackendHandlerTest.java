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

package org.apache.shardingsphere.proxy.backend.text.distsql.rdl.impl;

import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.impl.DropEncryptRuleStatement;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.EncryptRulesNotExistedException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DropEncryptRuleBackendHandlerTest {
    
    @Mock
    private BackendConnection backendConnection;
    
    @Mock
    private DropEncryptRuleStatement sqlStatement;
    
    @Mock
    private MetaDataContexts metaDataContexts;
    
    @Mock
    private TransactionContexts transactionContexts;
    
    @Mock
    private ShardingSphereMetaData shardingSphereMetaData;
    
    @Mock
    private ShardingSphereRuleMetaData ruleMetaData;
    
    @Mock
    private EncryptTableRuleConfiguration encryptTableRuleConfiguration;
    
    @Mock
    private ShardingSphereAlgorithmConfiguration shardingSphereAlgorithmConfiguration;
    
    private final DropEncryptRuleBackendHandler handler = new DropEncryptRuleBackendHandler(sqlStatement, backendConnection);
    
    @Before
    public void setUp() {
        ProxyContext.getInstance().init(metaDataContexts, transactionContexts);
        when(metaDataContexts.getAllSchemaNames()).thenReturn(Collections.singletonList("test"));
        when(metaDataContexts.getMetaData("test")).thenReturn(shardingSphereMetaData);
        when(shardingSphereMetaData.getRuleMetaData()).thenReturn(ruleMetaData);
    }
    
    @Test
    public void assertExecute() {
        when(sqlStatement.getTables()).thenReturn(Collections.singletonList("t_encrypt"));
        Map<String, ShardingSphereAlgorithmConfiguration> encryptors = new HashMap<>(1, 1);
        encryptors.put("t_encrypt_user_id_MD5", shardingSphereAlgorithmConfiguration);
        when(ruleMetaData.getConfigurations()).thenReturn(
                new LinkedList<>(Collections.singleton(new EncryptRuleConfiguration(new LinkedList<>(Collections.singleton(encryptTableRuleConfiguration)), encryptors))));
        when(encryptTableRuleConfiguration.getName()).thenReturn("t_encrypt");
        when(encryptTableRuleConfiguration.getColumns()).thenReturn(buildColumnRuleConfigurations());
        ResponseHeader responseHeader = handler.execute("test", sqlStatement);
        assertNotNull(responseHeader);
        assertTrue(responseHeader instanceof UpdateResponseHeader);
    }
    
    @Test(expected = EncryptRulesNotExistedException.class)
    public void assertExecuteWithNotExistEncryptRule() {
        when(ruleMetaData.getConfigurations()).thenReturn(Collections.emptyList());
        handler.execute("test", sqlStatement);
    }
    
    @Test(expected = EncryptRulesNotExistedException.class)
    public void assertExecuteWithNoDroppedEncryptRule() {
        when(sqlStatement.getTables()).thenReturn(Collections.singletonList("t_encrypt"));
        when(ruleMetaData.getConfigurations()).thenReturn(Collections.singletonList(new EncryptRuleConfiguration(Collections.emptyList(), Collections.emptyMap())));
        handler.execute("test", sqlStatement);
    }
    
    private Collection<EncryptColumnRuleConfiguration> buildColumnRuleConfigurations() {
        return Collections.singleton(new EncryptColumnRuleConfiguration("user_id", "user_cipher", "", "user_plain", "t_encrypt_user_id_MD5"));
    }
}
