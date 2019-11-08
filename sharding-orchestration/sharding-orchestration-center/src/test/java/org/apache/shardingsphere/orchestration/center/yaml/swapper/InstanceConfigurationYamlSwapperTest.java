package org.apache.shardingsphere.orchestration.center.yaml.swapper;

import java.util.Properties;
import org.apache.shardingsphere.orchestration.center.configuration.InstanceConfiguration;
import org.apache.shardingsphere.orchestration.center.yaml.config.YamlInstanceConfiguration;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class InstanceConfigurationYamlSwapperTest {
    
    @Test
    public void assertToYaml() {
        InstanceConfiguration instanceConfiguration = getInstanceConfiguration();
        YamlInstanceConfiguration yamlConfiguration = new InstanceConfigurationYamlSwapper().swap(instanceConfiguration);
        assertThat(yamlConfiguration.getCenterType(), is(instanceConfiguration.getCenterType()));
        assertThat(yamlConfiguration.getInstanceType(), is(instanceConfiguration.getType()));
        assertThat(yamlConfiguration.getServerLists(), is(instanceConfiguration.getServerLists()));
        assertThat(yamlConfiguration.getNamespace(), is(instanceConfiguration.getNamespace()));
        assertThat(yamlConfiguration.getProps(), is(instanceConfiguration.getProperties()));
    }
    
    private InstanceConfiguration getInstanceConfiguration() {
        InstanceConfiguration result = new InstanceConfiguration("zookeeper", new Properties());
        result.setCenterType("config_center");
        result.setServerLists("127.0.0.1:2181,127.0.0.1:2182");
        result.setNamespace("orchestration");
        return result;
    }
    
    @Test
    public void assertSwapToConfiguration() {
        YamlInstanceConfiguration yamlConfiguration = getYamlInstanceConfiguration();
        InstanceConfiguration instanceConfiguration = new InstanceConfigurationYamlSwapper().swap(yamlConfiguration);
        assertThat(instanceConfiguration.getCenterType(), is(yamlConfiguration.getCenterType()));
        assertThat(instanceConfiguration.getType(), is(yamlConfiguration.getInstanceType()));
        assertThat(instanceConfiguration.getServerLists(), is(yamlConfiguration.getServerLists()));
        assertThat(instanceConfiguration.getNamespace(), is(yamlConfiguration.getNamespace()));
        assertThat(instanceConfiguration.getProperties(), is(yamlConfiguration.getProps()));
    }
    
    private YamlInstanceConfiguration getYamlInstanceConfiguration() {
        YamlInstanceConfiguration result = new YamlInstanceConfiguration();
        result.setInstanceType("zookeeper");
        result.setProps(new Properties());
        result.setCenterType("config_center");
        result.setServerLists("127.0.0.1:2181,127.0.0.1:2182");
        result.setNamespace("orchestration");
        return result;
    }
}
