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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable;

import lombok.SneakyThrows;
import org.apache.groovy.util.Maps;
import org.apache.shardingsphere.distsql.handler.validate.DistSQLDataSourcePoolPropertiesValidator;
import org.apache.shardingsphere.distsql.statement.ral.updatable.ImportMetaDataStatement;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.exception.dialect.exception.syntax.database.DatabaseCreateExistsException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.EmptyStorageUnitException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.util.YamlDatabaseConfigurationImportExecutor;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ImportMetaDataExecutorTest {
    
    private static final String METADATA_VALUE = "eyJtZXRhX2RhdGEiOnsiZGF0YWJhc2VzIjp7InNoYXJkaW5nX2RiIjoiZGF0YWJhc2VOYW1lOiBzaGFyZGluZ19kYlxuZGF0YVNvdXJjZXM6XG4gIGRzXzA6XG4gICAgcGFzc3dvcmQ6IFxu"
            + "ICAgIGRhdGFTb3VyY2VDbGFzc05hbWU6IG51bGxcbiAgICB1cmw6IGpkYmM6bXlzcWw6Ly8xMjcuMC4wLjE6MzMwNi9kZW1vX2RzXzA/dXNlU1NMPWZhbHNlXG4gICAgdXNlcm5hbWU6IHJvb3RcbiAgICBtaW5Qb29sU2l6ZTogMVxuICAgI"
            + "GNvbm5lY3Rpb25UaW1lb3V0TWlsbGlzZWNvbmRzOiAzMDAwMFxuICAgIG1heExpZmV0aW1lTWlsbGlzZWNvbmRzOiAxODAwMDAwXG4gICAgaWRsZVRpbWVvdXRNaWxsaXNlY29uZHM6IDYwMDAwXG4gICAgbWF4UG9vbFNpemU6IDUwXG4gIG"
            + "RzXzE6XG4gICAgcGFzc3dvcmQ6IFxuICAgIGRhdGFTb3VyY2VDbGFzc05hbWU6IG51bGxcbiAgICB1cmw6IGpkYmM6bXlzcWw6Ly8xMjcuMC4wLjE6MzMwNi9kZW1vX2RzXzE/dXNlU1NMPWZhbHNlXG4gICAgdXNlcm5hbWU6IHJvb3RcbiA"
            + "gICBtaW5Qb29sU2l6ZTogMVxuICAgIGNvbm5lY3Rpb25UaW1lb3V0TWlsbGlzZWNvbmRzOiAzMDAwMFxuICAgIG1heExpZmV0aW1lTWlsbGlzZWNvbmRzOiAxODAwMDAwXG4gICAgaWRsZVRpbWVvdXRNaWxsaXNlY29uZHM6IDYwMDAwXG4g"
            + "ICAgbWF4UG9vbFNpemU6IDUwXG5ydWxlczpcbiJ9LCJwcm9wcyI6InByb3BzOlxuICBzeXN0ZW0tbG9nLWxldmVsOiBJTkZPXG4gIHNxbC1zaG93OiBmYWxzZVxuIiwicnVsZXMiOiJydWxlczpcbi0gIUFVVEhPUklUWVxuICBwcml2aWxlZ"
            + "2U6XG4gICAgdHlwZTogQUxMX1BFUk1JVFRFRFxuICB1c2VyczpcbiAgLSBhdXRoZW50aWNhdGlvbk1ldGhvZE5hbWU6ICcnXG4gICAgcGFzc3dvcmQ6IHJvb3RcbiAgICB1c2VyOiByb290QCVcbiJ9fQ==";
    
    private static final String EMPTY = "empty_metadata";
    
    private ImportMetaDataExecutor executor;
    
    private final Map<String, String> featureMap = new HashMap<>(1, 1F);
    
    @BeforeEach
    void setup() {
        featureMap.put(EMPTY, "/conf/import/empty-metadata.json");
    }
    
    @Test
    void assertImportEmptyMetaData() {
        init(null);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        assertThrows(EmptyStorageUnitException.class, () -> executor.executeUpdate(
                new ImportMetaDataStatement(null, Objects.requireNonNull(ImportMetaDataExecutorTest.class.getResource(featureMap.get(EMPTY))).getPath()), contextManager));
    }
    
    @Test
    void assertImportMetaDataFromJsonValue() throws SQLException {
        init(EMPTY);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        executor.executeUpdate(new ImportMetaDataStatement(METADATA_VALUE, null), contextManager);
        assertNotNull(contextManager.getDatabase("sharding_db"));
    }
    
    @Test
    void assertImportExistedMetaDataFromFile() {
        init(EMPTY);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        assertThrows(DatabaseCreateExistsException.class, () -> executor.executeUpdate(
                new ImportMetaDataStatement(null, Objects.requireNonNull(ImportMetaDataExecutorTest.class.getResource(featureMap.get(EMPTY))).getPath()), contextManager));
    }
    
    @SneakyThrows({IllegalAccessException.class, NoSuchFieldException.class})
    private void init(final String feature) {
        executor = new ImportMetaDataExecutor();
        ContextManager contextManager = mockContextManager(feature);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().databaseExists(feature)).thenReturn(true);
        YamlDatabaseConfigurationImportExecutor databaseConfigImportExecutor = new YamlDatabaseConfigurationImportExecutor();
        Plugins.getMemberAccessor().set(ImportMetaDataExecutor.class.getDeclaredField("databaseConfigImportExecutor"), executor, databaseConfigImportExecutor);
        Plugins.getMemberAccessor().set(
                YamlDatabaseConfigurationImportExecutor.class.getDeclaredField("validateHandler"), databaseConfigImportExecutor, mock(DistSQLDataSourcePoolPropertiesValidator.class));
    }
    
    private ContextManager mockContextManager(final String feature) {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts().getMetaData().getProps())
                .thenReturn(new ConfigurationProperties(PropertiesBuilder.build(new Property(ConfigurationPropertyKey.PROXY_FRONTEND_DATABASE_PROTOCOL_TYPE.getKey(), "MySQL"))));
        if (null != feature) {
            ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
            when(database.getName()).thenReturn(feature);
            when(database.getSchema("foo_db")).thenReturn(new ShardingSphereSchema("foo_db", createTables(), Collections.emptyList()));
            Map<String, StorageUnit> storageUnits = createStorageUnits();
            when(database.getResourceMetaData().getStorageUnits()).thenReturn(storageUnits);
            when(result.getMetaDataContexts().getMetaData().getAllDatabases()).thenReturn(Collections.singleton(database));
            when(result.getMetaDataContexts().getMetaData().getDatabase(feature)).thenReturn(database);
        }
        return result;
    }
    
    private Map<String, StorageUnit> createStorageUnits() {
        Map<String, StorageUnit> result = new LinkedHashMap<>(2, 1F);
        DataSourcePoolProperties dataSourcePoolProps0 = mock(DataSourcePoolProperties.class, RETURNS_DEEP_STUBS);
        when(dataSourcePoolProps0.getConnectionPropertySynonyms().getStandardProperties()).thenReturn(Maps.of("url", "jdbc:mock://127.0.0.1/ds_0", "username", "test"));
        result.put("ds_0", new StorageUnit(mock(StorageNode.class), dataSourcePoolProps0, new MockedDataSource()));
        DataSourcePoolProperties dataSourcePoolProps1 = mock(DataSourcePoolProperties.class, RETURNS_DEEP_STUBS);
        when(dataSourcePoolProps1.getConnectionPropertySynonyms().getStandardProperties()).thenReturn(Maps.of("url", "jdbc:mock://127.0.0.1/ds_1", "username", "test"));
        result.put("ds_1", new StorageUnit(mock(StorageNode.class), dataSourcePoolProps1, new MockedDataSource()));
        return result;
    }
    
    private Collection<ShardingSphereTable> createTables() {
        Collection<ShardingSphereColumn> columns = Collections.singleton(new ShardingSphereColumn("order_id", 0, false, false, false, true, false, false));
        Collection<ShardingSphereIndex> indexes = Collections.singleton(new ShardingSphereIndex("primary", Collections.emptyList(), false));
        return Collections.singletonList(new ShardingSphereTable("t_order", columns, indexes, Collections.emptyList()));
    }
}
