/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.proxy.transport.mysql.packet.command;

import io.shardingsphere.core.metadata.ShardingMetaData;
import io.shardingsphere.proxy.backend.jdbc.connection.BackendConnection;
import io.shardingsphere.proxy.config.BackendNIOConfiguration;
import io.shardingsphere.proxy.config.RuleRegistry;
import io.shardingsphere.proxy.transport.mysql.constant.NewParametersBoundFlag;
import io.shardingsphere.proxy.transport.mysql.packet.MySQLPacketPayload;
import io.shardingsphere.proxy.transport.mysql.packet.command.admin.UnsupportedCommandPacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.admin.initdb.ComInitDbPacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.admin.ping.ComPingPacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.admin.quit.ComQuitPacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.query.binary.BinaryStatementRegistry;
import io.shardingsphere.proxy.transport.mysql.packet.command.query.binary.close.ComStmtClosePacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.query.binary.execute.ComStmtExecutePacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.query.binary.prepare.ComStmtPreparePacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.query.text.fieldlist.ComFieldListPacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.query.text.query.ComQueryPacket;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CommandPacketFactoryTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Mock
    private BackendConnection backendConnection;
    
    @Before
    public void setUp() throws ReflectiveOperationException {
        setRuleRegistryBackendNIOConfig();
        setRuleRegistryMetaData();
    }
    
    private void setRuleRegistryBackendNIOConfig() throws ReflectiveOperationException {
        Field field = RuleRegistry.class.getDeclaredField("backendNIOConfig");
        field.setAccessible(true);
        field.set(RuleRegistry.getInstance(), new BackendNIOConfiguration(true, 1, 0));
    }
    
    private void setRuleRegistryMetaData() throws ReflectiveOperationException {
        Field field = RuleRegistry.class.getDeclaredField("metaData");
        field.setAccessible(true);
        ShardingMetaData shardingMetaData = mock(ShardingMetaData.class);
        field.set(RuleRegistry.getInstance(), shardingMetaData);
    }
    
    @Test
    public void assertNewInstanceWithComQuitPacket() {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_QUIT.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection), instanceOf(ComQuitPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComInitDbPacket() {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_INIT_DB.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection), instanceOf(ComInitDbPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComFieldListPacket() {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_FIELD_LIST.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection), instanceOf(ComFieldListPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComQueryPacket() {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_QUERY.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection), instanceOf(ComQueryPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComStmtPreparePacket() {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_STMT_PREPARE.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection), instanceOf(ComStmtPreparePacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComStmtExecutePacket() {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_STMT_EXECUTE.getValue(), NewParametersBoundFlag.PARAMETER_TYPE_EXIST.getValue());
        when(payload.readInt4()).thenReturn(1);
        BinaryStatementRegistry.getInstance().register("", 1);
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection), instanceOf(ComStmtExecutePacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComStmtClosePacket() {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_STMT_CLOSE.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection), instanceOf(ComStmtClosePacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComPingPacket() {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_PING.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection), instanceOf(ComPingPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComSleepPacket() {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_SLEEP.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection), instanceOf(UnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComCreateDbPacket() {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_CREATE_DB.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection), instanceOf(UnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComDropDbPacket() {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_DROP_DB.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection), instanceOf(UnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComRefreshPacket() {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_REFRESH.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection), instanceOf(UnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComShutDownPacket() {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_SHUTDOWN.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection), instanceOf(UnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComStatisticsPacket() {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_STATISTICS.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection), instanceOf(UnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComProcessInfoPacket() {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_PROCESS_INFO.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection), instanceOf(UnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComConnectPacket() {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_CONNECT.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection), instanceOf(UnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComProcessKillPacket() {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_PROCESS_KILL.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection), instanceOf(UnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComDebugPacket() {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_DEBUG.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection), instanceOf(UnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComTimePacket() {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_TIME.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection), instanceOf(UnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComDelayedInsertPacket() {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_DELAYED_INSERT.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection), instanceOf(UnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComChangeUserPacket() {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_CHANGE_USER.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection), instanceOf(UnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComBinlogDumpPacket() {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_BINLOG_DUMP.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection), instanceOf(UnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComTableDumpPacket() {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_TABLE_DUMP.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection), instanceOf(UnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComConnectOutPacket() {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_CONNECT_OUT.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection), instanceOf(UnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComRegisterSlavePacket() {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_REGISTER_SLAVE.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection), instanceOf(UnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComStmtSendLongDataPacket() {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_STMT_SEND_LONG_DATA.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection), instanceOf(UnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComStmtResetPacket() {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_STMT_RESET.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection), instanceOf(UnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComSetOptionPacket() {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_SET_OPTION.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection), instanceOf(UnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComStmtFetchPacket() {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_STMT_FETCH.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection), instanceOf(UnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComDaemonPacket() {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_DAEMON.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection), instanceOf(UnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComBinlogDumpGTIDPacket() {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_BINLOG_DUMP_GTID.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection), instanceOf(UnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComResetConnectionPacket() {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_RESET_CONNECTION.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection), instanceOf(UnsupportedCommandPacket.class));
    }
}
