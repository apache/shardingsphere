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

import io.netty.buffer.ByteBuf;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostgreSQLBoolBinaryProtocolValueTest {
    
    @Mock
    private PostgreSQLPacketPayload payload;
    
    @Mock
    private ByteBuf byteBuf;
    
    @Test
    void assertGetColumnLength() {
        PostgreSQLBoolBinaryProtocolValue actual = new PostgreSQLBoolBinaryProtocolValue();
        assertThat(actual.getColumnLength(payload, null), is(1));
    }
    
    @Test
    void assertRead() {
        PostgreSQLBoolBinaryProtocolValue actual = new PostgreSQLBoolBinaryProtocolValue();
        when(payload.getByteBuf()).thenReturn(byteBuf);
        when(byteBuf.readBoolean()).thenReturn(true);
        assertTrue((boolean) actual.read(payload, 1));
    }
    
    @Test
    void assertWrite() {
        PostgreSQLBoolBinaryProtocolValue actual = new PostgreSQLBoolBinaryProtocolValue();
        when(payload.getByteBuf()).thenReturn(byteBuf);
        actual.write(payload, "f");
        verify(payload.getByteBuf()).writeBoolean(false);
    }
}
