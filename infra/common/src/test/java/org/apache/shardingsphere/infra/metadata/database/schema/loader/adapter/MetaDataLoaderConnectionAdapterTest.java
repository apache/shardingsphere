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

package org.apache.shardingsphere.infra.metadata.database.schema.loader.adapter;

import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetaDataLoaderConnectionAdapterTest {
    
    private static final String TEST_CATALOG = "catalog";
    
    private static final String TEST_SCHEMA = "schema";
    
    private final DatabaseType databaseType = DatabaseTypeEngine.getTrunkDatabaseType("MySQL");
    
    private final DatabaseType oracleDatabaseType = DatabaseTypeEngine.getTrunkDatabaseType("Oracle");
    
    @Mock
    private Connection connection;
    
    @Test
    void assertGetCatalog() throws SQLException {
        when(connection.getCatalog()).thenReturn(TEST_CATALOG);
        MetaDataLoaderConnectionAdapter connectionAdapter = new MetaDataLoaderConnectionAdapter(databaseType, connection);
        assertThat(connectionAdapter.getCatalog(), is(TEST_CATALOG));
    }
    
    @Test
    void assertGetCatalogReturnNullWhenThrowsSQLException() throws SQLException {
        when(connection.getCatalog()).thenThrow(SQLException.class);
        MetaDataLoaderConnectionAdapter connectionAdapter = new MetaDataLoaderConnectionAdapter(databaseType, connection);
        assertNull(connectionAdapter.getCatalog());
    }
    
    @Test
    void assertGetSchema() throws SQLException {
        when(connection.getSchema()).thenReturn(TEST_SCHEMA);
        MetaDataLoaderConnectionAdapter connectionAdapter = new MetaDataLoaderConnectionAdapter(databaseType, connection);
        assertThat(connectionAdapter.getSchema(), is(TEST_SCHEMA));
    }
    
    @Test
    void assertGetSchemaByOracleSPI() throws SQLException {
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getUserName()).thenReturn(TEST_SCHEMA);
        MetaDataLoaderConnectionAdapter connectionAdapter = new MetaDataLoaderConnectionAdapter(oracleDatabaseType, connection);
        assertThat(connectionAdapter.getSchema(), is(TEST_SCHEMA.toUpperCase()));
    }
    
    @Test
    void assertGetSchemaByMySQLSPI() throws SQLException {
        when(connection.getSchema()).thenReturn(TEST_SCHEMA);
        MetaDataLoaderConnectionAdapter connectionAdapter = new MetaDataLoaderConnectionAdapter(databaseType, connection);
        assertThat(connectionAdapter.getSchema(), is(TEST_SCHEMA));
    }
    
    @Test
    void assertGetSchemaReturnNullWhenThrowsSQLException() throws SQLException {
        when(connection.getSchema()).thenThrow(SQLException.class);
        MetaDataLoaderConnectionAdapter connectionAdapter = new MetaDataLoaderConnectionAdapter(databaseType, connection);
        assertNull(connectionAdapter.getSchema());
    }
}
