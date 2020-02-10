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

package org.apache.shardingsphere.transaction.xa.jta.datasource.properties.dialect;

import org.apache.shardingsphere.underlying.common.config.DatabaseAccessConfiguration;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class MySQLXADataSourceDefinitionTest {
    
    @Test
    public void assertGetXADriverClassName() {
        assertThat(new MySQLXADataSourceDefinition().getXADriverClassName(), 
                CoreMatchers.<Collection<String>>is(Arrays.asList("com.mysql.jdbc.jdbc2.optional.MysqlXADataSource", "com.mysql.cj.jdbc.MysqlXADataSource")));
    }
    
    @Test
    public void assertGetXAProperties() {
        Properties actual = new MySQLXADataSourceDefinition().getXAProperties(new DatabaseAccessConfiguration("jdbc:mysql://127.0.0.1:3306/demo", "root", "root"));
        assertThat(actual.getProperty("user"), is("root"));
        assertThat(actual.getProperty("password"), is("root"));
        assertThat(actual.getProperty("URL"), is("jdbc:mysql://127.0.0.1:3306/demo"));
        assertThat(actual.getProperty("pinGlobalTxToPhysicalConnection"), is(Boolean.TRUE.toString()));
        assertThat(actual.getProperty("autoReconnect"), is(Boolean.TRUE.toString()));
        assertThat(actual.getProperty("useServerPrepStmts"), is(Boolean.TRUE.toString()));
        assertThat(actual.getProperty("cachePrepStmts"), is(Boolean.TRUE.toString()));
        assertThat(actual.getProperty("prepStmtCacheSize"), is("250"));
        assertThat(actual.getProperty("prepStmtCacheSqlLimit"), is("2048"));
        assertThat(actual.getProperty("useLocalSessionState"), is(Boolean.TRUE.toString()));
        assertThat(actual.getProperty("rewriteBatchedStatements"), is(Boolean.TRUE.toString()));
        assertThat(actual.getProperty("cacheResultSetMetadata"), is(Boolean.FALSE.toString()));
        assertThat(actual.getProperty("cacheServerConfiguration"), is(Boolean.TRUE.toString()));
        assertThat(actual.getProperty("elideSetAutoCommits"), is(Boolean.TRUE.toString()));
        assertThat(actual.getProperty("maintainTimeStats"), is(Boolean.FALSE.toString()));
        assertThat(actual.getProperty("netTimeoutForStreamingResults"), is("0"));
    }
}
