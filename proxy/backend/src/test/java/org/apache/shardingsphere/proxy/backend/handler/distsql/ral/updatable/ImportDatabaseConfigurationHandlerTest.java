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
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesValidator;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.checker.DatabaseDiscoveryRuleConfigurationImportChecker;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.checker.ReadwriteSplittingRuleConfigurationImportChecker;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.checker.ShardingRuleConfigurationImportChecker;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ImportDatabaseConfigurationHandlerTest extends ProxyContextRestorer {
    
    private final String shardingFilePath = "/conf/import/config-sharding.yaml";
    
    private final String readwriteSplittingFilePath = "/conf/import/config-readwrite-splitting.yaml";
    
    private final String dbDiscoveryFilePath = "/conf/import/config-database-discovery.yaml";
    
    private final String sharding = "sharding_db";
    
    private final String readwriteSplitting = "readwrite_splitting_db";
    
    private final String databaseDiscovery = "database_discovery_db";
    
    @Mock
    private DataSourcePropertiesValidator validator;
    
    @Mock
    private ShardingRuleConfigurationImportChecker shardingRuleConfigurationImportChecker;
    
    @Mock
    private ReadwriteSplittingRuleConfigurationImportChecker readwriteSplittingRuleConfigurationImportChecker;
    
    @Mock
    private DatabaseDiscoveryRuleConfigurationImportChecker databaseDiscoveryRuleConfigurationImportChecker;
    
    private ImportDatabaseConfigurationHandler importDatabaseConfigurationHandler;
    
    private final Map<String, String> featureMap = new HashMap<>(3, 1);
    
    @Before
    public void setup() {
        featureMap.put(sharding, shardingFilePath);
        featureMap.put(readwriteSplitting, readwriteSplittingFilePath);
        featureMap.put(databaseDiscovery, dbDiscoveryFilePath);
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertImportDatabaseExecutorForSharding() throws Exception {
        init(sharding);
        Field shardingRuleConfigurationImportCheckerField = importDatabaseConfigurationHandler.getClass().getDeclaredField("shardingRuleConfigurationImportChecker");
        shardingRuleConfigurationImportCheckerField.setAccessible(true);
        shardingRuleConfigurationImportCheckerField.set(importDatabaseConfigurationHandler, shardingRuleConfigurationImportChecker);
        assertNotNull(ProxyContext.getInstance().getContextManager().getDataSourceMap(sharding));
        assertNotNull(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabase(sharding).getRuleMetaData().getConfigurations());
        assertThat(importDatabaseConfigurationHandler.execute(), instanceOf(UpdateResponseHeader.class));
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertImportDatabaseExecutorForReadwriteSplitting() throws Exception {
        init(readwriteSplitting);
        Field readwriteSplittingRuleConfigurationImportCheckerField = importDatabaseConfigurationHandler.getClass().getDeclaredField("readwriteSplittingRuleConfigurationImportChecker");
        readwriteSplittingRuleConfigurationImportCheckerField.setAccessible(true);
        readwriteSplittingRuleConfigurationImportCheckerField.set(importDatabaseConfigurationHandler, readwriteSplittingRuleConfigurationImportChecker);
        assertNotNull(ProxyContext.getInstance().getContextManager().getDataSourceMap(readwriteSplitting));
        assertNotNull(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabase(readwriteSplitting).getRuleMetaData().getConfigurations());
        assertThat(importDatabaseConfigurationHandler.execute(), instanceOf(UpdateResponseHeader.class));
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertImportDatabaseExecutorForDatabaseDiscovery() throws Exception {
        init(databaseDiscovery);
        Field databaseDiscoveryRuleConfigurationImportCheckerField = importDatabaseConfigurationHandler.getClass().getDeclaredField("databaseDiscoveryRuleConfigurationImportChecker");
        databaseDiscoveryRuleConfigurationImportCheckerField.setAccessible(true);
        databaseDiscoveryRuleConfigurationImportCheckerField.set(importDatabaseConfigurationHandler, databaseDiscoveryRuleConfigurationImportChecker);
        assertNotNull(ProxyContext.getInstance().getContextManager().getDataSourceMap(databaseDiscovery));
        assertNotNull(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabase(databaseDiscovery).getRuleMetaData().getConfigurations());
        assertThat(importDatabaseConfigurationHandler.execute(), instanceOf(UpdateResponseHeader.class));
    }
    
    private void init(final String feature) throws Exception {
        ImportDatabaseConfigurationHandler handler = importDatabaseConfigurationHandler = new ImportDatabaseConfigurationHandler();
        handler.init(new ImportDatabaseConfigurationStatement(Objects.requireNonNull(ImportDatabaseConfigurationHandlerTest.class.getResource(featureMap.get(feature))).getPath()),
                mock(ConnectionSession.class));
        Field validatorField = importDatabaseConfigurationHandler.getClass().getDeclaredField("validator");
        validatorField.setAccessible(true);
        validatorField.set(importDatabaseConfigurationHandler, validator);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(new ShardingSphereSchema(createTableMap(), Collections.emptyMap()));
        when(database.getResourceMetaData().getDataSources()).thenReturn(createDataSourceMap());
        when(contextManager.getMetaDataContexts().getMetaData().getDatabases()).thenReturn(Collections.singletonMap(feature, database));
        when(contextManager.getMetaDataContexts().getMetaData().getDatabase(feature)).thenReturn(database);
        when(contextManager.getMetaDataContexts().getMetaData().containsDatabase(feature)).thenReturn(true);
        ProxyContext.init(contextManager);
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new LinkedHashMap<>(2, 1);
        result.put("ds_0", new MockedDataSource());
        result.put("ds_1", new MockedDataSource());
        return result;
    }
    
    private Map<String, ShardingSphereTable> createTableMap() {
        Collection<ShardingSphereColumn> columns = Collections.singleton(new ShardingSphereColumn("order_id", 0, false, false, false, true));
        Collection<ShardingSphereIndex> indexes = Collections.singleton(new ShardingSphereIndex("primary"));
        return Collections.singletonMap("t_order", new ShardingSphereTable("t_order", columns, indexes, Collections.emptyList()));
    }
}
