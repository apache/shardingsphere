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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rql;

import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowResourcesStatement;
import org.apache.shardingsphere.infra.distsql.query.DatabaseDistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResources;
import org.apache.shardingsphere.proxy.backend.handler.distsql.rql.resource.DataSourceQueryResultSet;
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DataSourceQueryResultSetTest {
    
    @Mock
    private ShardingSphereDatabase database;
    
    @Before
    public void before() {
        ShardingSphereResources resource = new ShardingSphereResources("sharding_db", Collections.singletonMap("foo_ds", createDataSource()));
        when(database.getResources()).thenReturn(resource);
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
        DatabaseDistSQLResultSet resultSet = new DataSourceQueryResultSet();
        resultSet.init(database, mock(ShowResourcesStatement.class));
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
