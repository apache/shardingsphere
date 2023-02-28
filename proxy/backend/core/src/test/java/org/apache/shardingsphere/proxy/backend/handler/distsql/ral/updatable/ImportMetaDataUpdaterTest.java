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

import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.ImportMetaDataStatement;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
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
public final class ImportMetaDataUpdaterTest {
    
    private static final String METADATA_VALUE = "{\"storageNodes\":[{\"ip\":\"127.0.0.1\",\"port\":\"3306\",\"username\":\"root\",\"password\":\"\",\"database\":\"demo_ds_0\"}],"
            + "\"metaData\":{\"databases\":{\"sharding_db\":\"databaseName: sharding_db\\ndataSources:\\nrules:\\n\"},\"props\":\"props:\\n  system-log-level: INFO\\n  sql-show: false\\n\","
            + "\"rules\":\"rules:\\n- !AUTHORITY\\n  privilege:\\n    type: ALL_PERMITTED\\n  users:\\n  - authenticationMethodName: ''\\n    password: root\\n    user: root@%\\n\"}}";
    
    private final String empty = "empty_metadata";
    
    private final MockedStatic<ProxyContext> proxyContext = mockStatic(ProxyContext.class, RETURNS_DEEP_STUBS);
    
    private ImportMetaDataUpdater importMetaDataUpdater;
    
    private final Map<String, String> featureMap = new HashMap<>(1, 1);
    
    @Before
    public void setup() {
        featureMap.put(empty, "/conf/import/empty-metadata.json");
    }
    
    @After
    public void tearDown() {
        proxyContext.close();
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertCheckImportEmptyMetaData() throws SQLException {
        init(null);
        importMetaDataUpdater.executeUpdate(empty, new ImportMetaDataStatement(null, Objects.requireNonNull(ImportMetaDataUpdaterTest.class.getResource(featureMap.get(empty))).getPath()));
    }
    
    @Test(expected = NullPointerException.class)
    public void assertImportMetaDataFromJsonValue() throws SQLException {
        init(empty);
        importMetaDataUpdater.executeUpdate(empty, new ImportMetaDataStatement(METADATA_VALUE, null));
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertImportExistedMetaDataFromFile() throws SQLException {
        init(empty);
        importMetaDataUpdater.executeUpdate(empty,
                new ImportMetaDataStatement(null, Objects.requireNonNull(ImportMetaDataUpdaterTest.class.getResource(featureMap.get(empty))).getPath()));
    }
    
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void init(final String feature) {
        importMetaDataUpdater = new ImportMetaDataUpdater();
        ContextManager contextManager = mockContextManager(feature);
        proxyContext.when(() -> ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        proxyContext.when(() -> ProxyContext.getInstance().databaseExists(feature)).thenReturn(true);
    }
    
    private ContextManager mockContextManager(final String feature) {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts().getMetaData().getProps())
                .thenReturn(new ConfigurationProperties(PropertiesBuilder.build(new Property(ConfigurationPropertyKey.PROXY_FRONTEND_DATABASE_PROTOCOL_TYPE.getKey(), "MySQL"))));
        if (feature != null) {
            ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
            when(database.getSchema(DefaultDatabase.LOGIC_NAME)).thenReturn(new ShardingSphereSchema(createTableMap(), Collections.emptyMap()));
            when(database.getResourceMetaData().getDataSources()).thenReturn(createDataSourceMap());
            when(result.getMetaDataContexts().getMetaData().getDatabases()).thenReturn(Collections.singletonMap(feature, database));
            when(result.getMetaDataContexts().getMetaData().getDatabase(feature)).thenReturn(database);
        }
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
