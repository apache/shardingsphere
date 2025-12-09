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

package org.apache.shardingsphere.sqlfederation.mysql;

import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sqlfederation.resultset.converter.SQLFederationColumnTypeConverter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class MySQLSQLFederationColumnTypeConverterTest {
    
    private final SQLFederationColumnTypeConverter converter = DatabaseTypedSPILoader.getService(SQLFederationColumnTypeConverter.class, TypedSPILoader.getService(DatabaseType.class, "MySQL"));
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("convertValueSource")
    void assertConvertColumnValue(final String name, final Object input, final Object expected) {
        if (null == expected) {
            assertNull(converter.convertColumnValue(input));
            return;
        }
        assertThat(converter.convertColumnValue(input), is(expected));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("convertTypeSource")
    void assertConvertColumnType(final String name, final SqlTypeName sqlTypeName, final int expected) {
        assertThat(converter.convertColumnType(sqlTypeName), is(expected));
    }
    
    private static Iterable<Arguments> convertValueSource() {
        return Arrays.asList(
                arguments("booleanTrueConvertedToOne", Boolean.TRUE, 1),
                arguments("booleanFalseConvertedToZero", Boolean.FALSE, 0),
                arguments("nonBooleanValueUntouched", "text", "text"),
                arguments("nullRemainsNull", null, null)
        );
    }
    
    private static Iterable<Arguments> convertTypeSource() {
        return Arrays.asList(
                arguments("booleanMapsToVarchar", SqlTypeName.BOOLEAN, SqlTypeName.VARCHAR.getJdbcOrdinal()),
                arguments("anyMapsToVarchar", SqlTypeName.ANY, SqlTypeName.VARCHAR.getJdbcOrdinal()),
                arguments("otherTypesUnchanged", SqlTypeName.INTEGER, SqlTypeName.INTEGER.getJdbcOrdinal())
        );
    }
}
