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

import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.ImportDatabaseConfigurationStatement;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
public final class ImportDatabaseConfigurationUpdaterTest {
    
    private final String sharding = "sharding_db";
    
    private final String readwriteSplitting = "readwrite_splitting_db";
    
    private final String databaseDiscovery = "database_discovery_db";
    
    private final String encrypt = "encrypt_db";
    
    private final String shadow = "shadow_db";
    
    private final String mask = "mask_db";
    
    private ImportDatabaseConfigurationUpdater importDatabaseConfigurationUpdater;
    
    private final Map<String, String> featureMap = new HashMap<>(3, 1);
    
    @BeforeEach
    public void setup() {
        featureMap.put(sharding, "/conf/import/config-sharding.yaml");
        featureMap.put(readwriteSplitting, "/conf/import/config-readwrite-splitting.yaml");
        featureMap.put(databaseDiscovery, "/conf/import/config-database-discovery.yaml");
        featureMap.put(encrypt, "/conf/import/config-encrypt.yaml");
        featureMap.put(shadow, "/conf/import/config-shadow.yaml");
        featureMap.put(mask, "/conf/import/config-mask.yaml");
    }
    
    @Test
    public void assertImportDatabaseExecutorForSharding() {
        init(sharding);
        assertThrows(IllegalStateException.class, () -> importDatabaseConfigurationUpdater.executeUpdate(sharding,
                new ImportDatabaseConfigurationStatement(Objects.requireNonNull(ImportDatabaseConfigurationUpdaterTest.class.getResource(featureMap.get(sharding))).getPath())));
    }
    
    @Test
    public void assertImportDatabaseExecutorForReadwriteSplitting() {
        init(readwriteSplitting);
        assertThrows(IllegalStateException.class, () -> importDatabaseConfigurationUpdater.executeUpdate(readwriteSplitting,
                new ImportDatabaseConfigurationStatement(Objects.requireNonNull(ImportDatabaseConfigurationUpdaterTest.class.getResource(featureMap.get(readwriteSplitting))).getPath())));
    }
    
    @Test
    public void assertImportDatabaseExecutorForDatabaseDiscovery() {
        init(databaseDiscovery);
        assertThrows(IllegalStateException.class, () -> importDatabaseConfigurationUpdater.executeUpdate(databaseDiscovery,
                new ImportDatabaseConfigurationStatement(Objects.requireNonNull(ImportDatabaseConfigurationUpdaterTest.class.getResource(featureMap.get(databaseDiscovery))).getPath())));
    }
    
    @Test
    public void assertImportDatabaseExecutorForEncrypt() {
        init(encrypt);
        assertThrows(IllegalStateException.class, () -> importDatabaseConfigurationUpdater.executeUpdate(encrypt,
                new ImportDatabaseConfigurationStatement(Objects.requireNonNull(ImportDatabaseConfigurationUpdaterTest.class.getResource(featureMap.get(encrypt))).getPath())));
    }
    
    @Test
    public void assertImportDatabaseExecutorForShadow() {
        init(shadow);
        assertThrows(IllegalStateException.class, () -> importDatabaseConfigurationUpdater.executeUpdate(shadow,
                new ImportDatabaseConfigurationStatement(Objects.requireNonNull(ImportDatabaseConfigurationUpdaterTest.class.getResource(featureMap.get(shadow))).getPath())));
    }
    
    @Test
    public void assertImportDatabaseExecutorForMask() {
        init(mask);
        assertThrows(IllegalStateException.class, () -> importDatabaseConfigurationUpdater.executeUpdate(mask,
                new ImportDatabaseConfigurationStatement(Objects.requireNonNull(ImportDatabaseConfigurationUpdaterTest.class.getResource(featureMap.get(mask))).getPath())));
    }
    
    private void init(final String feature) {
        importDatabaseConfigurationUpdater = new ImportDatabaseConfigurationUpdater();
        ContextManager contextManager = mockContextManager(feature);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().databaseExists(feature)).thenReturn(true);
    }
    
    private ContextManager mockContextManager(final String feature) {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(new ShardingSphereSchema(createTableMap(), Collections.emptyMap()));
        when(database.getResourceMetaData().getDataSources()).thenReturn(createDataSourceMap());
        when(result.getMetaDataContexts().getMetaData().getDatabases()).thenReturn(Collections.singletonMap(feature, database));
        when(result.getMetaDataContexts().getMetaData().getDatabase(feature)).thenReturn(database);
        return result;
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new LinkedHashMap<>(2, 1);
        result.put("ds_0", new MockedDataSource());
        result.put("ds_1", new MockedDataSource());
        return result;
    }
    
    private Map<String, ShardingSphereTable> createTableMap() {
        Collection<ShardingSphereColumn> columns = Collections.singleton(new ShardingSphereColumn("order_id", 0, false, false, false, true, false));
        Collection<ShardingSphereIndex> indexes = Collections.singleton(new ShardingSphereIndex("primary"));
        return Collections.singletonMap("t_order", new ShardingSphereTable("t_order", columns, indexes, Collections.emptyList()));
    }
}
