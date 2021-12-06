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

package org.apache.shardingsphere.proxy.backend.text.distsql.rql;

import io.netty.util.DefaultAttributeMap;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionUpdater;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.distsql.fixture.CreateFixtureRuleStatement;
import org.apache.shardingsphere.proxy.backend.text.distsql.rdl.rule.RuleDefinitionBackendHandler;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class RuleDefinitionBackendHandlerTest {
    
    @Before
    public void setUp() {
        ShardingSphereServiceLoader.register(RuleDefinitionUpdater.class);
        ProxyContext.getInstance().init(mockContextManager());
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts().getAllSchemaNames()).thenReturn(Collections.singleton("test"));
        when(result.getMetaDataContexts().getMetaData("test").getRuleMetaData()).thenReturn(mock(ShardingSphereRuleMetaData.class));
        return result;
    }
    
    @Test
    public void assertExecute() throws SQLException {
        BackendConnection backendConnection = new BackendConnection(TransactionType.LOCAL, new DefaultAttributeMap());
        backendConnection.setCurrentSchema("test");
        ResponseHeader response = new RuleDefinitionBackendHandler<>(new CreateFixtureRuleStatement(), backendConnection).execute();
        assertThat(response, instanceOf(UpdateResponseHeader.class));
        assertThat(backendConnection.getTransactionStatus().getTransactionType(), is(TransactionType.LOCAL));
    }
}
