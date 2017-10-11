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
    public void assertDataSourceForDBCP() throws ReflectiveOperationException {
        Map<String, Object> dataSourceProperties = new HashMap<>(3, 1);
        dataSourceProperties.put("driverClassName", org.h2.Driver.class.getName());
        dataSourceProperties.put("url", "jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        dataSourceProperties.put("username", "sa");
        BasicDataSource actual = (BasicDataSource) DataSourceUtil.getDataSource(BasicDataSource.class.getName(), dataSourceProperties);
        assertThat(actual.getDriverClassName(), is(org.h2.Driver.class.getName()));
        assertThat(actual.getUrl(), is("jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL"));
        assertThat(actual.getUsername(), is("sa"));
    }
    
    @Test
    public void assertDataSourceForHikariCP() throws ReflectiveOperationException {
        Map<String, Object> dataSourceProperties = new HashMap<>(3, 1);
        dataSourceProperties.put("driverClassName", org.h2.Driver.class.getName());
        dataSourceProperties.put("jdbcUrl", "jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        dataSourceProperties.put("username", "sa");
        HikariDataSource actual = (HikariDataSource) DataSourceUtil.getDataSource(HikariDataSource.class.getName(), dataSourceProperties);
        assertThat(actual.getDriverClassName(), is(org.h2.Driver.class.getName()));
        assertThat(actual.getJdbcUrl(), is("jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL"));
        assertThat(actual.getUsername(), is("sa"));
    }
}
