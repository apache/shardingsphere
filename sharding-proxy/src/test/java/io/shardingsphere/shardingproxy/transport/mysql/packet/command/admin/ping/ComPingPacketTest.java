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

package io.shardingsphere.shardingproxy.transport.mysql.packet.command.admin.ping;

import com.google.common.base.Optional;
import io.shardingsphere.shardingproxy.transport.mysql.packet.MySQLPacketPayload;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandPacketType;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.shardingproxy.transport.mysql.packet.generic.OKPacket;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class ComPingPacketTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Test
    public void assertExecute() {
        Optional<CommandResponsePackets> actual = new ComPingPacket(1).execute();
        assertTrue(actual.isPresent());
        assertThat(actual.get().getPackets().size(), is(1));
        assertThat(actual.get().getHeadPacket().getSequenceId(), is(2));
        assertThat(((OKPacket) actual.get().getHeadPacket()).getAffectedRows(), is(0L));
        assertThat(((OKPacket) actual.get().getHeadPacket()).getLastInsertId(), is(0L));
        assertThat(((OKPacket) actual.get().getHeadPacket()).getWarnings(), is(0));
        assertThat(((OKPacket) actual.get().getHeadPacket()).getInfo(), is(""));
    }
    
    @Test
    public void assertWrite() {
        ComPingPacket actual = new ComPingPacket(1);
        assertThat(actual.getSequenceId(), is(1));
        actual.write(payload);
        verify(payload).writeInt1(CommandPacketType.COM_PING.getValue());
    }
}
