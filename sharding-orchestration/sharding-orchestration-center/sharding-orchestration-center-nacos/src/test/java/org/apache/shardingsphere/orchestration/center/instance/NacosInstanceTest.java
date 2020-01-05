package org.apache.shardingsphere.orchestration.center.instance;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import java.lang.reflect.Field;
import java.util.Properties;
import lombok.SneakyThrows;
import org.apache.shardingsphere.orchestration.center.api.ConfigCenter;
import org.apache.shardingsphere.orchestration.center.configuration.InstanceConfiguration;
import org.apache.shardingsphere.orchestration.center.listener.DataChangedEvent;
import org.apache.shardingsphere.orchestration.center.listener.DataChangedEventListener;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
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
    @SneakyThrows
    public void assertWatch() {
        final String expectValue = "expectValue";
        final String[] actualValue = {null};
        doAnswer(getListenerAnswer(expectValue)).when(configService).addListener(anyString(), anyString(), any(Listener.class));
        DataChangedEventListener listener = new DataChangedEventListener() {
            @Override
            public void onChange(final DataChangedEvent dataChangedEvent) {
                actualValue[0] = dataChangedEvent.getValue();
            }
        };
        nacosConfigCenter.watch("sharding/test", listener);
        Assert.assertEquals(expectValue, actualValue[0]);
    }
    
    private Answer getListenerAnswer(final String expectValue) {
        return new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) {
                Listener listener = invocation.getArgument(2);
                listener.receiveConfigInfo(expectValue);
                return null;
            }
        };
    }
}
