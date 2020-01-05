package org.apache.shardingsphere.orchestration.center.instance;

import com.alibaba.nacos.api.config.ConfigService;
import java.lang.reflect.Field;
import java.util.Properties;
import lombok.SneakyThrows;
import org.apache.shardingsphere.orchestration.center.api.ConfigCenter;
import org.apache.shardingsphere.orchestration.center.configuration.InstanceConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NacosInstanceTest {
    
    private static ConfigCenter nacosConfigCenter = new NacosInstance();
    
    private ConfigService configService = mock(ConfigService.class);
    
    private String group = "SHARDING_SPHERE_DEFAULT_GROUP";
    
    @Before
    public void init() {
        Properties properties = new Properties();
        properties.setProperty("group", group);
        properties.setProperty("timeout", "3000");
        InstanceConfiguration configuration = new InstanceConfiguration(nacosConfigCenter.getType(), properties);
        configuration.setServerLists("x.x.x.:8848");
        nacosConfigCenter.init(configuration);
        setConfigService(configService);
    }
    
    @SneakyThrows
    private void setConfigService(final ConfigService configService) {
        Field configServiceField = NacosInstance.class.getDeclaredField("configService");
        configServiceField.setAccessible(true);
        configServiceField.set(nacosConfigCenter, configService);
    }
    
    @Test
    @SneakyThrows
    public void assertPersist() {
        String value = "value";
        nacosConfigCenter.persist("sharding/test", value);
        verify(configService).publishConfig("sharding.test", group, value);
    }
    
    @Test
    @SneakyThrows
    public void assertGet() {
        String value = "value";
        when(configService.getConfig(eq("sharding.test"), eq(group), anyLong())).thenReturn(value);
        Assert.assertEquals(value, nacosConfigCenter.get("sharding/test"));
    }
    
    @Test
    public void assertUpdate() {
//        nacosConfigCenter.update("sharding/test", "value2");
    }
}