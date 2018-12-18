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

public class DataSourceSwapperRegistryTest {
    
    private final String databaseName = "demo_ds";
    
    @Test
    public void assertBuildParameterFromHikari() {
        DataSource dataSource = DataSourceUtils.build(PoolType.HIKARI, DatabaseType.MySQL, databaseName);
        DataSourceParameter parameter = DataSourceSwapperRegistry.getInstance().getSwapper(dataSource).swap(dataSource);
        assertThatParameter(parameter);
        assertThat(parameter.getMinimumPoolSize(), is(1));
        assertThat(parameter.getMaintenanceInterval(), is(30 * 1000L));
        assertThat(parameter.getMaxLifetime(), is(30 * 60 * 1000L));
    }
    
    @Test
    public void assertBuildParameterFromDruid() {
        DataSource dataSource = DataSourceUtils.build(PoolType.DRUID, DatabaseType.MySQL, databaseName);
        DataSourceParameter parameter = DataSourceSwapperRegistry.getInstance().getSwapper(dataSource).swap(dataSource);
        assertThatParameter(parameter);
        assertThat(parameter.getMinimumPoolSize(), is(2));
        assertThat(parameter.getMaintenanceInterval(), is(20 * 1000L));
        assertThat(parameter.getMaxLifetime(), is(0L));
    }
    
    @Test
    public void assertBuildParameterFromDBCP2() {
        DataSource dataSource = DataSourceUtils.build(PoolType.DBCP2, DatabaseType.MySQL, databaseName);
        DataSourceParameter parameter = DataSourceSwapperRegistry.getInstance().getSwapper(dataSource).swap(dataSource);
        assertThatParameter(parameter);
        assertThat(parameter.getMinimumPoolSize(), is(2));
        assertThat(parameter.getMaintenanceInterval(), is(20 * 1000L));
        assertThat(parameter.getMaxLifetime(), is(500 * 1000L));
    }
    
    @Test
    public void assertBuildParameterFromUnsupportedDataSource() {
        DataSource dataSource = mock(DataSource.class);
        DataSourceParameter actual = DataSourceSwapperRegistry.getInstance().getSwapper(dataSource).swap(dataSource);
        assertNotNull(actual);
        assertNull(actual.getUrl());
        assertNull(actual.getUsername());
        assertNull(actual.getPassword());
        assertThat(actual.getMaximumPoolSize(), is(50));
        assertThat(actual.getMinimumPoolSize(), is(1));
        assertThat(actual.getConnectionTimeout(), is(30 * 1000L));
        assertThat(actual.getIdleTimeout(), is(60 * 1000L));
        assertThat(actual.getMaxLifetime(), is(0L));
        assertThat(actual.getMaintenanceInterval(), is(30 * 1000L));
    }
    
    private void assertThatParameter(final DataSourceParameter parameter) {
        assertThat(parameter.getUrl(), is("jdbc:mysql://localhost:3306/demo_ds"));
        assertThat(parameter.getUsername(), is("root"));
        assertThat(parameter.getPassword(), is("root"));
        assertThat(parameter.getMaximumPoolSize(), is(10));
        assertThat(parameter.getConnectionTimeout(), is(15 * 1000L));
        assertThat(parameter.getIdleTimeout(), is(40 * 1000L));
    }
}
