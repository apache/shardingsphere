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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.close;

import org.apache.shardingsphere.database.protocol.postgresql.exception.PostgreSQLProtocolException;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.database.protocol.postgresql.packet.identifier.PostgreSQLIdentifierTag;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostgreSQLComClosePacketTest {
    
    @Mock
    private PostgreSQLPacketPayload payload;
    
    @Test
    void assertClosePreparedStatement() {
        when(payload.readInt1()).thenReturn((int) 'S');
        when(payload.readStringNul()).thenReturn("S_1");
        PostgreSQLComClosePacket actual = new PostgreSQLComClosePacket(payload);
        assertThat(actual.getType(), is(PostgreSQLComClosePacket.Type.PREPARED_STATEMENT));
        assertThat(actual.getName(), is("S_1"));
    }
    
    @Test
    void assertClosePortal() {
        when(payload.readInt1()).thenReturn((int) 'P');
        when(payload.readStringNul()).thenReturn("P_1");
        PostgreSQLComClosePacket actual = new PostgreSQLComClosePacket(payload);
        assertThat(actual.getType(), is(PostgreSQLComClosePacket.Type.PORTAL));
        assertThat(actual.getName(), is("P_1"));
    }
    
    @Test
    void assertIdentifier() {
        when(payload.readInt1()).thenReturn((int) 'S');
        PostgreSQLIdentifierTag actual = new PostgreSQLComClosePacket(payload).getIdentifier();
        assertThat(actual, is(PostgreSQLCommandPacketType.CLOSE_COMMAND));
    }
    
    @Test
    void assertInvalidType() {
        assertThrows(PostgreSQLProtocolException.class, () -> new PostgreSQLComClosePacket(payload));
    }
}
