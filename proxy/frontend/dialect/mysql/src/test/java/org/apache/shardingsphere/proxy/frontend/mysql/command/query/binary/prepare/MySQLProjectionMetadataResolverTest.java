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
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.mysql.command.query.binary.MySQLServerPreparedStatement;
import org.apache.shardingsphere.sql.parser.statement.core.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
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
    
    @Test
    void assertResolveProjectionPacketsFallsBackToResultSetMetaDataWhenProjectionCountMismatches() throws SQLException {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getTablesContext().getDatabaseName()).thenReturn(Optional.of("foo_db"));
        when(selectStatementContext.getProjectionsContext().getExpandProjections()).thenReturn(Collections.singletonList(mock(Projection.class)));
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(preparedStatement.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnCount()).thenReturn(2);
        when(resultSetMetaData.getColumnName(1)).thenReturn("order_id");
        when(resultSetMetaData.getColumnLabel(1)).thenReturn("order_id");
        when(resultSetMetaData.getColumnType(1)).thenReturn(Types.INTEGER);
        when(resultSetMetaData.getColumnTypeName(1)).thenReturn("INT");
        when(resultSetMetaData.getColumnDisplaySize(1)).thenReturn(11);
        when(resultSetMetaData.isNullable(1)).thenReturn(ResultSetMetaData.columnNullable);
        when(resultSetMetaData.getTableName(1)).thenReturn("");
        when(resultSetMetaData.getColumnName(2)).thenReturn("add_test");
        when(resultSetMetaData.getColumnLabel(2)).thenReturn("add_test");
        when(resultSetMetaData.getColumnType(2)).thenReturn(Types.VARCHAR);
        when(resultSetMetaData.getColumnTypeName(2)).thenReturn("VARCHAR");
        when(resultSetMetaData.getColumnDisplaySize(2)).thenReturn(50);
        when(resultSetMetaData.isNullable(2)).thenReturn(ResultSetMetaData.columnNullable);
        when(resultSetMetaData.getTableName(2)).thenReturn("");
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
        assertThat(actual.size(), is(2));
        assertThat(readColumnName(createPayload((MySQLColumnDefinition41Packet) actual.iterator().next())), is("order_id"));
        assertThat(readColumnName(createPayload((MySQLColumnDefinition41Packet) actual.toArray()[1])), is("add_test"));
        verify(preparedStatement).close();
    }
    
    @Test
    void assertResolveProjectionPacketsUsesProjectionsWhenAggregationProjectionMismatches() throws SQLException {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getTablesContext().getDatabaseName()).thenReturn(Optional.of("foo_db"));
        when(selectStatementContext.containsDerivedProjections()).thenReturn(true);
        // AVG is rewritten to SUM and COUNT on the backend, so the backend returns 2 columns for 1 expanded projection.
        AggregationProjection avgProjection = new AggregationProjection(AggregationType.AVG,
                new AggregationProjectionSegment(0, 0, AggregationType.AVG, "AVG(user_id)"), new IdentifierValue("user_id_avg"), mock(DatabaseType.class));
        avgProjection.getDerivedAggregationProjections().add(new AggregationProjection(AggregationType.COUNT,
                new AggregationProjectionSegment(0, 0, AggregationType.COUNT, "COUNT(user_id)"), new IdentifierValue("AVG_DERIVED_COUNT_0"), mock(DatabaseType.class)));
        avgProjection.getDerivedAggregationProjections().add(new AggregationProjection(AggregationType.SUM,
                new AggregationProjectionSegment(0, 0, AggregationType.SUM, "SUM(user_id)"), new IdentifierValue("AVG_DERIVED_SUM_0"), mock(DatabaseType.class)));
        when(selectStatementContext.getProjectionsContext().getExpandProjections()).thenReturn(Collections.singletonList(avgProjection));
        when(selectStatementContext.getProjectionsContext().getProjections()).thenReturn(Collections.singletonList(avgProjection));
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(preparedStatement.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnType(1)).thenReturn(Types.DECIMAL);
        when(resultSetMetaData.getColumnTypeName(1)).thenReturn("DECIMAL");
        when(resultSetMetaData.getColumnDisplaySize(1)).thenReturn(20);
        when(resultSetMetaData.isNullable(1)).thenReturn(ResultSetMetaData.columnNullable);
        when(resultSetMetaData.getTableName(1)).thenReturn("");
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
        assertThat(readColumnName(createPayload((MySQLColumnDefinition41Packet) actual.iterator().next())), is("user_id_avg"));
        verify(preparedStatement).close();
    }
    
    private void mockResultSetMetaData(final ResultSetMetaData resultSetMetaData) throws SQLException {
        when(resultSetMetaData.getColumnCount()).thenReturn(1);
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
    
    private String readColumnName(final MySQLPacketPayload payload) {
        for (int i = 0; i < 4; i++) {
            payload.readStringLenenc();
        }
        return payload.readStringLenenc();
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
