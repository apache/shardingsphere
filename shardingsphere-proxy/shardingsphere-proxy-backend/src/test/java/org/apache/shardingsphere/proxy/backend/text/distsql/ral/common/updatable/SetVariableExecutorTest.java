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

import org.apache.shardingsphere.distsql.parser.statement.ral.common.updatable.SetVariableStatement;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.transaction.TransactionStatus;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.RALBackendHandler.HandlerParameter;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.enums.VariableEnum;
import org.apache.shardingsphere.proxy.backend.util.SystemPropertyUtil;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SetVariableExecutorTest {
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Test
    public void assertExecuteWithTransactionType() throws SQLException {
        SetVariableStatement statement = new SetVariableStatement("transaction_type", "local");
        when(connectionSession.getTransactionStatus()).thenReturn(new TransactionStatus(TransactionType.XA));
        new SetVariableHandler().init(getParameter(statement, connectionSession)).execute();
        assertThat(connectionSession.getTransactionStatus().getTransactionType().name(), is(TransactionType.LOCAL.name()));
    }
    
    @Test
    public void assertExecuteWithAgent() throws SQLException {
        SetVariableStatement statement = new SetVariableStatement("AGENT_PLUGINS_ENABLED", "false");
        new SetVariableHandler().init(getParameter(statement, connectionSession)).execute();
        String actualValue = SystemPropertyUtil.getSystemProperty(VariableEnum.AGENT_PLUGINS_ENABLED.name(), "default");
        assertThat(actualValue, is("false"));
    }
    
    @Test
    public void assertExecuteWithConfigurationKey() throws SQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = new MetaDataContexts(null);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.getInstance().init(contextManager);
        SetVariableStatement statement = new SetVariableStatement("proxy_frontend_flush_threshold", "1024");
        new SetVariableHandler().init(getParameter(statement, connectionSession)).execute();
        Object actualValue = contextManager.getMetaDataContexts().getProps().getProps().get("proxy-frontend-flush-threshold");
        assertNotNull(actualValue);
        assertThat(actualValue.toString(), is("1024"));
    }
    
    private HandlerParameter<SetVariableStatement> getParameter(final SetVariableStatement statement, final ConnectionSession connectionSession) {
        return new HandlerParameter<SetVariableStatement>().setStatement(statement).setConnectionSession(connectionSession);
    }
}
