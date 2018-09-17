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

package io.shardingsphere.proxy.transport.mysql.packet.command.query.text.query;

import com.google.common.base.Optional;
import com.google.common.eventbus.Subscribe;
import io.shardingsphere.core.constant.ShardingConstant;
import io.shardingsphere.core.constant.transaction.TransactionOperationType;
import io.shardingsphere.core.constant.transaction.TransactionType;
import io.shardingsphere.core.event.ShardingEventBusInstance;
import io.shardingsphere.core.event.transaction.xa.XATransactionEvent;
import io.shardingsphere.proxy.backend.BackendHandler;
import io.shardingsphere.proxy.backend.ResultPacket;
import io.shardingsphere.proxy.backend.jdbc.connection.BackendConnection;
import io.shardingsphere.proxy.config.ProxyContext;
import io.shardingsphere.proxy.config.RuleRegistry;
import io.shardingsphere.proxy.frontend.common.FrontendHandler;
import io.shardingsphere.proxy.transport.common.packet.DatabasePacket;
import io.shardingsphere.proxy.transport.mysql.constant.ColumnType;
import io.shardingsphere.proxy.transport.mysql.packet.MySQLPacketPayload;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandPacketType;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.proxy.transport.mysql.packet.command.query.FieldCountPacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.query.text.TextResultSetRowPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.OKPacket;
import lombok.Getter;
import lombok.Setter;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ComQueryPacketTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Mock
    private BackendConnection backendConnection;
    
    @Mock
    private FrontendHandler frontendHandler;
    
    private Listener listener;
    
    @Before
    public void setUp() throws ReflectiveOperationException {
        setProxyContextNIOConfig();
        setProxyContextRuleRegistryMap();
        setFrontendHandlerSchema();
        listener = new Listener();
        listener.setExpected(TransactionOperationType.COMMIT);
        ShardingEventBusInstance.getInstance().register(listener);
    }
    
    @After
    public void tearDown() throws ReflectiveOperationException {
        ShardingEventBusInstance.getInstance().unregister(listener);
        setTransactionType(null);
    }
    
    private void setProxyContextNIOConfig() throws ReflectiveOperationException {
        Field field = ProxyContext.class.getDeclaredField("useNIO");
        field.setAccessible(true);
        field.set(ProxyContext.getInstance(), true);
    }
    
    private void setProxyContextRuleRegistryMap() throws ReflectiveOperationException {
        RuleRegistry ruleRegistry = mock(RuleRegistry.class);
        Map<String, RuleRegistry> ruleRegistryMap = new HashMap<>();
        ruleRegistryMap.put(ShardingConstant.LOGIC_SCHEMA_NAME, ruleRegistry);
        Field field = ProxyContext.class.getDeclaredField("ruleRegistryMap");
        field.setAccessible(true);
        field.set(ProxyContext.getInstance(), ruleRegistryMap);
    }
    
    private void setFrontendHandlerSchema() {
        when(frontendHandler.getCurrentSchema()).thenReturn(ShardingConstant.LOGIC_SCHEMA_NAME);
    }
    
    private void setTransactionType(final TransactionType transactionType) throws ReflectiveOperationException {
        Field transactionTypeField = ProxyContext.class.getDeclaredField("transactionType");
        transactionTypeField.setAccessible(true);
        transactionTypeField.set(ProxyContext.getInstance(), transactionType);
    }
    
    @Test
    public void assertWrite() {
        ComQueryPacket actual = new ComQueryPacket(1, "SELECT id FROM tbl");
        assertThat(actual.getSequenceId(), is(1));
        actual.write(payload);
        verify(payload).writeInt1(CommandPacketType.COM_QUERY.getValue());
        verify(payload).writeStringEOF("SELECT id FROM tbl");
    }
    
    @Test
    public void assertExecuteWithoutTransaction() throws SQLException, ReflectiveOperationException {
        when(payload.readStringEOF()).thenReturn("SELECT id FROM tbl");
        BackendHandler backendHandler = mock(BackendHandler.class);
        when(backendHandler.next()).thenReturn(true, false);
        when(backendHandler.getResultValue()).thenReturn(new ResultPacket(1, Collections.<Object>singletonList("id"), 1, Collections.singletonList(ColumnType.MYSQL_TYPE_VARCHAR)));
        FieldCountPacket expectedFieldCountPacket = new FieldCountPacket(1, 1);
        when(backendHandler.execute()).thenReturn(new CommandResponsePackets(expectedFieldCountPacket));
        when(backendHandler.next()).thenReturn(true, false);
        when(backendHandler.getResultValue()).thenReturn(new ResultPacket(2, Collections.<Object>singletonList(99999L), 1, Collections.singletonList(ColumnType.MYSQL_TYPE_LONG)));
        ComQueryPacket packet = new ComQueryPacket(1, 1000, payload, backendConnection, frontendHandler);
        setBackendHandler(packet, backendHandler);
        Optional<CommandResponsePackets> actual = packet.execute();
        assertFalse(listener.isCalled());
        assertTrue(actual.isPresent());
        assertThat(actual.get().getPackets().size(), is(1));
        assertThat(actual.get().getPackets().iterator().next(), is((DatabasePacket) expectedFieldCountPacket));
        assertTrue(packet.next());
        assertThat(packet.getResultValue().getSequenceId(), is(2));
        assertThat(((TextResultSetRowPacket) packet.getResultValue()).getData(), is(Collections.<Object>singletonList(99999L)));
        assertFalse(packet.next());
    }
    
    private void setBackendHandler(final ComQueryPacket packet, final BackendHandler backendHandler) throws ReflectiveOperationException {
        Field field = ComQueryPacket.class.getDeclaredField("backendHandler");
        field.setAccessible(true);
        field.set(packet, backendHandler);
    }
    
    @Test
    public void assertExecuteTCLWithLocalTransaction() throws SQLException, ReflectiveOperationException {
        setTransactionType(TransactionType.LOCAL);
        when(payload.readStringEOF()).thenReturn("COMMIT");
        ComQueryPacket packet = new ComQueryPacket(1, 1000, payload, backendConnection, frontendHandler);
        Optional<CommandResponsePackets> actual = packet.execute();
        assertFalse(listener.isCalled());
        assertTrue(actual.isPresent());
        assertOKPacket(actual.get());
    }
    
    @Test
    public void assertExecuteTCLWithXATransaction() throws SQLException, ReflectiveOperationException {
        setTransactionType(TransactionType.XA);
        when(payload.readStringEOF()).thenReturn("COMMIT");
        ComQueryPacket packet = new ComQueryPacket(1, 1000, payload, backendConnection, frontendHandler);
        Optional<CommandResponsePackets> actual = packet.execute();
        assertTrue(listener.isCalled());
        assertTrue(actual.isPresent());
        assertOKPacket(actual.get());
    }
    
    @Test
    public void assertExecuteRollbackWithXATransaction() throws SQLException, ReflectiveOperationException {
        setTransactionType(TransactionType.XA);
        listener.setExpected(TransactionOperationType.ROLLBACK);
        when(payload.readStringEOF()).thenReturn("ROLLBACK");
        ComQueryPacket packet = new ComQueryPacket(1, 1000, payload, backendConnection, frontendHandler);
        Optional<CommandResponsePackets> actual = packet.execute();
        assertFalse(listener.isCalled());
        assertTrue(actual.isPresent());
        assertOKPacket(actual.get());
    }
    
    private void assertOKPacket(final CommandResponsePackets actual) {
        assertThat(actual.getPackets().size(), is(1));
        assertThat((actual.getPackets().iterator().next()).getSequenceId(), is(1));
        assertThat(actual.getPackets().iterator().next(), CoreMatchers.<DatabasePacket>instanceOf(OKPacket.class));
    }
    
    private final class Listener {
        
        @Setter
        private TransactionOperationType expected;
        
        @Getter
        private boolean called;
        
        @Subscribe
        public void listen(final XATransactionEvent event) {
            assertThat(event.getOperationType(), is(expected)); 
            called = true;
        }
    }
}
