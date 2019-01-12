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

package io.shardingsphere.transaction.xa.swapper;

import com.alibaba.druid.pool.DruidDataSource;
import com.zaxxer.hikari.HikariDataSource;
import io.shardingsphere.core.config.DatabaseAccessConfiguration;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.transaction.xa.fixture.DataSourceUtils;
import org.junit.Test;

import javax.sql.DataSource;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class DataSourceSwapperEngineTest {
    
    private final String dataSourceName = "demo_ds";
    
    @Test
    public void assertSwapForHikariCP() {
        DataSource dataSource = DataSourceUtils.build(HikariDataSource.class, DatabaseType.MySQL, dataSourceName);
        assertDatabaseAccessConfiguration(DataSourceSwapperEngine.swap(dataSource));
    }
    
    @Test
    public void assertSwapFromDBCP2() {
        DataSource dataSource = DataSourceUtils.build(org.apache.commons.dbcp2.BasicDataSource.class, DatabaseType.MySQL, dataSourceName);
        assertDatabaseAccessConfiguration(DataSourceSwapperEngine.swap(dataSource));
    }
    
    @Test
    public void assertSwapFromTomcatDBCP() {
        DataSource dataSource = DataSourceUtils.build(org.apache.tomcat.dbcp.dbcp2.BasicDataSource.class, DatabaseType.MySQL, dataSourceName);
        assertDatabaseAccessConfiguration(DataSourceSwapperEngine.swap(dataSource));
    }
    
    @Test
    public void assertSwapFromDruid() {
        DataSource dataSource = DataSourceUtils.build(DruidDataSource.class, DatabaseType.MySQL, dataSourceName);
        DatabaseAccessConfiguration databaseAccessConfiguration = DataSourceSwapperEngine.swap(dataSource);
        assertDatabaseAccessConfiguration(databaseAccessConfiguration);
    }
    
    private void assertDatabaseAccessConfiguration(final DatabaseAccessConfiguration databaseAccessConfiguration) {
        assertThat(databaseAccessConfiguration.getUrl(), is("jdbc:mysql://localhost:3306/demo_ds"));
        assertThat(databaseAccessConfiguration.getUsername(), is("root"));
        assertThat(databaseAccessConfiguration.getPassword(), is("root"));
    }
}
