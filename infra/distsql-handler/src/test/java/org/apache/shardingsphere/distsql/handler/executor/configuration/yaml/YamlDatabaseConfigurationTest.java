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

package org.apache.shardingsphere.distsql.handler.executor.configuration.yaml;

import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlDatabaseConfigurationTest {
    
    @Test
    void assertUnmarshalWithDefaults() {
        YamlDatabaseConfiguration actual = YamlEngine.unmarshal("databaseName: foo_db", YamlDatabaseConfiguration.class);
        assertThat(actual.getDatabaseName(), is("foo_db"));
        assertTrue(actual.getDataSources().isEmpty());
        assertTrue(actual.getRules().isEmpty());
    }
    
    @Test
    void assertUnmarshalDataSourceProperties() {
        String yaml = "databaseName: foo_db\n"
                + "dataSources:\n"
                + "  ds_0:\n"
                + "    dataSourceClassName: com.zaxxer.hikari.HikariDataSource\n"
                + "    driverClassName: org.h2.Driver\n"
                + "    url: jdbc:h2:mem:foo_db\n"
                + "    username: foo_user\n"
                + "    password: foo_password\n"
                + "    connectionTimeoutMilliseconds: 30000\n"
                + "    idleTimeoutMilliseconds: 60000\n"
                + "    maxLifetimeMilliseconds: 1800000\n"
                + "    maxPoolSize: 50\n"
                + "    minPoolSize: 1\n"
                + "    readOnly: true\n"
                + "    customPoolProps:\n"
                + "      foo_prop: bar_value\n";
        YamlDatabaseConfiguration actual = YamlEngine.unmarshal(yaml, YamlDatabaseConfiguration.class);
        assertThat(actual.getDatabaseName(), is("foo_db"));
        YamlDataSourceConfiguration actualDataSource = actual.getDataSources().get("ds_0");
        assertThat(actualDataSource.getDataSourceClassName(), is("com.zaxxer.hikari.HikariDataSource"));
        assertThat(actualDataSource.getDriverClassName(), is("org.h2.Driver"));
        assertThat(actualDataSource.getUrl(), is("jdbc:h2:mem:foo_db"));
        assertThat(actualDataSource.getUsername(), is("foo_user"));
        assertThat(actualDataSource.getPassword(), is("foo_password"));
        assertThat(actualDataSource.getConnectionTimeoutMilliseconds(), is(30000L));
        assertThat(actualDataSource.getIdleTimeoutMilliseconds(), is(60000L));
        assertThat(actualDataSource.getMaxLifetimeMilliseconds(), is(1800000L));
        assertThat(actualDataSource.getMaxPoolSize(), is(50));
        assertThat(actualDataSource.getMinPoolSize(), is(1));
        assertTrue(actualDataSource.getReadOnly());
        assertThat(actualDataSource.getCustomPoolProps().getProperty("foo_prop"), is("bar_value"));
        assertTrue(actual.getRules().isEmpty());
    }
}
