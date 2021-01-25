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
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.transaction.xa.jta.datasource.properties.XADataSourceDefinition;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import javax.sql.XADataSource;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DataSourceSwapperTest {
    
    @Mock
    private XADataSourceDefinition xaDataSourceDefinition;
    
    private DataSourceSwapper swapper;
    
    @Before
    public void before() {
        when(xaDataSourceDefinition.getXADriverClassName()).thenReturn(ImmutableList.of("org.h2.jdbcx.JdbcDataSource"));
    }
    
    @Test
    public void assertSwapByDefaultProvider() {
        swapper = new DataSourceSwapper(xaDataSourceDefinition);
        assertResult(swapper.swap(createDBCPDataSource()));
    }
    
    private DataSource createDBCPDataSource() {
        BasicDataSource result = new BasicDataSource();
        result.setUrl("jdbc:mysql://localhost:3306/demo_ds");
        result.setUsername("root");
        result.setPassword("root");
        return result;
    }
    
    @Test
    public void assertSwapBySPIProvider() {
        swapper = new DataSourceSwapper(xaDataSourceDefinition);
        assertResult(swapper.swap(createHikariCPDataSource()));
    }
    
    private DataSource createHikariCPDataSource() {
        HikariDataSource result = new HikariDataSource();
        result.setJdbcUrl("jdbc:mysql://localhost:3306/demo_ds");
        result.setUsername("root");
        result.setPassword("root");
        return result;
    }
    
    private void assertResult(final XADataSource xaDataSource) {
        assertThat(xaDataSource, instanceOf(JdbcDataSource.class));
        JdbcDataSource h2XADataSource = (JdbcDataSource) xaDataSource;
        assertThat(h2XADataSource.getUrl(), is("jdbc:mysql://localhost:3306/demo_ds"));
        assertThat(h2XADataSource.getUser(), is("root"));
        assertThat(h2XADataSource.getPassword(), is("root"));
    }
}
