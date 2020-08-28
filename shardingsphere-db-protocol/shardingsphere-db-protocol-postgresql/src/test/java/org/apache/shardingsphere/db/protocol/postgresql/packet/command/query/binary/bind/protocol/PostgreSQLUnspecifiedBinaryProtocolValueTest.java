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
import java.nio.charset.StandardCharsets;
import org.apache.shardingsphere.db.protocol.postgresql.packet.ByteBufTestUtils;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.bind.PostgreSQLTypeUnspecifiedSQLParameter;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class PostgreSQLUnspecifiedBinaryProtocolValueTest {
    
    @Test(expected = UnsupportedOperationException.class)
    public void assertGetColumnLength() {
        new PostgreSQLUnspecifiedBinaryProtocolValue().getColumnLength("val");
    }
    
    @Test
    public void assertRead() {
        String timestampStr = "2020-08-23 15:57:03+08";
        int expectedLength = 4 + timestampStr.length();
        ByteBuf byteBuf = ByteBufTestUtils.createByteBuf(expectedLength);
        byteBuf.writeInt(timestampStr.length());
        byteBuf.writeCharSequence(timestampStr, StandardCharsets.ISO_8859_1);
        byteBuf.readInt();
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(byteBuf);
        Object result = new PostgreSQLUnspecifiedBinaryProtocolValue().read(payload);
        assertNotNull(result);
        assertTrue(result instanceof PostgreSQLTypeUnspecifiedSQLParameter);
        assertThat(result.toString(), is(timestampStr));
        assertThat(byteBuf.readerIndex(), is(expectedLength));
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void assertWrite() {
        new PostgreSQLUnspecifiedBinaryProtocolValue().write(mock(PostgreSQLPacketPayload.class), "val");
    }
    
}
