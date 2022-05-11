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

import io.netty.util.DefaultAttributeMap;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.updatable.SetVariableStatement;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.RALBackendHandler.HandlerParameter;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.enums.VariableEnum;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.exception.UnsupportedVariableException;
import org.apache.shardingsphere.proxy.backend.util.SystemPropertyUtil;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class SetVariableBackendHandlerTest {
    
    private static final String DATABASE_PATTERN = "db_%s";
    
    private ContextManager contextManagerBefore;
    
    private ConnectionSession connectionSession;
    
    @Before
    public void setUp() {
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class), createMetaDataMap(),
                mock(ShardingSphereRuleMetaData.class), mock(ExecutorEngine.class), mock(OptimizerContext.class), new ConfigurationProperties(createProperties()));
        contextManagerBefore = ProxyContext.getInstance().getContextManager();
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.getInstance().init(contextManager);
        connectionSession = new ConnectionSession(mock(MySQLDatabaseType.class), TransactionType.LOCAL, new DefaultAttributeMap());
    }
    
    private Map<String, ShardingSphereMetaData> createMetaDataMap() {
        Map<String, ShardingSphereMetaData> result = new HashMap<>(10, 1);
        for (int i = 0; i < 10; i++) {
            ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
            when(metaData.getResource()).thenReturn(new ShardingSphereResource(Collections.emptyMap(), null, null, new MySQLDatabaseType()));
            when(metaData.getRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Collections.emptyList(), Collections.emptyList()));
            when(metaData.getSchemaByName(DefaultDatabase.LOGIC_NAME)).thenReturn(mock(ShardingSphereSchema.class));
            result.put(String.format(DATABASE_PATTERN, i), metaData);
        }
        return result;
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.setProperty(ConfigurationPropertyKey.PROXY_BACKEND_DRIVER_TYPE.getKey(), ConfigurationPropertyKey.PROXY_BACKEND_DRIVER_TYPE.getDefaultValue());
        return result;
    }
    
    @Test
    public void assertSwitchTransactionTypeXA() throws SQLException {
        connectionSession.setCurrentDatabase(String.format(DATABASE_PATTERN, 0));
        ResponseHeader actual = new SetVariableHandler().init(getParameter(new SetVariableStatement("transaction_type", "XA"), connectionSession)).execute();
        assertThat(actual, instanceOf(UpdateResponseHeader.class));
        assertThat(connectionSession.getTransactionStatus().getTransactionType(), is(TransactionType.XA));
    }
    
    @Test
    public void assertSwitchTransactionTypeBASE() throws SQLException {
        connectionSession.setCurrentDatabase(String.format(DATABASE_PATTERN, 0));
        ResponseHeader actual = new SetVariableHandler().init(getParameter(new SetVariableStatement("transaction_type", "BASE"), connectionSession)).execute();
        assertThat(actual, instanceOf(UpdateResponseHeader.class));
        assertThat(connectionSession.getTransactionStatus().getTransactionType(), is(TransactionType.BASE));
    }
    
    @Test
    public void assertSwitchTransactionTypeLOCAL() throws SQLException {
        connectionSession.setCurrentDatabase(String.format(DATABASE_PATTERN, 0));
        ResponseHeader actual = new SetVariableHandler().init(getParameter(new SetVariableStatement("transaction_type", "LOCAL"), connectionSession)).execute();
        assertThat(actual, instanceOf(UpdateResponseHeader.class));
        assertThat(connectionSession.getTransactionStatus().getTransactionType(), is(TransactionType.LOCAL));
    }
    
    @Test(expected = UnsupportedVariableException.class)
    public void assertSwitchTransactionTypeFailed() throws SQLException {
        connectionSession.setCurrentDatabase(String.format(DATABASE_PATTERN, 0));
        new SetVariableHandler().init(getParameter(new SetVariableStatement("transaction_type", "XXX"), connectionSession)).execute();
    }
    
    @Test(expected = UnsupportedVariableException.class)
    public void assertNotSupportedVariable() throws SQLException {
        new SetVariableHandler().init(getParameter(new SetVariableStatement("@@session", "XXX"), connectionSession)).execute();
    }
    
    @Test
    public void assertSetAgentPluginsEnabledTrue() throws SQLException {
        connectionSession.setCurrentDatabase(String.format(DATABASE_PATTERN, 0));
        ResponseHeader actual = new SetVariableHandler().init(getParameter(new SetVariableStatement(VariableEnum.AGENT_PLUGINS_ENABLED.name(), Boolean.TRUE.toString()), null)).execute();
        assertThat(actual, instanceOf(UpdateResponseHeader.class));
        assertThat(SystemPropertyUtil.getSystemProperty(VariableEnum.AGENT_PLUGINS_ENABLED.name(), Boolean.FALSE.toString()), is(Boolean.TRUE.toString()));
    }
    
    @Test
    public void assertSetAgentPluginsEnabledFalse() throws SQLException {
        connectionSession.setCurrentDatabase(String.format(DATABASE_PATTERN, 0));
        ResponseHeader actual = new SetVariableHandler().init(getParameter(new SetVariableStatement(VariableEnum.AGENT_PLUGINS_ENABLED.name(), Boolean.FALSE.toString()), null)).execute();
        assertThat(actual, instanceOf(UpdateResponseHeader.class));
        assertThat(SystemPropertyUtil.getSystemProperty(VariableEnum.AGENT_PLUGINS_ENABLED.name(), Boolean.FALSE.toString()), is(Boolean.FALSE.toString()));
    }
    
    @Test
    public void assertSetAgentPluginsEnabledFalseWithUnknownValue() throws SQLException {
        connectionSession.setCurrentDatabase(String.format(DATABASE_PATTERN, 0));
        ResponseHeader actual = new SetVariableHandler().init(getParameter(new SetVariableStatement(VariableEnum.AGENT_PLUGINS_ENABLED.name(), "xxx"), connectionSession)).execute();
        assertThat(actual, instanceOf(UpdateResponseHeader.class));
        assertThat(SystemPropertyUtil.getSystemProperty(VariableEnum.AGENT_PLUGINS_ENABLED.name(), Boolean.FALSE.toString()), is(Boolean.FALSE.toString()));
    }
    
    private HandlerParameter<SetVariableStatement> getParameter(final SetVariableStatement statement, final ConnectionSession connectionSession) {
        return new HandlerParameter<SetVariableStatement>().setStatement(statement).setConnectionSession(connectionSession);
    }
    
    @After
    public void tearDown() {
        ProxyContext.getInstance().init(contextManagerBefore);
    }
}
