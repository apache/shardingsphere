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
import org.apache.shardingsphere.db.protocol.postgresql.packet.ByteBufTestUtils;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public final class PostgreSQLInt2ArrayBinaryProtocolValueTest {
    
    private PostgreSQLBinaryProtocolValue newInstance() {
        return new PostgreSQLInt2ArrayBinaryProtocolValue();
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void assertGetColumnLength() {
        newInstance().getColumnLength("val");
    }
    
    @Test
    public void assertRead() {
        String parameterValue = "{\"11\",\"12\"}";
        int expectedLength = 4 + parameterValue.length();
        ByteBuf byteBuf = ByteBufTestUtils.createByteBuf(expectedLength);
        byteBuf.writeInt(parameterValue.length());
        byteBuf.writeCharSequence(parameterValue, StandardCharsets.ISO_8859_1);
        byteBuf.readInt();
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(byteBuf);
        Object result = newInstance().read(payload, parameterValue.length());
        assertNotNull(result);
        assertThat(result, is(new short[] {11, 12}));
        assertThat(byteBuf.readerIndex(), is(expectedLength));
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void assertWrite() {
        newInstance().write(new PostgreSQLPacketPayload(null), "val");
    }
    
}
