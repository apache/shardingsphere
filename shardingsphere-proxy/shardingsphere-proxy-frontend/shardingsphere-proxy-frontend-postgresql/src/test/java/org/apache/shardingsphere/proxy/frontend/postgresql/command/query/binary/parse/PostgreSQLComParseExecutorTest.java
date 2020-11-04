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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.binary.parse;

import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.BinaryStatementRegistry;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.parse.PostgreSQLComParsePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.parse.PostgreSQLParseCompletePacket;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.schema.impl.StandardSchemaContexts;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorKernel;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PostgreSQLComParseExecutorTest {
    
    @Mock
    private PostgreSQLComParsePacket parsePacket;
    
    @Mock
    private BackendConnection backendConnection;
    
    @Test
    public void assertNewInstance() throws NoSuchFieldException, IllegalAccessException {
        when(parsePacket.getSql()).thenReturn("SELECT 1");
        when(parsePacket.getStatementId()).thenReturn("2");
        when(backendConnection.getConnectionId()).thenReturn(1);
        Field schemaContexts = ProxyContext.getInstance().getClass().getDeclaredField("schemaContexts");
        schemaContexts.setAccessible(true);
        schemaContexts.set(ProxyContext.getInstance(), 
                new StandardSchemaContexts(getMetaDataMap(), mock(ExecutorKernel.class), new Authentication(), new ConfigurationProperties(new Properties()), new MySQLDatabaseType()));
        BinaryStatementRegistry.getInstance().register(1);
        PostgreSQLComParseExecutor actual = new PostgreSQLComParseExecutor(parsePacket, backendConnection);
        assertThat(actual.execute().iterator().next(), instanceOf(PostgreSQLParseCompletePacket.class));
    }
    
    private Map<String, ShardingSphereMetaData> getMetaDataMap() {
        return Collections.singletonMap("schema", mock(ShardingSphereMetaData.class));
    }
    
    @Test
    public void assertGetSqlWithNull() {
        when(parsePacket.getSql()).thenReturn("");
        when(backendConnection.getConnectionId()).thenReturn(1);
        PostgreSQLComParseExecutor actual = new PostgreSQLComParseExecutor(parsePacket, backendConnection);
        assertThat(actual.execute().iterator().next(), instanceOf(PostgreSQLParseCompletePacket.class));
    }
}
