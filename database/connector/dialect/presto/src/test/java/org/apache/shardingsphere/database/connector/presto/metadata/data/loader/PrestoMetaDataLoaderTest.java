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

package org.apache.shardingsphere.database.connector.presto.metadata.data.loader;

import org.apache.shardingsphere.database.connector.core.metadata.data.loader.DialectMetaDataLoader;
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.MetaDataLoaderMaterial;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.ColumnMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.SchemaMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.datatype.DataTypeRegistry;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(DataTypeRegistry.class)
class PrestoMetaDataLoaderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "Presto");
    
    private final DialectMetaDataLoader loader = DatabaseTypedSPILoader.getService(DialectMetaDataLoader.class, databaseType);
    
    @Test
    void assertLoadWithoutTableFilter() throws SQLException {
        Connection connectionWithoutTables = mockConnectionWithoutTables();
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenReturn(connectionWithoutTables);
        when(DataTypeRegistry.getDataType("Presto", "int")).thenReturn(Optional.of(Types.INTEGER));
        when(DataTypeRegistry.getDataType("Presto", "varchar")).thenReturn(Optional.empty());
        Collection<SchemaMetaData> actualWithoutTables = loader.load(new MetaDataLoaderMaterial(Collections.emptyList(), "ds_0", dataSource, databaseType, "def_schema"));
        SchemaMetaData schemaMetaDataWithoutTables = actualWithoutTables.iterator().next();
        assertThat(schemaMetaDataWithoutTables.getName(), is("def_schema"));
        TableMetaData tableMetaDataWithoutTables = schemaMetaDataWithoutTables.getTables().iterator().next();
        assertThat(tableMetaDataWithoutTables.getName(), is("tbl_one"));
        Iterator<ColumnMetaData> columnIterator = tableMetaDataWithoutTables.getColumns().iterator();
        ColumnMetaData firstColumn = columnIterator.next();
        assertThat(firstColumn.getName(), is("id"));
        assertThat(firstColumn.getDataType(), is(Types.INTEGER));
        assertTrue(firstColumn.isNullable());
        ColumnMetaData secondColumn = columnIterator.next();
        assertThat(secondColumn.getName(), is("name"));
        assertThat(secondColumn.getDataType(), is(Types.OTHER));
        assertFalse(secondColumn.isNullable());
    }
    
    @Test
    void assertLoadWithTableFilter() throws SQLException {
        Connection connectionWithTables = mockConnectionWithTables();
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenReturn(connectionWithTables);
        when(DataTypeRegistry.getDataType("Presto", "json")).thenReturn(Optional.of(Types.JAVA_OBJECT));
        Collection<SchemaMetaData> actualWithTables = loader.load(new MetaDataLoaderMaterial(Collections.singletonList("target_table"), "ds_0", dataSource, databaseType, "def_schema"));
        SchemaMetaData schemaMetaDataWithTables = actualWithTables.iterator().next();
        TableMetaData tableMetaDataWithTables = schemaMetaDataWithTables.getTables().iterator().next();
        ColumnMetaData payloadColumn = tableMetaDataWithTables.getColumns().iterator().next();
        assertThat(tableMetaDataWithTables.getName(), is("target_table"));
        assertThat(payloadColumn.getDataType(), is(Types.JAVA_OBJECT));
    }
    
    private Connection mockConnectionWithoutTables() throws SQLException {
        Connection result = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mockResultSetWithoutTables();
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(result.getCatalog()).thenReturn("catalog_one");
        String sql = "SELECT TABLE_CATALOG,TABLE_NAME,COLUMN_NAME,DATA_TYPE,ORDINAL_POSITION,IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_CATALOG=? ORDER BY ORDINAL_POSITION";
        when(result.prepareStatement(sql)).thenReturn(preparedStatement);
        return result;
    }
    
    private ResultSet mockResultSetWithoutTables() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, true, false);
        when(result.getString("TABLE_NAME")).thenReturn("tbl_one", "tbl_one");
        when(result.getString("COLUMN_NAME")).thenReturn("id", "name");
        when(result.getString("DATA_TYPE")).thenReturn("int", "varchar");
        when(result.getString("IS_NULLABLE")).thenReturn("YES", "NO");
        return result;
    }
    
    private Connection mockConnectionWithTables() throws SQLException {
        Connection result = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mockResultSetWithTables();
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        String sql = "SELECT TABLE_CATALOG,TABLE_NAME,COLUMN_NAME,DATA_TYPE,ORDINAL_POSITION,IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS "
                + "WHERE TABLE_CATALOG=? AND UPPER(TABLE_NAME) IN ('TARGET_TABLE') ORDER BY ORDINAL_POSITION";
        when(result.prepareStatement(sql)).thenReturn(preparedStatement);
        when(result.getCatalog()).thenReturn("catalog_two");
        return result;
    }
    
    private ResultSet mockResultSetWithTables() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        when(result.getString("TABLE_NAME")).thenReturn("target_table");
        when(result.getString("COLUMN_NAME")).thenReturn("payload");
        when(result.getString("DATA_TYPE")).thenReturn("json");
        when(result.getString("IS_NULLABLE")).thenReturn("NO");
        return result;
    }
}
