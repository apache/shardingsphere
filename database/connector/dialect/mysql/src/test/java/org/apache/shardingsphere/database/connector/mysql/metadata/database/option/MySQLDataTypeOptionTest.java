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

package org.apache.shardingsphere.database.connector.mysql.metadata.database.option;

import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.datatype.DialectDataTypeOption;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigInteger;
import java.sql.Types;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MySQLDataTypeOptionTest {
    
    private final DialectDataTypeOption dataTypeOption = new MySQLDataTypeOption();
    
    @Test
    void assertGetExtraDataTypes() {
        Map<String, Integer> extraDataTypes = dataTypeOption.getExtraDataTypes();
        assertThat(extraDataTypes.size(), is(10));
        assertThat(extraDataTypes.get("JSON"), is(Types.LONGVARCHAR));
        assertThat(extraDataTypes.get("geometry"), is(Types.BINARY));
        assertThat(extraDataTypes.get("YEAR"), is(Types.DATE));
    }
    
    @Test
    void assertFindExtraSQLTypeClass() {
        Optional<Class<?>> tinyintActual = dataTypeOption.findExtraSQLTypeClass(Types.TINYINT, false);
        assertTrue(tinyintActual.isPresent());
        assertThat(tinyintActual.get(), is(Integer.class));
        Optional<Class<?>> smallintActual = dataTypeOption.findExtraSQLTypeClass(Types.SMALLINT, false);
        assertTrue(smallintActual.isPresent());
        assertThat(smallintActual.get(), is(Integer.class));
        Optional<Class<?>> integerUnsignedActual = dataTypeOption.findExtraSQLTypeClass(Types.INTEGER, true);
        assertTrue(integerUnsignedActual.isPresent());
        assertThat(integerUnsignedActual.get(), is(Long.class));
        Optional<Class<?>> integerSignedActual = dataTypeOption.findExtraSQLTypeClass(Types.INTEGER, false);
        assertTrue(integerSignedActual.isPresent());
        assertThat(integerSignedActual.get(), is(Integer.class));
        Optional<Class<?>> bigintUnsignedActual = dataTypeOption.findExtraSQLTypeClass(Types.BIGINT, true);
        assertTrue(bigintUnsignedActual.isPresent());
        assertThat(bigintUnsignedActual.get(), is(BigInteger.class));
        Optional<Class<?>> bigintSignedActual = dataTypeOption.findExtraSQLTypeClass(Types.BIGINT, false);
        assertTrue(bigintSignedActual.isPresent());
        assertThat(bigintSignedActual.get(), is(Long.class));
        assertFalse(dataTypeOption.findExtraSQLTypeClass(Types.VARCHAR, false).isPresent());
    }
    
    @Test
    void assertIsIntegerDataType() {
        assertTrue(dataTypeOption.isIntegerDataType(Types.INTEGER));
        assertFalse(dataTypeOption.isIntegerDataType(Types.VARCHAR));
    }
    
    @Test
    void assertIsStringDataType() {
        assertTrue(dataTypeOption.isStringDataType(Types.VARCHAR));
        assertFalse(dataTypeOption.isStringDataType(Types.INTEGER));
    }
    
    @Test
    void assertIsBinaryDataType() {
        assertTrue(dataTypeOption.isBinaryDataType(Types.BINARY, null));
        assertFalse(dataTypeOption.isBinaryDataType(Types.INTEGER, null));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("datetimeArguments")
    void assertIsDatetimeDataType(final String name, final String storageType, final int sqlType, final String dataTypeName, final boolean expected) {
        DatabaseType databaseType = mock(DatabaseType.class);
        when(databaseType.getType()).thenReturn(storageType);
        assertTrue(dataTypeOption.isDatetimeDataType(sqlType, dataTypeName, databaseType) == expected);
    }
    
    private static Stream<Arguments> datetimeArguments() {
        return Stream.of(
                Arguments.of("MySQL DATETIME", "MySQL", Types.TIMESTAMP, "DATETIME", true),
                Arguments.of("MySQL lowercase datetime", "MySQL", Types.TIMESTAMP, "datetime", true),
                Arguments.of("MySQL TIMESTAMP", "MySQL", Types.TIMESTAMP, "TIMESTAMP", false),
                Arguments.of("different JDBC type", "MySQL", Types.DATE, "DATETIME", false),
                Arguments.of("missing native type", "MySQL", Types.TIMESTAMP, null, false),
                Arguments.of("empty native type", "MySQL", Types.TIMESTAMP, "", false),
                Arguments.of("other storage DATETIME", "SQLServer", Types.TIMESTAMP, "DATETIME", false),
                Arguments.of("MariaDB delegated option", "MariaDB", Types.TIMESTAMP, "DATETIME", false),
                Arguments.of("H2 MySQL trunk", "H2", Types.TIMESTAMP, "DATETIME", false));
    }
}
