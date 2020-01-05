package org.apache.shardingsphere.orchestration.center.instance;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.orchestration.center.api.ConfigCenter;
import org.apache.shardingsphere.orchestration.center.configuration.InstanceConfiguration;
import org.apache.shardingsphere.orchestration.center.listener.DataChangedEvent;
import org.apache.shardingsphere.orchestration.center.listener.DataChangedEventListener;

/**
 * The nacos instance for ConfigCenter.
 *
 * @author huangjian
 * @author sunbufu
 */
@Slf4j
public class NacosInstance implements ConfigCenter {
    
    private final static String DEFAULT_GROUP = "SHARDING_SPHERE_DEFAULT_GROUP";
    
    private ConfigService configService;
    
    @Getter
    @Setter
    private Properties properties = new Properties();
    
    @Override
    public void init(final InstanceConfiguration config) {
        try {
            Properties properties = new Properties();
            properties.put("serverAddr", config.getServerLists());
            properties.put("namespace", null == config.getNamespace() ? "" : config.getNamespace());
            configService = NacosFactory.createConfigService(properties);
        } catch (final NacosException ex) {
            log.debug("exception for: {}", ex.toString());
        }
    }
    
    @Override
    public String get(final String key) {
        return getDirectly(key);
    }
    
    private String getDirectly(final String key) {
        try {
            String dataId = key.replace("/", ".");
            String group = properties.getProperty("group", DEFAULT_GROUP);
            long timeoutMs = Long.parseLong(properties.getProperty("timeout", "3000"));
            return configService.getConfig(dataId, group, timeoutMs);
        } catch (final NacosException ex) {
            log.debug("exception for: {}", ex.toString());
            return null;
        }
    }
    
    @Override
    public List<String> getChildrenKeys(final String key) {
        return null;
    }
    
    @Override
    public void persist(final String key, final String value) {
        update(key, value);
    }
    
    private void update(final String key, final String value) {
        try {
            String dataId = key.replace("/", ".");
            String group = properties.getProperty("group", "SHARDING_SPHERE_DEFAULT_GROUP");
            configService.publishConfig(dataId, group, value);
        } catch (final NacosException ex) {
            log.debug("exception for: {}", ex.toString());
        }
    }
    
    @Override
    public void watch(final String key, final DataChangedEventListener dataChangedEventListener) {
        try {
            String dataId = key.replace("/", ".");
            String group = properties.getProperty("group", "SHARDING_SPHERE_DEFAULT_GROUP");
            configService.addListener(dataId, group, new Listener() {
                
                @Override
                public Executor getExecutor() {
                    return null;
                }
                
                @Override
                public void receiveConfigInfo(final String configInfo) {
                    dataChangedEventListener.onChange(new DataChangedEvent(key, configInfo, DataChangedEvent.ChangedType.UPDATED));
                }
            });
        } catch (final NacosException ex) {
            log.debug("exception for: {}", ex.toString());
        }
    }
    
    @Override
    public void close() {
    }
    
    @Override
    public String getType() {
        return "nacos";
    }
}
