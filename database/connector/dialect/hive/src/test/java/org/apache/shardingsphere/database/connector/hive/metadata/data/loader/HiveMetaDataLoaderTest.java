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

package org.apache.shardingsphere.database.connector.hive.metadata.data.loader;

import com.cedarsoftware.util.CaseInsensitiveMap;
import lombok.SneakyThrows;
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.DialectMetaDataLoader;
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.MetaDataLoaderMaterial;
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.type.TableMetaDataLoader;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.ColumnMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.SchemaMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.datatype.DataTypeRegistry;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.internal.configuration.plugins.Plugins;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(TableMetaDataLoader.class)
class HiveMetaDataLoaderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "Hive");
    
    private final DialectMetaDataLoader loader = DatabaseTypedSPILoader.getService(DialectMetaDataLoader.class, databaseType);
    
    private Map<String, Map<String, Integer>> dataTypes;
    
    @BeforeEach
    void setUp() {
        dataTypes = getDataTypes();
        Map<String, Integer> hiveDataTypes = new CaseInsensitiveMap<>();
        hiveDataTypes.put("string", Types.VARCHAR);
        hiveDataTypes.put("int", Types.INTEGER);
        dataTypes.put("Hive", hiveDataTypes);
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private Map<String, Map<String, Integer>> getDataTypes() {
        return (Map<String, Map<String, Integer>>) Plugins.getMemberAccessor().get(DataTypeRegistry.class.getDeclaredField("DATA_TYPES"), null);
    }
    
    @AfterEach
    void tearDown() {
        dataTypes.remove("Hive");
    }
    
    @Test
    void assertLoadWithInformationSchemaAndFilter() throws SQLException {
        DataSource dataSource = mockDataSource(mockInformationSchemaConnection(true), mockColumnMetaDataConnectionWithInformationSchemaAndFilter());
        Collection<SchemaMetaData> filteredSchemas = loader.load(new MetaDataLoaderMaterial(Collections.singleton("target_table"), "ds_0", dataSource, databaseType, "def_schema"));
        SchemaMetaData filteredSchema = filteredSchemas.iterator().next();
        TableMetaData filteredTable = filteredSchema.getTables().iterator().next();
        Iterator<ColumnMetaData> filteredColumns = filteredTable.getColumns().iterator();
        ColumnMetaData firstColumn = filteredColumns.next();
        assertThat(firstColumn.getName(), is("c1"));
        assertThat(firstColumn.getDataType(), is(Types.VARCHAR));
        assertTrue(firstColumn.isNullable());
        ColumnMetaData secondColumn = filteredColumns.next();
        assertThat(secondColumn.getDataType(), is(Types.OTHER));
        assertFalse(secondColumn.isNullable());
    }
    
    @Test
    void assertLoadWithInformationSchemaWithoutFilter() throws SQLException {
        DataSource dataSource = mockDataSource(mockInformationSchemaConnection(true), mockColumnMetaDataConnectionWithInformationSchemaWithoutFilter());
        Collection<SchemaMetaData> fullSchemas = loader.load(new MetaDataLoaderMaterial(Collections.emptyList(), "ds_1", dataSource, databaseType, "default_db"));
        SchemaMetaData fullSchema = fullSchemas.iterator().next();
        assertThat(fullSchema.getTables().iterator().next().getName(), is("full_table"));
    }
    
    @Test
    void assertLoadWithoutInformationSchemaFallbackToDefaultLoader() throws SQLException {
        DataSource dataSource = mockDataSource(mockInformationSchemaConnection(false));
        TableMetaData tableMetaData = new TableMetaData("present_table", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        when(TableMetaDataLoader.load(dataSource, "missing_table", databaseType)).thenReturn(Optional.empty());
        when(TableMetaDataLoader.load(dataSource, "present_table", databaseType)).thenReturn(Optional.of(tableMetaData));
        Collection<SchemaMetaData> fallbackSchemas = loader.load(new MetaDataLoaderMaterial(Arrays.asList("missing_table", "present_table"), "ds_2", dataSource, databaseType, "fallback_schema"));
        TableMetaData defaultLoadedTable = fallbackSchemas.iterator().next().getTables().iterator().next();
        assertThat(defaultLoadedTable.getName(), is("present_table"));
    }
    
    private DataSource mockDataSource(final Connection... connections) throws SQLException {
        DataSource result = mock(DataSource.class);
        when(result.getConnection()).thenReturn(connections[0], Arrays.copyOfRange(connections, 1, connections.length));
        return result;
    }
    
    private Connection mockInformationSchemaConnection(final boolean hasResult) throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(hasResult);
        Statement statement = mock(Statement.class);
        String sql = "SHOW DATABASES LIKE 'INFORMATION_SCHEMA'";
        when(statement.executeQuery(sql)).thenReturn(resultSet);
        Connection result = mock(Connection.class);
        when(result.createStatement()).thenReturn(statement);
        return result;
    }
    
    private Connection mockColumnMetaDataConnectionWithInformationSchemaAndFilter() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("TABLE_NAME")).thenReturn("target_table", "target_table");
        when(resultSet.getString("COLUMN_NAME")).thenReturn("c1", "c2");
        when(resultSet.getString("DATA_TYPE")).thenReturn("string", "unknown");
        when(resultSet.getString("IS_NULLABLE")).thenReturn("YES", "NO");
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        Connection connection = mock(Connection.class);
        String sql = "SELECT TABLE_CATALOG,TABLE_NAME,COLUMN_NAME,DATA_TYPE,ORDINAL_POSITION,IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS "
                + "WHERE TABLE_CATALOG=? AND UPPER(TABLE_NAME) IN ('TARGET_TABLE') ORDER BY ORDINAL_POSITION";
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        return connection;
    }
    
    private Connection mockColumnMetaDataConnectionWithInformationSchemaWithoutFilter() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("TABLE_NAME")).thenReturn("full_table");
        when(resultSet.getString("COLUMN_NAME")).thenReturn("id");
        when(resultSet.getString("DATA_TYPE")).thenReturn("int");
        when(resultSet.getString("IS_NULLABLE")).thenReturn("NO");
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        Connection result = mock(Connection.class);
        String sql = "SELECT TABLE_CATALOG,TABLE_NAME,COLUMN_NAME,DATA_TYPE,ORDINAL_POSITION,IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_CATALOG=? ORDER BY ORDINAL_POSITION";
        when(result.prepareStatement(sql)).thenReturn(preparedStatement);
        return result;
    }
}
