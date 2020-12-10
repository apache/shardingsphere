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

package org.apache.shardingsphere.proxy.frontend.mysql.command;

import org.apache.shardingsphere.db.protocol.mysql.packet.command.MySQLCommandPacketType;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.admin.initdb.MySQLComInitDbPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.close.MySQLComStmtClosePacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.execute.MySQLComStmtExecutePacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.prepare.MySQLComStmtPreparePacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.reset.MySQLComStmtResetPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.text.fieldlist.MySQLComFieldListPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.text.query.MySQLComQueryPacket;
import org.apache.shardingsphere.db.protocol.packet.CommandPacket;
import org.apache.shardingsphere.infra.auth.MemoryAuthentication;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.metadata.impl.StandardMetaDataContexts;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.frontend.mysql.command.admin.initdb.MySQLComInitDbExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.admin.ping.MySQLComPingExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.admin.quit.MySQLComQuitExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.generic.MySQLUnsupportedCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.close.MySQLComStmtCloseExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.execute.MySQLComStmtExecuteExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.prepare.MySQLComStmtPrepareExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.reset.MySQLComStmtResetExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.text.fieldlist.MySQLComFieldListPacketExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.text.query.MySQLComQueryPacketExecutor;
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
public final class MySQLCommandExecutorFactoryTest {
    
    @Mock
    private BackendConnection backendConnection;
    
    @Before
    public void setUp() throws ReflectiveOperationException {
        when(backendConnection.getSchemaName()).thenReturn("logic_db");
        Field field = ProxyContext.getInstance().getClass().getDeclaredField("metaDataContexts");
        field.setAccessible(true);
        Map<String, ShardingSphereMetaData> metaDataMap = Collections.singletonMap("logic_db", mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS));
        field.set(ProxyContext.getInstance(), 
                new StandardMetaDataContexts(metaDataMap, mock(ExecutorEngine.class), new MemoryAuthentication(), new ConfigurationProperties(new Properties()), new MySQLDatabaseType()));
    }
    
    @Test
    public void assertNewInstanceWithComQuit() {
        assertThat(MySQLCommandExecutorFactory.newInstance(MySQLCommandPacketType.COM_QUIT, mock(CommandPacket.class), backendConnection), instanceOf(MySQLComQuitExecutor.class));
    }
    
    @Test
    public void assertNewInstanceWithComInitDb() {
        assertThat(MySQLCommandExecutorFactory.newInstance(MySQLCommandPacketType.COM_INIT_DB, mock(MySQLComInitDbPacket.class), backendConnection), instanceOf(MySQLComInitDbExecutor.class));
    }
    
    @Test
    public void assertNewInstanceWithComFieldList() {
        MySQLComFieldListPacket packet = mock(MySQLComFieldListPacket.class);
        when(packet.getTable()).thenReturn("test");
        assertThat(MySQLCommandExecutorFactory.newInstance(MySQLCommandPacketType.COM_FIELD_LIST, packet, backendConnection), instanceOf(MySQLComFieldListPacketExecutor.class));
    }
    
    @Test
    public void assertNewInstanceWithComQuery() {
        assertThat(MySQLCommandExecutorFactory.newInstance(MySQLCommandPacketType.COM_QUERY, mock(MySQLComQueryPacket.class), backendConnection), instanceOf(MySQLComQueryPacketExecutor.class));
    }
    
    @Test
    public void assertNewInstanceWithComPing() {
        assertThat(MySQLCommandExecutorFactory.newInstance(MySQLCommandPacketType.COM_PING, mock(CommandPacket.class), backendConnection), instanceOf(MySQLComPingExecutor.class));
    }
    
    @Test
    public void assertNewInstanceWithComStmtPrepare() {
        assertThat(MySQLCommandExecutorFactory.newInstance(
                MySQLCommandPacketType.COM_STMT_PREPARE, mock(MySQLComStmtPreparePacket.class), backendConnection), instanceOf(MySQLComStmtPrepareExecutor.class));
    }
    
    @Test
    public void assertNewInstanceWithComStmtExecute() {
        MySQLComStmtExecutePacket packet = mock(MySQLComStmtExecutePacket.class);
        when(packet.getSql()).thenReturn("SELECT 1");
        assertThat(MySQLCommandExecutorFactory.newInstance(MySQLCommandPacketType.COM_STMT_EXECUTE, packet, backendConnection), instanceOf(MySQLComStmtExecuteExecutor.class));
    }
    
    @Test
    public void assertNewInstanceWithComStmtReset() {
        assertThat(MySQLCommandExecutorFactory.newInstance(MySQLCommandPacketType.COM_STMT_RESET,
                mock(MySQLComStmtResetPacket.class), backendConnection), instanceOf(MySQLComStmtResetExecutor.class));
    }
    
    @Test
    public void assertNewInstanceWithComStmtClose() {
        assertThat(MySQLCommandExecutorFactory.newInstance(MySQLCommandPacketType.COM_STMT_CLOSE,
                mock(MySQLComStmtClosePacket.class), backendConnection), instanceOf(MySQLComStmtCloseExecutor.class));
    }
    
    @Test
    public void assertNewInstanceWithUnsupportedCommand() {
        assertThat(MySQLCommandExecutorFactory.newInstance(MySQLCommandPacketType.COM_REFRESH,
                mock(CommandPacket.class), backendConnection), instanceOf(MySQLUnsupportedCommandExecutor.class));
    }
}
