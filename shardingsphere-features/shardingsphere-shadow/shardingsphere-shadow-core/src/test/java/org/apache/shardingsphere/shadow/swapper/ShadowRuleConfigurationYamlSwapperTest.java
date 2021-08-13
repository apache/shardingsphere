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

package org.apache.shardingsphere.shadow.swapper;

import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.yaml.config.YamlShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.yaml.swapper.ShadowRuleConfigurationYamlSwapper;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ShadowRuleConfigurationYamlSwapperTest {
    
    private ShadowRuleConfigurationYamlSwapper swapper;
    
    @Before
    public void init() {
        swapper = new ShadowRuleConfigurationYamlSwapper();
    }
    
    @Test
    public void assertSwapToYamlConfiguration() {
        ShadowRuleConfiguration expectedConfiguration = createShadowRuleConfiguration();
        YamlShadowRuleConfiguration actualConfiguration = swapper.swapToYamlConfiguration(expectedConfiguration);
        assertThat(actualConfiguration.isEnable(), is(expectedConfiguration.isEnable()));
        assertThat(actualConfiguration.getColumn(), is(expectedConfiguration.getColumn()));
        assertThat(actualConfiguration.getSourceDataSourceNames(), is(expectedConfiguration.getSourceDataSourceNames()));
        assertThat(actualConfiguration.getShadowDataSourceNames(), is(expectedConfiguration.getShadowDataSourceNames()));
    }
    
    private ShadowRuleConfiguration createShadowRuleConfiguration() {
        return new ShadowRuleConfiguration("shadow", Arrays.asList("ds", "ds1"), Arrays.asList("shadow_ds", "shadow_ds1"));
    }
    
    @Test
    public void assertSwapToObject() {
        YamlShadowRuleConfiguration expectedConfiguration = createYamlShadowRuleConfiguration();
        ShadowRuleConfiguration actualConfiguration = swapper.swapToObject(expectedConfiguration);
        assertThat(actualConfiguration.isEnable(), is(expectedConfiguration.isEnable()));
        assertThat(actualConfiguration.getColumn(), is(expectedConfiguration.getColumn()));
        assertThat(actualConfiguration.getSourceDataSourceNames(), is(expectedConfiguration.getSourceDataSourceNames()));
        assertThat(actualConfiguration.getShadowDataSourceNames(), is(expectedConfiguration.getShadowDataSourceNames()));
    }
    
    private YamlShadowRuleConfiguration createYamlShadowRuleConfiguration() {
        YamlShadowRuleConfiguration result = new YamlShadowRuleConfiguration();
        result.setColumn("shadow");
        result.setSourceDataSourceNames(Arrays.asList("ds", "ds1"));
        result.setShadowDataSourceNames(Arrays.asList("shadow_ds", "shadow_ds1"));
        return result;
    }
}
