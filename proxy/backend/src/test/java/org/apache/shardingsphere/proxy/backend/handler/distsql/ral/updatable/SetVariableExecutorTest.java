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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable;

import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.SetVariableStatement;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.enums.VariableEnum;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.transaction.TransactionStatus;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.apache.shardingsphere.proxy.backend.util.SystemPropertyUtil;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SetVariableExecutorTest extends ProxyContextRestorer {
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Test
    public void assertExecuteWithTransactionType() throws SQLException {
        SetVariableStatement statement = new SetVariableStatement("transaction_type", "local");
        when(connectionSession.getTransactionStatus()).thenReturn(new TransactionStatus(TransactionType.XA));
        SetVariableHandler handler = new SetVariableHandler();
        handler.init(statement, connectionSession);
        handler.execute();
        assertThat(connectionSession.getTransactionStatus().getTransactionType().name(), is(TransactionType.LOCAL.name()));
    }
    
    @Test
    public void assertExecuteWithAgent() throws SQLException {
        SetVariableStatement statement = new SetVariableStatement("AGENT_PLUGINS_ENABLED", Boolean.FALSE.toString());
        SetVariableHandler handler = new SetVariableHandler();
        handler.init(statement, connectionSession);
        handler.execute();
        String actualValue = SystemPropertyUtil.getSystemProperty(VariableEnum.AGENT_PLUGINS_ENABLED.name(), "default");
        assertThat(actualValue, is(Boolean.FALSE.toString()));
    }
    
    @Test
    public void assertExecuteWithConfigurationKey() throws SQLException {
        ContextManager contextManager = new ContextManager(new MetaDataContexts(mock(MetaDataPersistService.class), new ShardingSphereMetaData(), new ShardingSphereData()), null);
        ProxyContext.init(contextManager);
        SetVariableStatement statement = new SetVariableStatement("proxy_frontend_flush_threshold", "1024");
        SetVariableHandler handler = new SetVariableHandler();
        handler.init(statement, connectionSession);
        handler.execute();
        Object actualValue = contextManager.getMetaDataContexts().getMetaData().getProps().getProps().get("proxy-frontend-flush-threshold");
        assertThat(actualValue.toString(), is("1024"));
        assertThat(contextManager.getMetaDataContexts().getMetaData().getProps().getValue(ConfigurationPropertyKey.PROXY_FRONTEND_FLUSH_THRESHOLD), is(1024));
    }
}
