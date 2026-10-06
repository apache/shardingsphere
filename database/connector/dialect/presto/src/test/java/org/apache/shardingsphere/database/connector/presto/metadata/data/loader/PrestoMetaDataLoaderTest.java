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
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.TableType;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(DataTypeRegistry.class)
class PrestoMetaDataLoaderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "Presto");
    
    private final DialectMetaDataLoader loader = DatabaseTypedSPILoader.getService(DialectMetaDataLoader.class, databaseType);
    
    @Test
    void assertLoadWithoutTableFilter() throws SQLException {
        PreparedStatement columnMetaDataStatement = mock(PreparedStatement.class);
        PreparedStatement viewMetaDataStatement = mock(PreparedStatement.class);
        Connection connectionWithoutTables = mockConnectionWithoutTables(columnMetaDataStatement, viewMetaDataStatement);
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenReturn(connectionWithoutTables);
        when(DataTypeRegistry.getDataType("Presto", "int")).thenReturn(Optional.of(Types.INTEGER));
        when(DataTypeRegistry.getDataType("Presto", "varchar")).thenReturn(Optional.empty());
        Collection<SchemaMetaData> actualWithoutTables = loader.load(new MetaDataLoaderMaterial(Collections.emptyList(), "ds_0", dataSource, databaseType, "def_schema"));
        SchemaMetaData actualSchema = actualWithoutTables.iterator().next();
        assertThat(actualSchema.getName(), is("def_schema"));
        TableMetaData actualTable = actualSchema.getTables().iterator().next();
        assertThat(actualTable.getName(), is("tbl_one"));
        assertThat(actualTable.getType(), is(TableType.VIEW));
        Iterator<ColumnMetaData> actualColumns = actualTable.getColumns().iterator();
        ColumnMetaData actualFirstColumn = actualColumns.next();
        assertThat(actualFirstColumn.getName(), is("id"));
        assertThat(actualFirstColumn.getDataType(), is(Types.INTEGER));
        assertFalse(actualFirstColumn.isCaseSensitive());
        assertTrue(actualFirstColumn.isNullable());
        ColumnMetaData actualSecondColumn = actualColumns.next();
        assertThat(actualSecondColumn.getName(), is("name"));
        assertThat(actualSecondColumn.getDataType(), is(Types.OTHER));
        assertTrue(actualSecondColumn.isCaseSensitive());
        assertFalse(actualSecondColumn.isNullable());
        verify(columnMetaDataStatement).setString(1, "catalog_one");
        verify(columnMetaDataStatement).setString(2, "foo_schema");
        verify(viewMetaDataStatement).setString(1, "catalog_one");
        verify(viewMetaDataStatement).setString(2, "foo_schema");
    }
    
    @Test
    void assertLoadWithTableFilter() throws SQLException {
        Connection connectionWithTables = mockConnectionWithTables();
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenReturn(connectionWithTables);
        when(DataTypeRegistry.getDataType("Presto", "json")).thenReturn(Optional.of(Types.JAVA_OBJECT));
        Collection<SchemaMetaData> actualWithTables = loader.load(new MetaDataLoaderMaterial(Collections.singletonList("target_table"), "ds_0", dataSource, databaseType, "def_schema"));
        SchemaMetaData actualSchema = actualWithTables.iterator().next();
        TableMetaData actualTable = actualSchema.getTables().iterator().next();
        ColumnMetaData actualColumn = actualTable.getColumns().iterator().next();
        assertThat(actualTable.getName(), is("target_table"));
        assertThat(actualTable.getType(), is(TableType.TABLE));
        assertThat(actualColumn.getDataType(), is(Types.JAVA_OBJECT));
        assertFalse(actualColumn.isCaseSensitive());
    }
    
    private Connection mockConnectionWithoutTables(final PreparedStatement columnMetaDataStatement, final PreparedStatement viewMetaDataStatement) throws SQLException {
        Connection result = mock(Connection.class);
        ResultSet resultSet = mockResultSetWithoutTables();
        when(columnMetaDataStatement.executeQuery()).thenReturn(resultSet);
        ResultSet viewResultSet = mock(ResultSet.class);
        when(viewResultSet.next()).thenReturn(true, false);
        when(viewResultSet.getString("TABLE_NAME")).thenReturn("tbl_one");
        when(viewMetaDataStatement.executeQuery()).thenReturn(viewResultSet);
        when(result.getCatalog()).thenReturn("catalog_one");
        when(result.getSchema()).thenReturn("foo_schema");
        String sql = "SELECT TABLE_CATALOG,TABLE_NAME,COLUMN_NAME,DATA_TYPE,ORDINAL_POSITION,IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS"
                + " WHERE TABLE_CATALOG=? AND TABLE_SCHEMA=? ORDER BY ORDINAL_POSITION";
        when(result.prepareStatement(sql)).thenReturn(columnMetaDataStatement);
        String viewSql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_CATALOG=? AND TABLE_SCHEMA=? AND TABLE_TYPE='VIEW' AND UPPER(TABLE_NAME) IN ('TBL_ONE')";
        when(result.prepareStatement(viewSql)).thenReturn(viewMetaDataStatement);
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
        PreparedStatement columnMetaDataStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mockResultSetWithTables();
        when(columnMetaDataStatement.executeQuery()).thenReturn(resultSet);
        PreparedStatement viewMetaDataStatement = mock(PreparedStatement.class);
        ResultSet viewResultSet = mock(ResultSet.class);
        when(viewMetaDataStatement.executeQuery()).thenReturn(viewResultSet);
        String sql = "SELECT TABLE_CATALOG,TABLE_NAME,COLUMN_NAME,DATA_TYPE,ORDINAL_POSITION,IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS "
                + "WHERE TABLE_CATALOG=? AND TABLE_SCHEMA=? AND UPPER(TABLE_NAME) IN ('TARGET_TABLE') ORDER BY ORDINAL_POSITION";
        when(result.prepareStatement(sql)).thenReturn(columnMetaDataStatement);
        String viewSql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_CATALOG=? AND TABLE_SCHEMA=? AND TABLE_TYPE='VIEW' AND UPPER(TABLE_NAME) IN ('TARGET_TABLE')";
        when(result.prepareStatement(viewSql)).thenReturn(viewMetaDataStatement);
        when(result.getCatalog()).thenReturn("catalog_two");
        when(result.getSchema()).thenReturn("bar_schema");
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
