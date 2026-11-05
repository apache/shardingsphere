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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.batch;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.DefaultAttributeMap;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.protocol.constant.CommonConstants;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchCancelCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchRegistry;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.batch.FirebirdBatchStatement;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.CommandExecutorTask;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction.FirebirdTransactionIdGenerator;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction.FirebirdTransactionSessionRegistry;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.firebirdsql.gds.ISCConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class FirebirdBatchCancelCommandExecutorTest {
    
    private static final int CONNECTION_ID = 2;
    
    private static final int STATEMENT_ID = 22;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Mock
    private FirebirdBatchCancelCommandPacket packet;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChannelHandlerContext channelHandlerContext;
    
    @Mock
    private Connection backendConnection;
    
    @Test
    void assertExecute() throws SQLException {
        FirebirdBatchRegistry.getInstance().registerConnection(CONNECTION_ID);
        FirebirdBatchStatement batchStatement = new FirebirdBatchStatement(STATEMENT_ID);
        batchStatement.addParameterValues(Arrays.asList(1, "foo"));
        FirebirdBatchRegistry.getInstance().registerBatchStatement(CONNECTION_ID, STATEMENT_ID, batchStatement);
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        Collection<DatabasePacket> actual = new FirebirdBatchCancelCommandExecutor(packet, connectionSession).execute();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), isA(FirebirdGenericResponsePacket.class));
        FirebirdBatchStatement actualBatchStatement = FirebirdBatchRegistry.getInstance().getBatchStatement(CONNECTION_ID, STATEMENT_ID);
        assertThat(actualBatchStatement, is(batchStatement));
        assertTrue(actualBatchStatement.getParameterValues().isEmpty());
        FirebirdBatchRegistry.getInstance().unregisterConnection(CONNECTION_ID);
    }
    
    @Test
    void assertExecuteWhenBatchNotFound() throws SQLException {
        when(connectionSession.getConnectionId()).thenReturn(CONNECTION_ID);
        when(packet.getStatementHandle()).thenReturn(STATEMENT_ID);
        Collection<DatabasePacket> actual = new FirebirdBatchCancelCommandExecutor(packet, connectionSession).execute();
        assertThat(actual.size(), is(1));
        assertThat(((FirebirdGenericResponsePacket) actual.iterator().next()).getErrorCode(), is(ISCConstants.isc_bad_batch_handle));
    }
    
    @Test
    void assertExecuteWhenBatchNotFoundInTransaction() throws SQLException {
        ConnectionSession transactionSession = createConnectionSessionInTransaction();
        int transactionHandle = FirebirdTransactionIdGenerator.getInstance().nextTransactionId(CONNECTION_ID);
        FirebirdTransactionSessionRegistry.getInstance().register(transactionSession, transactionHandle);
        DatabaseProtocolFrontendEngine engine = DatabaseTypedSPILoader.getService(DatabaseProtocolFrontendEngine.class, TypedSPILoader.getService(DatabaseType.class, "Firebird"));
        new CommandExecutorTask(engine, transactionSession, channelHandlerContext, Unpooled.buffer().writeInt(FirebirdCommandPacketType.BATCH_CANCEL.getValue()).writeInt(STATEMENT_ID)).run();
        new CommandExecutorTask(engine, transactionSession, channelHandlerContext, Unpooled.buffer().writeInt(FirebirdCommandPacketType.COMMIT.getValue()).writeInt(transactionHandle)).run();
        FirebirdTransactionIdGenerator.getInstance().unregisterConnection(CONNECTION_ID);
        ArgumentCaptor<FirebirdGenericResponsePacket> responseCaptor = ArgumentCaptor.forClass(FirebirdGenericResponsePacket.class);
        verify(channelHandlerContext, times(2)).write(responseCaptor.capture());
        assertThat(responseCaptor.getAllValues().get(0).getErrorCode(), is(ISCConstants.isc_bad_batch_handle));
        verify(backendConnection).commit();
        verify(backendConnection, never()).rollback();
    }
    
    private ConnectionSession createConnectionSessionInTransaction() {
        TransactionRule transactionRule = mock(TransactionRule.class);
        when(transactionRule.getDefaultType()).thenReturn(TransactionType.LOCAL);
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData()).thenReturn(
                new ShardingSphereMetaData(Collections.emptyList(), mock(), new RuleMetaData(Collections.singleton(transactionRule)), new ConfigurationProperties(new Properties())));
        when(channelHandlerContext.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).get()).thenReturn(StandardCharsets.UTF_8);
        ConnectionSession result = new ConnectionSession(TypedSPILoader.getService(DatabaseType.class, "Firebird"), new DefaultAttributeMap());
        result.setConnectionId(CONNECTION_ID);
        result.setGrantee(new Grantee("foo_user", "%"));
        result.setAutoCommit(false);
        result.getDatabaseConnectionManager().handleAutoCommit();
        result.getDatabaseConnectionManager().getCachedConnections().put("foo_db.foo_ds", backendConnection);
        FirebirdTransactionIdGenerator.getInstance().registerConnection(CONNECTION_ID);
        return result;
    }
}
