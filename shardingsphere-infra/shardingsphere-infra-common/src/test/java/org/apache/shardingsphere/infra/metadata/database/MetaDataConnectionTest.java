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

package org.apache.shardingsphere.infra.metadata.database;

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
public final class MetaDataConnectionTest {
    
    private static final String TEST_CATALOG = "catalog";
    
    private static final String TEST_SCHEMA = "schema";
    
    @Mock
    private Connection connection;
    
    @Test
    public void assertGetCatalog() throws SQLException {
        when(connection.getCatalog()).thenReturn(TEST_CATALOG);
        MetaDataConnection metaDataConnection = new MetaDataConnection("MySQL", connection);
        assertThat(metaDataConnection.getCatalog(), is(TEST_CATALOG));
    }
    
    @Test
    public void assertGetCatalogReturnNullWhenThrowsSQLException() throws SQLException {
        when(connection.getCatalog()).thenThrow(SQLException.class);
        MetaDataConnection metaDataConnection = new MetaDataConnection("MySQL", connection);
        assertNull(metaDataConnection.getCatalog());
    }
    
    @Test
    public void assertGetSchema() throws SQLException {
        when(connection.getSchema()).thenReturn(TEST_SCHEMA);
        MetaDataConnection metaDataConnection = new MetaDataConnection("MySQL", connection);
        assertThat(metaDataConnection.getSchema(), is(TEST_SCHEMA));
    }
    
    @Test
    public void assertGetSchemaReturnNullWhenThrowsSQLException() throws SQLException {
        when(connection.getSchema()).thenThrow(SQLException.class);
        MetaDataConnection metaDataConnection = new MetaDataConnection("MySQL", connection);
        assertNull(metaDataConnection.getSchema());
    }
}
