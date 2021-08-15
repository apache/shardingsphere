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

package org.apache.shardingsphere.infra.yaml.config.swapper.persist;

import org.apache.shardingsphere.infra.constant.DistMetaDataPersistOrder;
import org.apache.shardingsphere.infra.persist.config.DistMetaDataPersistRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.persist.YamlDistMetaDataPersistRuleConfiguration;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class DistMetaDataPersistRuleConfigurationYamlSwapperTest {
    
    private final DistMetaDataPersistRuleConfigurationYamlSwapper swapper = new DistMetaDataPersistRuleConfigurationYamlSwapper();
    
    @Test
    public void assertGetOrder() {
        assertThat(swapper.getOrder(), is(DistMetaDataPersistOrder.ORDER));
    }
    
    @Test
    public void assertGetTypeClass() {
        assertThat(swapper.getTypeClass(), equalTo(DistMetaDataPersistRuleConfiguration.class));
    }
    
    @Test
    public void assertSwapToYamlConfiguration() {
        DistMetaDataPersistRuleConfiguration distMetaDataPersistRuleConfiguration = 
                new DistMetaDataPersistRuleConfiguration("Local", true, buildProperties());
        YamlDistMetaDataPersistRuleConfiguration actual = swapper.swapToYamlConfiguration(distMetaDataPersistRuleConfiguration);
        assertNotNull(actual);
        assertThat(actual.getType(), is("Local"));
        assertThat(actual.getProps().get("path"), is("test"));
        assertTrue(actual.isOverwrite());
    }
    
    @Test
    public void assertSwapToObject() {
        YamlDistMetaDataPersistRuleConfiguration yamlConfig = new YamlDistMetaDataPersistRuleConfiguration();
        yamlConfig.setType("Local");
        yamlConfig.setOverwrite(true);
        yamlConfig.setProps(buildProperties());
        DistMetaDataPersistRuleConfiguration actual = swapper.swapToObject(yamlConfig);
        assertNotNull(actual);
        assertThat(actual.getType(), is("Local"));
        assertThat(actual.getProps().get("path"), is("test"));
        assertTrue(actual.isOverwrite());
    }
    
    @Test
    public void assertGetRuleTagName() {
        assertThat(swapper.getRuleTagName(), is("DIST_METADATA_PERSIST"));
    }
    
    private Properties buildProperties() {
        Properties result = new Properties();
        result.setProperty("path", "test");
        return result;
    }
}
