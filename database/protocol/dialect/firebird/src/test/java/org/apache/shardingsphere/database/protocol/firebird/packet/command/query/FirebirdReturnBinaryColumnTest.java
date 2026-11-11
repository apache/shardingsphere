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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class FirebirdReturnBinaryColumnTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getTypeOnlyArguments")
    void assertNewInstanceWithSpecifiedType(final String name, final FirebirdBinaryColumnType type) {
        FirebirdReturnBinaryColumn actual = new FirebirdReturnBinaryColumn(type);
        assertThat(actual.getType(), is(type));
        assertThat(actual.getLength(), is(type.getLength()));
        assertThat(actual.getSubtype(), is(type.getSubtype()));
        assertThat(actual.getScale(), is(0));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getTypeAndLengthArguments")
    void assertNewInstanceWithSpecifiedTypeAndLength(final String name, final FirebirdBinaryColumnType type, final int length) {
        FirebirdReturnBinaryColumn actual = new FirebirdReturnBinaryColumn(type, length);
        assertThat(actual.getType(), is(type));
        assertThat(actual.getLength(), is(length));
        assertThat(actual.getSubtype(), is(type.getSubtype()));
        assertThat(actual.getScale(), is(0));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getTypeLengthAndSubtypeArguments")
    void assertNewInstanceWithSpecifiedTypeLengthAndSubtype(final String name, final FirebirdBinaryColumnType type, final int length, final int subtype) {
        FirebirdReturnBinaryColumn actual = new FirebirdReturnBinaryColumn(type, length, subtype);
        assertThat(actual.getType(), is(type));
        assertThat(actual.getLength(), is(length));
        assertThat(actual.getSubtype(), is(subtype));
        assertThat(actual.getScale(), is(0));
    }
    
    @Test
    void assertNewInstanceWithNullType() {
        FirebirdReturnBinaryColumn actual = new FirebirdReturnBinaryColumn(null);
        assertThat(actual.getType(), is(FirebirdBinaryColumnType.LONG));
        assertThat(actual.getLength(), is(FirebirdBinaryColumnType.LONG.getLength()));
        assertThat(actual.getSubtype(), is(FirebirdBinaryColumnType.LONG.getSubtype()));
        assertThat(actual.getScale(), is(0));
    }
    
    @Test
    void assertNewInstanceWithNullTypeAndLength() {
        FirebirdReturnBinaryColumn actual = new FirebirdReturnBinaryColumn(null, 64);
        assertThat(actual.getType(), is(FirebirdBinaryColumnType.LONG));
        assertThat(actual.getLength(), is(64));
        assertThat(actual.getSubtype(), is(FirebirdBinaryColumnType.LONG.getSubtype()));
        assertThat(actual.getScale(), is(0));
    }
    
    @Test
    void assertNewInstanceWithNullTypeLengthAndSubtype() {
        FirebirdReturnBinaryColumn actual = new FirebirdReturnBinaryColumn(null, 16, 1);
        assertThat(actual.getType(), is(FirebirdBinaryColumnType.LONG));
        assertThat(actual.getLength(), is(16));
        assertThat(actual.getSubtype(), is(1));
        assertThat(actual.getScale(), is(0));
    }
    
    private static Stream<Arguments> getTypeOnlyArguments() {
        return Stream.of(
                Arguments.of("specified_short", FirebirdBinaryColumnType.SHORT),
                Arguments.of("specified_varying", FirebirdBinaryColumnType.VARYING),
                Arguments.of("specified_blob_subtype_text", FirebirdBinaryColumnType.BLOB_SUBTYPE_TEXT));
    }
    
    private static Stream<Arguments> getTypeAndLengthArguments() {
        return Stream.of(
                Arguments.of("specified_long_with_custom_length", FirebirdBinaryColumnType.LONG, 64),
                Arguments.of("specified_varying_with_custom_length", FirebirdBinaryColumnType.VARYING, 512),
                Arguments.of("specified_date_with_custom_length", FirebirdBinaryColumnType.DATE, 128));
    }
    
    private static Stream<Arguments> getTypeLengthAndSubtypeArguments() {
        return Stream.of(
                Arguments.of("specified_short_with_custom_subtype", FirebirdBinaryColumnType.SHORT, 2, 1),
                Arguments.of("specified_timestamp_with_custom_subtype", FirebirdBinaryColumnType.TIMESTAMP, 16, 2),
                Arguments.of("specified_blob_subtype_text_with_custom_subtype", FirebirdBinaryColumnType.BLOB_SUBTYPE_TEXT, 32, 0));
    }
}
