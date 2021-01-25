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

package org.apache.shardingsphere.governance.core.yaml.swapper;

import org.apache.shardingsphere.governance.core.yaml.config.YamlDataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class DataSourceConfigurationYamlSwapperTest {
    
    private final DataSourceConfigurationYamlSwapper dataSourceConfigurationYamlSwapper = new DataSourceConfigurationYamlSwapper();
    
    @Test
    public void assertSwapToYaml() {
        DataSourceConfiguration dataSourceConfig = new DataSourceConfiguration("xxx.jdbc.driver");
        dataSourceConfig.getProps().put("url", "xx:xxx");
        dataSourceConfig.getProps().put("username", "root");
        YamlDataSourceConfiguration actual = dataSourceConfigurationYamlSwapper.swapToYamlConfiguration(dataSourceConfig);
        assertThat(actual.getDataSourceClassName(), is("xxx.jdbc.driver"));
        assertThat(actual.getProps().size(), is(2));
        assertThat(actual.getProps().get("url").toString(), is("xx:xxx"));
        assertThat(actual.getProps().get("username").toString(), is("root"));
    }
    
    @Test
    public void assertSwapToConfiguration() {
        YamlDataSourceConfiguration yamlConfig = new YamlDataSourceConfiguration();
        yamlConfig.setDataSourceClassName("xxx.jdbc.driver");
        yamlConfig.getProps().put("url", "xx:xxx");
        yamlConfig.getProps().put("username", "root");
        DataSourceConfiguration actual = dataSourceConfigurationYamlSwapper.swapToObject(yamlConfig);
        assertThat(actual.getDataSourceClassName(), is("xxx.jdbc.driver"));
        assertThat(actual.getProps().size(), is(2));
        assertThat(actual.getProps().get("url").toString(), is("xx:xxx"));
        assertThat(actual.getProps().get("username").toString(), is("root"));
    }
}
