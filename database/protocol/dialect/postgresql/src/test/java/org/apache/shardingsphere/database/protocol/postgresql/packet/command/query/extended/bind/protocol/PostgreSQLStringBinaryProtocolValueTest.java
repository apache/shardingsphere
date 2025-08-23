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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostgreSQLStringBinaryProtocolValueTest {
    
    @Mock
    private ByteBuf byteBuf;
    
    private PostgreSQLPacketPayload payload;
    
    @BeforeEach
    void setup() {
        payload = new PostgreSQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
    }
    
    @Test
    void assertGetColumnLength() {
        PostgreSQLStringBinaryProtocolValue actual = new PostgreSQLStringBinaryProtocolValue();
        assertThat(actual.getColumnLength(payload, "English"), is(7));
        assertThat(actual.getColumnLength(payload, "中文"), is(6));
        assertThat(actual.getColumnLength(payload, new byte[]{1, 2, 3}), is(3));
    }
    
    @Test
    void assertRead() {
        doAnswer((Answer<ByteBuf>) invocation -> {
            ((byte[]) invocation.getArguments()[0])[0] = 'a';
            return byteBuf;
        }).when(byteBuf).readBytes(any(byte[].class));
        PostgreSQLStringBinaryProtocolValue actual = new PostgreSQLStringBinaryProtocolValue();
        assertThat(actual.read(payload, "a".length()), is("a"));
    }
    
    @Test
    void assertWrite() {
        PostgreSQLStringBinaryProtocolValue actual = new PostgreSQLStringBinaryProtocolValue();
        actual.write(payload, "foo");
        verify(byteBuf).writeBytes("foo".getBytes(StandardCharsets.UTF_8));
        actual.write(payload, new byte[1]);
        verify(byteBuf).writeBytes(new byte[1]);
    }
}
