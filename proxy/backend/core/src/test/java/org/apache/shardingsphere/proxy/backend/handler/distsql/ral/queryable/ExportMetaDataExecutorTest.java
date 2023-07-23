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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable;

import lombok.SneakyThrows;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.authority.rule.builder.DefaultAuthorityRuleConfigurationBuilder;
import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ExportMetaDataStatement;
import org.apache.shardingsphere.globalclock.core.rule.GlobalClockRule;
import org.apache.shardingsphere.globalclock.core.rule.builder.DefaultGlobalClockRuleConfigurationBuilder;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesCreator;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.mode.ModeContextManager;
import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.standalone.workerid.generator.StandaloneWorkerIdGenerator;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class ExportMetaDataExecutorTest {
    
    private static final String METADATA_VALUE_EXPECTED = "eyJtZXRhX2RhdGEiOnsiZGF0YWJhc2VzIjp7ImVtcHR5X21ldGFkYXRhIjoiZGF0YWJhc2VOYW1lOiBudWxsXG5kYXRhU291cmNlczpcbn"
            + "J1bGVzOlxuIn0sInByb3BzIjoiIiwicnVsZXMiOiJydWxlczpcbi0gIUdMT0JBTF9DTE9DS1xuICBlbmFibGVkOiBmYWxzZVxuICBwcm92aWRlcjogbG9jYWxcbiAgdHlwZTogVFNPXG4ifX0=";
    
    private final ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
    
    @Test
    void assertGetColumns() {
        Collection<String> columns = new ExportMetaDataExecutor().getColumnNames();
        assertThat(columns.size(), is(3));
        Iterator<String> columnIterator = columns.iterator();
        assertThat(columnIterator.next(), is("id"));
        assertThat(columnIterator.next(), is("create_time"));
        assertThat(columnIterator.next(), is("cluster_info"));
    }
    
    @Test
    void assertExecuteWithEmptyMetaData() {
        ContextManager contextManager = mockEmptyContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().getAllDatabaseNames()).thenReturn(Collections.singleton("empty_metadata"));
        when(database.getName()).thenReturn("empty_metadata");
        when(database.getResourceMetaData().getAllInstanceDataSourceNames()).thenReturn(Collections.singleton("empty_metadata"));
        when(database.getResourceMetaData().getDataSourcePropsMap()).thenReturn(Collections.emptyMap());
        when(database.getRuleMetaData().getConfigurations()).thenReturn(Collections.emptyList());
        ExportMetaDataStatement sqlStatement = new ExportMetaDataStatement(null);
        Collection<LocalDataQueryResultRow> actual = new ExportMetaDataExecutor().getRows(contextManager.getMetaDataContexts().getMetaData(), sqlStatement);
        assertThat(actual.size(), is(1));
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(3), is(METADATA_VALUE_EXPECTED));
    }
    
    private ContextManager mockEmptyContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class), new ShardingSphereMetaData(new HashMap<>(),
                new ShardingSphereResourceMetaData(Collections.emptyMap()), new ShardingSphereRuleMetaData(Collections.singletonList(
                        new GlobalClockRule(new DefaultGlobalClockRuleConfigurationBuilder().build(), Collections.emptyMap()))),
                new ConfigurationProperties(new Properties())));
        when(result.getMetaDataContexts()).thenReturn(metaDataContexts);
        return result;
    }
    
    @Test
    void assertExecute() {
        when(database.getName()).thenReturn("normal_db");
        when(database.getResourceMetaData().getAllInstanceDataSourceNames()).thenReturn(Collections.singleton("empty_metadata"));
        Map<String, DataSource> dataSourceMap = createDataSourceMap();
        when(database.getResourceMetaData().getDataSourcePropsMap()).thenReturn(DataSourcePropertiesCreator.create(dataSourceMap));
        when(database.getRuleMetaData().getConfigurations()).thenReturn(Collections.emptyList());
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().getAllDatabaseNames()).thenReturn(Collections.singleton("normal_db"));
        when(ProxyContext.getInstance().getDatabase("normal_db")).thenReturn(database);
        Collection<LocalDataQueryResultRow> actual = new ExportMetaDataExecutor().getRows(contextManager.getMetaDataContexts().getMetaData(), new ExportMetaDataStatement(null));
        assertThat(actual.size(), is(1));
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(3).toString(), is(loadExpectedRow()));
    }
    
    private ContextManager mockContextManager() {
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class), new ShardingSphereMetaData(Collections.singletonMap(database.getName(), database),
                new ShardingSphereResourceMetaData(Collections.emptyMap()),
                new ShardingSphereRuleMetaData(Arrays.asList(new AuthorityRule(new DefaultAuthorityRuleConfigurationBuilder().build(), Collections.emptyMap()),
                        new GlobalClockRule(new DefaultGlobalClockRuleConfigurationBuilder().build(), Collections.singletonMap(database.getName(), database)))),
                new ConfigurationProperties(PropertiesBuilder.build(new Property(ConfigurationPropertyKey.SQL_SHOW.getKey(), "true")))));
        InstanceContext instanceContext = new InstanceContext(
                new ComputeNodeInstance(mock(InstanceMetaData.class)), new StandaloneWorkerIdGenerator(), new ModeConfiguration("Standalone", null),
                mock(ModeContextManager.class), mock(LockContext.class), new EventBusContext());
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts()).thenReturn(metaDataContexts);
        when(result.getInstanceContext()).thenReturn(instanceContext);
        return result;
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new LinkedHashMap<>(2, 1F);
        result.put("ds_0", createDataSource("demo_ds_0"));
        result.put("ds_1", createDataSource("demo_ds_1"));
        return result;
    }
    
    private DataSource createDataSource(final String name) {
        MockedDataSource result = new MockedDataSource();
        result.setUrl(String.format("jdbc:opengauss://127.0.0.1:5432/%s", name));
        result.setUsername("root");
        result.setPassword("");
        result.setMaxPoolSize(50);
        result.setMinPoolSize(1);
        return result;
    }
    
    @SneakyThrows(IOException.class)
    private String loadExpectedRow() {
        StringBuilder result = new StringBuilder();
        String fileName = Objects.requireNonNull(ExportMetaDataExecutorTest.class.getResource("/expected/export-metadata-configuration.data")).getFile();
        try (
                FileReader fileReader = new FileReader(fileName);
                BufferedReader reader = new BufferedReader(fileReader)) {
            String line;
            while (null != (line = reader.readLine())) {
                result.append(line);
            }
        }
        return result.toString();
    }
}
