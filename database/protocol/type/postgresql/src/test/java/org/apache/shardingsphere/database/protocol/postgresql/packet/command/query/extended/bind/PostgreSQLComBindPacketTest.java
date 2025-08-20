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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind;

import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.PostgreSQLColumnType;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PostgreSQLComBindPacketTest {
    
    private static final byte[] BIND_MESSAGE_BYTES = {
            0x00, 0x00, 0x00, 0x19, 0x00, 0x53, 0x5f,
            0x31, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x01,
            0x00, 0x00, 0x00, 0x04, 0x00, 0x00, 0x00, 0x0a,
            0x00, 0x00
    };
    
    @Test
    void assertConstructPostgreSQLComBindPacket() {
        PostgreSQLComBindPacket actual = new PostgreSQLComBindPacket(new PostgreSQLPacketPayload(Unpooled.wrappedBuffer(BIND_MESSAGE_BYTES), StandardCharsets.UTF_8));
        assertThat(actual.getPortal(), is(""));
        assertThat(actual.getStatementId(), is("S_1"));
        assertThat(actual.readParameters(Collections.singletonList(PostgreSQLColumnType.INT4)), is(Collections.singletonList(10)));
        assertTrue(actual.readResultFormats().isEmpty());
    }
}
