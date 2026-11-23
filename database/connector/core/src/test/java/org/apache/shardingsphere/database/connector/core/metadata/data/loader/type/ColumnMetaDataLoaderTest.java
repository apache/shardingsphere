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

package org.apache.shardingsphere.database.connector.core.metadata.data.loader.type;

import ch.qos.logback.classic.spi.ThrowableProxy;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.ColumnMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.infra.framework.extension.log.LogCaptureAssertion;
import org.apache.shardingsphere.test.infra.framework.extension.log.LogCaptureExtension;
import org.apache.shardingsphere.test.infra.framework.extension.log.LogCaptureSettings;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, LogCaptureExtension.class, AutoMockExtension.class})
@LogCaptureSettings(suppressOutput = true)
@StaticMockSettings(DatabaseTypedSPILoader.class)
class ColumnMetaDataLoaderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Connection connection;
    
    @Mock
    private ResultSet columnResultSet;
    
    @BeforeEach
    void setUp() throws SQLException {
        when(DatabaseTypedSPILoader.getService(any(), any(DatabaseType.class))).thenReturn(TypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType));
        when(connection.getCatalog()).thenReturn("catalog");
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("COLUMN_NAME")).thenReturn("pk_col");
        when(connection.getMetaData().getPrimaryKeys("catalog", null, "tbl")).thenReturn(resultSet);
        when(connection.getMetaData().getColumns("catalog", null, "tbl", "%")).thenReturn(columnResultSet);
    }
    
    @Test
    void assertLoad() throws SQLException {
        when(columnResultSet.next()).thenReturn(true, true, true, false);
        when(columnResultSet.getString("TABLE_NAME")).thenReturn("other_tbl", "tbl", "tbl");
        when(columnResultSet.getString("COLUMN_NAME")).thenReturn("pk_col", "col");
        when(columnResultSet.getInt("DATA_TYPE")).thenReturn(Types.INTEGER, Types.VARCHAR);
        ResultSet caseSensitivesResultSet = mock(ResultSet.class);
        when(caseSensitivesResultSet.findColumn("pk_col")).thenReturn(1);
        when(caseSensitivesResultSet.findColumn("col")).thenReturn(2);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(resultSetMetaData.isCaseSensitive(1)).thenReturn(true);
        when(caseSensitivesResultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(connection.createStatement().executeQuery(anyString())).thenReturn(caseSensitivesResultSet);
        Collection<ColumnMetaData> actual = ColumnMetaDataLoader.load(connection, "tbl", databaseType);
        assertThat(actual.size(), is(2));
        Iterator<ColumnMetaData> columnMetaDataIterator = actual.iterator();
        assertColumnMetaData(columnMetaDataIterator.next(), "pk_col", Types.INTEGER, true, true);
        assertColumnMetaData(columnMetaDataIterator.next(), "col", Types.VARCHAR, false, false);
    }
    
    private void assertColumnMetaData(final ColumnMetaData actual, final String name, final int dataType, final boolean primaryKey, final boolean caseSensitive) {
        assertThat(actual.getName(), is(name));
        assertThat(actual.getDataType(), is(dataType));
        assertThat(actual.isPrimaryKey(), is(primaryKey));
        assertThat(actual.isCaseSensitive(), is(caseSensitive));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("datetimeArguments")
    void assertLoadDatetime(final String name, final String storageType, final String columnTypeName, final boolean expectedMysqlDatetime) throws SQLException {
        DatabaseType sourceType = mock(DatabaseType.class);
        when(sourceType.getType()).thenReturn(storageType);
        when(columnResultSet.next()).thenReturn(true, false);
        when(columnResultSet.getString("TABLE_NAME")).thenReturn("tbl");
        when(columnResultSet.getString("COLUMN_NAME")).thenReturn("pk_col");
        when(columnResultSet.getInt("DATA_TYPE")).thenReturn(Types.TIMESTAMP);
        ResultSet queryResultSet = mock(ResultSet.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);
        when(queryResultSet.getMetaData()).thenReturn(resultSetMetaData);
        if ("MySQL".equals(storageType)) {
            when(resultSetMetaData.getColumnTypeName(1)).thenReturn(columnTypeName);
        }
        when(connection.createStatement().executeQuery(anyString())).thenReturn(queryResultSet);
        Collection<ColumnMetaData> actual = ColumnMetaDataLoader.load(connection, "tbl", sourceType);
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getDataType(), is(Types.TIMESTAMP));
        assertTrue(actual.iterator().next().isMysqlDatetime() == expectedMysqlDatetime);
    }
    
    private static Stream<Arguments> datetimeArguments() {
        return Stream.of(
                Arguments.of("MySQL DATETIME", "MySQL", "DATETIME", true),
                Arguments.of("MySQL TIMESTAMP", "MySQL", "TIMESTAMP", false),
                Arguments.of("other storage DATETIME", "SQLServer", "DATETIME", false),
                Arguments.of("missing type name", "MySQL", null, false));
    }
    
    @Test
    void assertLoadWhenThrowsSQLException(final LogCaptureAssertion logCaptureAssertion) throws SQLException {
        when(columnResultSet.next()).thenReturn(true, false);
        when(columnResultSet.getString("TABLE_NAME")).thenReturn("tbl");
        when(columnResultSet.getString("COLUMN_NAME")).thenReturn("pk_col");
        when(columnResultSet.getInt("DATA_TYPE")).thenReturn(Types.INTEGER);
        SQLException expectedException = new SQLException();
        when(connection.createStatement().executeQuery(anyString())).thenThrow(expectedException);
        assertThrows(SQLException.class, () -> ColumnMetaDataLoader.load(connection, "tbl", databaseType));
        logCaptureAssertion.assertErrorLog(actualException -> assertThat(((ThrowableProxy) actualException).getThrowable(), sameInstance(expectedException)));
    }
}
