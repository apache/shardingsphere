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

package org.apache.shardingsphere.scaling.core.config.rule;

import com.google.gson.JsonObject;
import org.apache.shardingsphere.scaling.core.config.rule.RuleConfiguration.DataSourceConfigurationWrapper;
import org.apache.shardingsphere.scaling.core.fixture.FixtureShardingSphereJDBCConfiguration;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class RuleConfigurationTest {
    
    @Test
    public void assertToJDBConfig() {
        String jdbcUrl = "jdbc:h2:mem:test_db_2;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL";
        String username = "root";
        String password = "password";
        DataSourceConfiguration actual = mockStandardJDBCDataSourceConfigWrapper(jdbcUrl, username, password).unwrap();
        assertThat(actual, instanceOf(StandardJDBCDataSourceConfiguration.class));
        StandardJDBCDataSourceConfiguration jdbcDataSourceConfig = (StandardJDBCDataSourceConfiguration) actual;
        assertThat(jdbcDataSourceConfig.getJdbcUrl(), is(jdbcUrl));
        assertThat(jdbcDataSourceConfig.getUsername(), is(username));
        assertThat(jdbcDataSourceConfig.getPassword(), is(password));
    }
    
    @Test
    public void assertToShardingSphereJDBConfiguration() {
        String dataSource = FixtureShardingSphereJDBCConfiguration.DATA_SOURCE;
        String rule = FixtureShardingSphereJDBCConfiguration.RULE;
        DataSourceConfigurationWrapper dataSourceConfigurationWrapper = getDataSourceConfigurationWrapper(dataSource, rule);
        DataSourceConfiguration actual = dataSourceConfigurationWrapper.unwrap();
        assertThat(actual, instanceOf(ShardingSphereJDBCDataSourceConfiguration.class));
        ShardingSphereJDBCDataSourceConfiguration shardingSphereJDBCConfig = (ShardingSphereJDBCDataSourceConfiguration) actual;
        assertThat(shardingSphereJDBCConfig.getDataSource(), is(dataSource));
        assertThat(shardingSphereJDBCConfig.getRule(), is(rule));
    }
    
    private DataSourceConfigurationWrapper mockStandardJDBCDataSourceConfigWrapper(final String jdbcUrl, final String username, final String password) {
        JsonObject parameter = new JsonObject();
        parameter.addProperty("jdbcUrl", jdbcUrl);
        parameter.addProperty("username", username);
        parameter.addProperty("password", password);
        return new DataSourceConfigurationWrapper("JDBC", parameter);
    }
    
    private DataSourceConfigurationWrapper getDataSourceConfigurationWrapper(final String dataSource, final String rule) {
        JsonObject parameter = new JsonObject();
        parameter.addProperty("dataSource", dataSource);
        parameter.addProperty("rule", rule);
        return new DataSourceConfigurationWrapper("shardingSphereJdbc", parameter);
    }
}
