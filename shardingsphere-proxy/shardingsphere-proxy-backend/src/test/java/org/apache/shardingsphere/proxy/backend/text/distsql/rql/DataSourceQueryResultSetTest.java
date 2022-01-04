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

package org.apache.shardingsphere.proxy.backend.text.distsql.rql;

import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowResourcesStatement;
import org.apache.shardingsphere.infra.config.DatabaseAccessConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.DataSourcesMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.text.distsql.rql.resource.DataSourceQueryResultSet;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DataSourceQueryResultSetTest {
    
    @Mock
    private ShardingSphereMetaData shardingSphereMetaData;
    
    @Before
    public void before() {
        DatabaseType databaseType = new MySQLDatabaseType();
        DataSourcesMetaData dataSourcesMetaData = new DataSourcesMetaData(databaseType, createDatabaseAccessConfigurationMap());
        ShardingSphereResource resource = new ShardingSphereResource(createDataSourceMap(), dataSourcesMetaData, null, databaseType);
        when(shardingSphereMetaData.getResource()).thenReturn(resource);
    }
    
    private Map<String, DatabaseAccessConfiguration> createDatabaseAccessConfigurationMap() {
        Map<String, DatabaseAccessConfiguration> result = new HashMap<>(1, 1);
        result.put("ds_0", new DatabaseAccessConfiguration("jdbc:mysql://localhost/demo_ds", "root"));
        return result;
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>(1, 1);
        MockedDataSource ds0 = createDataSource();
        result.put("ds_0", ds0);
        return result;
    }
    
    private MockedDataSource createDataSource() {
        MockedDataSource result = new MockedDataSource();
        result.setUrl("jdbc:mysql://localhost/demo_ds");
        result.setUsername("root");
        result.setPassword("password");
        return result;
    }
    
    @Test
    public void assertGetRowData() {
        ContextManager manager = mock(ContextManager.class);
        when(manager.getMetaDataContexts()).thenReturn(mock(MetaDataContexts.class));
        when(manager.getMetaDataContexts().getMetaDataPersistService()).thenReturn(Optional.empty());
        ProxyContext.getInstance().init(manager);
        DistSQLResultSet resultSet = new DataSourceQueryResultSet();
        resultSet.init(shardingSphereMetaData, mock(ShowResourcesStatement.class));
        Collection<Object> actual = resultSet.getRowData();
        assertThat(actual.size(), is(6));
        Iterator<Object> rowData = actual.iterator();
        assertThat(rowData.next(), is("ds_0"));
        assertThat(rowData.next(), is("MySQL"));
        assertThat(rowData.next(), is("localhost"));
        assertThat(rowData.next(), is(3306));
        assertThat(rowData.next(), is("demo_ds"));
        assertThat(rowData.next(), 
                is("{\"connectionTimeoutMilliseconds\":30000,\"idleTimeoutMilliseconds\":60000,\"maxLifetimeMilliseconds\":1800000,\"maxPoolSize\":50,\"minPoolSize\":1,\"readOnly\":false}"));
        MetaDataPersistService persistService = mock(MetaDataPersistService.class, RETURNS_DEEP_STUBS);
        when(persistService.getDataSourceService().load(any())).thenReturn(createDataSourceConfigurationMap());
        when(manager.getMetaDataContexts().getMetaDataPersistService()).thenReturn(Optional.of(persistService));
        resultSet.init(shardingSphereMetaData, mock(ShowResourcesStatement.class));
        actual = resultSet.getRowData();
        assertThat(actual.size(), is(6));
        rowData = actual.iterator();
        assertThat(rowData.next(), is("ds_0"));
        assertThat(rowData.next(), is("MySQL"));
        assertThat(rowData.next(), is("localhost"));
        assertThat(rowData.next(), is(3306));
        assertThat(rowData.next(), is("demo_ds"));
        assertThat(rowData.next(), 
                is("{\"connectionTimeoutMilliseconds\":30000,\"idleTimeoutMilliseconds\":60000,\"maxLifetimeMilliseconds\":1800000,\"maxPoolSize\":50,\"minPoolSize\":1,"
                        + "\"readOnly\":true,\"customPoolProps\":{\"test\":\"test\"}}"));
    }
    
    private Map<String, DataSourceConfiguration> createDataSourceConfigurationMap() {
        Map<String, DataSourceConfiguration> result = new HashMap<>();
        DataSourceConfiguration ds0 = new DataSourceConfiguration("ds_0");
        ds0.getCustomPoolProps().put("test", "test");
        ds0.getProps().put("readOnly", true);
        result.put("ds_0", ds0);
        return result;
    }
}
