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

import org.apache.shardingsphere.distsql.handler.exception.datasource.MissingRequiredDataSourcesException;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.ImportMetaDataStatement;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

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
@MockitoSettings(strictness = Strictness.LENIENT)
class ImportMetaDataUpdaterTest {
    
    private static final String METADATA_VALUE = "eyJtZXRhX2RhdGEiOnsiZGF0YWJhc2VzIjp7InNoYXJkaW5nX2RiIjoiZGF0YWJhc2VOYW1lOiBzaGFyZGluZ19kYlxuZGF0YVNvdXJjZXM6XG5ydWxlczpcbiJ9LCJwcm9w"
            + "cyI6InByb3BzOlxuICBzeXN0ZW0tbG9nLWxldmVsOiBJTkZPXG4gIHNxbC1zaG93OiBmYWxzZVxuIiwicnVsZXMiOiJydWxlczpcbi0gIUFVVEhPUklUWVxuICBwcml2aWxlZ2U6XG4gICAgdHlwZTogQUxMX1BFUk1JVFRFR"
            + "FxuICB1c2VyczpcbiAgLSBhdXRoZW50aWNhdGlvbk1ldGhvZE5hbWU6ICcnXG4gICAgcGFzc3dvcmQ6IHJvb3RcbiAgICB1c2VyOiByb290QCVcbiJ9fQ==";
    
    private static final String EMPTY = "empty_metadata";
    
    private ImportMetaDataUpdater importMetaDataUpdater;
    
    private final Map<String, String> featureMap = new HashMap<>(1, 1F);
    
    @BeforeEach
    void setup() {
        featureMap.put(EMPTY, "/conf/import/empty-metadata.json");
    }
    
    @Test
    void assertCheckImportEmptyMetaData() {
        init(null);
        assertThrows(MissingRequiredDataSourcesException.class, () -> importMetaDataUpdater.executeUpdate(
                EMPTY, new ImportMetaDataStatement(null, Objects.requireNonNull(ImportMetaDataUpdaterTest.class.getResource(featureMap.get(EMPTY))).getPath())));
    }
    
    @Test
    void assertImportMetaDataFromJsonValue() {
        init(EMPTY);
        assertThrows(NullPointerException.class, () -> importMetaDataUpdater.executeUpdate(EMPTY, new ImportMetaDataStatement(METADATA_VALUE, null)));
    }
    
    @Test
    void assertImportExistedMetaDataFromFile() {
        init(EMPTY);
        assertThrows(UnsupportedSQLOperationException.class, () -> importMetaDataUpdater.executeUpdate(
                EMPTY, new ImportMetaDataStatement(null, Objects.requireNonNull(ImportMetaDataUpdaterTest.class.getResource(featureMap.get(EMPTY))).getPath())));
    }
    
    private void init(final String feature) {
        importMetaDataUpdater = new ImportMetaDataUpdater();
        ContextManager contextManager = mockContextManager(feature);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().databaseExists(feature)).thenReturn(true);
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
        Map<String, DataSource> result = new LinkedHashMap<>(2, 1F);
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
