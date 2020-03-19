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

public final class RegisterSlaveCommandPacketTest {
    
    @Test
    public void assertToByteBuf() {
        RegisterSlaveCommandPacket registerSlaveCommandPacket = new RegisterSlaveCommandPacket();
        registerSlaveCommandPacket.setReportHost("localhost");
        registerSlaveCommandPacket.setReportPort((short) 3306);
        registerSlaveCommandPacket.setReportUser("root");
        registerSlaveCommandPacket.setReportPassword("");
        registerSlaveCommandPacket.setServerId(1);
        assertThat(registerSlaveCommandPacket.toByteBuf(), is(getExpectedByteBuf()));
    }
    
    private ByteBuf getExpectedByteBuf() {
        ByteBuf result = ByteBufAllocator.DEFAULT.heapBuffer();
        result.writeByte(0x15);
        result.writeIntLE(1);
        result.writeByte("localhost".getBytes().length);
        result.writeBytes("localhost".getBytes());
        result.writeByte("root".getBytes().length);
        result.writeBytes("root".getBytes());
        result.writeByte(0);
        result.writeBytes("".getBytes());
        result.writeShortLE((short) 3306);
        result.writeIntLE(0);
        result.writeIntLE(0);
        return result;
    }
}
