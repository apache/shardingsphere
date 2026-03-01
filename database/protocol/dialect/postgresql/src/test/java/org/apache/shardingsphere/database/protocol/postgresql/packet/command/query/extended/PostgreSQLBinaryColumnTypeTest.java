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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended;

import org.apache.shardingsphere.database.protocol.postgresql.exception.PostgreSQLProtocolException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.Types;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PostgreSQLBinaryColumnTypeTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertValueOfJDBCTypeArguments")
    void assertValueOfJDBCType(final String name, final int jdbcType, final PostgreSQLBinaryColumnType expectedColumnType) {
        assertThat(PostgreSQLBinaryColumnType.valueOfJDBCType(jdbcType), is(expectedColumnType));
    }
    
    @Test
    void assertValueOfJDBCTypeThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> PostgreSQLBinaryColumnType.valueOfJDBCType(Types.REF_CURSOR));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertValueOfJDBCTypeWithColumnTypeNameArguments")
    void assertValueOfJDBCTypeWithColumnTypeName(final String name, final int jdbcType, final String columnTypeName, final PostgreSQLBinaryColumnType expectedColumnType) {
        assertThat(PostgreSQLBinaryColumnType.valueOfJDBCType(jdbcType, columnTypeName), is(expectedColumnType));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertIsBitArguments")
    void assertIsBit(final String name, final int jdbcType, final String columnTypeName, final boolean expected) {
        assertThat(PostgreSQLBinaryColumnType.isBit(jdbcType, columnTypeName), is(expected));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertIsBoolArguments")
    void assertIsBool(final String name, final int jdbcType, final String columnTypeName, final boolean expected) {
        assertThat(PostgreSQLBinaryColumnType.isBool(jdbcType, columnTypeName), is(expected));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertIsUUIDArguments")
    void assertIsUUID(final String name, final int jdbcType, final String columnTypeName, final boolean expected) {
        assertThat(PostgreSQLBinaryColumnType.isUUID(jdbcType, columnTypeName), is(expected));
    }
    
    @Test
    void assertValueOf() {
        assertThat(PostgreSQLBinaryColumnType.valueOf(20), is(PostgreSQLBinaryColumnType.INT8));
    }
    
    @Test
    void assertValueOfThrowsPostgreSQLProtocolException() {
        assertThrows(PostgreSQLProtocolException.class, () -> PostgreSQLBinaryColumnType.valueOf(9999));
    }
    
    private static Stream<Arguments> assertValueOfJDBCTypeArguments() {
        return Stream.of(
                Arguments.of("tinyint", Types.TINYINT, PostgreSQLBinaryColumnType.INT2),
                Arguments.of("smallint", Types.SMALLINT, PostgreSQLBinaryColumnType.INT2),
                Arguments.of("integer", Types.INTEGER, PostgreSQLBinaryColumnType.INT4),
                Arguments.of("bigint", Types.BIGINT, PostgreSQLBinaryColumnType.INT8),
                Arguments.of("numeric", Types.NUMERIC, PostgreSQLBinaryColumnType.NUMERIC),
                Arguments.of("decimal", Types.DECIMAL, PostgreSQLBinaryColumnType.NUMERIC),
                Arguments.of("real", Types.REAL, PostgreSQLBinaryColumnType.FLOAT4),
                Arguments.of("double", Types.DOUBLE, PostgreSQLBinaryColumnType.FLOAT8),
                Arguments.of("char", Types.CHAR, PostgreSQLBinaryColumnType.CHAR),
                Arguments.of("varchar", Types.VARCHAR, PostgreSQLBinaryColumnType.VARCHAR),
                Arguments.of("binary", Types.BINARY, PostgreSQLBinaryColumnType.BYTEA),
                Arguments.of("bit", Types.BIT, PostgreSQLBinaryColumnType.BIT),
                Arguments.of("date", Types.DATE, PostgreSQLBinaryColumnType.DATE),
                Arguments.of("time", Types.TIME, PostgreSQLBinaryColumnType.TIME),
                Arguments.of("timestamp", Types.TIMESTAMP, PostgreSQLBinaryColumnType.TIMESTAMP),
                Arguments.of("other", Types.OTHER, PostgreSQLBinaryColumnType.JSON),
                Arguments.of("sqlxml", Types.SQLXML, PostgreSQLBinaryColumnType.XML),
                Arguments.of("boolean", Types.BOOLEAN, PostgreSQLBinaryColumnType.BOOL),
                Arguments.of("struct", Types.STRUCT, PostgreSQLBinaryColumnType.VARCHAR),
                Arguments.of("array", Types.ARRAY, PostgreSQLBinaryColumnType.TEXT_ARRAY));
    }
    
    private static Stream<Arguments> assertValueOfJDBCTypeWithColumnTypeNameArguments() {
        return Stream.of(
                Arguments.of("bit override", Types.BIT, "bit", PostgreSQLBinaryColumnType.BIT),
                Arguments.of("bool override", Types.BIT, "bool", PostgreSQLBinaryColumnType.BOOL),
                Arguments.of("uuid override", Types.OTHER, "uuid", PostgreSQLBinaryColumnType.UUID),
                Arguments.of("fallback to jdbc type map", Types.INTEGER, "INT4", PostgreSQLBinaryColumnType.INT4));
    }
    
    private static Stream<Arguments> assertIsBitArguments() {
        return Stream.of(
                Arguments.of("bit exact", Types.BIT, "bit", true),
                Arguments.of("bit ignore case", Types.BIT, "BiT", true),
                Arguments.of("bit type with bool name", Types.BIT, "bool", false),
                Arguments.of("non bit jdbc type", Types.INTEGER, "bit", false));
    }
    
    private static Stream<Arguments> assertIsBoolArguments() {
        return Stream.of(
                Arguments.of("bool exact", Types.BIT, "bool", true),
                Arguments.of("bool ignore case", Types.BIT, "BoOl", true),
                Arguments.of("bit type with bit name", Types.BIT, "bit", false),
                Arguments.of("non bit jdbc type", Types.BOOLEAN, "bool", false));
    }
    
    private static Stream<Arguments> assertIsUUIDArguments() {
        return Stream.of(
                Arguments.of("uuid exact", Types.OTHER, "uuid", true),
                Arguments.of("uuid ignore case", Types.OTHER, "UUID", true),
                Arguments.of("other type with non uuid name", Types.OTHER, "json", false),
                Arguments.of("non other jdbc type", Types.VARCHAR, "uuid", false));
    }
}
