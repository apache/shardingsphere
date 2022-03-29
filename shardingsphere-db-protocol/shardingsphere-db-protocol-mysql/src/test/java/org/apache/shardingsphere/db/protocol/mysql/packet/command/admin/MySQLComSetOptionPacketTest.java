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

package org.apache.shardingsphere.db.protocol.mysql.packet.command.admin;

import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class MySQLComSetOptionPacketTest {
    
    @Test
    public void assertMultiStatementsOn() {
        MySQLComSetOptionPacket actual = new MySQLComSetOptionPacket(new MySQLPacketPayload(Unpooled.wrappedBuffer(new byte[]{0x00, 0x00}), StandardCharsets.UTF_8));
        assertThat(actual.getValue(), is(MySQLComSetOptionPacket.MYSQL_OPTION_MULTI_STATEMENTS_ON));
    }
    
    @Test
    public void assertMultiStatementsOff() {
        MySQLComSetOptionPacket actual = new MySQLComSetOptionPacket(new MySQLPacketPayload(Unpooled.wrappedBuffer(new byte[]{0x01, 0x00}), StandardCharsets.UTF_8));
        assertThat(actual.getValue(), is(MySQLComSetOptionPacket.MYSQL_OPTION_MULTI_STATEMENTS_OFF));
    }
}
