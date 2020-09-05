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
import java.util.Optional;
import org.apache.shardingsphere.infra.config.datasource.JDBCParameterDecorator;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class HikariJDBCParameterDecoratorTest {
    
    private final HikariDataSource testHikariDataSource = new HikariDataSource();
    
    private JDBCParameterDecorator testJdbcParameterDecorator;
    
    @Before
    public void setUp() {
        ShardingSphereServiceLoader.register(JDBCParameterDecorator.class);
        Optional<JDBCParameterDecorator> optionalJDBCParameterDecorator =
            ShardingSphereServiceLoader.newServiceInstances(JDBCParameterDecorator.class).stream().filter(each -> each.getType() == testHikariDataSource.getClass()).findFirst();
        assertTrue(optionalJDBCParameterDecorator.isPresent());
        testJdbcParameterDecorator = optionalJDBCParameterDecorator.get();
    }
    
    @Test
    public void assertDecorate() {
        testJdbcParameterDecorator.decorate(testHikariDataSource);
        assertThat(testHikariDataSource.getDataSourceProperties().getProperty("useServerPrepStmts"), is(Boolean.TRUE.toString()));
        assertThat(testHikariDataSource.getDataSourceProperties().getProperty("cachePrepStmts"), is(Boolean.TRUE.toString()));
        assertThat(testHikariDataSource.getDataSourceProperties().getProperty("prepStmtCacheSize"), is("250"));
        assertThat(testHikariDataSource.getDataSourceProperties().getProperty("prepStmtCacheSqlLimit"), is("2048"));
        assertThat(testHikariDataSource.getDataSourceProperties().getProperty("useLocalSessionState"), is(Boolean.TRUE.toString()));
        assertThat(testHikariDataSource.getDataSourceProperties().getProperty("rewriteBatchedStatements"), is(Boolean.TRUE.toString()));
        assertThat(testHikariDataSource.getDataSourceProperties().getProperty("cacheResultSetMetadata"), is(Boolean.FALSE.toString()));
        assertThat(testHikariDataSource.getDataSourceProperties().getProperty("cacheServerConfiguration"), is(Boolean.TRUE.toString()));
        assertThat(testHikariDataSource.getDataSourceProperties().getProperty("elideSetAutoCommits"), is(Boolean.TRUE.toString()));
        assertThat(testHikariDataSource.getDataSourceProperties().getProperty("maintainTimeStats"), is(Boolean.FALSE.toString()));
        assertThat(testHikariDataSource.getDataSourceProperties().getProperty("netTimeoutForStreamingResults"), is("0"));
        assertThat(testHikariDataSource.getDataSourceProperties().getProperty("tinyInt1isBit"), is(Boolean.FALSE.toString()));
    }
    
    @Test
    public void assertGetType() {
        assertTrue(testJdbcParameterDecorator.getType() == HikariDataSource.class);
    }
}
