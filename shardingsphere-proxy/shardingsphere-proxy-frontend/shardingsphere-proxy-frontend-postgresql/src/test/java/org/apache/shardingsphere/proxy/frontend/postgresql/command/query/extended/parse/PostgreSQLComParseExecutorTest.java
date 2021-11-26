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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.parse;

import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.PostgreSQLPreparedStatementRegistry;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.parse.PostgreSQLComParsePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.parse.PostgreSQLParseCompletePacket;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.federation.optimizer.context.OptimizerContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.EmptyStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PostgreSQLComParseExecutorTest {
    
    @Mock
    private PostgreSQLComParsePacket parsePacket;
    
    @Mock
    private BackendConnection backendConnection;
    
    @Before
    public void setup() {
        PostgreSQLPreparedStatementRegistry.getInstance().register(1);
        PostgreSQLPreparedStatementRegistry.getInstance().register(1, "2", "", new EmptyStatement(), Collections.emptyList());
        when(backendConnection.getConnectionId()).thenReturn(1);
    }
    
    @Test
    public void assertNewInstance() throws NoSuchFieldException, IllegalAccessException {
        when(parsePacket.getSql()).thenReturn("SELECT 1");
        when(parsePacket.getStatementId()).thenReturn("2");
        when(backendConnection.getSchemaName()).thenReturn("schema");
        Field contextManagerField = ProxyContext.getInstance().getClass().getDeclaredField("contextManager");
        contextManagerField.setAccessible(true);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class), getMetaDataMap(),
                mock(ShardingSphereRuleMetaData.class), mock(ExecutorEngine.class), new ConfigurationProperties(new Properties()), mock(OptimizerContext.class));
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        contextManagerField.set(ProxyContext.getInstance(), contextManager);
        PostgreSQLComParseExecutor actual = new PostgreSQLComParseExecutor(parsePacket, backendConnection);
        assertThat(actual.execute().iterator().next(), instanceOf(PostgreSQLParseCompletePacket.class));
    }
    
    private Map<String, ShardingSphereMetaData> getMetaDataMap() {
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(metaData.getResource().getDatabaseType()).thenReturn(new MySQLDatabaseType());
        return Collections.singletonMap("schema", metaData);
    }
    
    @Test
    public void assertGetSqlWithNull() {
        when(parsePacket.getStatementId()).thenReturn("");
        when(parsePacket.getSql()).thenReturn("");
        PostgreSQLComParseExecutor actual = new PostgreSQLComParseExecutor(parsePacket, backendConnection);
        assertThat(actual.execute().iterator().next(), instanceOf(PostgreSQLParseCompletePacket.class));
    }
}
