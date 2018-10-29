/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.transaction.manager.xa.property;

import io.shardingsphere.core.rule.DataSourceParameter;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class XAPropertyFactoryTest {
    
    private DataSourceParameter dataSourceParameter;
    
    @Before
    public void setup() {
        dataSourceParameter = new DataSourceParameter();
        dataSourceParameter.setUrl("jdbc:mysql://127.0.0.1:3306/demo");
        dataSourceParameter.setUsername("root");
        dataSourceParameter.setPassword("root");
        dataSourceParameter.setMaximumPoolSize(100);
    }
    
    @Test
    public void assertGetMysqlXAProperties() {
        Properties xaProperties = XAPropertyFactory.build(XADatabaseType.MySQL, dataSourceParameter);
        assertThat(xaProperties.getProperty("user"), is("root"));
        assertThat(xaProperties.getProperty("password"), is("root"));
        assertThat(xaProperties.getProperty("URL"), is("jdbc:mysql://127.0.0.1:3306/demo"));
        assertThat(xaProperties.getProperty("pinGlobalTxToPhysicalConnection"), is(Boolean.TRUE.toString()));
        assertThat(xaProperties.getProperty("autoReconnect"), is(Boolean.TRUE.toString()));
        assertThat(xaProperties.getProperty("useServerPrepStmts"), is(Boolean.TRUE.toString()));
        assertThat(xaProperties.getProperty("cachePrepStmts"), is(Boolean.TRUE.toString()));
        assertThat(xaProperties.getProperty("prepStmtCacheSize"), is("250"));
        assertThat(xaProperties.getProperty("prepStmtCacheSqlLimit"), is("2048"));
        assertThat(xaProperties.getProperty("useLocalSessionState"), is(Boolean.TRUE.toString()));
        assertThat(xaProperties.getProperty("rewriteBatchedStatements"), is(Boolean.TRUE.toString()));
        assertThat(xaProperties.getProperty("cacheResultSetMetadata"), is(Boolean.TRUE.toString()));
        assertThat(xaProperties.getProperty("cacheServerConfiguration"), is(Boolean.TRUE.toString()));
        assertThat(xaProperties.getProperty("elideSetAutoCommits"), is(Boolean.TRUE.toString()));
        assertThat(xaProperties.getProperty("maintainTimeStats"), is(Boolean.FALSE.toString()));
        assertThat(xaProperties.getProperty("netTimeoutForStreamingResults"), is("0"));
    }
    
    @Test
    public void assertGetH2XAProperties() {
    }
    
    @Test
    public void assertGetPGXAProperties() {
    }
    
    @Test
    public void assertGetSQLServerXAProperties() {
    }
}
