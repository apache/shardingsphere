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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(DatabaseTypedSPILoader.class)
class ResultSetMapperTest {
    
    private DatabaseType databaseType;
    
    @Mock
    private ResultSet resultSet;
    
    @Mock
    private ResultSetMetaData metaData;
    
    @BeforeEach
    void setUp() throws SQLException {
        databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
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
    void assertLoadTinyIntValueWithDialectResultSetMapper() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.TINYINT);
        DialectResultSetMapper dialectResultSetMapper = mock(DialectResultSetMapper.class);
        when(DatabaseTypedSPILoader.findService(DialectResultSetMapper.class, databaseType)).thenReturn(Optional.of(dialectResultSetMapper));
        when(dialectResultSetMapper.getSmallintValue(resultSet, 1)).thenReturn(1);
        assertThat(new ResultSetMapper(databaseType).load(resultSet, 1), is(1));
    }
    
    @Test
    void assertLoadTinyIntValueWithoutDialectResultSetMapper() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.TINYINT);
        when(resultSet.getInt(1)).thenReturn(1);
        assertThat(new ResultSetMapper(databaseType).load(resultSet, 1), is(1));
    }
    
    @Test
    void assertLoadSmallIntValueWithDialectResultSetMapper() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.SMALLINT);
        DialectResultSetMapper dialectResultSetMapper = mock(DialectResultSetMapper.class);
        when(DatabaseTypedSPILoader.findService(DialectResultSetMapper.class, databaseType)).thenReturn(Optional.of(dialectResultSetMapper));
        when(dialectResultSetMapper.getSmallintValue(resultSet, 1)).thenReturn(1);
        assertThat(new ResultSetMapper(databaseType).load(resultSet, 1), is(1));
    }
    
    @Test
    void assertLoadSmallIntValueWithoutDialectResultSetMapper() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.SMALLINT);
        when(resultSet.getInt(1)).thenReturn(1);
        assertThat(new ResultSetMapper(databaseType).load(resultSet, 1), is(1));
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
    void assertLoadNumericValue() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.NUMERIC);
        when(resultSet.getBigDecimal(1)).thenReturn(new BigDecimal("1"));
        assertThat(new ResultSetMapper(databaseType).load(resultSet, 1), is(new BigDecimal("1")));
    }
    
    @Test
    void assertLoadDecimalValue() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.DECIMAL);
        when(resultSet.getBigDecimal(1)).thenReturn(new BigDecimal("1"));
        assertThat(new ResultSetMapper(databaseType).load(resultSet, 1), is(new BigDecimal("1")));
    }
    
    @Test
    void assertLoadFloatValue() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.FLOAT);
        when(resultSet.getDouble(1)).thenReturn(1D);
        assertThat(new ResultSetMapper(databaseType).load(resultSet, 1), is(1D));
    }
    
    @Test
    void assertLoadDoubleValue() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.DOUBLE);
        when(resultSet.getDouble(1)).thenReturn(1D);
        assertThat(new ResultSetMapper(databaseType).load(resultSet, 1), is(1D));
    }
    
    @Test
    void assertLoadCharValue() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.CHAR);
        when(resultSet.getString(1)).thenReturn("foo");
        assertThat(new ResultSetMapper(databaseType).load(resultSet, 1), is("foo"));
    }
    
    @Test
    void assertLoadVarcharValue() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.VARCHAR);
        when(resultSet.getString(1)).thenReturn("foo");
        assertThat(new ResultSetMapper(databaseType).load(resultSet, 1), is("foo"));
    }
    
    @Test
    void assertLoadLongVarcharValue() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.LONGVARCHAR);
        when(resultSet.getString(1)).thenReturn("foo");
        assertThat(new ResultSetMapper(databaseType).load(resultSet, 1), is("foo"));
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
    void assertLoadBinaryValue() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.BINARY);
        byte[] bytes = new byte[0];
        when(resultSet.getBytes(1)).thenReturn(bytes);
        assertThat(new ResultSetMapper(databaseType).load(resultSet, 1), is(bytes));
    }
    
    @Test
    void assertLoadVarbinaryValue() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.VARBINARY);
        byte[] bytes = new byte[0];
        when(resultSet.getBytes(1)).thenReturn(bytes);
        assertThat(new ResultSetMapper(databaseType).load(resultSet, 1), is(bytes));
    }
    
    @Test
    void assertLoadLongVarbinaryValue() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.LONGVARBINARY);
        byte[] bytes = new byte[0];
        when(resultSet.getBytes(1)).thenReturn(bytes);
        assertThat(new ResultSetMapper(databaseType).load(resultSet, 1), is(bytes));
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
}
