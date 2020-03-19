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

package org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.response;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ErrorPacketTest {
    
    @Test
    public void assertFromByteBuf() {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeByte(0x80);
        byteBuf.writeShortLE(Short.MIN_VALUE);
        byteBuf.writeByte(0x80);
        byteBuf.writeBytes(new byte[5]);
        byteBuf.writeBytes("test message".getBytes());
        ErrorPacket actual = new ErrorPacket();
        actual.fromByteBuf(byteBuf);
        assertThat(actual.getFieldCount(), is((short) 128));
        assertThat(actual.getErrorNumber(), is(32768));
        assertThat(actual.getSqlStateMarker(), is((short) 128));
        assertThat(actual.getSqlState(), is(new byte[5]));
        assertThat(actual.getMessage(), is("test message"));
    }
}
