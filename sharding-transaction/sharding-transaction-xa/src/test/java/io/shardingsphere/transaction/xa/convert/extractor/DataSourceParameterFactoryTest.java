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

package io.shardingsphere.transaction.xa.convert.extractor;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.PoolType;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.transaction.xa.fixture.DataSourceUtils;
import org.junit.Test;

import javax.sql.DataSource;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class DataSourceParameterFactoryTest {
    
    private final String databaseName = "demo_ds";
    
    @Test
    public void assertBuildParameterFromHikari() {
        DataSourceParameter parameter = DataSourceParameterFactory.build(DataSourceUtils.build(PoolType.HIKARI, DatabaseType.MySQL, databaseName));
        assertThatParameter(parameter);
    }
    
    @Test
    public void assertBuildParameterFromDruid() {
        DataSourceParameter parameter = DataSourceParameterFactory.build(DataSourceUtils.build(PoolType.DRUID, DatabaseType.MySQL, databaseName));
        assertThatParameter(parameter);
    }
    
    @Test
    public void assertBuildParameterFromDBCPTomcat() {
        DataSourceParameter parameter = DataSourceParameterFactory.build(DataSourceUtils.build(PoolType.DBCP2_TOMCAT, DatabaseType.MySQL, databaseName));
        assertThatParameter(parameter);
    }
    
    @Test
    public void assertBuildParameterFromDBCP2() {
        DataSourceParameter parameter = DataSourceParameterFactory.build(DataSourceUtils.build(PoolType.DBCP2, DatabaseType.MySQL, databaseName));
        assertThatParameter(parameter);
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void assertBuildParameterFromUnsupportedDataSource() {
        DataSource dataSource = mock(DataSource.class);
        DataSourceParameterFactory.build(dataSource);
    }
    
    private void assertThatParameter(final DataSourceParameter parameter) {
        assertThat(parameter.getUrl(), is("jdbc:mysql://localhost:3306/demo_ds"));
        assertThat(parameter.getUsername(), is("root"));
        assertThat(parameter.getPassword(), is("root"));
        assertThat(parameter.getMaximumPoolSize(), is(10));
        assertThat(parameter.getIdleTimeout(), is(200L));
        assertThat(parameter.getConnectionTimeout(), is(2000L));
        assertThat(parameter.getMaxLifetime(), is(100000L));
    }
}
