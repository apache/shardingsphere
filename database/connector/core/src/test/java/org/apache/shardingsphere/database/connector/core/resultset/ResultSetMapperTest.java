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

package org.apache.shardingsphere.database.connector.core.resultset;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(DatabaseTypedSPILoader.class)
class ResultSetMapperTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Mock
    private ResultSet resultSet;
    
    @Mock
    private ResultSetMetaData metaData;
    
    @BeforeEach
    void setUp() throws SQLException {
        when(resultSet.getMetaData()).thenReturn(metaData);
    }
    
    @SuppressWarnings("DataFlowIssue")
    @Test
    void assertLoadBooleanValue() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.BOOLEAN);
        when(resultSet.getBoolean(1)).thenReturn(true);
        assertTrue((Boolean) new ResultSetMapper(databaseType).load(resultSet, 1));
    }
    
    @Test
    void assertLoadSignedIntegerValue() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.INTEGER);
        when(metaData.isSigned(1)).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(1);
        assertThat(new ResultSetMapper(databaseType).load(resultSet, 1), is(1));
    }
    
    @Test
    void assertLoadUnsignedIntegerValue() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.INTEGER);
        when(resultSet.getLong(1)).thenReturn(1L);
        assertThat(new ResultSetMapper(databaseType).load(resultSet, 1), is(1L));
    }
    
    @Test
    void assertLoadSignedBigIntValue() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.BIGINT);
        when(metaData.isSigned(1)).thenReturn(true);
        when(resultSet.getLong(1)).thenReturn(1L);
        assertThat(new ResultSetMapper(databaseType).load(resultSet, 1), is(1L));
    }
    
    @Test
    void assertLoadUnsignedBigIntValue() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.BIGINT);
        when(resultSet.getBigDecimal(1)).thenReturn(new BigDecimal("1"));
        assertThat(new ResultSetMapper(databaseType).load(resultSet, 1), is(new BigInteger("1")));
    }
    
    @Test
    void assertLoadUnsignedBigIntValueWithNull() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.BIGINT);
        when(resultSet.getBigDecimal(1)).thenReturn(null);
        assertNull(new ResultSetMapper(databaseType).load(resultSet, 1));
    }
    
    @Test
    void assertLoadDateValueWithoutDialectResultSetMapper() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.DATE);
        when(resultSet.getDate(1)).thenReturn(new Date(0L));
        assertThat(new ResultSetMapper(databaseType).load(resultSet, 1), is(new Date(0L)));
    }
    
    @Test
    void assertLoadDateValueWithDialectResultSetMapper() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.DATE);
        DialectResultSetMapper dialectResultSetMapper = mock(DialectResultSetMapper.class);
        when(DatabaseTypedSPILoader.findService(DialectResultSetMapper.class, databaseType)).thenReturn(Optional.of(dialectResultSetMapper));
        when(dialectResultSetMapper.getDateValue(resultSet, 1)).thenReturn(new Date(0L));
        assertThat(new ResultSetMapper(databaseType).load(resultSet, 1), is(new Date(0L)));
    }
    
    @Test
    void assertLoadObjectValueWithDialectResultSetMapper() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.JAVA_OBJECT);
        DialectResultSetMapper dialectResultSetMapper = mock(DialectResultSetMapper.class);
        Object object = new Object();
        when(DatabaseTypedSPILoader.findService(DialectResultSetMapper.class, databaseType)).thenReturn(Optional.of(dialectResultSetMapper));
        when(dialectResultSetMapper.getDefaultValue(resultSet, 1, Types.JAVA_OBJECT)).thenReturn(object);
        assertThat(new ResultSetMapper(databaseType).load(resultSet, 1), is(object));
    }
    
    @Test
    void assertLoadTimeValue() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.TIME);
        when(resultSet.getTime(1)).thenReturn(new Time(0L));
        assertThat(new ResultSetMapper(databaseType).load(resultSet, 1), is(new Time(0L)));
    }
    
    @Test
    void assertLoadTimestampValue() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.TIMESTAMP);
        when(resultSet.getTimestamp(1)).thenReturn(new Timestamp(0L));
        assertThat(new ResultSetMapper(databaseType).load(resultSet, 1), is(new Timestamp(0L)));
    }
    
    @Test
    void assertLoadBlobValue() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.BLOB);
        Blob blob = mock(Blob.class);
        when(resultSet.getBlob(1)).thenReturn(blob);
        assertThat(new ResultSetMapper(databaseType).load(resultSet, 1), is(blob));
    }
    
    @Test
    void assertLoadClobValue() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.CLOB);
        Clob clob = mock(Clob.class);
        when(resultSet.getClob(1)).thenReturn(clob);
        assertThat(new ResultSetMapper(databaseType).load(resultSet, 1), is(clob));
    }
    
    @Test
    void assertLoadArrayValue() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.ARRAY);
        Array array = mock(Array.class);
        when(resultSet.getArray(1)).thenReturn(array);
        assertThat(new ResultSetMapper(databaseType).load(resultSet, 1), is(array));
    }
    
    @Test
    void assertLoadObjectValue() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.JAVA_OBJECT);
        Object object = new Object();
        when(resultSet.getObject(1)).thenReturn(object);
        assertThat(new ResultSetMapper(databaseType).load(resultSet, 1), is(object));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("smallintTypeArguments")
    void assertLoadSmallintValue(final String name, final int columnType, final boolean hasDialectResultSetMapper) throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(columnType);
        if (hasDialectResultSetMapper) {
            DialectResultSetMapper dialectResultSetMapper = mock(DialectResultSetMapper.class);
            when(DatabaseTypedSPILoader.findService(DialectResultSetMapper.class, databaseType)).thenReturn(Optional.of(dialectResultSetMapper));
            when(dialectResultSetMapper.getSmallintValue(resultSet, 1)).thenReturn(1);
        } else {
            when(resultSet.getInt(1)).thenReturn(1);
        }
        assertThat(new ResultSetMapper(databaseType).load(resultSet, 1), is(1));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("numericAndFloatingTypeArguments")
    void assertLoadNumericAndFloatingValue(final String name, final int columnType, final Object expected) throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(columnType);
        if (Types.FLOAT == columnType || Types.DOUBLE == columnType) {
            when(resultSet.getDouble(1)).thenReturn((Double) expected);
        } else {
            when(resultSet.getBigDecimal(1)).thenReturn((BigDecimal) expected);
        }
        assertThat(new ResultSetMapper(databaseType).load(resultSet, 1), is(expected));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("stringTypeArguments")
    void assertLoadStringValue(final String name, final int columnType) throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(columnType);
        when(resultSet.getString(1)).thenReturn("foo");
        assertThat(new ResultSetMapper(databaseType).load(resultSet, 1), is("foo"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("binaryTypeArguments")
    void assertLoadBinaryValue(final String name, final int columnType) throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(columnType);
        byte[] bytes = new byte[0];
        when(resultSet.getBytes(1)).thenReturn(bytes);
        assertThat(new ResultSetMapper(databaseType).load(resultSet, 1), is(bytes));
    }
    
    private static Stream<Arguments> smallintTypeArguments() {
        return Stream.of(
                Arguments.of("tinyint with dialect result set mapper", Types.TINYINT, true),
                Arguments.of("tinyint without dialect result set mapper", Types.TINYINT, false),
                Arguments.of("smallint with dialect result set mapper", Types.SMALLINT, true),
                Arguments.of("smallint without dialect result set mapper", Types.SMALLINT, false));
    }
    
    private static Stream<Arguments> numericAndFloatingTypeArguments() {
        return Stream.of(
                Arguments.of("numeric", Types.NUMERIC, new BigDecimal("1")),
                Arguments.of("decimal", Types.DECIMAL, new BigDecimal("1")),
                Arguments.of("float", Types.FLOAT, 1D),
                Arguments.of("double", Types.DOUBLE, 1D));
    }
    
    private static Stream<Arguments> stringTypeArguments() {
        return Stream.of(
                Arguments.of("char", Types.CHAR),
                Arguments.of("varchar", Types.VARCHAR),
                Arguments.of("longvarchar", Types.LONGVARCHAR));
    }
    
    private static Stream<Arguments> binaryTypeArguments() {
        return Stream.of(
                Arguments.of("binary", Types.BINARY),
                Arguments.of("varbinary", Types.VARBINARY),
                Arguments.of("longvarbinary", Types.LONGVARBINARY));
    }
}
