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

package org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.column;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(DatabaseTypedSPILoader.class)
class InventoryColumnValueReaderEngineTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private final InventoryColumnValueReaderEngine engine = new InventoryColumnValueReaderEngine(databaseType);
    
    @Mock
    private ResultSet resultSet;
    
    @Mock
    private ResultSetMetaData metaData;
    
    @Test
    void assertReadWithDialectInventoryColumnValueReader() throws SQLException {
        DialectInventoryColumnValueReader dialectReader = mock(DialectInventoryColumnValueReader.class);
        when(dialectReader.read(resultSet, metaData, 1)).thenReturn(Optional.of("foo"));
        when(DatabaseTypedSPILoader.findService(DialectInventoryColumnValueReader.class, databaseType)).thenReturn(Optional.of(dialectReader));
        assertThat(engine.read(resultSet, metaData, 1), is("foo"));
    }
    
    @Test
    void assertReadWithBooleanValue() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.BOOLEAN);
        when(resultSet.getBoolean(1)).thenReturn(true);
        assertThat(engine.read(resultSet, metaData, 1), is(true));
    }
    
    @Test
    void assertReadWithSingedTinyIntValue() throws SQLException {
        when(metaData.isSigned(1)).thenReturn(true);
        when(metaData.getColumnType(1)).thenReturn(Types.TINYINT);
        when(resultSet.getByte(1)).thenReturn((byte) 1);
        assertThat(engine.read(resultSet, metaData, 1), is((byte) 1));
    }
    
    @Test
    void assertReadWithUnSingedTinyIntValue() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.TINYINT);
        when(resultSet.getShort(1)).thenReturn((short) 1);
        assertThat(engine.read(resultSet, metaData, 1), is((short) 1));
    }
    
    @Test
    void assertReadWithSingedSmallIntValue() throws SQLException {
        when(metaData.isSigned(1)).thenReturn(true);
        when(metaData.getColumnType(1)).thenReturn(Types.SMALLINT);
        when(resultSet.getShort(1)).thenReturn((short) 1);
        assertThat(engine.read(resultSet, metaData, 1), is((short) 1));
    }
    
    @Test
    void assertReadWithUnSingedSmallIntValue() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.SMALLINT);
        when(resultSet.getInt(1)).thenReturn(1);
        assertThat(engine.read(resultSet, metaData, 1), is(1));
    }
    
    @Test
    void assertReadWithSingedIntegerValue() throws SQLException {
        when(metaData.isSigned(1)).thenReturn(true);
        when(metaData.getColumnType(1)).thenReturn(Types.INTEGER);
        when(resultSet.getInt(1)).thenReturn(1);
        assertThat(engine.read(resultSet, metaData, 1), is(1));
    }
    
    @Test
    void assertReadWithUnSingedIntegerValue() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.INTEGER);
        when(resultSet.getLong(1)).thenReturn(1L);
        assertThat(engine.read(resultSet, metaData, 1), is(1L));
    }
    
    @Test
    void assertReadWithSingedBigIntValue() throws SQLException {
        when(metaData.isSigned(1)).thenReturn(true);
        when(metaData.getColumnType(1)).thenReturn(Types.BIGINT);
        when(resultSet.getLong(1)).thenReturn(1L);
        assertThat(engine.read(resultSet, metaData, 1), is(1L));
    }
    
    @Test
    void assertReadWithUnSingedBigIntValue() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.BIGINT);
        when(resultSet.getBigDecimal(1)).thenReturn(new BigDecimal("1"));
        assertThat(engine.read(resultSet, metaData, 1), is(new BigInteger("1")));
    }
    
    @Test
    void assertReadWithNumericValue() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.NUMERIC);
        when(resultSet.getBigDecimal(1)).thenReturn(new BigDecimal("1"));
        assertThat(engine.read(resultSet, metaData, 1), is(new BigDecimal("1")));
    }
    
    @Test
    void assertReadWithDecimalValue() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.DECIMAL);
        when(resultSet.getBigDecimal(1)).thenReturn(new BigDecimal("1"));
        assertThat(engine.read(resultSet, metaData, 1), is(new BigDecimal("1")));
    }
    
    @Test
    void assertReadWithRealValue() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.REAL);
        when(resultSet.getFloat(1)).thenReturn(1F);
        assertThat(engine.read(resultSet, metaData, 1), is(1F));
    }
    
    @Test
    void assertReadWithFloatValue() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.FLOAT);
        when(resultSet.getFloat(1)).thenReturn(1F);
        assertThat(engine.read(resultSet, metaData, 1), is(1F));
    }
    
    @Test
    void assertReadWithDoubleValue() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.DOUBLE);
        when(resultSet.getDouble(1)).thenReturn(1D);
        assertThat(engine.read(resultSet, metaData, 1), is(1D));
    }
    
    @Test
    void assertReadWithTimeValue() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.TIME);
        when(resultSet.getTime(1)).thenReturn(new Time(1L));
        assertThat(engine.read(resultSet, metaData, 1), is(new Time(1L)));
    }
    
    @Test
    void assertReadWithDateValue() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.DATE);
        when(resultSet.getDate(1)).thenReturn(new Date(1L));
        assertThat(engine.read(resultSet, metaData, 1), is(new Date(1L)));
    }
    
    @Test
    void assertReadWithTimestampValue() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.TIMESTAMP);
        when(resultSet.getTimestamp(1)).thenReturn(new Timestamp(1L));
        assertThat(engine.read(resultSet, metaData, 1), is(new Timestamp(1L)));
    }
    
    @Test
    void assertReadWithCharValue() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.CHAR);
        when(resultSet.getString(1)).thenReturn("foo");
        assertThat(engine.read(resultSet, metaData, 1), is("foo"));
    }
    
    @Test
    void assertReadWithVarCharValue() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.VARCHAR);
        when(resultSet.getString(1)).thenReturn("foo");
        assertThat(engine.read(resultSet, metaData, 1), is("foo"));
    }
    
    @Test
    void assertReadWithLongVarCharValue() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.LONGVARCHAR);
        when(resultSet.getString(1)).thenReturn("foo");
        assertThat(engine.read(resultSet, metaData, 1), is("foo"));
    }
    
    @Test
    void assertReadWithNCharValue() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.NCHAR);
        when(resultSet.getString(1)).thenReturn("foo");
        assertThat(engine.read(resultSet, metaData, 1), is("foo"));
    }
    
    @Test
    void assertReadWithNVarCharValue() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.NVARCHAR);
        when(resultSet.getString(1)).thenReturn("foo");
        assertThat(engine.read(resultSet, metaData, 1), is("foo"));
    }
    
    @Test
    void assertReadWithLongNVarCharValue() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.LONGNVARCHAR);
        when(resultSet.getString(1)).thenReturn("foo");
        assertThat(engine.read(resultSet, metaData, 1), is("foo"));
    }
    
    @Test
    void assertReadWithBinaryValue() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.BINARY);
        when(resultSet.getBytes(1)).thenReturn(new byte[]{1});
        assertThat(engine.read(resultSet, metaData, 1), is(new byte[]{1}));
    }
    
    @Test
    void assertReadWithVarBinaryValue() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.VARBINARY);
        when(resultSet.getBytes(1)).thenReturn(new byte[]{1});
        assertThat(engine.read(resultSet, metaData, 1), is(new byte[]{1}));
    }
    
    @Test
    void assertReadWithLongVarBinaryValue() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.LONGVARBINARY);
        when(resultSet.getBytes(1)).thenReturn(new byte[]{1});
        assertThat(engine.read(resultSet, metaData, 1), is(new byte[]{1}));
    }
    
    @Test
    void assertReadWithNullBlob() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.BLOB);
        assertNull(engine.read(resultSet, metaData, 1));
    }
    
    @Test
    void assertReadWithBlob() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.BLOB);
        Blob blob = mock(Blob.class);
        when(blob.length()).thenReturn(10L);
        when(blob.getBytes(1, 10)).thenReturn(new byte[]{1});
        when(resultSet.getBlob(1)).thenReturn(blob);
        assertThat(engine.read(resultSet, metaData, 1), is(new byte[]{1}));
    }
    
    @Test
    void assertReadWithNullClob() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.CLOB);
        assertNull(engine.read(resultSet, metaData, 1));
    }
    
    @Test
    void assertReadWithClob() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.CLOB);
        Clob clob = mock(Clob.class);
        when(clob.length()).thenReturn(10L);
        when(clob.getSubString(1, 10)).thenReturn("foo");
        when(resultSet.getClob(1)).thenReturn(clob);
        assertThat(engine.read(resultSet, metaData, 1), is("foo"));
    }
    
    @Test
    void assertReadWithNullNClob() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.NCLOB);
        assertNull(engine.read(resultSet, metaData, 1));
    }
    
    @Test
    void assertReadWithNClob() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.NCLOB);
        NClob nClob = mock(NClob.class);
        when(nClob.length()).thenReturn(10L);
        when(nClob.getSubString(1, 10)).thenReturn("foo");
        when(resultSet.getNClob(1)).thenReturn(nClob);
        assertThat(engine.read(resultSet, metaData, 1), is("foo"));
    }
    
    @Test
    void assertReadWithArray() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.ARRAY);
        Array array = mock(Array.class);
        when(resultSet.getArray(1)).thenReturn(array);
        assertThat(engine.read(resultSet, metaData, 1), is(array));
    }
    
    @Test
    void assertReadWithObject() throws SQLException {
        when(metaData.getColumnType(1)).thenReturn(Types.JAVA_OBJECT);
        Object object = new Object();
        when(resultSet.getObject(1)).thenReturn(object);
        assertThat(engine.read(resultSet, metaData, 1), is(object));
    }
    
    @Test
    void assertReadWithNull() throws SQLException {
        when(resultSet.wasNull()).thenReturn(true);
        assertNull(engine.read(resultSet, metaData, 1));
    }
}
