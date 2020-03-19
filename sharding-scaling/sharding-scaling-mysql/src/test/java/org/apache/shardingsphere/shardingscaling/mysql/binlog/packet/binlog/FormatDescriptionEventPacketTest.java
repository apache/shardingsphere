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

package org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.binlog;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class FormatDescriptionEventPacketTest {
    
    @Test
    public void assertParseWithoutChecksum() {
        int eventLength = 2 + 50 + 4 + 1 + (EventTypes.FORMAT_DESCRIPTION_EVENT - 1) + 1 + 23;
        ByteBuf byteBuf = Unpooled.buffer(eventLength);
        writeInfoIntoBuffer(byteBuf, eventLength);
        FormatDescriptionEventPacket actual = new FormatDescriptionEventPacket();
        actual.parse(byteBuf);
        assertThat(actual.getBinlogVersion(), is(4));
        String expectedServerVersion = "5.7.14-log" + new String(new byte[40]);
        assertThat(actual.getMysqlServerVersion(), is(expectedServerVersion));
        assertThat(actual.getCreateTimestamp(), is(0x80000000L));
        assertThat(actual.getEventHeaderLength(), is((short) 19));
        assertThat(actual.getChecksumType(), is((short) 0));
        assertThat(actual.getChecksumLength(), is(0));
    }
    
    @Test
    public void assertParseWithChecksum() {
        int eventLength = 2 + 50 + 4 + 1 + (EventTypes.FORMAT_DESCRIPTION_EVENT - 1) + 1 + 23;
        ByteBuf byteBuf = Unpooled.buffer(eventLength + 5);
        writeInfoIntoBuffer(byteBuf, eventLength);
        byteBuf.writeByte(1);
        byteBuf.writeBytes(new byte[4]);
        FormatDescriptionEventPacket actual = new FormatDescriptionEventPacket();
        actual.parse(byteBuf);
        assertThat(actual.getBinlogVersion(), is(4));
        String expectedServerVersion = "5.7.14-log" + new String(new byte[40]);
        assertThat(actual.getMysqlServerVersion(), is(expectedServerVersion));
        assertThat(actual.getCreateTimestamp(), is(0x80000000L));
        assertThat(actual.getEventHeaderLength(), is((short) 19));
        assertThat(actual.getChecksumType(), is((short) 1));
        assertThat(actual.getChecksumLength(), is(4));
    }
    
    private void writeInfoIntoBuffer(final ByteBuf byteBuf, final int eventLength) {
        byteBuf.writeShortLE(4);
        byteBuf.writeBytes(createServerVersionBytes());
        byteBuf.writeIntLE(Integer.MIN_VALUE);
        byteBuf.writeByte(19);
        byteBuf.writeBytes(new byte[EventTypes.FORMAT_DESCRIPTION_EVENT - 1]);
        byteBuf.writeByte(eventLength);
        byteBuf.writeBytes(new byte[23]);
    }
    
    private byte[] createServerVersionBytes() {
        byte[] result = new byte[50];
        byte[] serverVersionString = "5.7.14-log".getBytes();
        System.arraycopy(serverVersionString, 0, result, 0, serverVersionString.length);
        return result;
    }
}
