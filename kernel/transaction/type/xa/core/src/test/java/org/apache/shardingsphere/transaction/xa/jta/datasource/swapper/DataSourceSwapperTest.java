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

package org.apache.shardingsphere.transaction.xa.jta.datasource.swapper;

import com.google.common.collect.ImmutableList;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.apache.shardingsphere.transaction.xa.jta.datasource.properties.XADataSourceDefinition;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.XADataSource;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DataSourceSwapperTest {
    
    @Mock
    private XADataSourceDefinition xaDataSourceDefinition;
    
    @Before
    public void before() {
        when(xaDataSourceDefinition.getXADriverClassNames()).thenReturn(ImmutableList.of("org.h2.jdbcx.JdbcDataSource"));
    }
    
    @Test
    public void assertSwap() {
        DataSourceSwapper swapper = new DataSourceSwapper(xaDataSourceDefinition);
        assertResult(swapper.swap(new MockedDataSource()));
    }
    
    private void assertResult(final XADataSource xaDataSource) {
        assertThat(xaDataSource, instanceOf(JdbcDataSource.class));
        JdbcDataSource h2XADataSource = (JdbcDataSource) xaDataSource;
        assertThat(h2XADataSource.getUrl(), is("jdbc:mock://127.0.0.1/foo_ds"));
        assertThat(h2XADataSource.getUser(), is("root"));
        assertThat(h2XADataSource.getPassword(), is("root"));
    }
}
