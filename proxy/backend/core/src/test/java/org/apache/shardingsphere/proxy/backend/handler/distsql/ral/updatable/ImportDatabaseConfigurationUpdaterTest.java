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

import org.apache.shardingsphere.distsql.handler.validate.DataSourcePropertiesValidateHandler;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.ImportDatabaseConfigurationStatement;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.checker.DatabaseDiscoveryRuleConfigurationImportChecker;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.checker.EncryptRuleConfigurationImportChecker;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.checker.MaskRuleConfigurationImportChecker;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.checker.ReadwriteSplittingRuleConfigurationImportChecker;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.checker.ShadowRuleConfigurationImportChecker;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.checker.ShardingRuleConfigurationImportChecker;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ImportDatabaseConfigurationUpdaterTest {
    
    private final String sharding = "sharding_db";
    
    private final String readwriteSplitting = "readwrite_splitting_db";
    
    private final String databaseDiscovery = "database_discovery_db";
    
    private final String encrypt = "encrypt_db";
    
    private final String shadow = "shadow_db";
    
    private final String mask = "mask_db";
    
    private final MockedStatic<ProxyContext> proxyContext = mockStatic(ProxyContext.class, RETURNS_DEEP_STUBS);
    
    @Mock
    private DataSourcePropertiesValidateHandler validateHandler;
    
    @Mock
    private ShardingRuleConfigurationImportChecker shardingRuleConfigurationImportChecker;
    
    @Mock
    private ReadwriteSplittingRuleConfigurationImportChecker readwriteSplittingRuleConfigurationImportChecker;
    
    @Mock
    private DatabaseDiscoveryRuleConfigurationImportChecker databaseDiscoveryRuleConfigurationImportChecker;
    
    @Mock
    private EncryptRuleConfigurationImportChecker encryptRuleConfigurationImportChecker;
    
    @Mock
    private ShadowRuleConfigurationImportChecker shadowRuleConfigurationImportChecker;
    
    @Mock
    private MaskRuleConfigurationImportChecker maskRuleConfigurationImportChecker;
    
    private ImportDatabaseConfigurationUpdater importDatabaseConfigurationUpdater;
    
    private final Map<String, String> featureMap = new HashMap<>(3, 1);
    
    @Before
    public void setup() {
        featureMap.put(sharding, "/conf/import/config-sharding.yaml");
        featureMap.put(readwriteSplitting, "/conf/import/config-readwrite-splitting.yaml");
        featureMap.put(databaseDiscovery, "/conf/import/config-database-discovery.yaml");
        featureMap.put(encrypt, "/conf/import/config-encrypt.yaml");
        featureMap.put(shadow, "/conf/import/config-shadow.yaml");
        featureMap.put(mask, "/conf/import/config-mask.yaml");
    }
    
    @After
    public void tearDown() {
        proxyContext.close();
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertImportDatabaseExecutorForSharding() throws ReflectiveOperationException, SQLException {
        init(sharding);
        Plugins.getMemberAccessor().set(importDatabaseConfigurationUpdater.getClass().getDeclaredField("shardingRuleConfigurationImportChecker"),
                importDatabaseConfigurationUpdater, shardingRuleConfigurationImportChecker);
        importDatabaseConfigurationUpdater.executeUpdate(sharding,
                new ImportDatabaseConfigurationStatement(Objects.requireNonNull(ImportDatabaseConfigurationUpdaterTest.class.getResource(featureMap.get(sharding))).getPath()));
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertImportDatabaseExecutorForReadwriteSplitting() throws ReflectiveOperationException, SQLException {
        init(readwriteSplitting);
        Plugins.getMemberAccessor().set(importDatabaseConfigurationUpdater.getClass().getDeclaredField("readwriteSplittingRuleConfigurationImportChecker"),
                importDatabaseConfigurationUpdater, readwriteSplittingRuleConfigurationImportChecker);
        importDatabaseConfigurationUpdater.executeUpdate(readwriteSplitting,
                new ImportDatabaseConfigurationStatement(Objects.requireNonNull(ImportDatabaseConfigurationUpdaterTest.class.getResource(featureMap.get(readwriteSplitting))).getPath()));
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertImportDatabaseExecutorForDatabaseDiscovery() throws ReflectiveOperationException, SQLException {
        init(databaseDiscovery);
        Plugins.getMemberAccessor().set(importDatabaseConfigurationUpdater.getClass().getDeclaredField("databaseDiscoveryRuleConfigurationImportChecker"),
                importDatabaseConfigurationUpdater, databaseDiscoveryRuleConfigurationImportChecker);
        importDatabaseConfigurationUpdater.executeUpdate(databaseDiscovery,
                new ImportDatabaseConfigurationStatement(Objects.requireNonNull(ImportDatabaseConfigurationUpdaterTest.class.getResource(featureMap.get(databaseDiscovery))).getPath()));
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertImportDatabaseExecutorForEncrypt() throws ReflectiveOperationException, SQLException {
        init(encrypt);
        Plugins.getMemberAccessor().set(importDatabaseConfigurationUpdater.getClass().getDeclaredField("encryptRuleConfigurationImportChecker"),
                importDatabaseConfigurationUpdater, encryptRuleConfigurationImportChecker);
        importDatabaseConfigurationUpdater.executeUpdate(encrypt,
                new ImportDatabaseConfigurationStatement(Objects.requireNonNull(ImportDatabaseConfigurationUpdaterTest.class.getResource(featureMap.get(encrypt))).getPath()));
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertImportDatabaseExecutorForShadow() throws ReflectiveOperationException, SQLException {
        init(shadow);
        Plugins.getMemberAccessor().set(importDatabaseConfigurationUpdater.getClass().getDeclaredField("shadowRuleConfigurationImportChecker"),
                importDatabaseConfigurationUpdater, shadowRuleConfigurationImportChecker);
        importDatabaseConfigurationUpdater.executeUpdate(shadow,
                new ImportDatabaseConfigurationStatement(Objects.requireNonNull(ImportDatabaseConfigurationUpdaterTest.class.getResource(featureMap.get(shadow))).getPath()));
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertImportDatabaseExecutorForMask() throws ReflectiveOperationException, SQLException {
        init(mask);
        Plugins.getMemberAccessor().set(importDatabaseConfigurationUpdater.getClass().getDeclaredField("maskRuleConfigurationImportChecker"),
                importDatabaseConfigurationUpdater, maskRuleConfigurationImportChecker);
        importDatabaseConfigurationUpdater.executeUpdate(mask,
                new ImportDatabaseConfigurationStatement(Objects.requireNonNull(ImportDatabaseConfigurationUpdaterTest.class.getResource(featureMap.get(mask))).getPath()));
    }
    
    private void init(final String feature) throws ReflectiveOperationException {
        importDatabaseConfigurationUpdater = new ImportDatabaseConfigurationUpdater();
        Plugins.getMemberAccessor().set(importDatabaseConfigurationUpdater.getClass().getDeclaredField("validateHandler"), importDatabaseConfigurationUpdater, validateHandler);
        ContextManager contextManager = mockContextManager(feature);
        proxyContext.when(() -> ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        proxyContext.when(() -> ProxyContext.getInstance().databaseExists(feature)).thenReturn(true);
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
