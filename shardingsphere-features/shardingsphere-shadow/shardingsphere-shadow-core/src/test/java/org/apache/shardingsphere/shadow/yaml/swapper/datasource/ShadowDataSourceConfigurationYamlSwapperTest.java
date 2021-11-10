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

package org.apache.shardingsphere.shadow.yaml.swapper.datasource;

import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.yaml.config.datasource.YamlShadowDataSourceConfiguration;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ShadowDataSourceConfigurationYamlSwapperTest {

    @Test
    public void assertSwapToYamlConfiguration() {
        ShadowDataSourceConfiguration shadowDataSourceConfiguration = new ShadowDataSourceConfiguration("ds", "ds-shadow");
        ShadowDataSourceConfigurationYamlSwapper swapper = new ShadowDataSourceConfigurationYamlSwapper();
        YamlShadowDataSourceConfiguration configuration = swapper.swapToYamlConfiguration(shadowDataSourceConfiguration);
        assertThat(shadowDataSourceConfiguration.getSourceDataSourceName(), is(configuration.getSourceDataSourceName()));
        assertThat(shadowDataSourceConfiguration.getShadowDataSourceName(), is(configuration.getShadowDataSourceName()));
    }

    @Test
    public void assertSwapToObject() {
        YamlShadowDataSourceConfiguration yamlConfiguration = new YamlShadowDataSourceConfiguration();
        yamlConfiguration.setShadowDataSourceName("ds-shadow");
        yamlConfiguration.setSourceDataSourceName("ds");
        ShadowDataSourceConfigurationYamlSwapper swapper = new ShadowDataSourceConfigurationYamlSwapper();
        ShadowDataSourceConfiguration dataSourceConfiguration = swapper.swapToObject(yamlConfiguration);
        assertThat(yamlConfiguration.getSourceDataSourceName(), is(dataSourceConfiguration.getSourceDataSourceName()));
        assertThat(yamlConfiguration.getShadowDataSourceName(), is(dataSourceConfiguration.getShadowDataSourceName()));
    }
}
