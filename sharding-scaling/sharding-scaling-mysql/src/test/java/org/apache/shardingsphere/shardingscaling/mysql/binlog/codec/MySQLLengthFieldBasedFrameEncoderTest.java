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

package org.apache.shardingsphere.shardingscaling.mysql.binlog.codec;

import org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.AbstractPacket;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import io.netty.buffer.ByteBuf;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLLengthFieldBasedFrameEncoderTest {
    
    @Mock
    private AbstractPacket packet;
    
    @Mock
    private ByteBuf packetByteBuf;
    
    @Mock
    private ByteBuf outByteBuf;
    
    @Before
    public void setUp() {
        when(packet.toByteBuf()).thenReturn(packetByteBuf);
    }
    
    @Test
    public void assertEncode() {
        MySQLLengthFieldBasedFrameEncoder encoder = new MySQLLengthFieldBasedFrameEncoder();
        encoder.encode(null, packet, outByteBuf);
        verify(outByteBuf, times(2)).writeBytes(any(ByteBuf.class));
        verify(outByteBuf).writeBytes(packetByteBuf);
    }
}
