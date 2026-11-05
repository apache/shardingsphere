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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.transaction;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.exception.core.exception.protocol.DatabaseProtocolException;
import org.apache.shardingsphere.database.protocol.firebird.constant.buffer.type.FirebirdTransactionParameterBufferType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TransactionIsolationLevel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

class FirebirdStartTransactionPacketTest {
    
    @Test
    void assertEmptyTransactionParameterBuffer() {
        FirebirdStartTransactionPacket actual = new FirebirdStartTransactionPacket(createPayloadWithBytes(new byte[0]));
        assertThat(actual.getRawTpb(), is(new byte[0]));
        assertFalse(actual.isAutoCommit());
        assertFalse(actual.isReadOnly());
    }
    
    @Test
    void assertSnapshotNumberAndSizedOptions() {
        byte[] raw = {3, 12, 2, 1, 2, 13, 1, 3, 10, 1, 'T', 25, 1, 'S', 23, 8, 0x78, 0x56, 0x34, 0x12, (byte) 0xEF, (byte) 0xCD, (byte) 0xAB, 1, 8};
        FirebirdStartTransactionPacket actual = new FirebirdStartTransactionPacket(createPayloadWithBytes(raw));
        assertThat(actual.getRawTpb(), is(raw));
        assertThat(actual.getTpb().<Long>getValue(FirebirdTransactionParameterBufferType.AT_SNAPSHOT_NUMBER), is(0x01ABCDEF12345678L));
        assertThat(actual.getTpb().<String>getValue(FirebirdTransactionParameterBufferType.LOCK_TABLE_SCHEMA), is("S"));
        assertTrue(actual.isReadOnly());
    }
    
    @Test
    void assertMalformedTpbIsPreservedUntilParsed() {
        byte[] raw = {3, 21, 2, 1};
        FirebirdStartTransactionPacket actual = new FirebirdStartTransactionPacket(createPayloadWithBytes(raw));
        assertThat(actual.getRawTpb(), is(raw));
        assertThrows(IndexOutOfBoundsException.class, actual::getTpb);
    }
    
    @Test
    void assertSnapshotNumberRejectsMoreThanEightBytes() {
        byte[] raw = {3, 23, 9, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        FirebirdStartTransactionPacket actual = new FirebirdStartTransactionPacket(createPayloadWithBytes(raw));
        assertThrows(DatabaseProtocolException.class, actual::getTpb);
    }
    
    @ParameterizedTest
    @ValueSource(ints = {1, 2, 4})
    void assertVariableLengthTransactionParameters(final int numberLength) {
        ByteBuf source = Unpooled.buffer();
        source.writeByte(3).writeByte(FirebirdTransactionParameterBufferType.READ_COMMITTED.getCode())
                .writeByte(FirebirdTransactionParameterBufferType.LOCK_TIMEOUT.getCode()).writeByte(numberLength).writeByte(1).writeZero(numberLength - 1)
                .writeByte(FirebirdTransactionParameterBufferType.LOCK_READ.getCode()).writeByte(3).writeBytes(new byte[]{'T', '_', '1'})
                .writeByte(FirebirdTransactionParameterBufferType.SHARED.getCode()).writeByte(FirebirdTransactionParameterBufferType.AUTOCOMMIT.getCode())
                .writeByte(FirebirdTransactionParameterBufferType.AUTO_RELEASE_TEMP_BLOBID.getCode());
        byte[] expected = new byte[source.readableBytes()];
        source.readBytes(expected);
        source.release();
        FirebirdStartTransactionPacket actual = new FirebirdStartTransactionPacket(createPayloadWithBytes(expected));
        assertThat(actual.getRawTpb(), is(expected));
        assertThat(actual.getTpb().<Integer>getValue(FirebirdTransactionParameterBufferType.LOCK_TIMEOUT), is(1));
        assertThat(actual.getTpb().<String>getValue(FirebirdTransactionParameterBufferType.LOCK_READ), is("T_1"));
        assertTrue(actual.isAutoCommit());
        assertThat(actual.getIsolationLevel(), is(TransactionIsolationLevel.READ_COMMITTED));
    }
    
    @Test
    void assertIsAutoCommit() {
        assertTrue(new FirebirdStartTransactionPacket(createPayload(1, FirebirdTransactionParameterBufferType.AUTOCOMMIT)).isAutoCommit());
        assertFalse(new FirebirdStartTransactionPacket(createPayload(1, FirebirdTransactionParameterBufferType.READ)).isAutoCommit());
    }
    
    @Test
    void assertIsReadOnly() {
        assertTrue(new FirebirdStartTransactionPacket(createPayload(1, FirebirdTransactionParameterBufferType.READ)).isReadOnly());
        assertFalse(new FirebirdStartTransactionPacket(createPayload(1, FirebirdTransactionParameterBufferType.AUTOCOMMIT)).isReadOnly());
    }
    
    @Test
    void assertIsolationLevelReadCommitted() {
        assertThat(new FirebirdStartTransactionPacket(createPayload(1, FirebirdTransactionParameterBufferType.READ_COMMITTED)).getIsolationLevel(), is(TransactionIsolationLevel.READ_COMMITTED));
    }
    
    @Test
    void assertIsolationLevelRepeatableRead() {
        assertThat(new FirebirdStartTransactionPacket(createPayload(1, FirebirdTransactionParameterBufferType.CONCURRENCY)).getIsolationLevel(), is(TransactionIsolationLevel.REPEATABLE_READ));
    }
    
    @Test
    void assertIsolationLevelSerializable() {
        assertThat(new FirebirdStartTransactionPacket(createPayload(1, FirebirdTransactionParameterBufferType.CONSISTENCY)).getIsolationLevel(), is(TransactionIsolationLevel.SERIALIZABLE));
    }
    
    @Test
    void assertIsolationLevelDefaultsToRepeatableRead() {
        assertThat(new FirebirdStartTransactionPacket(createPayload(1)).getIsolationLevel(), is(TransactionIsolationLevel.REPEATABLE_READ));
    }
    
    @Test
    void assertWrite() {
        FirebirdPacketPayload payload = mock(FirebirdPacketPayload.class, RETURNS_DEEP_STUBS);
        assertDoesNotThrow(() -> new FirebirdStartTransactionPacket(payload).write(payload));
    }
    
    @Test
    void assertGetLength() {
        assertThat(FirebirdStartTransactionPacket.getLength(createPayload(1)), is(16));
    }
    
    private FirebirdPacketPayload createPayload(final int handle, final FirebirdTransactionParameterBufferType... types) {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeInt(0);
        byteBuf.writeInt(handle);
        ByteBuf tpbBuffer = Unpooled.buffer();
        tpbBuffer.writeByte(1);
        for (FirebirdTransactionParameterBufferType each : types) {
            tpbBuffer.writeByte(each.getCode());
        }
        int tpbLength = tpbBuffer.readableBytes();
        byteBuf.writeInt(tpbLength);
        byteBuf.writeBytes(tpbBuffer);
        byteBuf.writeZero((4 - tpbLength) & 3);
        return new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8);
    }
    
    private FirebirdPacketPayload createPayloadWithBytes(final byte[] bytes) {
        ByteBuf result = Unpooled.buffer().writeInt(0).writeInt(1).writeInt(bytes.length).writeBytes(bytes).writeZero((4 - bytes.length) & 3);
        return new FirebirdPacketPayload(result, StandardCharsets.UTF_8);
    }
}
