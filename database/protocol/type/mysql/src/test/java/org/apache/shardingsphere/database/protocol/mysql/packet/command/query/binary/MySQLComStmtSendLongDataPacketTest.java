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

package org.apache.shardingsphere.database.protocol.mysql.packet.command.query.binary;

import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class MySQLComStmtSendLongDataPacketTest {
    
    @Test
    void assertNewPacket() {
        byte[] data = {0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x61, 0x62, 0x63};
        MySQLComStmtSendLongDataPacket actual = new MySQLComStmtSendLongDataPacket(new MySQLPacketPayload(Unpooled.wrappedBuffer(data), StandardCharsets.UTF_8));
        assertThat(actual.getStatementId(), is(1));
        assertThat(actual.getParamId(), is(0));
        assertThat(actual.getData(), is("abc".getBytes(StandardCharsets.UTF_8)));
    }
}
