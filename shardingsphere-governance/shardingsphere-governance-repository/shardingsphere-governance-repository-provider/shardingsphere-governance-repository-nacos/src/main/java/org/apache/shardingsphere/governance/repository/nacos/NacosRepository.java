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

package org.apache.shardingsphere.governance.repository.nacos;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.governance.repository.api.ConfigurationRepository;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceCenterConfiguration;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent.ChangedType;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEventListener;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * Governance repository of Nacos.
 */
@Slf4j
public final class NacosRepository implements ConfigurationRepository {
    
    private ConfigService configService;
    
    private NacosProperties nacosProperties;
    
    @Getter
    @Setter
    private Properties props = new Properties();
    
    /**
     * Initialize nacos instance.
     *
     * @param config config center configuration
     */
    @Override
    public void init(final String name, final GovernanceCenterConfiguration config) {
        try {
            nacosProperties = new NacosProperties(props);
            Properties props = new Properties();
            props.setProperty(PropertyKeyConst.SERVER_ADDR, config.getServerLists());
            props.setProperty(PropertyKeyConst.NAMESPACE, null == name ? "" : name);
            configService = NacosFactory.createConfigService(props);
        } catch (final NacosException ex) {
            log.error("Init nacos config center exception for: {}", ex.toString());
        }
    }
    
    /**
     * Get data from nacos instance.
     *
     * @param key key of data
     * @return value of data
     */
    @Override
    public String get(final String key) {
        try {
            String dataId = pathToKey(key);
            String group = nacosProperties.getValue(NacosPropertyKey.GROUP);
            long timeoutMs = nacosProperties.getValue(NacosPropertyKey.TIMEOUT);
            return configService.getConfig(dataId, group, timeoutMs);
        } catch (final NacosException ex) {
            log.debug("Nacos get config value exception for: {}", ex.toString());
            return null;
        }
    }
    
    /**
     * Get node's sub-nodes list.
     *
     * @param key key of data
     * @return sub-nodes name list
     */
    @Override
    public List<String> getChildrenKeys(final String key) {
        return null;
    }
    
    /**
     * Persist data.
     *
     * @param key key of data
     * @param value value of data
     */
    @Override
    public void persist(final String key, final String value) {
        try {
            String dataId = pathToKey(key);
            String group = nacosProperties.getValue(NacosPropertyKey.GROUP);
            configService.publishConfig(dataId, group, value);
        } catch (final NacosException ex) {
            log.debug("Nacos persist config exception for: {}", ex.toString());
        }
    }
    
    /**
     * Watch key or path of the config server.
     *
     * @param key key of data
     * @param listener data changed event listener
     */
    @Override
    public void watch(final String key, final DataChangedEventListener listener) {
        try {
            String dataId = pathToKey(key);
            String group = nacosProperties.getValue(NacosPropertyKey.GROUP);
            configService.addListener(dataId, group, new Listener() {
                
                @Override
                public Executor getExecutor() {
                    return null;
                }
                
                @Override
                public void receiveConfigInfo(final String configInfo) {
                    listener.onChange(new DataChangedEvent(key, configInfo, ChangedType.UPDATED));
                }
            });
        } catch (final NacosException ex) {
            log.debug("Nacos watch key exception for: {}", ex.toString());
        }
    }
    
    @Override
    public void delete(final String key) {
        try {
            String dataId = pathToKey(key);
            configService.removeConfig(dataId, nacosProperties.getValue(NacosPropertyKey.GROUP));
        } catch (final NacosException ex) {
            log.debug("Nacos remove config exception for: {}", ex.toString());
        }
    }
    
    private String pathToKey(final String path) {
        String key = path.replace(PATH_SEPARATOR, DOT_SEPARATOR);
        return key.substring(key.indexOf(DOT_SEPARATOR) + 1);
    }
    
    @Override
    public void close() {
    }
    
    /**
     * Get algorithm type.
     *
     * @return type
     */
    @Override
    public String getType() {
        return "Nacos";
    }
}
