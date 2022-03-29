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
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.DataSourcesMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.proxy.backend.text.distsql.rql.resource.DataSourceQueryResultSet;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DataSourceQueryResultSetTest {
    
    @Mock
    private ShardingSphereMetaData shardingSphereMetaData;
    
    @Before
    public void before() {
        DatabaseType databaseType = new MySQLDatabaseType();
        DataSourcesMetaData dataSourcesMetaData = new DataSourcesMetaData(databaseType, Collections.singletonMap("foo_ds", createDataSource()));
        ShardingSphereResource resource = new ShardingSphereResource(Collections.singletonMap("foo_ds", createDataSource()), dataSourcesMetaData, null, databaseType);
        when(shardingSphereMetaData.getResource()).thenReturn(resource);
    }
    
    private MockedDataSource createDataSource() {
        MockedDataSource result = new MockedDataSource();
        result.setUrl("jdbc:mysql://localhost/demo_ds");
        result.setUsername("root");
        result.setPassword("password");
        result.setMaxPoolSize(100);
        result.setMinPoolSize(10);
        return result;
    }
    
    @Test
    public void assertGetRowData() {
        DistSQLResultSet resultSet = new DataSourceQueryResultSet();
        resultSet.init(shardingSphereMetaData, mock(ShowResourcesStatement.class));
        Collection<Object> actual = resultSet.getRowData();
        assertThat(actual.size(), is(12));
        Iterator<Object> rowData = actual.iterator();
        assertThat(rowData.next(), is("foo_ds"));
        assertThat(rowData.next(), is("MySQL"));
        assertThat(rowData.next(), is("localhost"));
        assertThat(rowData.next(), is(3306));
        assertThat(rowData.next(), is("demo_ds"));
        assertThat(rowData.next(), is(""));
        assertThat(rowData.next(), is(""));
        assertThat(rowData.next(), is(""));
        assertThat(rowData.next(), is("100"));
        assertThat(rowData.next(), is("10"));
        assertThat(rowData.next(), is(""));
        assertThat(rowData.next(), is(""));
    }
}
