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

package io.shardingsphere.transaction.xa.jta.datasource;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.PoolType;
import io.shardingsphere.transaction.xa.fixture.DataSourceUtils;
import io.shardingsphere.transaction.xa.jta.connection.ShardingXAConnection;
import lombok.SneakyThrows;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Test;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.sql.Connection;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ShardingXADataSourceTest {
    
    @Test
    public void assertBuildShardingXADataSourceOfXA() {
        DataSource dataSource = DataSourceUtils.build(PoolType.DRUID_XA, DatabaseType.MySQL, "ds1");
        ShardingXADataSource actual = new ShardingXADataSource(DatabaseType.MySQL, "ds1", dataSource);
        assertThat(actual.getDatabaseType(), is(DatabaseType.MySQL));
        assertThat(actual.getResourceName(), is("ds1"));
        assertThat(actual.getOriginalDataSource(), is(dataSource));
        assertTrue(actual.isOriginalXADataSource());
        assertThat(actual.getXaDataSource(), is((XADataSource) dataSource));
    }
    
    @Test
    public void assertBuildShardingXADataSourceOfNoneXA() {
        DataSource dataSource = DataSourceUtils.build(PoolType.HIKARI, DatabaseType.H2, "ds1");
        ShardingXADataSource actual = new ShardingXADataSource(DatabaseType.H2, "ds1", dataSource);
        assertThat(actual.getDatabaseType(), is(DatabaseType.H2));
        assertThat(actual.getResourceName(), is("ds1"));
        assertFalse(actual.isOriginalXADataSource());
        assertThat(actual.getOriginalDataSource(), is(dataSource));
        assertThat(actual.getXaDataSource(), instanceOf(JdbcDataSource.class));
        JdbcDataSource jdbcDataSource = (JdbcDataSource) actual.getXaDataSource();
        assertThat(jdbcDataSource.getUser(), is("root"));
        assertThat(jdbcDataSource.getPassword(), is("root"));
    }
    
    @Test
    @SneakyThrows
    public void assertGetXAConnectionOfXA() {
        DataSource dataSource = DataSourceUtils.build(PoolType.DRUID_XA, DatabaseType.H2, "ds1");
        ShardingXADataSource shardingXADataSource = new ShardingXADataSource(DatabaseType.H2, "ds1", dataSource);
        ShardingXAConnection actual = shardingXADataSource.getXAConnection();
        assertThat(actual.getConnection(), instanceOf(Connection.class));
    }
    
    @Test
    @SneakyThrows
    public void assertGetXAConnectionOfNoneXA() {
        DataSource dataSource = DataSourceUtils.build(PoolType.HIKARI, DatabaseType.H2, "ds1");
        ShardingXADataSource shardingXADataSource = new ShardingXADataSource(DatabaseType.H2, "ds1", dataSource);
        ShardingXAConnection actual = shardingXADataSource.getXAConnection();
        assertThat(actual.getConnection(), instanceOf(Connection.class));
    }
}
