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

package org.apache.shardingsphere.distsql.handler.executor.config.yaml.swapper;

import org.apache.shardingsphere.distsql.handler.executor.config.yaml.YamlDataSourceConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.config.ConnectionConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.config.PoolConfiguration;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlDataSourceConfigurationSwapperTest {
    
    @Test
    void assertSwap() {
        YamlDataSourceConfiguration yamlConfig = new YamlDataSourceConfiguration();
        yamlConfig.setDataSourceClassName("com.zaxxer.hikari.HikariDataSource");
        yamlConfig.setDriverClassName("org.h2.Driver");
        yamlConfig.setUrl("jdbc:h2:mem:foo_db");
        yamlConfig.setUsername("foo_user");
        yamlConfig.setPassword("foo_password");
        yamlConfig.setConnectionTimeoutMilliseconds(30000L);
        yamlConfig.setIdleTimeoutMilliseconds(60000L);
        yamlConfig.setMaxLifetimeMilliseconds(1800000L);
        yamlConfig.setMaxPoolSize(50);
        yamlConfig.setMinPoolSize(1);
        yamlConfig.setReadOnly(true);
        yamlConfig.setCustomPoolProps(PropertiesBuilder.build(new Property("foo_prop", "bar_value")));
        DataSourceConfiguration actual = new YamlDataSourceConfigurationSwapper().swap(yamlConfig);
        ConnectionConfiguration actualConnection = actual.getConnection();
        assertThat(actualConnection.getDataSourceClassName(), is("com.zaxxer.hikari.HikariDataSource"));
        assertThat(actualConnection.getDriverClassName(), is("org.h2.Driver"));
        assertThat(actualConnection.getUrl(), is("jdbc:h2:mem:foo_db"));
        assertThat(actualConnection.getUsername(), is("foo_user"));
        assertThat(actualConnection.getPassword(), is("foo_password"));
        PoolConfiguration actualPool = actual.getPool();
        assertThat(actualPool.getConnectionTimeoutMilliseconds(), is(30000L));
        assertThat(actualPool.getIdleTimeoutMilliseconds(), is(60000L));
        assertThat(actualPool.getMaxLifetimeMilliseconds(), is(1800000L));
        assertThat(actualPool.getMaxPoolSize(), is(50));
        assertThat(actualPool.getMinPoolSize(), is(1));
        assertTrue(actualPool.getReadOnly());
        assertThat(actualPool.getCustomProperties().getProperty("foo_prop"), is("bar_value"));
    }
}
