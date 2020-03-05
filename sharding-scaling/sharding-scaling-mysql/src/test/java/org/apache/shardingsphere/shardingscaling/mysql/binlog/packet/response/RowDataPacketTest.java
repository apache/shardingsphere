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

public final class RowDataPacketTest {
    
    private static final String VALUE_1 = "value_1";
    
    private static final String VALUE_2 = "111";
    
    @Test
    public void assertFromByteBuf() {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeByte(VALUE_1.length());
        byteBuf.writeBytes(VALUE_1.getBytes());
        byteBuf.writeByte(VALUE_2.length());
        byteBuf.writeBytes(VALUE_2.getBytes());
        RowDataPacket actual = new RowDataPacket();
        actual.fromByteBuf(byteBuf);
        assertThat(actual.getColumns().size(), is(2));
        assertThat(actual.getColumns().get(0), is(VALUE_1));
        assertThat(actual.getColumns().get(1), is(VALUE_2));
    }
}
