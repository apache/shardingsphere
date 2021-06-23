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

package org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.bind.protocol;

import io.netty.buffer.ByteBuf;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class PostgreSQLStringBinaryProtocolValueTest {
    
    @InjectMocks
    private PostgreSQLPacketPayload payload;
    
    @Mock
    private ByteBuf byteBuf;
    
    @Test
    public void assertNewInstance() {
        doAnswer((Answer<ByteBuf>) invocation -> {
            ((byte[]) invocation.getArguments()[0])[0] = 97;
            return byteBuf;
        }).when(byteBuf).readBytes(any(byte[].class));
        PostgreSQLStringBinaryProtocolValue actual = new PostgreSQLStringBinaryProtocolValue();
        assertThat(actual.getColumnLength("str"), is("str".length()));
        assertThat(actual.read(payload, "a".length()), is("a"));
        actual.write(payload, "a");
        verify(byteBuf).writeBytes("a".getBytes());
        actual.write(payload, new byte[1]);
        verify(byteBuf).writeBytes(new byte[1]);
    }
}
