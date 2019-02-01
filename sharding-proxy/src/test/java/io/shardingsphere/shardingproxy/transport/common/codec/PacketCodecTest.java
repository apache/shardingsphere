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

package io.shardingsphere.shardingproxy.transport.common.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.shardingsphere.shardingproxy.transport.common.codec.fixture.PacketCodecFixture;
import io.shardingsphere.shardingproxy.transport.common.packet.DatabasePacket;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.LinkedList;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PacketCodecTest {
    
    @Mock
    private ChannelHandlerContext context;
    
    @Mock
    private ByteBuf byteBuf;
    
    @Test
    public void assertDecodeWithValidHeader() {
        when(byteBuf.readableBytes()).thenReturn(1);
        new PacketCodecFixture().decode(context, byteBuf, new LinkedList<>());
    }
    
    @Test
    public void assertDecodeWithInvalidHeader() {
        when(byteBuf.readableBytes()).thenReturn(-1);
        new PacketCodecFixture().decode(context, byteBuf, new LinkedList<>());
        verify(context, times(0)).read();
    }
    
    @Test
    public void assertEncode() {
        DatabasePacket databasePacket = mock(DatabasePacket.class);
        new PacketCodecFixture().encode(context, databasePacket, byteBuf);
        verify(context).write(databasePacket);
    }
}
