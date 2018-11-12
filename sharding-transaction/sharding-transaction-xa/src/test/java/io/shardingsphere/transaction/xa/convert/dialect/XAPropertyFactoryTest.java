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

package io.shardingsphere.transaction.xa.convert.dialect;

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
        dataSourceParameter.setConnectionTimeout(1000);
        dataSourceParameter.setIdleTimeout(1000);
        dataSourceParameter.setMaxLifetime(60000);
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
        dataSourceParameter.setUrl("jdbc:h2:mem:db0;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL");
        Properties xaProperties = XAPropertyFactory.build(XADatabaseType.H2, dataSourceParameter);
        assertThat(xaProperties.getProperty("user"), is("root"));
        assertThat(xaProperties.getProperty("password"), is("root"));
        assertThat(xaProperties.getProperty("URL"), is("jdbc:h2:mem:db0;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL"));
    }
    
    @Test
    public void assertGetPGXAProperties() {
        dataSourceParameter.setUrl("jdbc:postgresql://db.psql:5432/test_db");
        Properties xaProperties = XAPropertyFactory.build(XADatabaseType.PostgreSQL, dataSourceParameter);
        assertThat(xaProperties.getProperty("user"), is("root"));
        assertThat(xaProperties.getProperty("password"), is("root"));
        assertThat(xaProperties.getProperty("serverName"), is("db.psql"));
        assertThat(xaProperties.getProperty("portNumber"), is("5432"));
        assertThat(xaProperties.getProperty("databaseName"), is("test_db"));
    }
    
    @Test
    public void assertGetSQLServerXAProperties() {
        dataSourceParameter.setUrl("jdbc:sqlserver://db.sqlserver:1433;DatabaseName=test_db");
        Properties xaProperties = XAPropertyFactory.build(XADatabaseType.SQLServer, dataSourceParameter);
        assertThat(xaProperties.getProperty("user"), is("root"));
        assertThat(xaProperties.getProperty("password"), is("root"));
        assertThat(xaProperties.getProperty("serverName"), is("db.sqlserver"));
        assertThat(xaProperties.getProperty("portNumber"), is("1433"));
        assertThat(xaProperties.getProperty("databaseName"), is("test_db"));
    }
    
    @Test
    public void assertGetOracleXAProperties() {
        dataSourceParameter.setUrl("jdbc:oracle:thin:@//db.oracle:9999/test_db");
        Properties xaProperties = XAPropertyFactory.build(XADatabaseType.Oracle, dataSourceParameter);
        assertThat(xaProperties.getProperty("user"), is("root"));
        assertThat(xaProperties.getProperty("password"), is("root"));
        assertThat(xaProperties.getProperty("serverName"), is("db.oracle"));
        assertThat(xaProperties.getProperty("portNumber"), is("9999"));
        assertThat(xaProperties.getProperty("databaseName"), is("test_db"));
    }
}
