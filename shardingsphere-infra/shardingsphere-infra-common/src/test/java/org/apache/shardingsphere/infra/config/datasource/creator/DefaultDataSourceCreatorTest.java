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

package org.apache.shardingsphere.infra.config.datasource.creator;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.creator.impl.DefaultDataSourceCreator;
import org.junit.Test;

import javax.sql.DataSource;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class DefaultDataSourceCreatorTest {
    
    @Test
    public void assertCreateDataSourceConfiguration() {
        assertThat(new DefaultDataSourceCreator().createDataSourceConfiguration(createDataSource()), is(createDataSourceConfiguration()));
    }
    
    private DataSource createDataSource() {
        HikariDataSource result = new HikariDataSource();
        result.setDriverClassName("org.h2.Driver");
        result.setJdbcUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        result.setUsername("root");
        result.setPassword("root");
        return result;
    }
    
    @Test
    public void assertCreateDataSource() {
        HikariDataSource actual = (HikariDataSource) new DefaultDataSourceCreator().createDataSource(createDataSourceConfiguration());
        assertThat(actual.getUsername(), is("root"));
        assertThat(actual.getPassword(), is("root"));
        assertThat(actual.getDriverClassName(), is("org.h2.Driver"));
        assertThat(actual.getJdbcUrl(), is("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL"));
    }
    
    private DataSourceConfiguration createDataSourceConfiguration() {
        DataSourceConfiguration result = new DataSourceConfiguration(HikariDataSource.class.getName());
        result.getProps().put("jdbcUrl", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        result.getProps().put("driverClassName", "org.h2.Driver");
        result.getProps().put("username", "root");
        result.getProps().put("password", "root");
        return result;
    }
}
