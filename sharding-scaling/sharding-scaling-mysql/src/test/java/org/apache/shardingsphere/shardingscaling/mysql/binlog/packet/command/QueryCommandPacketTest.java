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

package org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.command;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class QueryCommandPacketTest {
    
    @Test
    public void assertToByteBuf() {
        QueryCommandPacket queryCommandPacket = new QueryCommandPacket();
        queryCommandPacket.setQueryString("SELECT 1");
        assertThat(queryCommandPacket.toByteBuf(), is(mockExpectedByteBuf()));
    }
    
    private ByteBuf mockExpectedByteBuf() {
        ByteBuf result = ByteBufAllocator.DEFAULT.heapBuffer();
        result.writeByte(3);
        result.writeBytes("SELECT 1".getBytes());
        return result;
    }
}
