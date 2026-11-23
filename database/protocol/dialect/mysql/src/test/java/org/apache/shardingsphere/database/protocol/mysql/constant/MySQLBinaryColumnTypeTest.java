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

package org.apache.shardingsphere.database.protocol.mysql.constant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.Types;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MySQLBinaryColumnTypeTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("jdbcTypeArguments")
    void assertValueOfJDBC(final int jdbcType, final MySQLBinaryColumnType expected) {
        assertThat(MySQLBinaryColumnType.valueOfJDBCType(jdbcType), is(expected));
    }
    
    private static Stream<Arguments> jdbcTypeArguments() {
        return Stream.of(
                Arguments.of(Types.BIT, MySQLBinaryColumnType.BIT),
                Arguments.of(Types.TINYINT, MySQLBinaryColumnType.TINY),
                Arguments.of(Types.SMALLINT, MySQLBinaryColumnType.SHORT),
                Arguments.of(Types.INTEGER, MySQLBinaryColumnType.LONG),
                Arguments.of(Types.BIGINT, MySQLBinaryColumnType.LONGLONG),
                Arguments.of(Types.FLOAT, MySQLBinaryColumnType.FLOAT),
                Arguments.of(Types.REAL, MySQLBinaryColumnType.FLOAT),
                Arguments.of(Types.DOUBLE, MySQLBinaryColumnType.DOUBLE),
                Arguments.of(Types.NUMERIC, MySQLBinaryColumnType.NEWDECIMAL),
                Arguments.of(Types.DECIMAL, MySQLBinaryColumnType.NEWDECIMAL),
                Arguments.of(Types.CHAR, MySQLBinaryColumnType.STRING),
                Arguments.of(Types.VARCHAR, MySQLBinaryColumnType.VAR_STRING),
                Arguments.of(Types.LONGVARCHAR, MySQLBinaryColumnType.VAR_STRING),
                Arguments.of(Types.DATE, MySQLBinaryColumnType.DATE),
                Arguments.of(Types.TIME, MySQLBinaryColumnType.TIME),
                Arguments.of(Types.TIMESTAMP, MySQLBinaryColumnType.TIMESTAMP),
                Arguments.of(Types.BINARY, MySQLBinaryColumnType.LONG_BLOB),
                Arguments.of(Types.VARBINARY, MySQLBinaryColumnType.TINY_BLOB),
                Arguments.of(Types.LONGVARBINARY, MySQLBinaryColumnType.BLOB),
                Arguments.of(Types.NULL, MySQLBinaryColumnType.NULL),
                Arguments.of(Types.BLOB, MySQLBinaryColumnType.BLOB),
                Arguments.of(Types.CLOB, MySQLBinaryColumnType.BLOB),
                Arguments.of(Types.NCLOB, MySQLBinaryColumnType.BLOB));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("mysqlDatetimeArguments")
    void assertValueOfJDBCTypeWithMysqlDatetime(final String name, final int jdbcType, final boolean mysqlDatetime, final MySQLBinaryColumnType expected) {
        assertThat(MySQLBinaryColumnType.valueOfJDBCType(jdbcType, mysqlDatetime), is(expected));
    }
    
    private static Stream<Arguments> mysqlDatetimeArguments() {
        return Stream.of(
                Arguments.of("mysql-datetime", Types.TIMESTAMP, true, MySQLBinaryColumnType.DATETIME),
                Arguments.of("timestamp", Types.TIMESTAMP, false, MySQLBinaryColumnType.TIMESTAMP),
                Arguments.of("non-temporal", Types.INTEGER, true, MySQLBinaryColumnType.LONG));
    }
    
    @Test
    void assertValueOfJDBCIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> MySQLBinaryColumnType.valueOfJDBCType(9999));
    }
    
    @Test
    void assertValueOf() {
        assertThat(MySQLBinaryColumnType.valueOf(MySQLBinaryColumnType.DECIMAL.getValue()), is(MySQLBinaryColumnType.DECIMAL));
    }
    
    @Test
    void assertValueOfWithIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> MySQLBinaryColumnType.valueOf(-1));
    }
}
