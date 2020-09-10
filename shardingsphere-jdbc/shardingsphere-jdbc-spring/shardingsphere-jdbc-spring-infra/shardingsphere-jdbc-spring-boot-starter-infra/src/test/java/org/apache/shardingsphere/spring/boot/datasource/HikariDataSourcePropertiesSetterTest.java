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

package org.apache.shardingsphere.spring.boot.datasource;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.spring.boot.datasource.prop.impl.HikariDataSourcePropertiesSetter;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class HikariDataSourcePropertiesSetterTest {
    
    private final HikariDataSourcePropertiesSetter dbcpDataSourcePropertiesSetter = new HikariDataSourcePropertiesSetter();
    
    private final HikariDataSource dataSource = new HikariDataSource();
    
    private Environment environment;
    
    @Before
    public void setUp() {
        MockEnvironment mockEnvironment = new MockEnvironment();
        mockEnvironment.setProperty("spring.shardingsphere.datasource.ds_primary.type", "com.zaxxer.hikari.HikariDataSource");
        mockEnvironment.setProperty("spring.shardingsphere.datasource.ds_primary.data-source-properties.cachePrepStmts", "true");
        mockEnvironment.setProperty("spring.shardingsphere.datasource.ds_primary.data-source-properties.prepStmtCacheSize", "250");
        environment = mockEnvironment;
    }
    
    @Test
    public void assertPropertiesSet() {
        dbcpDataSourcePropertiesSetter.propertiesSet(environment, "spring.shardingsphere.datasource.", "ds_primary", dataSource);
        assertThat(dataSource.getDataSourceProperties().getProperty("cachePrepStmts"), is("true"));
        assertThat(dataSource.getDataSourceProperties().getProperty("prepStmtCacheSize"), is("250"));
    }
    
    @Test
    public void assertGetType() {
        assertThat(dbcpDataSourcePropertiesSetter.getType(), is("com.zaxxer.hikari.HikariDataSource"));
    }
    
}
