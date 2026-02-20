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

package org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.datatype;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.Types;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class DefaultDataTypeOptionTest {
    
    private final DialectDataTypeOption dataTypeOption = new DefaultDataTypeOption();
    
    @Test
    void assertGetExtraDataTypes() {
        assertThat(dataTypeOption.getExtraDataTypes(), is(Collections.emptyMap()));
    }
    
    @Test
    void assertFindExtraSQLTypeClass() {
        assertThat(dataTypeOption.findExtraSQLTypeClass(Types.INTEGER, true), is(Optional.empty()));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("integerDataTypeArguments")
    void assertIsIntegerDataType(final String name, final int sqlType, final boolean expected) {
        assertThat(dataTypeOption.isIntegerDataType(sqlType), is(expected));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("stringDataTypeArguments")
    void assertIsStringDataType(final String name, final int sqlType, final boolean expected) {
        assertThat(dataTypeOption.isStringDataType(sqlType), is(expected));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("binaryDataTypeArguments")
    void assertIsBinaryDataType(final String name, final int sqlType, final boolean expected) {
        assertThat(dataTypeOption.isBinaryDataType(sqlType), is(expected));
    }
    
    private static Stream<Arguments> integerDataTypeArguments() {
        return Stream.of(
                Arguments.of("integer", Types.INTEGER, true),
                Arguments.of("bigint", Types.BIGINT, true),
                Arguments.of("smallint", Types.SMALLINT, true),
                Arguments.of("tinyint", Types.TINYINT, true),
                Arguments.of("varchar", Types.VARCHAR, false));
    }
    
    private static Stream<Arguments> stringDataTypeArguments() {
        return Stream.of(
                Arguments.of("char", Types.CHAR, true),
                Arguments.of("varchar", Types.VARCHAR, true),
                Arguments.of("longvarchar", Types.LONGVARCHAR, true),
                Arguments.of("nchar", Types.NCHAR, true),
                Arguments.of("nvarchar", Types.NVARCHAR, true),
                Arguments.of("longnvarchar", Types.LONGNVARCHAR, true),
                Arguments.of("integer", Types.INTEGER, false));
    }
    
    private static Stream<Arguments> binaryDataTypeArguments() {
        return Stream.of(
                Arguments.of("binary", Types.BINARY, true),
                Arguments.of("varbinary", Types.VARBINARY, true),
                Arguments.of("longvarbinary", Types.LONGVARBINARY, true),
                Arguments.of("varchar", Types.VARCHAR, false));
    }
}
