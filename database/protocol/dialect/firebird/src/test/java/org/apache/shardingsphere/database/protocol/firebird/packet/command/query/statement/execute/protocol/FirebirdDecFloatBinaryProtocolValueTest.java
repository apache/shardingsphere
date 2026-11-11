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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.firebirdsql.extern.decimal.Decimal128;
import org.firebirdsql.extern.decimal.Decimal64;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FirebirdDecFloatBinaryProtocolValueTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertWriteArguments")
    void assertWrite(final String name, final FirebirdBinaryColumnType columnType, final BigDecimal value, final int expectedLength) {
        ByteBuf byteBuf = Unpooled.buffer();
        FirebirdPacketPayload payload = new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8);
        new FirebirdDecFloatBinaryProtocolValue(columnType).write(payload, value);
        byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);
        assertThat(actual.length, is(expectedLength));
        assertThat(actual, is(FirebirdBinaryColumnType.DEC16 == columnType ? Decimal64.valueOf(value).toBytes() : Decimal128.valueOf(value).toBytes()));
    }
    
    private static Stream<Arguments> assertWriteArguments() {
        return Stream.of(
                Arguments.of("dec16", FirebirdBinaryColumnType.DEC16, new BigDecimal("1.234567890123456"), 8),
                Arguments.of("dec34", FirebirdBinaryColumnType.DEC34, new BigDecimal("1.234567890123456789012345678901234"), 16));
    }
    
    @Test
    void assertWriteIntegerValue() {
        ByteBuf byteBuf = Unpooled.buffer();
        FirebirdPacketPayload payload = new FirebirdPacketPayload(byteBuf, StandardCharsets.UTF_8);
        new FirebirdDecFloatBinaryProtocolValue(FirebirdBinaryColumnType.DEC16).write(payload, 10);
        byte[] actual = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(actual);
        assertThat(actual, is(Decimal64.valueOf(new BigDecimal("10")).toBytes()));
    }
    
    @Test
    void assertWriteNonNumberValueThrowsException() {
        FirebirdPacketPayload payload = new FirebirdPacketPayload(Unpooled.buffer(), StandardCharsets.UTF_8);
        assertThrows(IllegalArgumentException.class, () -> new FirebirdDecFloatBinaryProtocolValue(FirebirdBinaryColumnType.DEC16).write(payload, "1.5"));
    }
    
    @Test
    void assertReadDec16() {
        BigDecimal value = new BigDecimal("1.234567890123456");
        FirebirdPacketPayload payload = new FirebirdPacketPayload(Unpooled.wrappedBuffer(Decimal64.valueOf(value).toBytes()), StandardCharsets.UTF_8);
        Decimal64 actual = (Decimal64) new FirebirdDecFloatBinaryProtocolValue(FirebirdBinaryColumnType.DEC16).read(payload);
        assertThat(actual.toBigDecimal().compareTo(value), is(0));
        assertThat(payload.getByteBuf().readableBytes(), is(0));
    }
    
    @Test
    void assertReadDec34() {
        BigDecimal value = new BigDecimal("1.234567890123456789012345678901234");
        FirebirdPacketPayload payload = new FirebirdPacketPayload(Unpooled.wrappedBuffer(Decimal128.valueOf(value).toBytes()), StandardCharsets.UTF_8);
        Decimal128 actual = (Decimal128) new FirebirdDecFloatBinaryProtocolValue(FirebirdBinaryColumnType.DEC34).read(payload);
        assertThat(actual.toBigDecimal().compareTo(value), is(0));
        assertThat(payload.getByteBuf().readableBytes(), is(0));
    }
    
    @Test
    void assertGetLength() {
        FirebirdPacketPayload payload = new FirebirdPacketPayload(Unpooled.buffer(), StandardCharsets.UTF_8);
        assertThat(new FirebirdDecFloatBinaryProtocolValue(FirebirdBinaryColumnType.DEC16).getLength(payload), is(8));
        assertThat(new FirebirdDecFloatBinaryProtocolValue(FirebirdBinaryColumnType.DEC34).getLength(payload), is(16));
    }
}
