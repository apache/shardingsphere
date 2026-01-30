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

package org.apache.shardingsphere.database.connector.firebird.metadata.data.loader;

import org.apache.shardingsphere.database.connector.core.metadata.data.loader.MetaDataLoaderMaterial;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdBlobColumnLoaderTest {
    
    private static final Collection<String> TABLES = Collections.singleton("test_table");
    
    @Mock
    private DataSource dataSource;
    
    @Mock
    private Connection connection;
    
    @Mock
    private PreparedStatement preparedStatement;
    
    @Mock
    private ResultSet resultSet;
    
    private MetaDataLoaderMaterial material;
    
    @BeforeEach
    void setUp() {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "Firebird");
        material = new MetaDataLoaderMaterial(TABLES, "logic_ds", dataSource, databaseType, "schema");
    }
    
    @Test
    void assertLoadReturnsBlobColumns() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("COLUMN_NAME")).thenReturn(" blob_col ", "  ");
        when(resultSet.getObject("SUB_TYPE")).thenReturn(2).thenReturn((Object) null);
        Map<String, Map<String, Integer>> actual = new FirebirdBlobColumnLoader(material).load();
        assertThat(actual, hasKey("test_table"));
        Map<String, Integer> actualTableColumns = actual.get("test_table");
        assertThat(actualTableColumns.size(), is(1));
        assertThat(actualTableColumns.get("BLOB_COL"), is(2));
        verify(preparedStatement).setString(1, "TEST_TABLE");
    }
    
    @Test
    void assertLoadReturnsEmptyWhenNoTables() throws SQLException {
        material = new MetaDataLoaderMaterial(Collections.emptyList(), "logic_ds", dataSource,
                TypedSPILoader.getService(DatabaseType.class, "Firebird"), "schema");
        Map<String, Map<String, Integer>> actual = new FirebirdBlobColumnLoader(material).load();
        assertTrue(actual.isEmpty());
    }
}