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

package org.apache.shardingsphere.scaling.core.config;

import com.google.gson.JsonObject;
import org.apache.shardingsphere.scaling.core.fixture.FixtureShardingSphereJDBCConfiguration;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class RuleConfigurationTest {
    
    @Test
    public void assertToJDBConfiguration() {
        RuleConfiguration.DataSourceConf dataSourceConf = new RuleConfiguration.DataSourceConf();
        dataSourceConf.setType("jdbc");
        String jdbcUrl = "jdbc:h2:mem:test_db_2;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL";
        String username = "root";
        String password = "password";
        dataSourceConf.setParameter(mockJDBCConfiguration(jdbcUrl, username, password));
        ScalingDataSourceConfiguration actual = dataSourceConf.toTypedDataSourceConfiguration();
        assertThat(actual, instanceOf(JDBCScalingDataSourceConfiguration.class));
        JDBCScalingDataSourceConfiguration jdbcDataSourceConfig = (JDBCScalingDataSourceConfiguration) actual;
        assertThat(jdbcDataSourceConfig.getJdbcUrl(), is(jdbcUrl));
        assertThat(jdbcDataSourceConfig.getUsername(), is(username));
        assertThat(jdbcDataSourceConfig.getPassword(), is(password));
    }
    
    private JsonObject mockJDBCConfiguration(final String jdbcUrl, final String username, final String password) {
        JsonObject result = new JsonObject();
        result.addProperty("jdbcUrl", jdbcUrl);
        result.addProperty("username", username);
        result.addProperty("password", password);
        return result;
    }
    
    @Test
    public void assertToShardingSphereJDBConfiguration() {
        RuleConfiguration.DataSourceConf dataSourceConf = new RuleConfiguration.DataSourceConf();
        dataSourceConf.setType("shardingSphereJdbc");
        String dataSource = FixtureShardingSphereJDBCConfiguration.DATA_SOURCE;
        String rule = FixtureShardingSphereJDBCConfiguration.RULE;
        dataSourceConf.setParameter(mockShardingSphereJDBCConfiguration(dataSource, rule));
        ScalingDataSourceConfiguration actual = dataSourceConf.toTypedDataSourceConfiguration();
        assertThat(actual, instanceOf(ShardingSphereJDBCScalingDataSourceConfiguration.class));
        ShardingSphereJDBCScalingDataSourceConfiguration shardingSphereJDBCConfig = (ShardingSphereJDBCScalingDataSourceConfiguration) actual;
        assertThat(shardingSphereJDBCConfig.getDataSource(), is(dataSource));
        assertThat(shardingSphereJDBCConfig.getRule(), is(rule));
    }
    
    private JsonObject mockShardingSphereJDBCConfiguration(final String dataSource, final String rule) {
        JsonObject result = new JsonObject();
        result.addProperty("dataSource", dataSource);
        result.addProperty("rule", rule);
        return result;
    }
}
