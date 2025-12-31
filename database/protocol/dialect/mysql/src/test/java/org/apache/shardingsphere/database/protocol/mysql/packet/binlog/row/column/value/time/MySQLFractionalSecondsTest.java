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

package org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.time;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class MySQLFractionalSecondsTest {
    
    @ParameterizedTest(name = "[{index}] precision={0}, expectedNanos={2}")
    @MethodSource("fractionalSecondsArguments")
    void assertNanosWithVariousPrecision(final int precision, final ByteBuf buf, final int expectedNanos, final int expectedRemaining) {
        assertThat(new MySQLFractionalSeconds(precision, new MySQLPacketPayload(buf, StandardCharsets.UTF_8)).getNanos(), is(expectedNanos));
        assertThat(buf.readableBytes(), is(expectedRemaining));
    }
    
    private static Stream<Arguments> fractionalSecondsArguments() {
        return Stream.of(
                Arguments.of(1, writeByte(10), 100000000, 0),
                Arguments.of(3, writeShort(2), 200000, 0),
                Arguments.of(5, writeMedium(3), 3000, 0),
                Arguments.of(0, writeByte(1), 0, 1));
    }
    
    private static ByteBuf writeByte(final int value) {
        ByteBuf result = Unpooled.buffer();
        result.writeByte(value);
        return result;
    }
    
    private static ByteBuf writeShort(final int value) {
        ByteBuf result = Unpooled.buffer();
        result.writeShort(value);
        return result;
    }
    
    private static ByteBuf writeMedium(final int value) {
        ByteBuf result = Unpooled.buffer();
        result.writeMedium(value);
        return result;
    }
}
