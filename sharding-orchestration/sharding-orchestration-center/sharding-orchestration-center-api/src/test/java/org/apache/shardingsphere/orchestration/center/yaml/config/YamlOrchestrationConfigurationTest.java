package org.apache.shardingsphere.orchestration.center.yaml.config;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class YamlOrchestrationConfigurationTest {

    @Test
    public void assertInstanceConfigurationMap() {
        Map<String, YamlInstanceConfiguration> instanceConfigurationMap = new HashMap<>();
        YamlOrchestrationConfiguration yamlOrchestrationConfiguration = new YamlOrchestrationConfiguration(instanceConfigurationMap);
        assertThat(yamlOrchestrationConfiguration.getInstanceConfigurationMap(), is(instanceConfigurationMap));
    }
}
