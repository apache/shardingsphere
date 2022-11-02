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

package org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.bind;

import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLValueFormat;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.PostgreSQLColumnType;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class PostgreSQLComBindPacketTest {
    
    private static final byte[] BIND_MESSAGE_BYTES = {
            0x00, 0x00, 0x00, 0x19, 0x00, 0x53, 0x5f,
            0x31, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x01,
            0x00, 0x00, 0x00, 0x04, 0x00, 0x00, 0x00, 0x0a,
            0x00, 0x00,
    };
    
    @Test
    public void assertConstructPostgreSQLComBindPacket() {
        PostgreSQLComBindPacket actual = new PostgreSQLComBindPacket(new PostgreSQLPacketPayload(Unpooled.wrappedBuffer(BIND_MESSAGE_BYTES), StandardCharsets.UTF_8));
        assertThat(actual.getPortal(), is(""));
        assertThat(actual.getStatementId(), is("S_1"));
        List<Object> actualParameters = actual.readParameters(Collections.singletonList(PostgreSQLColumnType.POSTGRESQL_TYPE_INT4));
        assertThat(actualParameters, is(Collections.singletonList(10)));
        List<PostgreSQLValueFormat> actualResultFormats = actual.readResultFormats();
        assertTrue(actualResultFormats.isEmpty());
    }
    
    @Test
    public void getMessageType() {
        PostgreSQLComBindPacket bindPacket = new PostgreSQLComBindPacket(mock(PostgreSQLPacketPayload.class));
        assertThat(bindPacket.getIdentifier(), is(PostgreSQLCommandPacketType.BIND_COMMAND));
    }
}
