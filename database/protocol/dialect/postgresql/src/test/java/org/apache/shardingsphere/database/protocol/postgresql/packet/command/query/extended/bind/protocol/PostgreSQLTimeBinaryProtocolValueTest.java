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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol;

import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.util.PostgreSQLBinaryTimestampUtils;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostgreSQLTimeBinaryProtocolValueTest {
    
    @Mock
    private PostgreSQLPacketPayload payload;
    
    @Test
    void assertGetColumnLength() {
        PostgreSQLTimeBinaryProtocolValue actual = new PostgreSQLTimeBinaryProtocolValue();
        assertThat(actual.getColumnLength(payload, null), is(8));
    }
    
    @Test
    void assertRead() {
        PostgreSQLTimeBinaryProtocolValue actual = new PostgreSQLTimeBinaryProtocolValue();
        when(payload.readInt8()).thenReturn(1L);
        assertThat(actual.read(payload, 8), is(1L));
    }
    
    @Test
    void assertWrite() {
        PostgreSQLTimeBinaryProtocolValue actual = new PostgreSQLTimeBinaryProtocolValue();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        actual.write(payload, timestamp);
        verify(payload).writeInt8(PostgreSQLBinaryTimestampUtils.toPostgreSQLTime(timestamp));
    }
}
