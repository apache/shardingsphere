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

package org.apache.shardingsphere.data.pipeline.postgresql.ingest.inventory;

import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.column.DialectInventoryColumnValueReader;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PGobject;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PostgreSQLInventoryColumnValueReaderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    private final DialectInventoryColumnValueReader reader = DatabaseTypedSPILoader.getService(DialectInventoryColumnValueReader.class, databaseType);
    
    @Test
    void assertReadWithMoneyDataTypeValue() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getBigDecimal(1)).thenReturn(new BigDecimal("1"));
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(metaData.getColumnTypeName(1)).thenReturn("money");
        assertThat(reader.read(resultSet, metaData, 1), is(Optional.of(new BigDecimal("1"))));
    }
    
    @Test
    void assertReadWithFalseBitDataTypeValue() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getObject(1)).thenReturn(false);
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(metaData.getColumnType(1)).thenReturn(Types.BIT);
        when(metaData.getColumnTypeName(1)).thenReturn("bit");
        Optional<Object> actual = reader.read(resultSet, metaData, 1);
        assertTrue(actual.isPresent());
        assertThat(((PGobject) actual.get()).getType(), is("bit"));
        assertThat(((PGobject) actual.get()).getValue(), is("0"));
    }
    
    @Test
    void assertReadWithTrueBitDataTypeValue() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getObject(1)).thenReturn(true);
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(metaData.getColumnType(1)).thenReturn(Types.BIT);
        when(metaData.getColumnTypeName(1)).thenReturn("bit");
        Optional<Object> actual = reader.read(resultSet, metaData, 1);
        assertTrue(actual.isPresent());
        assertThat(((PGobject) actual.get()).getType(), is("bit"));
        assertThat(((PGobject) actual.get()).getValue(), is("1"));
    }
    
    @Test
    void assertReadWithNullBitDataTypeValue() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(metaData.getColumnType(1)).thenReturn(Types.BIT);
        when(metaData.getColumnTypeName(1)).thenReturn("bit");
        Optional<Object> actual = reader.read(resultSet, metaData, 1);
        assertTrue(actual.isPresent());
        assertThat(((PGobject) actual.get()).getType(), is("bit"));
        assertNull(((PGobject) actual.get()).getValue());
    }
    
    @Test
    void assertReadWithOtherType() throws SQLException {
        assertFalse(reader.read(mock(ResultSet.class), mock(ResultSetMetaData.class), 1).isPresent());
    }
}
