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

import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FirebirdBinaryProtocolValueFactoryTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getBinaryProtocolValueArguments")
    void assertGetBinaryProtocolValue(final String name, final FirebirdBinaryColumnType binaryColumnType, final Class<? extends FirebirdBinaryProtocolValue> expectedClass) {
        assertThat(FirebirdBinaryProtocolValueFactory.getBinaryProtocolValue(binaryColumnType), isA(expectedClass));
    }
    
    @Test
    void assertGetBlobBinaryProtocolValue() {
        FirebirdBinaryProtocolValue actual = FirebirdBinaryProtocolValueFactory.getBinaryProtocolValue(FirebirdBinaryColumnType.BLOB);
        assertThat(actual, isA(FirebirdBlobBinaryProtocolValue.class));
    }
    
    @Test
    void assertGetBinaryProtocolValueExThrown() {
        assertThrows(IllegalArgumentException.class, () -> FirebirdBinaryProtocolValueFactory.getBinaryProtocolValue(FirebirdBinaryColumnType.TIME_TZ_EX));
    }
    
    private static Stream<Arguments> getBinaryProtocolValueArguments() {
        return Stream.of(
                Arguments.of("varying", FirebirdBinaryColumnType.VARYING, FirebirdStringBinaryProtocolValue.class),
                Arguments.of("int128", FirebirdBinaryColumnType.INT128, FirebirdInt16BinaryProtocolValue.class),
                Arguments.of("int64", FirebirdBinaryColumnType.INT64, FirebirdInt8BinaryProtocolValue.class),
                Arguments.of("long", FirebirdBinaryColumnType.LONG, FirebirdInt4BinaryProtocolValue.class),
                Arguments.of("short", FirebirdBinaryColumnType.SHORT, FirebirdInt2BinaryProtocolValue.class),
                Arguments.of("boolean", FirebirdBinaryColumnType.BOOLEAN, FirebirdBooleanBinaryProtocolValue.class),
                Arguments.of("double", FirebirdBinaryColumnType.DOUBLE, FirebirdDoubleBinaryProtocolValue.class),
                Arguments.of("float", FirebirdBinaryColumnType.FLOAT, FirebirdFloatBinaryProtocolValue.class),
                Arguments.of("date", FirebirdBinaryColumnType.DATE, FirebirdDateBinaryProtocolValue.class),
                Arguments.of("time", FirebirdBinaryColumnType.TIME, FirebirdTimeBinaryProtocolValue.class),
                Arguments.of("timestamp", FirebirdBinaryColumnType.TIMESTAMP, FirebirdTimestampBinaryProtocolValue.class),
                Arguments.of("timestamp_tz", FirebirdBinaryColumnType.TIMESTAMP_TZ, FirebirdTimestampTZBinaryProtocolValue.class),
                Arguments.of("null", FirebirdBinaryColumnType.NULL, FirebirdNullBinaryProtocolValue.class));
    }
}
