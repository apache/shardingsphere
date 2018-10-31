/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.orchestration.internal.yaml;

import io.shardingsphere.orchestration.config.OrchestrationConfiguration;
import io.shardingsphere.orchestration.reg.api.RegistryCenterConfiguration;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class YamlOrchestrationConfigurationTest {
    
    @Test
    public void assertLoadFromYamlWithMinConfiguration() {
        RegistryCenterConfiguration expectedRegistryCenterConfig = new RegistryCenterConfiguration();
        expectedRegistryCenterConfig.setServerLists("localhost:3181");
        OrchestrationConfiguration expected = new OrchestrationConfiguration("min_config", expectedRegistryCenterConfig, false);
        YamlOrchestrationConfiguration actual = loadYamlOrchestrationConfiguration("/yaml/orchestration-configuration.min.yaml");
        assertOrchestrationConfiguration(actual.getOrchestrationConfiguration(), expected);
    }
    
    @Test
    public void assertLoadFromYamlWithMaxConfiguration() {
        RegistryCenterConfiguration expectedRegistryCenterConfig = new RegistryCenterConfiguration();
        expectedRegistryCenterConfig.setServerLists("localhost:3181");
        expectedRegistryCenterConfig.setNamespace("orchestration-yaml-test");
        expectedRegistryCenterConfig.setDigest("user:password");
        expectedRegistryCenterConfig.setOperationTimeoutMilliseconds(1000);
        expectedRegistryCenterConfig.setMaxRetries(1);
        expectedRegistryCenterConfig.setRetryIntervalMilliseconds(1000);
        expectedRegistryCenterConfig.setTimeToLiveSeconds(10);
        OrchestrationConfiguration expected = new OrchestrationConfiguration("max_config", expectedRegistryCenterConfig, true);
        YamlOrchestrationConfiguration actual = loadYamlOrchestrationConfiguration("/yaml/orchestration-configuration.max.yaml");
        assertOrchestrationConfiguration(actual.getOrchestrationConfiguration(), expected);
    }
    
    private YamlOrchestrationConfiguration loadYamlOrchestrationConfiguration(final String yamlFile) {
        return new Yaml().loadAs(YamlOrchestrationConfigurationTest.class.getResourceAsStream(yamlFile), YamlOrchestrationConfiguration.class);
    }
    
    private void assertOrchestrationConfiguration(final OrchestrationConfiguration actual, final OrchestrationConfiguration expected) {
        assertThat(actual.getName(), is(expected.getName()));
        assertThat(actual.isOverwrite(), is(expected.isOverwrite()));
        assertThat(actual.getRegCenterConfig().getServerLists(), is(expected.getRegCenterConfig().getServerLists()));
        assertThat(actual.getRegCenterConfig().getNamespace(), is(expected.getRegCenterConfig().getNamespace()));
        assertThat(actual.getRegCenterConfig().getDigest(), is(expected.getRegCenterConfig().getDigest()));
        assertThat(actual.getRegCenterConfig().getOperationTimeoutMilliseconds(), is(expected.getRegCenterConfig().getOperationTimeoutMilliseconds()));
        assertThat(actual.getRegCenterConfig().getMaxRetries(), is(expected.getRegCenterConfig().getMaxRetries()));
        assertThat(actual.getRegCenterConfig().getRetryIntervalMilliseconds(), is(expected.getRegCenterConfig().getRetryIntervalMilliseconds()));
        assertThat(actual.getRegCenterConfig().getTimeToLiveSeconds(), is(expected.getRegCenterConfig().getTimeToLiveSeconds()));
    }
}
