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

package org.apache.shardingsphere.infra.metadata.schema.jdbc;

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypes;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MetaDataConnectionAdapterTest {
    
    private static final String TEST_CATALOG = "catalog";
    
    private static final String TEST_SCHEMA = "schema";
    
    private final DatabaseType databaseType = DatabaseTypes.getTrunkDatabaseType("MySQL");
    
    @Mock
    private Connection connection;
    
    @Test
    public void assertGetCatalog() throws SQLException {
        when(connection.getCatalog()).thenReturn(TEST_CATALOG);
        MetaDataConnectionAdapter connectionAdapter = new MetaDataConnectionAdapter(databaseType, connection);
        assertThat(connectionAdapter.getCatalog(), is(TEST_CATALOG));
    }
    
    @Test
    public void assertGetCatalogReturnNullWhenThrowsSQLException() throws SQLException {
        when(connection.getCatalog()).thenThrow(SQLException.class);
        MetaDataConnectionAdapter connectionAdapter = new MetaDataConnectionAdapter(databaseType, connection);
        assertNull(connectionAdapter.getCatalog());
    }
    
    @Test
    public void assertGetSchema() throws SQLException {
        when(connection.getSchema()).thenReturn(TEST_SCHEMA);
        MetaDataConnectionAdapter connectionAdapter = new MetaDataConnectionAdapter(databaseType, connection);
        assertThat(connectionAdapter.getSchema(), is(TEST_SCHEMA));
    }
    
    @Test
    public void assertGetSchemaReturnNullWhenThrowsSQLException() throws SQLException {
        when(connection.getSchema()).thenThrow(SQLException.class);
        MetaDataConnectionAdapter connectionAdapter = new MetaDataConnectionAdapter(databaseType, connection);
        assertNull(connectionAdapter.getSchema());
    }
}
