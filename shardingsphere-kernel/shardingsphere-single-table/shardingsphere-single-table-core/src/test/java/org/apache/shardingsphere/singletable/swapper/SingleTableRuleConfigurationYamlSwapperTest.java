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

package org.apache.shardingsphere.singletable.swapper;

import org.apache.shardingsphere.singletable.config.SingleTableRuleConfiguration;
import org.apache.shardingsphere.singletable.yaml.config.pojo.YamlSingleTableRuleConfiguration;
import org.apache.shardingsphere.singletable.yaml.config.swapper.SingleTableRuleConfigurationYamlSwapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public final class SingleTableRuleConfigurationYamlSwapperTest {
    
    private final SingleTableRuleConfigurationYamlSwapper swapper = new SingleTableRuleConfigurationYamlSwapper();
    
    @Test
    public void assertSwapToObject() {
        YamlSingleTableRuleConfiguration yamlConfiguration = new YamlSingleTableRuleConfiguration();
        yamlConfiguration.setDefaultDataSource("ds_0");
        SingleTableRuleConfiguration configuration = swapper.swapToObject(yamlConfiguration);
        assertTrue(configuration.getDefaultDataSource().isPresent());
        assertThat(configuration.getDefaultDataSource().get(), is("ds_0"));
    }
    
    @Test
    public void assertSwapToObjectWithoutDataSource() {
        YamlSingleTableRuleConfiguration yamlConfiguration = new YamlSingleTableRuleConfiguration();
        SingleTableRuleConfiguration configuration = swapper.swapToObject(yamlConfiguration);
        assertFalse(configuration.getDefaultDataSource().isPresent());
    }
    
    @Test
    public void assertSwapToYaml() {
        SingleTableRuleConfiguration configuration = new SingleTableRuleConfiguration();
        configuration.setDefaultDataSource("ds_0");
        YamlSingleTableRuleConfiguration yamlSingleTableRuleConfiguration = swapper.swapToYamlConfiguration(configuration);
        assertThat(yamlSingleTableRuleConfiguration.getDefaultDataSource(), is("ds_0"));
    }
    
    @Test
    public void assertSwapToYamlWithoutDataSource() {
        SingleTableRuleConfiguration configuration = new SingleTableRuleConfiguration();
        YamlSingleTableRuleConfiguration yamlSingleTableRuleConfiguration = swapper.swapToYamlConfiguration(configuration);
        assertNull(yamlSingleTableRuleConfiguration.getDefaultDataSource());
    }
}
