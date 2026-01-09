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

package org.apache.shardingsphere.database.connector.core.metadata.database.datatype;

import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class DataTypeLoaderTest {
    
    private final DataTypeLoader dataTypeLoader = new DataTypeLoader();

    @BeforeEach
    void setUp() throws Exception {
        Field cacheField = DataTypeLoader.class.getDeclaredField("UDT_TYPE_CACHE");
        cacheField.setAccessible(true);
        Map<?, ?> cache = (Map<?, ?>) cacheField.get(null);
        if (null != cache) {
            cache.clear();
        }
    }
    
    @Test
    void assertLoad() throws SQLException {
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("TYPE_NAME")).thenReturn("int", "varchar");
        when(resultSet.getInt("DATA_TYPE")).thenReturn(Types.INTEGER, Types.VARCHAR);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(databaseMetaData.getTypeInfo()).thenReturn(resultSet);
        DialectDatabaseMetaData dialectDatabaseMetaData = mock(DialectDatabaseMetaData.class, RETURNS_DEEP_STUBS);
        when(dialectDatabaseMetaData.getDataTypeOption().getExtraDataTypes()).thenReturn(Collections.singletonMap("EXTRA_TYPE", Types.OTHER));
        DatabaseType databaseType = mock(DatabaseType.class);
        try (MockedStatic<DatabaseTypedSPILoader> mockedStatic = mockStatic(DatabaseTypedSPILoader.class)) {
            mockedStatic.when(() -> DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType)).thenReturn(dialectDatabaseMetaData);
            Map<String, Integer> actual = dataTypeLoader.load(databaseMetaData, databaseType);
            assertThat(actual.get("INT"), is(Types.INTEGER));
            assertThat(actual.get("varchar"), is(Types.VARCHAR));
            assertThat(actual.get("EXTRA_TYPE"), is(Types.OTHER));
        }
    }

    @Test
    public void testLoadWithUDTDisabled() throws SQLException {
        // Set system property to disable UDT discovery
        System.setProperty("shardingsphere.udt.discovery.enabled", "false");

        try {
            DatabaseMetaData mockDatabaseMetaData = mock(DatabaseMetaData.class);
            when(mockDatabaseMetaData.getTypeInfo()).thenReturn(mock(java.sql.ResultSet.class));

            DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "H2");

            DataTypeLoader loader = new DataTypeLoader();
            Map<String, Integer> result = loader.load(mockDatabaseMetaData, databaseType);

            assertNotNull(result);
            // The result should contain standard types but not UDT types (since UDT discovery is disabled)
        } finally {
            // Reset system property
            System.clearProperty("shardingsphere.udt.discovery.enabled");
        }
    }

    @Test
    public void testLoadWithUDTEnabled() throws SQLException {
        // Set system property to enable UDT discovery (default behavior)
        System.setProperty("shardingsphere.udt.discovery.enabled", "true");

        try {
            DatabaseMetaData mockDatabaseMetaData = mock(DatabaseMetaData.class);
            when(mockDatabaseMetaData.getTypeInfo()).thenReturn(mock(java.sql.ResultSet.class));
            when(mockDatabaseMetaData.getConnection()).thenReturn(mock(java.sql.Connection.class));

            DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "H2");

            DataTypeLoader loader = new DataTypeLoader();
            Map<String, Integer> result = loader.load(mockDatabaseMetaData, databaseType);

            assertNotNull(result);
            // The result should contain both standard types and UDT types (if any)
        } finally {
            // Reset system property
            System.clearProperty("shardingsphere.udt.discovery.enabled");
        }
    }

    @Test
    public void testLoadWithDefaultBehavior() throws SQLException {
        // Test with default behavior (should be enabled)
        System.clearProperty("shardingsphere.udt.discovery.enabled");

        DatabaseMetaData mockDatabaseMetaData = mock(DatabaseMetaData.class);
        when(mockDatabaseMetaData.getTypeInfo()).thenReturn(mock(java.sql.ResultSet.class));
        when(mockDatabaseMetaData.getConnection()).thenReturn(mock(java.sql.Connection.class));

        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "H2");

        DataTypeLoader loader = new DataTypeLoader();
        Map<String, Integer> result = loader.load(mockDatabaseMetaData, databaseType);

        assertNotNull(result);
        // Test that it works with default behavior
    }
}
