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
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
public final class ExportMetaDataExecutorTest {
    
    private final ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
    
    @Test
    public void assertGetColumns() {
        Collection<String> columns = new ExportMetaDataExecutor().getColumnNames();
        assertThat(columns.size(), is(3));
        Iterator<String> columnIterator = columns.iterator();
        assertThat(columnIterator.next(), is("id"));
        assertThat(columnIterator.next(), is("create_time"));
        assertThat(columnIterator.next(), is("cluster_info"));
    }
    
    @Test
    public void assertExecuteWithEmptyMetaData() {
        ContextManager contextManager = mockEmptyContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().getAllDatabaseNames()).thenReturn(Collections.singleton("empty_metadata"));
        when(database.getName()).thenReturn("empty_metadata");
        when(database.getResourceMetaData().getAllInstanceDataSourceNames()).thenReturn(Collections.singleton("empty_metadata"));
        when(database.getResourceMetaData().getDataSources()).thenReturn(Collections.emptyMap());
        when(database.getRuleMetaData().getConfigurations()).thenReturn(Collections.emptyList());
        ExportMetaDataStatement sqlStatement = new ExportMetaDataStatement(null);
        Collection<LocalDataQueryResultRow> actual = new ExportMetaDataExecutor().getRows(database, sqlStatement);
        assertThat(actual.size(), is(1));
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(3), is("{\"storageNodes\":[],\"metaData\":{\"databases\":{\"empty_metadata\":\"databaseName: null\\ndataSources:\\nrules:\\n\"},\"props\":\"\",\"rules\":\"\"}}"));
    }
    
    private ContextManager mockEmptyContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class), new ShardingSphereMetaData(new HashMap<>(),
                new ShardingSphereRuleMetaData(Collections.emptyList()),
                new ConfigurationProperties(new Properties())));
        when(result.getMetaDataContexts()).thenReturn(metaDataContexts);
        return result;
    }
    
    @Test
    public void assertExecute() throws SQLException {
        when(database.getName()).thenReturn("normal_db");
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        Collection<LocalDataQueryResultRow> actual = new ExportMetaDataExecutor().getRows(database, new ExportMetaDataStatement(null));
        assertThat(actual.size(), is(1));
        LocalDataQueryResultRow row = actual.iterator().next();
        assertThat(row.getCell(3), is(loadExpectedRow()));
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class), new ShardingSphereMetaData(Collections.singletonMap(database.getName(), database),
                new ShardingSphereRuleMetaData(Collections.singleton(new AuthorityRule(new DefaultAuthorityRuleConfigurationBuilder().build(), Collections.emptyMap()))),
                new ConfigurationProperties(PropertiesBuilder.build(new Property(ConfigurationPropertyKey.SQL_SHOW.getKey(), "true")))));
        when(result.getMetaDataContexts()).thenReturn(metaDataContexts);
        return result;
    }
    
    @SneakyThrows(IOException.class)
    private String loadExpectedRow() {
        StringBuilder result = new StringBuilder();
        String fileName = Objects.requireNonNull(ExportMetaDataExecutorTest.class.getResource("/expected/export-metadata-configuration.json")).getFile();
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
