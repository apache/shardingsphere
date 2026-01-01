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
import org.apache.shardingsphere.database.protocol.postgresql.packet.ByteBufTestUtils;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.apache.shardingsphere.infra.exception.external.sql.type.wrapper.SQLWrapperException;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.postgresql.util.PGobject;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockConstruction;

class PostgreSQLTextArrayBinaryProtocolValueTest {
    
    @Test
    void assertGetColumnLength() {
        assertThrows(UnsupportedSQLOperationException.class, () -> new PostgreSQLTextArrayBinaryProtocolValue().getColumnLength(new PostgreSQLPacketPayload(null, StandardCharsets.UTF_8), "val"));
    }
    
    @Test
    void assertReadEmptyArray() throws SQLException {
        PGobject expected = new PGobject();
        expected.setType("text[]");
        expected.setValue("{}");
        assertThat(new PostgreSQLTextArrayBinaryProtocolValue().read(new PostgreSQLPacketPayload(null, StandardCharsets.UTF_8), 20), is(expected));
    }
    
    @Test
    void assertReadWithElements() throws SQLException {
        int parameterValueLength = 30;
        ByteBuf byteBuf = ByteBufTestUtils.createByteBuf(parameterValueLength, parameterValueLength + 10);
        byteBuf.writeZero(20);
        byteBuf.writeInt(1);
        byteBuf.writeCharSequence("a", StandardCharsets.UTF_8);
        byteBuf.writeInt(1);
        byteBuf.writeCharSequence("b", StandardCharsets.UTF_8);
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
        PGobject expected = new PGobject();
        expected.setType("text[]");
        expected.setValue("{\"a\",\"b\"}");
        assertThat(new PostgreSQLTextArrayBinaryProtocolValue().read(payload, parameterValueLength), is(expected));
        assertThat(byteBuf.readerIndex(), is(parameterValueLength));
    }
    
    @Test
    void assertReadWithIncompleteElement() throws SQLException {
        int parameterValueLength = 29;
        ByteBuf byteBuf = ByteBufTestUtils.createByteBuf(parameterValueLength, parameterValueLength + 10);
        byteBuf.writeZero(20);
        byteBuf.writeInt(1);
        byteBuf.writeCharSequence("a", StandardCharsets.UTF_8);
        byteBuf.writeInt(5);
        PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(byteBuf, StandardCharsets.UTF_8);
        Object actual = new PostgreSQLTextArrayBinaryProtocolValue().read(payload, parameterValueLength);
        PGobject expected = new PGobject();
        expected.setType("text[]");
        expected.setValue("{\"a\"}");
        assertThat(actual, is(expected));
        assertThat(byteBuf.readerIndex(), is(parameterValueLength));
    }
    
    @Test
    void assertReadWhenSetValueThrowSQLException() {
        try (MockedConstruction<PGobject> ignored = mockConstruction(PGobject.class, (mock, context) -> doThrow(new SQLException("failed")).when(mock).setValue(anyString()))) {
            PostgreSQLPacketPayload payload = new PostgreSQLPacketPayload(null, StandardCharsets.UTF_8);
            assertThrows(SQLWrapperException.class, () -> new PostgreSQLTextArrayBinaryProtocolValue().read(payload, 20));
        }
    }
    
    @Test
    void assertWrite() {
        assertThrows(UnsupportedSQLOperationException.class, () -> new PostgreSQLTextArrayBinaryProtocolValue().write(new PostgreSQLPacketPayload(null, StandardCharsets.UTF_8), "val"));
    }
}
