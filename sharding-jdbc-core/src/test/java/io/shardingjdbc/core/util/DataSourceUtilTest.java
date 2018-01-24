/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.util;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public final class DataSourceUtilTest {
    
    @Test
    public void assertDataSourceForDBCPAndCamel() throws ReflectiveOperationException {
        BasicDataSource actual = (BasicDataSource) DataSourceUtil.getDataSource(BasicDataSource.class.getName(), getDataSourcePoolProperties("driverClassName", "url", "username"));
        assertThat(actual.getDriverClassName(), is(org.h2.Driver.class.getName()));
        assertThat(actual.getUrl(), is("jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL"));
        assertThat(actual.getUsername(), is("sa"));
    }
    
    @Test
    public void assertDataSourceForDBCPAndHyphen() throws ReflectiveOperationException {
        BasicDataSource actual = (BasicDataSource) DataSourceUtil.getDataSource(BasicDataSource.class.getName(), getDataSourcePoolProperties("driver-class-name", "url", "username"));
        assertThat(actual.getDriverClassName(), is(org.h2.Driver.class.getName()));
        assertThat(actual.getUrl(), is("jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL"));
        assertThat(actual.getUsername(), is("sa"));
    }
    
    @Test
    public void assertDataSourceForHikariCPAndCamel() throws ReflectiveOperationException {
        HikariDataSource actual = (HikariDataSource) DataSourceUtil.getDataSource(HikariDataSource.class.getName(), getDataSourcePoolProperties("driverClassName", "jdbcUrl", "username"));
        assertThat(actual.getDriverClassName(), is(org.h2.Driver.class.getName()));
        assertThat(actual.getJdbcUrl(), is("jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL"));
        assertThat(actual.getUsername(), is("sa"));
    }
    
    @Test
    public void assertDataSourceForHikariCPAndHyphen() throws ReflectiveOperationException {
        HikariDataSource actual = (HikariDataSource) DataSourceUtil.getDataSource(HikariDataSource.class.getName(), getDataSourcePoolProperties("driver-class-name", "jdbc-url", "username"));
        assertThat(actual.getDriverClassName(), is(org.h2.Driver.class.getName()));
        assertThat(actual.getJdbcUrl(), is("jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL"));
        assertThat(actual.getUsername(), is("sa"));
    }
    
    private Map<String, Object> getDataSourcePoolProperties(final String driverClassName, final String url, final String username) {
        Map<String, Object> result = new HashMap<>(3, 1);
        result.put(driverClassName, org.h2.Driver.class.getName());
        result.put(url, "jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        result.put(username, "sa");
        return result;
    }

    @Test
    public void assertDataSourceForBooleanValue() throws ReflectiveOperationException {
        Map<String, Object> dataSourceProperties = new HashMap<>(7, 1);
        dataSourceProperties.put("defaultAutoCommit", true);
        dataSourceProperties.put("defaultReadOnly", false);
        dataSourceProperties.put("poolPreparedStatements", Boolean.TRUE);
        dataSourceProperties.put("testOnBorrow", Boolean.FALSE);
        dataSourceProperties.put("testOnReturn", true);
        dataSourceProperties.put("testWhileIdle", false);
        dataSourceProperties.put("accessToUnderlyingConnectionAllowed", Boolean.TRUE);
        BasicDataSource actual = (BasicDataSource) DataSourceUtil.getDataSource(BasicDataSource.class.getName(), dataSourceProperties);
        assertThat(actual.getDefaultAutoCommit(), is(true));
        assertThat(actual.getDefaultReadOnly(), is(false));
        assertThat(actual.isPoolPreparedStatements(), is(Boolean.TRUE));
        assertThat(actual.getTestOnBorrow(), is(Boolean.FALSE));
        assertThat(actual.getTestOnReturn(), is(Boolean.TRUE));
        assertThat(actual.getTestWhileIdle(), is(Boolean.FALSE));
        assertThat(actual.isAccessToUnderlyingConnectionAllowed(), is(true));
    }

    @Test
    public void assertDataSourceForIntValue() throws ReflectiveOperationException {
        Map<String, Object> dataSourceProperties = new HashMap<>(7, 1);
        dataSourceProperties.put("defaultTransactionIsolation", -13);
        dataSourceProperties.put("maxActive", 16);
        dataSourceProperties.put("maxIdle", 4);
        dataSourceProperties.put("minIdle", 16);
        dataSourceProperties.put("initialSize", 7);
        dataSourceProperties.put("maxOpenPreparedStatements", 128);
        dataSourceProperties.put("numTestsPerEvictionRun", 13);
        BasicDataSource actual = (BasicDataSource) DataSourceUtil.getDataSource(BasicDataSource.class.getName(), dataSourceProperties);
        assertThat(actual.getDefaultTransactionIsolation(), is(-13));
        assertThat(actual.getMaxActive(), is(16));
        assertThat(actual.getMaxIdle(), is(4));
        assertThat(actual.getMinIdle(), is(16));
        assertThat(actual.getInitialSize(), is(7));
        assertThat(actual.getMaxOpenPreparedStatements(), is(128));
        assertThat(actual.getNumTestsPerEvictionRun(), is(13));
    }

    @Test
    public void assertDataSourceForLongValue() throws ReflectiveOperationException {
        Map<String, Object> dataSourceProperties = new HashMap<>(3, 1);
        dataSourceProperties.put("maxWait", 1304L);
        dataSourceProperties.put("timeBetweenEvictionRunsMillis", 16L);
        dataSourceProperties.put("minEvictableIdleTimeMillis", 4000L);
        BasicDataSource actual = (BasicDataSource) DataSourceUtil.getDataSource(BasicDataSource.class.getName(), dataSourceProperties);
        assertThat(actual.getMaxWait(), is(1304L));
        assertThat(actual.getTimeBetweenEvictionRunsMillis(), is(16L));
        assertThat(actual.getMinEvictableIdleTimeMillis(), is(4000L));
    }
}
