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
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(MockitoExtension.class)
class FirebirdNonFixedLengthColumnSizeLoaderTest {
    
    private static final Collection<String> TABLES = Collections.singleton("test_table");
    
    @Mock
    private DataSource dataSource;
    
    @Mock
    private Connection connection;
    
    @Mock
    private DatabaseMetaData databaseMetaData;
    
    @Mock
    private ResultSet columnsResultSet;
    
    private MetaDataLoaderMaterial material;
    
    @BeforeEach
    void setUp() throws SQLException {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "Firebird");
        material = new MetaDataLoaderMaterial(TABLES, "logic_ds", dataSource, databaseType, "schema");
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(connection.getCatalog()).thenReturn("catalog");
        when(connection.getSchema()).thenReturn("schema");
        when(databaseMetaData.getColumns("catalog", "schema", "TEST_TABLE", "%")).thenReturn(columnsResultSet);
    }
    
    @Test
    void assertLoadReturnsNonFixedLengthSizes() throws SQLException {
        when(columnsResultSet.next()).thenReturn(true, true, false);
        when(columnsResultSet.getString("TABLE_NAME")).thenReturn("TEST_TABLE", "TEST_TABLE");
        when(columnsResultSet.getInt("DATA_TYPE")).thenReturn(Types.CHAR, Types.VARBINARY);
        when(columnsResultSet.getString("COLUMN_NAME")).thenReturn("char_col", "varbinary_col");
        when(columnsResultSet.getInt("COLUMN_SIZE")).thenReturn(128, 64);
        when(columnsResultSet.wasNull()).thenReturn(false, false);
        Map<String, Map<String, Integer>> actual = new FirebirdNonFixedLengthColumnSizeLoader(material).load();
        assertThat(actual, hasKey("test_table"));
        Map<String, Integer> actualTableSizes = actual.get("test_table");
        assertThat(actualTableSizes.size(), is(2));
        assertThat(actualTableSizes.get("CHAR_COL"), is(128));
        assertFalse(actualTableSizes.containsKey("BIGINT_COL"));
        assertThat(actualTableSizes.get("VARBINARY_COL"), is(64));
    }
}
