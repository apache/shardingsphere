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
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.type.TableMetaDataLoader;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.SchemaMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.firebird.metadata.data.FirebirdBlobInfoRegistry;
import org.apache.shardingsphere.database.connector.firebird.metadata.data.FirebirdNonFixedLengthColumnSizeRegistry;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FirebirdMetaDataLoaderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "Firebird");
    
    @Test
    void assertLoadRefreshesSizeRegistry() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        MetaDataLoaderMaterial material = new MetaDataLoaderMaterial(Collections.singleton("test_table"), "logic_ds", dataSource, databaseType, "schema");
        TableMetaData tableMetaData = new TableMetaData("test_table", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        Map<String, Integer> tableSizes = Collections.singletonMap("COLUMN", 16);
        Map<String, Map<String, Integer>> allSizes = Collections.singletonMap("test_table", tableSizes);
        Map<String, Integer> tableBlobColumns = Collections.singletonMap("BLOB_COL", 1);
        Map<String, Map<String, Integer>> allBlobColumns = Collections.singletonMap("test_table", tableBlobColumns);
        try (
                MockedStatic<TableMetaDataLoader> tableLoaderMocked = mockStatic(TableMetaDataLoader.class);
                MockedStatic<FirebirdNonFixedLengthColumnSizeRegistry> sizeRegistryMocked = mockStatic(FirebirdNonFixedLengthColumnSizeRegistry.class);
                MockedStatic<FirebirdBlobInfoRegistry> blobRegistryMocked = mockStatic(FirebirdBlobInfoRegistry.class);
                MockedConstruction<FirebirdNonFixedLengthColumnSizeLoader> columnSizeLoaderMocked =
                        mockConstruction(FirebirdNonFixedLengthColumnSizeLoader.class, (mock, context) -> when(mock.load()).thenReturn(allSizes));
                MockedConstruction<FirebirdBlobColumnLoader> blobColumnLoaderMocked =
                        mockConstruction(FirebirdBlobColumnLoader.class, (mock, context) -> when(mock.load()).thenReturn(allBlobColumns))) {
            tableLoaderMocked.when(() -> TableMetaDataLoader.load(dataSource, "test_table", databaseType)).thenReturn(Optional.of(tableMetaData));
            Collection<SchemaMetaData> actual = new FirebirdMetaDataLoader().load(material);
            assertThat(actual, hasSize(1));
            SchemaMetaData schema = actual.iterator().next();
            assertThat(schema.getName(), is("schema"));
            assertThat(schema.getTables(), contains(tableMetaData));
            sizeRegistryMocked.verify(() -> FirebirdNonFixedLengthColumnSizeRegistry.refreshTable("schema", "test_table", tableSizes));
            blobRegistryMocked.verify(() -> FirebirdBlobInfoRegistry.refreshTable("schema", "test_table", tableBlobColumns));
            verify(columnSizeLoaderMocked.constructed().get(0)).load();
            verify(blobColumnLoaderMocked.constructed().get(0)).load();
        }
    }
}
