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

package io.shardingsphere.transaction.xa.jta.datasource.swapper;

import com.zaxxer.hikari.HikariDataSource;
import io.shardingsphere.core.config.DatabaseAccessConfiguration;
import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.Test;

import javax.sql.DataSource;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class DataSourceSwapperTest {
    
    private final DataSourceSwapper swapper = new DataSourceSwapper();
    
    @Test
    public void assertSwapByDefaultProvider() {
        assertDatabaseAccessConfiguration(swapper.swap(createDBCPDataSource()));
    }
    
    private DataSource createDBCPDataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setUrl("jdbc:mysql://localhost:3306/demo_ds");
        dataSource.setUsername("root");
        dataSource.setPassword("root");
        return dataSource;
    }
    
    @Test
    public void assertSwapBySPIProvider() {
        assertDatabaseAccessConfiguration(swapper.swap(createHikariCPDataSource()));
    }
    
    private DataSource createHikariCPDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mysql://localhost:3306/demo_ds");
        dataSource.setUsername("root");
        dataSource.setPassword("root");
        return dataSource;
    }
    
    private void assertDatabaseAccessConfiguration(final DatabaseAccessConfiguration databaseAccessConfiguration) {
        assertThat(databaseAccessConfiguration.getUrl(), is("jdbc:mysql://localhost:3306/demo_ds"));
        assertThat(databaseAccessConfiguration.getUsername(), is("root"));
        assertThat(databaseAccessConfiguration.getPassword(), is("root"));
    }
}
