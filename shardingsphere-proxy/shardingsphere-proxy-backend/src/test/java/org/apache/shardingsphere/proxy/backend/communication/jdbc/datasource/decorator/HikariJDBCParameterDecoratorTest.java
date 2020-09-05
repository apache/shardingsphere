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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.datasource.decorator;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.Before;
import org.junit.Test;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public final class HikariJDBCParameterDecoratorTest {
    
    private HikariJDBCParameterDecorator hikariJDBCParameterDecorator;
    
    @Before
    public void setUp() {
        hikariJDBCParameterDecorator = new HikariJDBCParameterDecorator();
    }
    
    @Test
    public void assertGetTypeResultIsHikariDataSource() {
        assertEquals(HikariDataSource.class, hikariJDBCParameterDecorator.getType());
    }
    
    @Test
    public void assertDecoratedHikariDataSource() {
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariJDBCParameterDecorator.decorate(hikariDataSource);
        Properties properties = hikariDataSource.getDataSourceProperties();
        assertThat(properties.getProperty("useServerPrepStmts"), is(Boolean.TRUE.toString()));
        assertThat(properties.getProperty("cachePrepStmts"), is(Boolean.TRUE.toString()));
        assertThat(properties.getProperty("prepStmtCacheSize"), is("250"));
        assertThat(properties.getProperty("prepStmtCacheSqlLimit"), is("2048"));
        assertThat(properties.getProperty("useLocalSessionState"), is(Boolean.TRUE.toString()));
        assertThat(properties.getProperty("rewriteBatchedStatements"), is(Boolean.TRUE.toString()));
        assertThat(properties.getProperty("cacheResultSetMetadata"), is(Boolean.FALSE.toString()));
        assertThat(properties.getProperty("cacheServerConfiguration"), is(Boolean.TRUE.toString()));
        assertThat(properties.getProperty("elideSetAutoCommits"), is(Boolean.TRUE.toString()));
        assertThat(properties.getProperty("maintainTimeStats"), is(Boolean.FALSE.toString()));
        assertThat(properties.getProperty("netTimeoutForStreamingResults"), is("0"));
        assertThat(properties.getProperty("tinyInt1isBit"), is(Boolean.FALSE.toString()));
    }
}
