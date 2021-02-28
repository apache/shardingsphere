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

package org.apache.shardingsphere.infra.yaml.swapper;

import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class YamlDataSourceConfigurationSwapperTest {
    
    private final YamlDataSourceConfigurationSwapper yamlDataSourceConfigurationSwapper = new YamlDataSourceConfigurationSwapper();
    
    @Test
    public void assertSwapToMap() {
        DataSourceConfiguration dataSourceConfig = new DataSourceConfiguration("xxx.jdbc.driver");
        dataSourceConfig.getProps().put("url", "xx:xxx");
        dataSourceConfig.getProps().put("username", "root");
        Map<String, Object> actual = yamlDataSourceConfigurationSwapper.swapToMap(dataSourceConfig);
        assertThat(actual.get("dataSourceClassName"), is("xxx.jdbc.driver"));
        assertThat(actual.get("url").toString(), is("xx:xxx"));
        assertThat(actual.get("username").toString(), is("root"));
    }
    
    @Test
    public void assertSwapToConfiguration() {
        Map<String, Object> yamlConfig = new HashMap<>(3, 1);
        yamlConfig.put("dataSourceClassName", "xxx.jdbc.driver");
        yamlConfig.put("url", "xx:xxx");
        yamlConfig.put("username", "root");
        DataSourceConfiguration actual = yamlDataSourceConfigurationSwapper.swapToObjectFromMap(yamlConfig);
        assertThat(actual.getDataSourceClassName(), is("xxx.jdbc.driver"));
        assertThat(actual.getProps().size(), is(2));
        assertThat(actual.getProps().get("url").toString(), is("xx:xxx"));
        assertThat(actual.getProps().get("username").toString(), is("root"));
    }
}
