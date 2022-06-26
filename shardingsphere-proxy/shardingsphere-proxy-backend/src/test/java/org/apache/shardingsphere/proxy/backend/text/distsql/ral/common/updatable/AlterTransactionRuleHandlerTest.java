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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.updatable;

import org.apache.shardingsphere.distsql.parser.segment.TransactionProviderSegment;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.updatable.AlterTransactionRuleStatement;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.apache.shardingsphere.transaction.ShardingSphereTransactionManagerEngine;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class AlterTransactionRuleHandlerTest extends ProxyContextRestorer {
    
    @Test
    public void assertExecute() throws SQLException {
        mockContextManager();
        AlterTransactionRuleHandler handler = new AlterTransactionRuleHandler();
        TransactionProviderSegment local = new TransactionProviderSegment(TransactionType.LOCAL.name(), new Properties());
        handler.init(new AlterTransactionRuleStatement(TransactionType.LOCAL.name(), local), null);
        handler.execute();
    }
    
    private void mockContextManager() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        TransactionRule rule = mock(TransactionRule.class);
        ShardingSphereRuleMetaData globalRuleMetaData = new ShardingSphereRuleMetaData(new LinkedList<>(Collections.singleton(rule)));
        when(contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData()).thenReturn(globalRuleMetaData);
        when(contextManager.getMetaDataContexts().getMetaData().getDatabases()).thenReturn(createShardingSphereDatabase());
        MockedConstruction<TransactionRule> mockedConstruction = Mockito.mockConstruction(TransactionRule.class, (mock, content) -> {
            when(mock.getResources()).thenReturn(createTransactionRuleResource());
        });
        ProxyContext.init(contextManager);
    }
    
    private Map<String, ShardingSphereDatabase> createShardingSphereDatabase() {
        return Collections.singletonMap(DefaultDatabase.LOGIC_NAME, mock(ShardingSphereDatabase.class));
    }
    
    private Map<String, ShardingSphereTransactionManagerEngine> createTransactionRuleResource() {
        return Collections.singletonMap(DefaultDatabase.LOGIC_NAME, mock(ShardingSphereTransactionManagerEngine.class));
    }
}
