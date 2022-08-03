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

import io.netty.util.DefaultAttributeMap;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.SetVariableStatement;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.enums.VariableEnum;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.exception.UnsupportedVariableException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.apache.shardingsphere.proxy.backend.util.SystemPropertyUtil;
import org.apache.shardingsphere.transaction.core.TransactionType;
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

public final class SetVariableBackendHandlerTest extends ProxyContextRestorer {
    
    private static final String DATABASE_PATTERN = "db_%s";
    
    private ConnectionSession connectionSession;
    
    @Before
    public void setUp() {
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class),
                new ShardingSphereMetaData(createDatabases(), mock(ShardingSphereRuleMetaData.class), new ConfigurationProperties(createProperties())));
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.init(contextManager);
        connectionSession = new ConnectionSession(mock(MySQLDatabaseType.class), TransactionType.LOCAL, new DefaultAttributeMap());
    }
    
    private Map<String, ShardingSphereDatabase> createDatabases() {
        Map<String, ShardingSphereDatabase> result = new HashMap<>(10, 1);
        for (int i = 0; i < 10; i++) {
            ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
            when(database.getResource()).thenReturn(new ShardingSphereResource(Collections.emptyMap()));
            when(database.getRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Collections.emptyList()));
            when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(mock(ShardingSphereSchema.class));
            result.put(String.format(DATABASE_PATTERN, i), database);
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
        SetVariableHandler handler = new SetVariableHandler();
        handler.init(new SetVariableStatement("transaction_type", "XA"), connectionSession);
        ResponseHeader actual = handler.execute();
        assertThat(actual, instanceOf(UpdateResponseHeader.class));
        assertThat(connectionSession.getTransactionStatus().getTransactionType(), is(TransactionType.XA));
    }
    
    @Test
    public void assertSwitchTransactionTypeBASE() throws SQLException {
        connectionSession.setCurrentDatabase(String.format(DATABASE_PATTERN, 0));
        SetVariableHandler handler = new SetVariableHandler();
        handler.init(new SetVariableStatement("transaction_type", "BASE"), connectionSession);
        ResponseHeader actual = handler.execute();
        assertThat(actual, instanceOf(UpdateResponseHeader.class));
        assertThat(connectionSession.getTransactionStatus().getTransactionType(), is(TransactionType.BASE));
    }
    
    @Test
    public void assertSwitchTransactionTypeLOCAL() throws SQLException {
        connectionSession.setCurrentDatabase(String.format(DATABASE_PATTERN, 0));
        SetVariableHandler handler = new SetVariableHandler();
        handler.init(new SetVariableStatement("transaction_type", "LOCAL"), connectionSession);
        ResponseHeader actual = handler.execute();
        assertThat(actual, instanceOf(UpdateResponseHeader.class));
        assertThat(connectionSession.getTransactionStatus().getTransactionType(), is(TransactionType.LOCAL));
    }
    
    @Test(expected = UnsupportedVariableException.class)
    public void assertSwitchTransactionTypeFailed() throws SQLException {
        SetVariableHandler handler = new SetVariableHandler();
        connectionSession.setCurrentDatabase(String.format(DATABASE_PATTERN, 0));
        handler.init(new SetVariableStatement("transaction_type", "XXX"), connectionSession);
        handler.execute();
    }
    
    @Test(expected = UnsupportedVariableException.class)
    public void assertNotSupportedVariable() throws SQLException {
        SetVariableHandler handler = new SetVariableHandler();
        handler.init(new SetVariableStatement("@@session", "XXX"), connectionSession);
        handler.execute();
    }
    
    @Test
    public void assertSetAgentPluginsEnabledTrue() throws SQLException {
        SetVariableHandler handler = new SetVariableHandler();
        connectionSession.setCurrentDatabase(String.format(DATABASE_PATTERN, 0));
        handler.init(new SetVariableStatement(VariableEnum.AGENT_PLUGINS_ENABLED.name(), Boolean.TRUE.toString()), null);
        ResponseHeader actual = handler.execute();
        assertThat(actual, instanceOf(UpdateResponseHeader.class));
        assertThat(SystemPropertyUtil.getSystemProperty(VariableEnum.AGENT_PLUGINS_ENABLED.name(), Boolean.FALSE.toString()), is(Boolean.TRUE.toString()));
    }
    
    @Test
    public void assertSetAgentPluginsEnabledFalse() throws SQLException {
        SetVariableHandler handler = new SetVariableHandler();
        connectionSession.setCurrentDatabase(String.format(DATABASE_PATTERN, 0));
        handler.init(new SetVariableStatement(VariableEnum.AGENT_PLUGINS_ENABLED.name(), Boolean.FALSE.toString()), null);
        ResponseHeader actual = handler.execute();
        assertThat(actual, instanceOf(UpdateResponseHeader.class));
        assertThat(SystemPropertyUtil.getSystemProperty(VariableEnum.AGENT_PLUGINS_ENABLED.name(), Boolean.FALSE.toString()), is(Boolean.FALSE.toString()));
    }
    
    @Test
    public void assertSetAgentPluginsEnabledFalseWithUnknownValue() throws SQLException {
        SetVariableHandler handler = new SetVariableHandler();
        connectionSession.setCurrentDatabase(String.format(DATABASE_PATTERN, 0));
        handler.init(new SetVariableStatement(VariableEnum.AGENT_PLUGINS_ENABLED.name(), "xxx"), connectionSession);
        ResponseHeader actual = handler.execute();
        assertThat(actual, instanceOf(UpdateResponseHeader.class));
        assertThat(SystemPropertyUtil.getSystemProperty(VariableEnum.AGENT_PLUGINS_ENABLED.name(), Boolean.FALSE.toString()), is(Boolean.FALSE.toString()));
    }
}
