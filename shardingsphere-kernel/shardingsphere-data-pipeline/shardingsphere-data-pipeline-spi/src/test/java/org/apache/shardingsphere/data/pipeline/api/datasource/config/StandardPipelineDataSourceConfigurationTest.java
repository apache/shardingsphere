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

package org.apache.shardingsphere.data.pipeline.api.datasource.config;

import com.zaxxer.hikari.HikariConfig;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class StandardPipelineDataSourceConfigurationTest {

    private static final String WINDOWS = "Windows";

    private StandardPipelineDataSourceConfiguration dataSourceConfig;

    @Before
    public void setUp() throws SQLException {
        dataSourceConfig = new StandardPipelineDataSourceConfiguration("jdbc:mysql://127.0.0.1:3306/demo_ds?serverTimezone=UTC&useSSL=false", "userName", "password");
    }

    @Test
    public void assertConstructorWithStringSuccess() {
        String parameter = "jdbcUrl: jdbc:mysql://127.0.0.1:3306/demo_ds?serverTimezone=UTC&useSSL=false\n"
                + "username: userName\n"
                + "password: password\n";
        StandardPipelineDataSourceConfiguration standardPipelineDataSourceConfigurationOtherInstance = new StandardPipelineDataSourceConfiguration(parameter);
        String actualDatabaseTypeName = standardPipelineDataSourceConfigurationOtherInstance.getDatabaseType().getName();
        assertThat(actualDatabaseTypeName, is("MySQL"));
    }

    @Test
    public void assertEqualsAndHashCodeSuccess() {
        StandardPipelineDataSourceConfiguration standardPipelineDataSourceConfigurationOtherInstance = new StandardPipelineDataSourceConfiguration("jdbc:mysql://127.0.0.1:3306/demo_ds?"
                + "serverTimezone=UTC&useSSL=false", "userName", "password");
        assertThat(dataSourceConfig.hashCode(), is(standardPipelineDataSourceConfigurationOtherInstance.hashCode()));
    }

    @Test
    public void assertGetDatabaseTypeSuccess() {
        DatabaseType actualDatabaseType = dataSourceConfig.getDatabaseType();
        String actualDatabaseTypeName = actualDatabaseType.getName();
        assertThat(actualDatabaseTypeName, is("MySQL"));
    }

    @Test
    public void assertGetHikariConfigSuccess() {
        HikariConfig actualHikariConfig = dataSourceConfig.getHikariConfig();
        long actualActualHikariConfigConnectionTimeout = actualHikariConfig.getConnectionTimeout();
        long expectedActualHikariConfigConnectionTimeout = 30000;
        assertThat(actualActualHikariConfigConnectionTimeout, is(expectedActualHikariConfigConnectionTimeout));
    }

    @Test
    public void assertGetParameterSuccess() {
        String os = System.getProperty("os.name");
        String expectedParameter = "jdbcUrl: jdbc:mysql://127.0.0.1:3306/demo_ds?serverTimezone=UTC&useSSL=false\n"
                + "username: userName\n"
                + "password: password\n";
        if (os.contains(WINDOWS)) {
            expectedParameter = "jdbcUrl: jdbc:mysql://127.0.0.1:3306/demo_ds?serverTimezone=UTC&useSSL=false\r\n"
                    + "username: userName\r\n"
                    + "password: password\r\n";
        }
        String actualParameter = dataSourceConfig.getParameter();
        assertThat(actualParameter, is(expectedParameter));
    }

    @Test
    public void assertGetTypeSuccess() {
        String actualType = dataSourceConfig.getType();
        assertThat(actualType, is("JDBC"));
    }

    @Test
    public void assertGetDataSourceConfigurationSuccess() {
        DataSourceProperties actualDataSourceConfiguration = (DataSourceProperties) dataSourceConfig.getDataSourceConfiguration();
        String actualDataSourceClassName = actualDataSourceConfiguration.getDataSourceClassName();
        assertThat(actualDataSourceClassName, is("com.zaxxer.hikari.HikariDataSource"));
    }

    @Test
    public void assertAppendJDBCQueryPropertiesSuccess() {
        StandardPipelineDataSourceConfiguration standardPipelineDataSourceConfigurationSpy = Mockito.spy(dataSourceConfig);
        Properties propertiesInput = new Properties();
        standardPipelineDataSourceConfigurationSpy.appendJDBCQueryProperties(propertiesInput);
        verify(standardPipelineDataSourceConfigurationSpy, times(1)).appendJDBCQueryProperties(propertiesInput);
    }
}
