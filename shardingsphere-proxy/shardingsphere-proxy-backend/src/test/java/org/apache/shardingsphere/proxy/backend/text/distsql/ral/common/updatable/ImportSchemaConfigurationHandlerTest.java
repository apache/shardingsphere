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

import org.apache.shardingsphere.distsql.parser.statement.ral.common.updatable.ImportSchemaConfigurationStatement;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesValidator;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
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
import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ImportSchemaConfigurationHandlerTest {
    
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
    
    private ImportSchemaConfigurationHandler importSchemaConfigurationHandler;
    
    private final Map<String, String> featureMap = new HashMap<>();
    
    @Before
    public void setup() {
        featureMap.put(sharding, shardingFilePath);
        featureMap.put(readwriteSplitting, readwriteSplittingFilePath);
        featureMap.put(databaseDiscovery, dbDiscoveryFilePath);
    }
    
    private void init(final String feature) throws Exception {
        importSchemaConfigurationHandler = new ImportSchemaConfigurationHandler().init(getParameter(createSqlStatement(featureMap.get(feature)), mockConnectionSession()));
        Field validatorField = importSchemaConfigurationHandler.getClass().getDeclaredField("validator");
        validatorField.setAccessible(true);
        validatorField.set(importSchemaConfigurationHandler, validator);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getAllSchemaNames()).thenReturn(Collections.singletonList(feature));
        ShardingSphereMetaData shardingSphereMetaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(shardingSphereMetaData.getDefaultSchema()).thenReturn(new ShardingSphereSchema(createTableMap()));
        when(shardingSphereMetaData.getResource().getDataSources()).thenReturn(createDataSourceMap());
        when(contextManager.getMetaDataContexts().getMetaData(feature)).thenReturn(shardingSphereMetaData);
        ProxyContext.getInstance().init(contextManager);
    }
    
    @Test
    public void assertImportSchemaExecutorForSharding() throws Exception {
        init(sharding);
        Field shardingRuleConfigurationImportCheckerField = importSchemaConfigurationHandler.getClass().getDeclaredField("shardingRuleConfigurationImportChecker");
        shardingRuleConfigurationImportCheckerField.setAccessible(true);
        shardingRuleConfigurationImportCheckerField.set(importSchemaConfigurationHandler, shardingRuleConfigurationImportChecker);
        Map<String, DataSource> dataSourceMap = ProxyContext.getInstance().getContextManager().getDataSourceMap(sharding);
        assertNotNull(dataSourceMap);
        Collection<RuleConfiguration> ruleConfigurations = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData(sharding).getRuleMetaData().getConfigurations();
        assertNotNull(ruleConfigurations);
        ResponseHeader responseHeader = importSchemaConfigurationHandler.execute();
        assertTrue(responseHeader instanceof UpdateResponseHeader);
    }
    
    @Test
    public void assertImportSchemaExecutorForReadwriteSplitting() throws Exception {
        init(readwriteSplitting);
        Field readwriteSplittingRuleConfigurationImportCheckerField = importSchemaConfigurationHandler.getClass().getDeclaredField("readwriteSplittingRuleConfigurationImportChecker");
        readwriteSplittingRuleConfigurationImportCheckerField.setAccessible(true);
        readwriteSplittingRuleConfigurationImportCheckerField.set(importSchemaConfigurationHandler, readwriteSplittingRuleConfigurationImportChecker);
        Map<String, DataSource> dataSourceMap = ProxyContext.getInstance().getContextManager().getDataSourceMap(readwriteSplitting);
        assertNotNull(dataSourceMap);
        Collection<RuleConfiguration> ruleConfigurations = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData(readwriteSplitting).getRuleMetaData().getConfigurations();
        assertNotNull(ruleConfigurations);
        ResponseHeader responseHeader = importSchemaConfigurationHandler.execute();
        assertTrue(responseHeader instanceof UpdateResponseHeader);
    }
    
    @Test
    public void assertImportSchemaExecutorForDatabaseDiscovery() throws Exception {
        init(databaseDiscovery);
        Field databaseDiscoveryRuleConfigurationImportCheckerField = importSchemaConfigurationHandler.getClass().getDeclaredField("databaseDiscoveryRuleConfigurationImportChecker");
        databaseDiscoveryRuleConfigurationImportCheckerField.setAccessible(true);
        databaseDiscoveryRuleConfigurationImportCheckerField.set(importSchemaConfigurationHandler, databaseDiscoveryRuleConfigurationImportChecker);
        Map<String, DataSource> dataSourceMap = ProxyContext.getInstance().getContextManager().getDataSourceMap(databaseDiscovery);
        assertNotNull(dataSourceMap);
        Collection<RuleConfiguration> ruleConfigurations = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData(databaseDiscovery).getRuleMetaData().getConfigurations();
        assertNotNull(ruleConfigurations);
        ResponseHeader responseHeader = importSchemaConfigurationHandler.execute();
        assertTrue(responseHeader instanceof UpdateResponseHeader);
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new LinkedHashMap<>(2, 1);
        result.put("ds_0", createDataSource());
        result.put("ds_1", createDataSource());
        return result;
    }
    
    private DataSource createDataSource() {
        return new MockedDataSource();
    }
    
    private Map<String, TableMetaData> createTableMap() {
        Collection<ColumnMetaData> columns = Collections.singletonList(new ColumnMetaData("order_id", 0, false, false, false));
        Collection<IndexMetaData> indexes = Collections.singletonList(new IndexMetaData("primary"));
        Map<String, TableMetaData> result = new HashMap<>(1, 1);
        result.put("t_order", new TableMetaData("t_order", columns, indexes, Collections.emptyList()));
        return result;
    }
    
    private ImportSchemaConfigurationStatement createSqlStatement(final String importFilePath) {
        ImportSchemaConfigurationStatement result = new ImportSchemaConfigurationStatement();
        result.setFilePath(Optional.of(Objects.requireNonNull(ImportSchemaConfigurationHandlerTest.class.getResource(importFilePath)).getPath()));
        return result;
    }
    
    private ConnectionSession mockConnectionSession() {
        return mock(ConnectionSession.class);
    }
    
    private RALBackendHandler.HandlerParameter<ImportSchemaConfigurationStatement> getParameter(final ImportSchemaConfigurationStatement statement, final ConnectionSession connectionSession) {
        return new RALBackendHandler.HandlerParameter<ImportSchemaConfigurationStatement>().setStatement(statement).setConnectionSession(connectionSession);
    }
}
