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

package org.apache.shardingsphere.infra.database.core.metadata.data.loader;

import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetaDataLoaderConnectionTest {
    
    private static final String TEST_CATALOG = "catalog";
    
    private static final String TEST_SCHEMA = "schema";
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "TRUNK");
    
    @Mock
    private Connection connection;
    
    @Test
    void assertGetCatalog() throws SQLException {
        when(connection.getCatalog()).thenReturn(TEST_CATALOG);
        MetaDataLoaderConnection connection = new MetaDataLoaderConnection(databaseType, this.connection);
        assertThat(connection.getCatalog(), is(TEST_CATALOG));
    }
    
    @Test
    void assertGetCatalogReturnNullWhenThrowsSQLException() throws SQLException {
        when(connection.getCatalog()).thenThrow(SQLException.class);
        MetaDataLoaderConnection connection = new MetaDataLoaderConnection(databaseType, this.connection);
        assertNull(connection.getCatalog());
    }
    
    @Test
    void assertGetSchema() throws SQLException {
        when(connection.getSchema()).thenReturn(TEST_SCHEMA);
        MetaDataLoaderConnection connection = new MetaDataLoaderConnection(databaseType, this.connection);
        assertThat(connection.getSchema(), is(TEST_SCHEMA));
    }
    
    @Test
    void assertGetSchemaByMySQLSPI() throws SQLException {
        when(connection.getSchema()).thenReturn(TEST_SCHEMA);
        MetaDataLoaderConnection connection = new MetaDataLoaderConnection(databaseType, this.connection);
        assertThat(connection.getSchema(), is(TEST_SCHEMA));
    }
    
    @Test
    void assertGetSchemaReturnNullWhenThrowsSQLException() throws SQLException {
        when(connection.getSchema()).thenThrow(SQLException.class);
        MetaDataLoaderConnection connection = new MetaDataLoaderConnection(databaseType, this.connection);
        assertNull(connection.getSchema());
    }
}
