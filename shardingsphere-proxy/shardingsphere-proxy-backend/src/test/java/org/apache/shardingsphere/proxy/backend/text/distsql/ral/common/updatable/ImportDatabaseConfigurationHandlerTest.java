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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.updatable;

import org.apache.shardingsphere.distsql.parser.statement.ral.common.updatable.ImportDatabaseConfigurationStatement;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesValidator;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.RALBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.checker.DatabaseDiscoveryRuleConfigurationImportChecker;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.checker.ReadwriteSplittingRuleConfigurationImportChecker;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.checker.ShardingRuleConfigurationImportChecker;
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
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ImportDatabaseConfigurationHandlerTest {
    
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
    
    @Test
    public void assertImportDatabaseExecutorForSharding() throws Exception {
        init(sharding);
        Field shardingRuleConfigurationImportCheckerField = importDatabaseConfigurationHandler.getClass().getDeclaredField("shardingRuleConfigurationImportChecker");
        shardingRuleConfigurationImportCheckerField.setAccessible(true);
        shardingRuleConfigurationImportCheckerField.set(importDatabaseConfigurationHandler, shardingRuleConfigurationImportChecker);
        assertNotNull(ProxyContext.getInstance().getContextManager().getDataSourceMap(sharding));
        assertNotNull(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData(sharding).getRuleMetaData().getConfigurations());
        assertThat(importDatabaseConfigurationHandler.execute(), instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    public void assertImportDatabaseExecutorForReadwriteSplitting() throws Exception {
        init(readwriteSplitting);
        Field readwriteSplittingRuleConfigurationImportCheckerField = importDatabaseConfigurationHandler.getClass().getDeclaredField("readwriteSplittingRuleConfigurationImportChecker");
        readwriteSplittingRuleConfigurationImportCheckerField.setAccessible(true);
        readwriteSplittingRuleConfigurationImportCheckerField.set(importDatabaseConfigurationHandler, readwriteSplittingRuleConfigurationImportChecker);
        assertNotNull(ProxyContext.getInstance().getContextManager().getDataSourceMap(readwriteSplitting));
        assertNotNull(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData(readwriteSplitting).getRuleMetaData().getConfigurations());
        assertThat(importDatabaseConfigurationHandler.execute(), instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    public void assertImportDatabaseExecutorForDatabaseDiscovery() throws Exception {
        init(databaseDiscovery);
        Field databaseDiscoveryRuleConfigurationImportCheckerField = importDatabaseConfigurationHandler.getClass().getDeclaredField("databaseDiscoveryRuleConfigurationImportChecker");
        databaseDiscoveryRuleConfigurationImportCheckerField.setAccessible(true);
        databaseDiscoveryRuleConfigurationImportCheckerField.set(importDatabaseConfigurationHandler, databaseDiscoveryRuleConfigurationImportChecker);
        assertNotNull(ProxyContext.getInstance().getContextManager().getDataSourceMap(databaseDiscovery));
        assertNotNull(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData(databaseDiscovery).getRuleMetaData().getConfigurations());
        assertThat(importDatabaseConfigurationHandler.execute(), instanceOf(UpdateResponseHeader.class));
    }
    
    private void init(final String feature) throws Exception {
        importDatabaseConfigurationHandler = new ImportDatabaseConfigurationHandler().init(getParameter(featureMap.get(feature), mock(ConnectionSession.class)));
        Field validatorField = importDatabaseConfigurationHandler.getClass().getDeclaredField("validator");
        validatorField.setAccessible(true);
        validatorField.set(importDatabaseConfigurationHandler, validator);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getAllDatabaseNames()).thenReturn(Collections.singletonList(feature));
        ShardingSphereMetaData shardingSphereMetaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(shardingSphereMetaData.getSchemaByName(DefaultDatabase.LOGIC_NAME)).thenReturn(new ShardingSphereSchema(createTableMap()));
        when(shardingSphereMetaData.getResource().getDataSources()).thenReturn(createDataSourceMap());
        when(contextManager.getMetaDataContexts().getMetaData(feature)).thenReturn(shardingSphereMetaData);
        ProxyContext.getInstance().init(contextManager);
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new LinkedHashMap<>(2, 1);
        result.put("ds_0", new MockedDataSource());
        result.put("ds_1", new MockedDataSource());
        return result;
    }
    
    private Map<String, TableMetaData> createTableMap() {
        Collection<ColumnMetaData> columns = Collections.singleton(new ColumnMetaData("order_id", 0, false, false, false));
        Collection<IndexMetaData> indexes = Collections.singleton(new IndexMetaData("primary"));
        return Collections.singletonMap("t_order", new TableMetaData("t_order", columns, indexes, Collections.emptyList()));
    }
    
    private RALBackendHandler.HandlerParameter<ImportDatabaseConfigurationStatement> getParameter(final String importFilePath, final ConnectionSession connectionSession) {
        ImportDatabaseConfigurationStatement statement = new ImportDatabaseConfigurationStatement(
                Objects.requireNonNull(ImportDatabaseConfigurationHandlerTest.class.getResource(importFilePath)).getPath());
        return new RALBackendHandler.HandlerParameter<ImportDatabaseConfigurationStatement>().setStatement(statement).setConnectionSession(connectionSession);
    }
}
