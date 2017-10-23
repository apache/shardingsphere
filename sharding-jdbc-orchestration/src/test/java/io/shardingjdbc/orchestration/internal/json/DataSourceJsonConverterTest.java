/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.orchestration.internal.json;

import com.mysql.jdbc.Driver;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public final class DataSourceJsonConverterTest {
    
    private final String dataSourceMapJson = "[{\"name\":\"ds_jdbc_1\",\"clazz\":\"org.apache.commons.dbcp.BasicDataSource\",\"defaultAutoCommit\":\"true\","
            + "\"defaultReadOnly\":\"false\",\"defaultTransactionIsolation\":\"-1\",\"driverClassName\":\"com.mysql.jdbc.Driver\",\"initialSize\":\"0\",\"logAbandoned\":\"false\","
            + "\"maxActive\":\"8\",\"maxIdle\":\"8\",\"maxOpenPreparedStatements\":\"-1\",\"maxWait\":\"-1\",\"minEvictableIdleTimeMillis\":\"1800000\",\"minIdle\":\"0\","
            + "\"numTestsPerEvictionRun\":\"3\",\"password\":\"\",\"removeAbandoned\":\"false\",\"removeAbandonedTimeout\":\"300\",\"testOnBorrow\":\"true\",\"testOnReturn\":\"false\","
            + "\"testWhileIdle\":\"false\",\"timeBetweenEvictionRunsMillis\":\"-1\",\"url\":\"jdbc:mysql://localhost:3306/ds_jdbc_1\",\"username\":\"root\",\"validationQueryTimeout\":\"-1\"},"
            + "{\"name\":\"ds_jdbc_0\",\"clazz\":\"org.apache.commons.dbcp.BasicDataSource\",\"defaultAutoCommit\":\"true\",\"defaultReadOnly\":\"false\",\"defaultTransactionIsolation\":\"-1\","
            + "\"driverClassName\":\"com.mysql.jdbc.Driver\",\"initialSize\":\"0\",\"logAbandoned\":\"false\",\"maxActive\":\"8\",\"maxIdle\":\"8\",\"maxOpenPreparedStatements\":\"-1\","
            + "\"maxWait\":\"-1\",\"minEvictableIdleTimeMillis\":\"1800000\",\"minIdle\":\"0\",\"numTestsPerEvictionRun\":\"3\",\"password\":\"\",\"removeAbandoned\":\"false\","
            + "\"removeAbandonedTimeout\":\"300\",\"testOnBorrow\":\"true\",\"testOnReturn\":\"false\",\"testWhileIdle\":\"false\",\"timeBetweenEvictionRunsMillis\":\"-1\","
            + "\"url\":\"jdbc:mysql://localhost:3306/ds_jdbc_0\",\"username\":\"root\",\"validationQueryTimeout\":\"-1\"}]";
    
    @Test
    public void assertToJson() {
        assertThat(DataSourceJsonConverter.toJson(createDataSourceMap()), is(dataSourceMapJson));
    }
    
    @Test
    public void assertFromJson() {
        Map<String, DataSource> actual = DataSourceJsonConverter.fromJson(dataSourceMapJson);
        assertThat(actual.size(), is(2));
        assertDataSource((BasicDataSource) actual.get("ds_jdbc_0"), (BasicDataSource) createDataSource("ds_jdbc_0"));
        assertDataSource((BasicDataSource) actual.get("ds_jdbc_1"), (BasicDataSource) createDataSource("ds_jdbc_1"));
    }
    
    private void assertDataSource(final BasicDataSource actual, final BasicDataSource expect) {
        assertThat(actual.getUrl(), is(expect.getUrl()));
        assertThat(actual.getMaxActive(), is(expect.getMaxActive()));
        assertThat(actual.getDefaultTransactionIsolation(), is(expect.getDefaultTransactionIsolation()));
        assertThat(actual.getRemoveAbandonedTimeout(), is(expect.getRemoveAbandonedTimeout()));
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>(2, 1);
        result.put("ds_jdbc_0", createDataSource("ds_jdbc_0"));
        result.put("ds_jdbc_1", createDataSource("ds_jdbc_1"));
        return result;
    }
    
    private DataSource createDataSource(final String dataSourceName) {
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName(Driver.class.getName());
        result.setUrl(String.format("jdbc:mysql://localhost:3306/%s", dataSourceName));
        result.setUsername("root");
        result.setPassword("");
        return result;
    }
}
