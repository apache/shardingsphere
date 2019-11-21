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

package io.shardingsphere.transaction.xa.convert.swap;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.PoolType;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.transaction.xa.fixture.DataSourceUtils;
import org.junit.Test;

import javax.sql.DataSource;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class DataSourceSwapperRegistryTest {
    
    private final String dataSourceName = "demo_ds";
    
    @Test
    public void assertSwapFromHikari() {
        DataSource dataSource = DataSourceUtils.build(PoolType.HIKARI, DatabaseType.MySQL, dataSourceName);
        DataSourceParameter parameter = DataSourceSwapperRegistry.getSwapper(dataSource.getClass()).swap(dataSource);
        assertDataSourceParameter(parameter);
        assertThat(parameter.getMinPoolSize(), is(2));
        assertThat(parameter.getMaintenanceIntervalMilliseconds(), is(30 * 1000L));
        assertThat(parameter.getMaxLifetimeMilliseconds(), is(30 * 60 * 1000L));
    }
    
    @Test
    public void assertSwapFromDruid() {
        DataSource dataSource = DataSourceUtils.build(PoolType.DRUID, DatabaseType.MySQL, dataSourceName);
        DataSourceParameter parameter = DataSourceSwapperRegistry.getSwapper(dataSource.getClass()).swap(dataSource);
        assertDataSourceParameter(parameter);
        assertThat(parameter.getMinPoolSize(), is(2));
        assertThat(parameter.getMaintenanceIntervalMilliseconds(), is(20 * 1000L));
        assertThat(parameter.getMaxLifetimeMilliseconds(), is(0L));
    }
    
    @Test
    public void assertSwapFromDBCP2() {
        DataSource dataSource = DataSourceUtils.build(PoolType.DBCP2, DatabaseType.MySQL, dataSourceName);
        DataSourceParameter parameter = DataSourceSwapperRegistry.getSwapper(dataSource.getClass()).swap(dataSource);
        assertDataSourceParameter(parameter);
        assertThat(parameter.getMinPoolSize(), is(2));
        assertThat(parameter.getMaintenanceIntervalMilliseconds(), is(20 * 1000L));
        assertThat(parameter.getMaxLifetimeMilliseconds(), is(500 * 1000L));
    }
    
    @Test
    public void assertSwapFromUnregisteredDataSource() {
        DataSource dataSource = mock(DataSource.class);
        DataSourceParameter actual = DataSourceSwapperRegistry.getSwapper(dataSource.getClass()).swap(dataSource);
        assertNotNull(actual);
        assertNull(actual.getUrl());
        assertNull(actual.getUsername());
        assertNull(actual.getPassword());
        assertThat(actual.getMaxPoolSize(), is(50));
        assertThat(actual.getMinPoolSize(), is(1));
        assertThat(actual.getConnectionTimeoutMilliseconds(), is(30 * 1000L));
        assertThat(actual.getIdleTimeoutMilliseconds(), is(60 * 1000L));
        assertThat(actual.getMaxLifetimeMilliseconds(), is(0L));
        assertThat(actual.getMaintenanceIntervalMilliseconds(), is(30 * 1000L));
    }
    
    private void assertDataSourceParameter(final DataSourceParameter dataSourceParameter) {
        assertThat(dataSourceParameter.getUrl(), is("jdbc:mysql://localhost:3306/demo_ds"));
        assertThat(dataSourceParameter.getUsername(), is("root"));
        assertThat(dataSourceParameter.getPassword(), is("root"));
        assertThat(dataSourceParameter.getMaxPoolSize(), is(10));
        assertThat(dataSourceParameter.getConnectionTimeoutMilliseconds(), is(15 * 1000L));
        assertThat(dataSourceParameter.getIdleTimeoutMilliseconds(), is(40 * 1000L));
    }
}
