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

import com.cedarsoftware.util.CaseInsensitiveSet;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DialectSchemaOption;
import org.apache.shardingsphere.database.connector.core.metadata.database.system.DialectSystemDatabase;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.MockedStatic;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SchemaMetaDataLoaderTest {
    
    private static final String[] TABLE_TYPES = {"TABLE", "PARTITIONED TABLE", "VIEW", "SYSTEM TABLE", "SYSTEM VIEW"};
    
    private final DatabaseType databaseType = mock(DatabaseType.class);
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DataSource dataSourceWithoutDefaultSchema;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DataSource dataSourceWithDefaultSchema;
    
    @Test
    void assertLoadSchemaTableNamesWithoutDefaultSchema() throws SQLException {
        DialectSchemaOption schemaOption = mock(DialectSchemaOption.class);
        when(schemaOption.getDefaultSchema()).thenReturn(Optional.empty());
        when(schemaOption.getSchema(any())).thenReturn("public");
        DialectDatabaseMetaData dialectDatabaseMetaData = mock(DialectDatabaseMetaData.class);
        when(dialectDatabaseMetaData.getSchemaOption()).thenReturn(schemaOption);
        try (MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType)).thenReturn(dialectDatabaseMetaData);
            try (MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class)) {
                typedSPILoader.when(() -> TypedSPILoader.getService(DialectDatabaseMetaData.class, null)).thenReturn(dialectDatabaseMetaData);
                Connection connection = dataSourceWithoutDefaultSchema.getConnection();
                when(connection.getCatalog()).thenReturn("catalog");
                ResultSet tableResultSet = mock(ResultSet.class);
                when(tableResultSet.next()).thenReturn(true, true, true, true, true, false);
                when(tableResultSet.getString("TABLE_NAME")).thenReturn("tbl", "$tbl", "/tbl", "##tbl", "excluded_tbl");
                when(connection.getMetaData().getTables("catalog", "public", null, TABLE_TYPES)).thenReturn(tableResultSet);
                Map<String, Collection<String>> actual = new SchemaMetaDataLoader(databaseType)
                        .loadSchemaTableNames("logic_db", dataSourceWithoutDefaultSchema, Collections.singleton("excluded_tbl"));
                Map<String, Collection<String>> expected = Collections.singletonMap("logic_db", new CaseInsensitiveSet<>(Collections.singleton("tbl")));
                assertThat(actual, is(expected));
            }
        }
    }
    
    @Test
    void assertLoadSchemaTableNamesWithDefaultSchema() throws SQLException {
        DialectSchemaOption schemaOption = mock(DialectSchemaOption.class);
        when(schemaOption.getDefaultSchema()).thenReturn(Optional.of("public"));
        DialectDatabaseMetaData dialectDatabaseMetaData = mock(DialectDatabaseMetaData.class);
        when(dialectDatabaseMetaData.getSchemaOption()).thenReturn(schemaOption);
        DialectSystemDatabase dialectSystemDatabase = mock(DialectSystemDatabase.class);
        when(dialectSystemDatabase.getSystemSchemas()).thenReturn(Collections.singleton("sys"));
        try (MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType)).thenReturn(dialectDatabaseMetaData);
            databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.findService(DialectSystemDatabase.class, databaseType)).thenReturn(Optional.of(dialectSystemDatabase));
            try (MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class)) {
                typedSPILoader.when(() -> TypedSPILoader.getService(DialectDatabaseMetaData.class, null)).thenReturn(dialectDatabaseMetaData);
                Connection connection = dataSourceWithDefaultSchema.getConnection();
                when(connection.getCatalog()).thenReturn("catalog_2");
                ResultSet schemaResultSet = mock(ResultSet.class);
                when(schemaResultSet.next()).thenReturn(true, true, false);
                when(schemaResultSet.getString("TABLE_SCHEM")).thenReturn("sys", "user_schema");
                when(connection.getMetaData().getSchemas()).thenReturn(schemaResultSet);
                ResultSet tableResultSet = mock(ResultSet.class);
                when(tableResultSet.next()).thenReturn(true, false);
                when(tableResultSet.getString("TABLE_NAME")).thenReturn("tbl_visible");
                when(connection.getMetaData().getTables("catalog_2", "user_schema", null, TABLE_TYPES)).thenReturn(tableResultSet);
                Map<String, Collection<String>> actual = new SchemaMetaDataLoader(databaseType).loadSchemaTableNames("logic_db_2", dataSourceWithDefaultSchema, Collections.emptyList());
                Map<String, Collection<String>> expected = Collections.singletonMap("user_schema", new CaseInsensitiveSet<>(Collections.singleton("tbl_visible")));
                assertThat(actual, is(expected));
            }
        }
    }
    
    @Test
    void assertLoadSchemaNames() throws SQLException {
        DialectSchemaOption schemaOption = mock(DialectSchemaOption.class);
        when(schemaOption.getDefaultSchema()).thenReturn(Optional.of("public"));
        DialectDatabaseMetaData dialectDatabaseMetaData = mock(DialectDatabaseMetaData.class);
        when(dialectDatabaseMetaData.getSchemaOption()).thenReturn(schemaOption);
        DialectSystemDatabase dialectSystemDatabase = mock(DialectSystemDatabase.class);
        when(dialectSystemDatabase.getSystemSchemas()).thenReturn(Collections.singleton("information_schema"));
        try (MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class)) {
            databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType)).thenReturn(dialectDatabaseMetaData);
            databaseTypedSPILoader.when(() -> DatabaseTypedSPILoader.findService(DialectSystemDatabase.class, databaseType)).thenReturn(Optional.of(dialectSystemDatabase));
            try (MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class)) {
                typedSPILoader.when(() -> TypedSPILoader.getService(DialectDatabaseMetaData.class, null)).thenReturn(dialectDatabaseMetaData);
                Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
                ResultSet resultSet = mock(ResultSet.class);
                when(resultSet.next()).thenReturn(true, false);
                when(resultSet.getString("TABLE_SCHEM")).thenReturn("information_schema");
                when(connection.getMetaData().getSchemas()).thenReturn(resultSet);
                when(connection.getSchema()).thenReturn("current_schema");
                assertThat(new SchemaMetaDataLoader(databaseType).loadSchemaNames(connection), is(Collections.singletonList("current_schema")));
            }
        }
    }
}
