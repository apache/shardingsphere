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

package io.shardingsphere.shardingproxy.transport.mysql.packet.command;

import io.shardingsphere.core.constant.ShardingConstant;
import io.shardingsphere.core.constant.properties.ShardingProperties;
import io.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import io.shardingsphere.core.metadata.ShardingMetaData;
import io.shardingsphere.shardingproxy.backend.jdbc.connection.BackendConnection;
import io.shardingsphere.shardingproxy.frontend.common.FrontendHandler;
import io.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import io.shardingsphere.shardingproxy.runtime.schema.ShardingSchema;
import io.shardingsphere.shardingproxy.transport.mysql.constant.NewParametersBoundFlag;
import io.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacketPayload;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.admin.UnsupportedCommandPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.admin.initdb.ComInitDbPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.admin.ping.ComPingPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.admin.quit.ComQuitPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.BinaryStatementRegistry;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.close.ComStmtClosePacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.execute.ComStmtExecutePacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.prepare.ComStmtPreparePacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.text.fieldlist.ComFieldListPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.text.query.ComQueryPacket;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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
    
    @Mock
    private FrontendHandler frontendHandler;
    
    @Before
    public void setUp() throws ReflectiveOperationException {
        setShardingSchemas();
        setFrontendHandlerSchema();
        setMaxConnectionsSizePerQuery();
    }
    
    @SneakyThrows
    private void setShardingSchemas() {
        ShardingSchema shardingSchema = mock(ShardingSchema.class);
        ShardingMetaData metaData = mock(ShardingMetaData.class);
        when(shardingSchema.getMetaData()).thenReturn(metaData);
        Map<String, ShardingSchema> shardingSchemas = new HashMap<>();
        shardingSchemas.put(ShardingConstant.LOGIC_SCHEMA_NAME, shardingSchema);
        Field field = GlobalRegistry.class.getDeclaredField("logicSchemas");
        field.setAccessible(true);
        field.set(GlobalRegistry.getInstance(), shardingSchemas);
    }
    
    private void setFrontendHandlerSchema() {
        when(frontendHandler.getCurrentSchema()).thenReturn(ShardingConstant.LOGIC_SCHEMA_NAME);
    }
    
    private void setMaxConnectionsSizePerQuery() throws ReflectiveOperationException {
        Field field = GlobalRegistry.getInstance().getClass().getDeclaredField("shardingProperties");
        field.setAccessible(true);
        Properties props = new Properties();
        props.setProperty(ShardingPropertiesConstant.MAX_CONNECTIONS_SIZE_PER_QUERY.getKey(), String.valueOf(1));
        field.set(GlobalRegistry.getInstance(), new ShardingProperties(props));
    }
    
    @Test
    public void assertNewInstanceWithComQuitPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_QUIT.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection, frontendHandler), instanceOf(ComQuitPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComInitDbPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_INIT_DB.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection, frontendHandler), instanceOf(ComInitDbPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComFieldListPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_FIELD_LIST.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection, frontendHandler), instanceOf(ComFieldListPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComQueryPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_QUERY.getValue());
        when(payload.readStringEOF()).thenReturn("SHOW TABLES");
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection, frontendHandler), instanceOf(ComQueryPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComStmtPreparePacket() throws SQLException {
        when(frontendHandler.getCurrentSchema()).thenReturn(ShardingConstant.LOGIC_SCHEMA_NAME);
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_STMT_PREPARE.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection, frontendHandler), instanceOf(ComStmtPreparePacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComStmtExecutePacket() throws SQLException {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_STMT_EXECUTE.getValue(), NewParametersBoundFlag.PARAMETER_TYPE_EXIST.getValue());
        when(payload.readInt4()).thenReturn(1);
        BinaryStatementRegistry.getInstance().register("SELECT * FROM t_order", 1);
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection, frontendHandler), instanceOf(ComStmtExecutePacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComStmtClosePacket() throws SQLException {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_STMT_CLOSE.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection, frontendHandler), instanceOf(ComStmtClosePacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComPingPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_PING.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection, frontendHandler), instanceOf(ComPingPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComSleepPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_SLEEP.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection, frontendHandler), instanceOf(UnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComCreateDbPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_CREATE_DB.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection, frontendHandler), instanceOf(UnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComDropDbPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_DROP_DB.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection, frontendHandler), instanceOf(UnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComRefreshPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_REFRESH.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection, frontendHandler), instanceOf(UnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComShutDownPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_SHUTDOWN.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection, frontendHandler), instanceOf(UnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComStatisticsPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_STATISTICS.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection, frontendHandler), instanceOf(UnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComProcessInfoPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_PROCESS_INFO.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection, frontendHandler), instanceOf(UnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComConnectPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_CONNECT.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection, frontendHandler), instanceOf(UnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComProcessKillPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_PROCESS_KILL.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection, frontendHandler), instanceOf(UnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComDebugPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_DEBUG.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection, frontendHandler), instanceOf(UnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComTimePacket() throws SQLException {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_TIME.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection, frontendHandler), instanceOf(UnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComDelayedInsertPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_DELAYED_INSERT.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection, frontendHandler), instanceOf(UnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComChangeUserPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_CHANGE_USER.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection, frontendHandler), instanceOf(UnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComBinlogDumpPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_BINLOG_DUMP.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection, frontendHandler), instanceOf(UnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComTableDumpPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_TABLE_DUMP.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection, frontendHandler), instanceOf(UnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComConnectOutPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_CONNECT_OUT.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection, frontendHandler), instanceOf(UnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComRegisterSlavePacket() throws SQLException {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_REGISTER_SLAVE.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection, frontendHandler), instanceOf(UnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComStmtSendLongDataPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_STMT_SEND_LONG_DATA.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection, frontendHandler), instanceOf(UnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComStmtResetPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_STMT_RESET.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection, frontendHandler), instanceOf(UnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComSetOptionPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_SET_OPTION.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection, frontendHandler), instanceOf(UnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComStmtFetchPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_STMT_FETCH.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection, frontendHandler), instanceOf(UnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComDaemonPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_DAEMON.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection, frontendHandler), instanceOf(UnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComBinlogDumpGTIDPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_BINLOG_DUMP_GTID.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection, frontendHandler), instanceOf(UnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComResetConnectionPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(CommandPacketType.COM_RESET_CONNECTION.getValue());
        assertThat(CommandPacketFactory.newInstance(1, 1000, payload, backendConnection, frontendHandler), instanceOf(UnsupportedCommandPacket.class));
    }
}
