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

package org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command;

import lombok.SneakyThrows;
import org.apache.shardingsphere.core.constant.ShardingConstant;
import org.apache.shardingsphere.core.constant.properties.ShardingProperties;
import org.apache.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import org.apache.shardingsphere.core.metadata.ShardingMetaData;
import org.apache.shardingsphere.core.parsing.cache.ParsingResultCache;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.schema.LogicSchemas;
import org.apache.shardingsphere.shardingproxy.backend.schema.ShardingSchema;
import org.apache.shardingsphere.shardingproxy.context.GlobalContext;
import org.apache.shardingsphere.shardingproxy.transport.mysql.constant.MySQLNewParametersBoundFlag;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.admin.MySQLUnsupportedCommandPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.admin.initdb.MySQLComInitDbPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.admin.ping.MySQLComPingPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.admin.quit.MySQLComQuitPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.MySQLBinaryStatementRegistry;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.close.MySQLComStmtClosePacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.execute.MySQLQueryComStmtExecutePacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.binary.prepare.MySQLComStmtPreparePacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.text.fieldlist.MySQLComFieldListPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.packet.command.query.text.query.MySQLComQueryPacket;
import org.apache.shardingsphere.shardingproxy.transport.mysql.payload.MySQLPacketPayload;
import org.apache.shardingsphere.transaction.core.TransactionType;
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
public final class MySQLMySQLCommandPacketFactoryTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    private BackendConnection backendConnection = new BackendConnection(TransactionType.LOCAL);
    
    @Before
    public void setUp() throws ReflectiveOperationException {
        setShardingSchemas();
        backendConnection.setCurrentSchema(ShardingConstant.LOGIC_SCHEMA_NAME);
        setMaxConnectionsSizePerQuery();
    }
    
    @SneakyThrows
    private void setShardingSchemas() {
        ShardingSchema shardingSchema = mock(ShardingSchema.class);
        ShardingMetaData metaData = mock(ShardingMetaData.class);
        when(shardingSchema.getMetaData()).thenReturn(metaData);
        ShardingRule shardingRule = mock(ShardingRule.class);
        ParsingResultCache parsingResultCache = mock(ParsingResultCache.class);
        when(shardingRule.getParsingResultCache()).thenReturn(parsingResultCache);
        when(shardingSchema.getShardingRule()).thenReturn(shardingRule);
        Map<String, ShardingSchema> shardingSchemas = new HashMap<>();
        shardingSchemas.put(ShardingConstant.LOGIC_SCHEMA_NAME, shardingSchema);
        Field field = LogicSchemas.class.getDeclaredField("logicSchemas");
        field.setAccessible(true);
        field.set(LogicSchemas.getInstance(), shardingSchemas);
    }
    
    private void setMaxConnectionsSizePerQuery() throws ReflectiveOperationException {
        Field field = GlobalContext.getInstance().getClass().getDeclaredField("shardingProperties");
        field.setAccessible(true);
        Properties props = new Properties();
        props.setProperty(ShardingPropertiesConstant.MAX_CONNECTIONS_SIZE_PER_QUERY.getKey(), String.valueOf(1));
        field.set(GlobalContext.getInstance(), new ShardingProperties(props));
    }
    
    @Test
    public void assertNewInstanceWithComQuitPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(0, MySQLCommandPacketType.COM_QUIT.getValue());
        assertThat(MySQLCommandPacketFactory.newInstance(payload, backendConnection), instanceOf(MySQLComQuitPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComInitDbPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(0, MySQLCommandPacketType.COM_INIT_DB.getValue());
        assertThat(MySQLCommandPacketFactory.newInstance(payload, backendConnection), instanceOf(MySQLComInitDbPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComFieldListPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(0, MySQLCommandPacketType.COM_FIELD_LIST.getValue());
        assertThat(MySQLCommandPacketFactory.newInstance(payload, backendConnection), instanceOf(MySQLComFieldListPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComQueryPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(0, MySQLCommandPacketType.COM_QUERY.getValue());
        when(payload.readStringEOF()).thenReturn("SHOW TABLES");
        assertThat(MySQLCommandPacketFactory.newInstance(payload, backendConnection), instanceOf(MySQLComQueryPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComStmtPreparePacket() throws SQLException {
        when(payload.readInt1()).thenReturn(0, MySQLCommandPacketType.COM_STMT_PREPARE.getValue());
        assertThat(MySQLCommandPacketFactory.newInstance(payload, backendConnection), instanceOf(MySQLComStmtPreparePacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComStmtExecutePacket() throws SQLException {
        when(payload.readInt1()).thenReturn(0, MySQLCommandPacketType.COM_STMT_EXECUTE.getValue(), MySQLNewParametersBoundFlag.PARAMETER_TYPE_EXIST.getValue());
        when(payload.readInt4()).thenReturn(1);
        MySQLBinaryStatementRegistry.getInstance().register("SELECT * FROM t_order", 1);
        assertThat(MySQLCommandPacketFactory.newInstance(payload, backendConnection), instanceOf(MySQLQueryComStmtExecutePacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComStmtClosePacket() throws SQLException {
        when(payload.readInt1()).thenReturn(0, MySQLCommandPacketType.COM_STMT_CLOSE.getValue());
        assertThat(MySQLCommandPacketFactory.newInstance(payload, backendConnection), instanceOf(MySQLComStmtClosePacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComPingPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(0, MySQLCommandPacketType.COM_PING.getValue());
        assertThat(MySQLCommandPacketFactory.newInstance(payload, backendConnection), instanceOf(MySQLComPingPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComSleepPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(0, MySQLCommandPacketType.COM_SLEEP.getValue());
        assertThat(MySQLCommandPacketFactory.newInstance(payload, backendConnection), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComCreateDbPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(0, MySQLCommandPacketType.COM_CREATE_DB.getValue());
        assertThat(MySQLCommandPacketFactory.newInstance(payload, backendConnection), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComDropDbPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(0, MySQLCommandPacketType.COM_DROP_DB.getValue());
        assertThat(MySQLCommandPacketFactory.newInstance(payload, backendConnection), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComRefreshPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(0, MySQLCommandPacketType.COM_REFRESH.getValue());
        assertThat(MySQLCommandPacketFactory.newInstance(payload, backendConnection), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComShutDownPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(0, MySQLCommandPacketType.COM_SHUTDOWN.getValue());
        assertThat(MySQLCommandPacketFactory.newInstance(payload, backendConnection), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComStatisticsPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(0, MySQLCommandPacketType.COM_STATISTICS.getValue());
        assertThat(MySQLCommandPacketFactory.newInstance(payload, backendConnection), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComProcessInfoPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(0, MySQLCommandPacketType.COM_PROCESS_INFO.getValue());
        assertThat(MySQLCommandPacketFactory.newInstance(payload, backendConnection), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComConnectPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(0, MySQLCommandPacketType.COM_CONNECT.getValue());
        assertThat(MySQLCommandPacketFactory.newInstance(payload, backendConnection), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComProcessKillPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(0, MySQLCommandPacketType.COM_PROCESS_KILL.getValue());
        assertThat(MySQLCommandPacketFactory.newInstance(payload, backendConnection), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComDebugPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(0, MySQLCommandPacketType.COM_DEBUG.getValue());
        assertThat(MySQLCommandPacketFactory.newInstance(payload, backendConnection), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComTimePacket() throws SQLException {
        when(payload.readInt1()).thenReturn(0, MySQLCommandPacketType.COM_TIME.getValue());
        assertThat(MySQLCommandPacketFactory.newInstance(payload, backendConnection), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComDelayedInsertPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(0, MySQLCommandPacketType.COM_DELAYED_INSERT.getValue());
        assertThat(MySQLCommandPacketFactory.newInstance(payload, backendConnection), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComChangeUserPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(0, MySQLCommandPacketType.COM_CHANGE_USER.getValue());
        assertThat(MySQLCommandPacketFactory.newInstance(payload, backendConnection), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComBinlogDumpPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(0, MySQLCommandPacketType.COM_BINLOG_DUMP.getValue());
        assertThat(MySQLCommandPacketFactory.newInstance(payload, backendConnection), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComTableDumpPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(0, MySQLCommandPacketType.COM_TABLE_DUMP.getValue());
        assertThat(MySQLCommandPacketFactory.newInstance(payload, backendConnection), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComConnectOutPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(0, MySQLCommandPacketType.COM_CONNECT_OUT.getValue());
        assertThat(MySQLCommandPacketFactory.newInstance(payload, backendConnection), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComRegisterSlavePacket() throws SQLException {
        when(payload.readInt1()).thenReturn(0, MySQLCommandPacketType.COM_REGISTER_SLAVE.getValue());
        assertThat(MySQLCommandPacketFactory.newInstance(payload, backendConnection), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComStmtSendLongDataPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(0, MySQLCommandPacketType.COM_STMT_SEND_LONG_DATA.getValue());
        assertThat(MySQLCommandPacketFactory.newInstance(payload, backendConnection), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComStmtResetPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(0, MySQLCommandPacketType.COM_STMT_RESET.getValue());
        assertThat(MySQLCommandPacketFactory.newInstance(payload, backendConnection), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComSetOptionPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(0, MySQLCommandPacketType.COM_SET_OPTION.getValue());
        assertThat(MySQLCommandPacketFactory.newInstance(payload, backendConnection), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComStmtFetchPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(0, MySQLCommandPacketType.COM_STMT_FETCH.getValue());
        assertThat(MySQLCommandPacketFactory.newInstance(payload, backendConnection), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComDaemonPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(0, MySQLCommandPacketType.COM_DAEMON.getValue());
        assertThat(MySQLCommandPacketFactory.newInstance(payload, backendConnection), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComBinlogDumpGTIDPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(0, MySQLCommandPacketType.COM_BINLOG_DUMP_GTID.getValue());
        assertThat(MySQLCommandPacketFactory.newInstance(payload, backendConnection), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
    
    @Test
    public void assertNewInstanceWithComResetConnectionPacket() throws SQLException {
        when(payload.readInt1()).thenReturn(0, MySQLCommandPacketType.COM_RESET_CONNECTION.getValue());
        assertThat(MySQLCommandPacketFactory.newInstance(payload, backendConnection), instanceOf(MySQLUnsupportedCommandPacket.class));
    }
}
