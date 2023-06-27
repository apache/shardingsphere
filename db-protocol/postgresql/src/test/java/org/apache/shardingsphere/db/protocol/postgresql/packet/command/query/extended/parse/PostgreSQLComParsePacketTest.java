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

package org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.parse;

import org.apache.shardingsphere.db.protocol.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.PostgreSQLColumnType;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostgreSQLComParsePacketTest {
    
    @Mock
    private PostgreSQLPacketPayload payload;
    
    @Test
    void assertNewInstance() {
        when(payload.readInt2()).thenReturn(1);
        when(payload.readInt4()).thenReturn(0);
        when(payload.readStringNul()).thenReturn("sql");
        PostgreSQLComParsePacket actual = new PostgreSQLComParsePacket(payload, false);
        actual.write(payload);
        assertThat(actual.getIdentifier(), is(PostgreSQLCommandPacketType.PARSE_COMMAND));
        assertThat(actual.getSQL(), is("sql"));
        assertThat(actual.getStatementId(), is("sql"));
        List<PostgreSQLColumnType> types = actual.readParameterTypes();
        assertThat(types.size(), is(1));
        assertThat(types.get(0), is(PostgreSQLColumnType.UNSPECIFIED));
    }
}
