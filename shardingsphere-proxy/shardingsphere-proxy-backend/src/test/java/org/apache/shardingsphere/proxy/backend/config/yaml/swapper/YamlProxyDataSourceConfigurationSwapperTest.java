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

package org.apache.shardingsphere.proxy.backend.config.yaml.swapper;

import org.apache.shardingsphere.infra.datasource.config.ConnectionConfiguration;
import org.apache.shardingsphere.infra.datasource.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.datasource.config.PoolConfiguration;
import org.apache.shardingsphere.proxy.backend.config.ProxyConfigurationLoader;
import org.apache.shardingsphere.proxy.backend.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDataSourceConfiguration;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public final class YamlProxyDataSourceConfigurationSwapperTest {
    
    @Test
    public void assertSwap() throws IOException {
        YamlProxyConfiguration yamlProxyConfig = ProxyConfigurationLoader.load("/conf/swap");
        YamlProxyDataSourceConfiguration yamlProxyDataSourceConfig = yamlProxyConfig.getDatabaseConfigurations().get("swapper_test").getDataSources().get("foo_db");
        DataSourceConfiguration actualDataSourceConfig = new YamlProxyDataSourceConfigurationSwapper().swap(yamlProxyDataSourceConfig);
        assertConnectionConfig(actualDataSourceConfig);
        assertPoolConfig(actualDataSourceConfig);
    }
    
    private void assertConnectionConfig(final DataSourceConfiguration actualDataSourceConfig) {
        ConnectionConfiguration actualConnection = actualDataSourceConfig.getConnection();
        assertNotNull(actualConnection);
        assertThat(actualConnection.getUrl(), is("jdbc:h2:mem:foo_db;DB_CLOSE_DELAY=-1"));
        assertThat(actualConnection.getUsername(), is("sa"));
        assertThat(actualConnection.getPassword(), is(""));
    }
    
    private void assertPoolConfig(final DataSourceConfiguration actualDataSourceConfig) {
        PoolConfiguration actualPool = actualDataSourceConfig.getPool();
        assertNotNull(actualPool);
        assertThat(actualPool.getConnectionTimeoutMilliseconds(), is(250L));
        assertThat(actualPool.getIdleTimeoutMilliseconds(), is(2L));
        assertThat(actualPool.getMaxLifetimeMilliseconds(), is(3L));
        assertThat(actualPool.getMaxPoolSize(), is(4));
        assertThat(actualPool.getMinPoolSize(), is(5));
        assertTrue(actualPool.getReadOnly());
    }
}
