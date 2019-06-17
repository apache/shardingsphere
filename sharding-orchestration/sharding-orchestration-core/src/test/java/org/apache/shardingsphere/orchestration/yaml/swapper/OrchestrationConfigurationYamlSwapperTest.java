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

package org.apache.shardingsphere.orchestration.yaml.swapper;

import org.apache.shardingsphere.orchestration.config.OrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.reg.api.RegistryCenterConfiguration;
import org.apache.shardingsphere.orchestration.yaml.config.YamlOrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.yaml.config.YamlRegistryCenterConfiguration;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class OrchestrationConfigurationYamlSwapperTest {
    
    private final OrchestrationConfigurationYamlSwapper orchestrationConfigurationYamlSwapper = new OrchestrationConfigurationYamlSwapper();
    
    @Test
    public void assertSwapToYaml() {
        RegistryCenterConfiguration registryCenterConfiguration = mock(RegistryCenterConfiguration.class);
        YamlOrchestrationConfiguration actual = orchestrationConfigurationYamlSwapper.swap(new OrchestrationConfiguration("orche_ds", registryCenterConfiguration, true));
        assertThat(actual.getName(), is("orche_ds"));
        assertThat(actual.getRegistry(), instanceOf(YamlRegistryCenterConfiguration.class));
        assertTrue(actual.isOverwrite());
    }
    
    @Test
    public void assertSwapToConfiguration() {
        YamlOrchestrationConfiguration yamlConfiguration = new YamlOrchestrationConfiguration();
        YamlRegistryCenterConfiguration registryCenterConfiguration = mock(YamlRegistryCenterConfiguration.class);
        when(registryCenterConfiguration.getType()).thenReturn("type");
        yamlConfiguration.setName("orche_ds");
        yamlConfiguration.setRegistry(registryCenterConfiguration);
        yamlConfiguration.setOverwrite(true);
        OrchestrationConfiguration actual = orchestrationConfigurationYamlSwapper.swap(yamlConfiguration);
        assertThat(actual.getName(), is("orche_ds"));
        assertThat(actual.getRegCenterConfig(), instanceOf(RegistryCenterConfiguration.class));
        assertTrue(actual.isOverwrite());
    }
}
