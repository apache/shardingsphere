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

import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLCharacterSet;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLConstants;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.MySQLCommandPacketType;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.admin.MySQLComSetOptionPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.admin.initdb.MySQLComInitDbPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.MySQLComStmtSendLongDataPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.close.MySQLComStmtClosePacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.execute.MySQLComStmtExecutePacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.prepare.MySQLComStmtPreparePacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.binary.reset.MySQLComStmtResetPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.text.fieldlist.MySQLComFieldListPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.query.text.query.MySQLComQueryPacket;
import org.apache.shardingsphere.db.protocol.packet.CommandPacket;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.mysql.ProxyContextRestorer;
import org.apache.shardingsphere.proxy.frontend.mysql.command.admin.MySQLComSetOptionExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.admin.initdb.MySQLComInitDbExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.admin.ping.MySQLComPingExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.admin.quit.MySQLComQuitExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.generic.MySQLUnsupportedCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.MySQLComStmtSendLongDataExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.close.MySQLComStmtCloseExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.execute.MySQLComStmtExecuteExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.prepare.MySQLComStmtPrepareExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.reset.MySQLComStmtResetExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.text.fieldlist.MySQLComFieldListPacketExecutor;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.text.query.MySQLComQueryPacketExecutor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLCommandExecutorFactoryTest extends ProxyContextRestorer {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Mock
    private JDBCBackendConnection backendConnection;
    
    @Before
    public void setUp() {
        when(connectionSession.getAttributeMap().attr(MySQLConstants.MYSQL_CHARACTER_SET_ATTRIBUTE_KEY).get()).thenReturn(MySQLCharacterSet.UTF8MB4_GENERAL_CI);
        when(connectionSession.getBackendConnection()).thenReturn(backendConnection);
        ShardingSphereDatabase database = mockDatabase();
        Map<String, ShardingSphereDatabase> databases = new LinkedHashMap<>(1, 1);
        databases.put("logic_db", database);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class),
                new ShardingSphereMetaData(databases, mock(ShardingSphereRuleMetaData.class), new ConfigurationProperties(new Properties())));
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.init(contextManager);
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getRuleMetaData().getRules()).thenReturn(Collections.emptyList());
        when(result.getResourceMetaData().getStorageTypes()).thenReturn(Collections.singletonMap("ds_0", new MySQLDatabaseType()));
        when(result.getProtocolType()).thenReturn(new MySQLDatabaseType());
        return result;
    }
    
    @Test
    public void assertNewInstanceWithComQuit() throws SQLException {
        assertThat(MySQLCommandExecutorFactory.newInstance(MySQLCommandPacketType.COM_QUIT, mock(CommandPacket.class), connectionSession), instanceOf(MySQLComQuitExecutor.class));
    }
    
    @Test
    public void assertNewInstanceWithComInitDb() throws SQLException {
        assertThat(MySQLCommandExecutorFactory.newInstance(MySQLCommandPacketType.COM_INIT_DB, mock(MySQLComInitDbPacket.class), connectionSession), instanceOf(MySQLComInitDbExecutor.class));
    }
    
    @Test
    public void assertNewInstanceWithComFieldList() throws SQLException {
        MySQLComFieldListPacket packet = mock(MySQLComFieldListPacket.class);
        assertThat(MySQLCommandExecutorFactory.newInstance(MySQLCommandPacketType.COM_FIELD_LIST, packet, connectionSession), instanceOf(MySQLComFieldListPacketExecutor.class));
    }
    
    @Test
    public void assertNewInstanceWithComQuery() throws SQLException {
        MySQLComQueryPacket packet = mock(MySQLComQueryPacket.class);
        when(packet.getSql()).thenReturn("");
        assertThat(MySQLCommandExecutorFactory.newInstance(MySQLCommandPacketType.COM_QUERY, packet, connectionSession), instanceOf(MySQLComQueryPacketExecutor.class));
    }
    
    @Test
    public void assertNewInstanceWithComPing() throws SQLException {
        assertThat(MySQLCommandExecutorFactory.newInstance(MySQLCommandPacketType.COM_PING, mock(CommandPacket.class), connectionSession), instanceOf(MySQLComPingExecutor.class));
    }
    
    @Test
    public void assertNewInstanceWithComStmtPrepare() throws SQLException {
        assertThat(MySQLCommandExecutorFactory.newInstance(
                MySQLCommandPacketType.COM_STMT_PREPARE, mock(MySQLComStmtPreparePacket.class), connectionSession), instanceOf(MySQLComStmtPrepareExecutor.class));
    }
    
    @Test
    public void assertNewInstanceWithComStmtExecute() throws SQLException {
        assertThat(MySQLCommandExecutorFactory.newInstance(MySQLCommandPacketType.COM_STMT_EXECUTE, mock(MySQLComStmtExecutePacket.class), connectionSession),
                instanceOf(MySQLComStmtExecuteExecutor.class));
    }
    
    @Test
    public void assertNewInstanceWithComStmtSendLongData() throws SQLException {
        assertThat(MySQLCommandExecutorFactory.newInstance(MySQLCommandPacketType.COM_STMT_SEND_LONG_DATA, mock(MySQLComStmtSendLongDataPacket.class), connectionSession),
                instanceOf(MySQLComStmtSendLongDataExecutor.class));
    }
    
    @Test
    public void assertNewInstanceWithComStmtReset() throws SQLException {
        assertThat(MySQLCommandExecutorFactory.newInstance(MySQLCommandPacketType.COM_STMT_RESET,
                mock(MySQLComStmtResetPacket.class), connectionSession), instanceOf(MySQLComStmtResetExecutor.class));
    }
    
    @Test
    public void assertNewInstanceWithComStmtClose() throws SQLException {
        assertThat(MySQLCommandExecutorFactory.newInstance(MySQLCommandPacketType.COM_STMT_CLOSE,
                mock(MySQLComStmtClosePacket.class), connectionSession), instanceOf(MySQLComStmtCloseExecutor.class));
    }
    
    @Test
    public void assertNewInstanceWithComSetOption() throws SQLException {
        assertThat(MySQLCommandExecutorFactory.newInstance(MySQLCommandPacketType.COM_SET_OPTION, mock(MySQLComSetOptionPacket.class), connectionSession), instanceOf(MySQLComSetOptionExecutor.class));
    }
    
    @Test
    public void assertNewInstanceWithUnsupportedCommand() throws SQLException {
        assertThat(MySQLCommandExecutorFactory.newInstance(MySQLCommandPacketType.COM_REFRESH, mock(CommandPacket.class), connectionSession), instanceOf(MySQLUnsupportedCommandExecutor.class));
    }
}
