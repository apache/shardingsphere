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

package org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.raw.metadata;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RawResultSetMetaDataTest {
    
    @Test
    void assertGetColumnCount() {
        assertThat(createMetaData().getColumnCount(), is(1));
    }
    
    @Test
    void assertIsAutoIncrement() throws SQLException {
        assertTrue(createMetaData().isAutoIncrement(1));
    }
    
    @Test
    void assertIsCaseSensitive() throws SQLException {
        assertTrue(createMetaData().isCaseSensitive(1));
    }
    
    @Test
    void assertIsSearchable() throws SQLException {
        assertTrue(createMetaData().isSearchable(1));
    }
    
    @Test
    void assertIsCurrency() throws SQLException {
        assertFalse(createMetaData().isCurrency(1));
    }
    
    @Test
    void assertIsNullable() throws SQLException {
        assertThat(createMetaData().isNullable(1), is(ResultSetMetaData.columnNoNulls));
    }
    
    @Test
    void assertIsNullableWithNullableColumn() throws SQLException {
        assertThat(new RawResultSetMetaData(Collections.singletonList(new RawQueryResultColumnMetaData("foo_table", "foo_column", "foo_label", Types.VARCHAR, "VARCHAR", 100, 2,
                true, false, true))).isNullable(1), is(ResultSetMetaData.columnNullable));
    }
    
    @Test
    void assertIsSigned() throws SQLException {
        assertTrue(createMetaData().isSigned(1));
    }
    
    @Test
    void assertGetColumnDisplaySize() throws SQLException {
        assertThat(createMetaData().getColumnDisplaySize(1), is(100));
    }
    
    @Test
    void assertGetColumnLabel() throws SQLException {
        assertThat(createMetaData().getColumnLabel(1), is("foo_label"));
    }
    
    @Test
    void assertGetColumnName() throws SQLException {
        assertThat(createMetaData().getColumnName(1), is("foo_column"));
    }
    
    @Test
    void assertGetColumnNameWithTooSmallColumnIndex() {
        assertThrows(SQLException.class, () -> createMetaData().getColumnName(0));
    }
    
    @Test
    void assertGetColumnNameWithTooLargeColumnIndex() {
        assertThrows(SQLException.class, () -> createMetaData().getColumnName(2));
    }
    
    @Test
    void assertGetSchemaName() throws SQLException {
        assertThat(createMetaData().getSchemaName(1), is(""));
    }
    
    @Test
    void assertGetPrecision() throws SQLException {
        assertThat(createMetaData().getPrecision(1), is(100));
    }
    
    @Test
    void assertGetScale() throws SQLException {
        assertThat(createMetaData().getScale(1), is(2));
    }
    
    @Test
    void assertGetTableName() throws SQLException {
        assertThat(createMetaData().getTableName(1), is("foo_table"));
    }
    
    @Test
    void assertGetCatalogName() throws SQLException {
        assertThat(createMetaData().getCatalogName(1), is(""));
    }
    
    @Test
    void assertGetColumnType() throws SQLException {
        assertThat(createMetaData().getColumnType(1), is(Types.VARCHAR));
    }
    
    @Test
    void assertGetColumnTypeName() throws SQLException {
        assertThat(createMetaData().getColumnTypeName(1), is("VARCHAR"));
    }
    
    @Test
    void assertIsReadOnly() throws SQLException {
        assertFalse(createMetaData().isReadOnly(1));
    }
    
    @Test
    void assertIsWritable() throws SQLException {
        assertTrue(createMetaData().isWritable(1));
    }
    
    @Test
    void assertIsDefinitelyWritable() throws SQLException {
        assertFalse(createMetaData().isDefinitelyWritable(1));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getColumnClassNameArguments")
    void assertGetColumnClassName(final String name, final int columnType, final String expected) throws SQLException {
        assertThat(createMetaData(columnType).getColumnClassName(1), is(expected));
    }
    
    private static Stream<Arguments> getColumnClassNameArguments() {
        return Stream.of(
                Arguments.of("boolean", Types.BOOLEAN, Boolean.class.getName()),
                Arguments.of("integer", Types.INTEGER, Integer.class.getName()),
                Arguments.of("bigint", Types.BIGINT, Long.class.getName()),
                Arguments.of("float", Types.FLOAT, Float.class.getName()),
                Arguments.of("double", Types.DOUBLE, Double.class.getName()),
                Arguments.of("decimal", Types.DECIMAL, BigDecimal.class.getName()),
                Arguments.of("date", Types.DATE, Date.class.getName()),
                Arguments.of("time", Types.TIME, Time.class.getName()),
                Arguments.of("timestamp", Types.TIMESTAMP, Timestamp.class.getName()),
                Arguments.of("binary", Types.BINARY, byte[].class.getName()),
                Arguments.of("varchar", Types.VARCHAR, String.class.getName()),
                Arguments.of("other", Types.OTHER, Object.class.getName()));
    }
    
    @Test
    void assertUnwrap() throws SQLException {
        RawResultSetMetaData metaData = createMetaData();
        assertThat(metaData.unwrap(RawResultSetMetaData.class), is(metaData));
    }
    
    @Test
    void assertUnwrapWithUnsupportedClass() {
        assertThrows(SQLFeatureNotSupportedException.class, () -> createMetaData().unwrap(String.class));
    }
    
    @Test
    void assertIsWrapperFor() {
        assertTrue(createMetaData().isWrapperFor(RawResultSetMetaData.class));
    }
    
    @Test
    void assertIsWrapperForWithUnsupportedClass() {
        assertFalse(createMetaData().isWrapperFor(String.class));
    }
    
    private RawResultSetMetaData createMetaData() {
        return createMetaData(Types.VARCHAR);
    }
    
    private RawResultSetMetaData createMetaData(final int columnType) {
        return new RawResultSetMetaData(Collections.singletonList(new RawQueryResultColumnMetaData("foo_table", "foo_column", "foo_label", columnType, "VARCHAR", 100, 2, true, true, true)));
    }
}
