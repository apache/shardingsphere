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

package io.shardingsphere.shardingproxy.transport.mysql.packet.command.admin.initdb;

import com.google.common.base.Optional;
import io.shardingsphere.core.constant.ShardingConstant;
import io.shardingsphere.shardingproxy.frontend.common.FrontendHandler;
import io.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import io.shardingsphere.shardingproxy.runtime.schema.LogicSchema;
import io.shardingsphere.shardingproxy.transport.mysql.constant.ServerErrorCode;
import io.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacketPayload;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandPacketType;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.shardingproxy.transport.mysql.packet.generic.ErrPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.generic.OKPacket;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ComInitDbPacketTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Mock
    private FrontendHandler frontendHandler;
    
    @Before
    @SneakyThrows
    public void setUp() {
        Map<String, LogicSchema> logicSchemas = Collections.singletonMap(ShardingConstant.LOGIC_SCHEMA_NAME, mock(LogicSchema.class));
        Field field = GlobalRegistry.class.getDeclaredField("logicSchemas");
        field.setAccessible(true);
        field.set(GlobalRegistry.getInstance(), logicSchemas);
    }
    
    @Test
    public void assertExecuteWithValidSchemaName() {
        when(payload.readStringEOF()).thenReturn(ShardingConstant.LOGIC_SCHEMA_NAME);
        Optional<CommandResponsePackets> actual = new ComInitDbPacket(1, payload, frontendHandler).execute();
        assertTrue(actual.isPresent());
        assertThat(actual.get().getPackets().size(), is(1));
        assertThat(actual.get().getHeadPacket().getSequenceId(), is(2));
        assertThat(((OKPacket) actual.get().getHeadPacket()).getAffectedRows(), is(0L));
        assertThat(((OKPacket) actual.get().getHeadPacket()).getLastInsertId(), is(0L));
        assertThat(((OKPacket) actual.get().getHeadPacket()).getWarnings(), is(0));
        assertThat(((OKPacket) actual.get().getHeadPacket()).getInfo(), is(""));
    }
    
    @Test
    public void assertExecuteWithInvalidSchemaName() {
        String invalidSchema = "invalid_schema";
        when(payload.readStringEOF()).thenReturn(invalidSchema);
        Optional<CommandResponsePackets> actual = new ComInitDbPacket(1, payload, frontendHandler).execute();
        assertTrue(actual.isPresent());
        assertThat(actual.get().getPackets().size(), is(1));
        assertThat(actual.get().getHeadPacket().getSequenceId(), is(2));
        assertThat(((ErrPacket) actual.get().getHeadPacket()).getErrorCode(), is(ServerErrorCode.ER_BAD_DB_ERROR.getErrorCode()));
        assertThat(((ErrPacket) actual.get().getHeadPacket()).getSqlState(), is(ServerErrorCode.ER_BAD_DB_ERROR.getSqlState()));
        assertThat(((ErrPacket) actual.get().getHeadPacket()).getErrorMessage(), is(String.format(ServerErrorCode.ER_BAD_DB_ERROR.getErrorMessage(), invalidSchema)));
    }
    
    @Test
    public void assertWrite() {
        when(payload.readStringEOF()).thenReturn(ShardingConstant.LOGIC_SCHEMA_NAME);
        ComInitDbPacket actual = new ComInitDbPacket(1, payload, frontendHandler);
        assertThat(actual.getSequenceId(), is(1));
        actual.write(payload);
        verify(payload).writeInt1(CommandPacketType.COM_INIT_DB.getValue());
        verify(payload).writeStringEOF(ShardingConstant.LOGIC_SCHEMA_NAME);
    }
}
