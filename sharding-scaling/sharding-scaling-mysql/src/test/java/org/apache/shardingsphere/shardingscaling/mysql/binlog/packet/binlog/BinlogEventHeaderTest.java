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
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class BinlogEventHeaderTest {
    
    @Test
    public void assertFromBytes() {
        byte[] eventHeaderBytes = ByteBufUtil.decodeHexDump("00010203000100000010000000040000002000");
        ByteBuf eventHeaderByteBuf = Unpooled.buffer(eventHeaderBytes.length);
        eventHeaderByteBuf.writeBytes(eventHeaderBytes);
        BinlogEventHeader actual = new BinlogEventHeader();
        actual.fromBytes(eventHeaderByteBuf);
        assertThat(actual.getTimeStamp(), is(50462976L));
        assertThat(actual.getTypeCode(), is(EventTypes.UNKNOWN_EVENT));
        assertThat(actual.getServerId(), is(1L));
        assertThat(actual.getEventLength(), is(16L));
        assertThat(actual.getEndLogPos(), is(4L));
        assertThat(actual.getFlags(), is(32));
    }
}
