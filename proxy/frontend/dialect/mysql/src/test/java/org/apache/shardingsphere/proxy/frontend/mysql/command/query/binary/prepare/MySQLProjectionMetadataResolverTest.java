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

package org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.prepare;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLBinaryColumnType;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLCharacterSets;
import org.apache.shardingsphere.database.protocol.mysql.packet.MySQLPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.MySQLColumnDefinition41Packet;
import org.apache.shardingsphere.database.protocol.mysql.packet.command.query.MySQLColumnDefinitionFlag;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.Projection;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.MySQLServerPreparedStatement;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({ProxyContext.class, MySQLPreparedStatementMetadataFactory.class})
class MySQLProjectionMetadataResolverTest {
    
    @Test
    void assertResolveProjectionPackets() throws SQLException {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getTablesContext().getDatabaseName()).thenReturn(Optional.of("foo_db"));
        when(selectStatementContext.getProjectionsContext().getExpandProjections()).thenReturn(Collections.singletonList(mock(Projection.class)));
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(preparedStatement.getMetaData()).thenReturn(resultSetMetaData);
        mockResultSetMetaData(resultSetMetaData);
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        MySQLServerPreparedStatement serverPreparedStatement = mock(MySQLServerPreparedStatement.class);
        when(MySQLPreparedStatementMetadataFactory.load(connectionSession, serverPreparedStatement)).thenReturn(preparedStatement);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getName()).thenReturn("foo_db");
        when(database.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.emptyList()));
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
        when(database.getProtocolType()).thenReturn(databaseType);
        when(contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db")).thenReturn(database);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        int expectedCharacterSet = MySQLCharacterSets.UTF8MB4_UNICODE_CI.getId();
        Collection<MySQLPacket> actual = MySQLProjectionMetadataResolver.resolveProjectionPackets(connectionSession, serverPreparedStatement, selectStatementContext, expectedCharacterSet);
        assertThat(actual.size(), is(1));
        MySQLPacketPayload payload = createPayload((MySQLColumnDefinition41Packet) actual.iterator().next());
        skipColumnDefinitionStrings(payload);
        payload.readIntLenenc();
        assertThat(payload.readInt2(), is(expectedCharacterSet));
        payload.skipReserved(4);
        assertThat(payload.readInt1(), is(MySQLBinaryColumnType.BLOB.getValue()));
        assertThat(payload.readInt2(), is(MySQLColumnDefinitionFlag.BLOB.getValue()));
        verify(preparedStatement).close();
    }
    
    private void mockResultSetMetaData(final ResultSetMetaData resultSetMetaData) throws SQLException {
        when(resultSetMetaData.getColumnName(1)).thenReturn("content");
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("content");
        when(resultSetMetaData.getColumnType(1)).thenReturn(Types.CLOB);
        when(resultSetMetaData.getColumnTypeName(1)).thenReturn("BLOB SUB_TYPE TEXT");
        when(resultSetMetaData.getColumnDisplaySize(1)).thenReturn(1024);
        when(resultSetMetaData.isNullable(1)).thenReturn(ResultSetMetaData.columnNullable);
        when(resultSetMetaData.getTableName(1)).thenReturn("");
    }
    
    private MySQLPacketPayload createPayload(final MySQLColumnDefinition41Packet packet) {
        ByteBuf byteBuf = Unpooled.buffer();
        packet.write(new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8));
        return new MySQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
    }
    
    private void skipColumnDefinitionStrings(final MySQLPacketPayload payload) {
        payload.readStringLenenc();
        payload.readStringLenenc();
        payload.readStringLenenc();
        payload.readStringLenenc();
        payload.readStringLenenc();
        payload.readStringLenenc();
    }
}
