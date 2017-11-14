/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.orchestration.reg.zookeeper;

import io.shardingjdbc.orchestration.api.config.OrchestratorConfiguration;
import io.shardingjdbc.orchestration.reg.base.RegistryCenterConfiguration;
import io.shardingjdbc.orchestration.reg.base.RegistryChangeEvent;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * Zookeeper based registry center configuration.
 * 
 * @author zhangliang
 */
@Getter
@Setter
public final class ZookeeperConfiguration implements RegistryCenterConfiguration {
    
    /**
     * Zookeeper server list.
     * 
     * <p>Include ip address and port, multiple servers split by comma. Etc: {@code host1:2181,host2:2181}</p>
     */
    private String serverLists;
    
    /**
     * Namespace of zookeeper.
     */
    private String namespace;
    
    /**
     * Base sleep time milliseconds.
     */
    private int baseSleepTimeMilliseconds = 1000;
    
    /**
     * Max sleep time milliseconds.
     */
    private int maxSleepTimeMilliseconds = 3000;
    
    /**
     * Max retries.
     */
    private int maxRetries = 3;
    
    /**
     * Session timeout milliseconds.
     */
    private int sessionTimeoutMilliseconds;
    
    /**
     * Connection timeout milliseconds.
     */
    private int connectionTimeoutMilliseconds;
    
    /**
     * Digest for zookeeper.
     * 
     * <p>Default is not need digest</p>
     */
    private String digest;

    public static ZookeeperConfiguration from(OrchestratorConfiguration configuration) {
        ZookeeperConfiguration zkConfig = new ZookeeperConfiguration();
        Map<String, String> props = configuration.getRegistryCenter();
        if (props != null && !props.isEmpty()) {
            zkConfig.setNamespace(get(props, "namespace", zkConfig.namespace));
            zkConfig.setServerLists(get(props, "server-lists", zkConfig.serverLists));
            zkConfig.setBaseSleepTimeMilliseconds(get(props, "base-sleep-time-milliseconds", zkConfig.baseSleepTimeMilliseconds));
            zkConfig.setMaxSleepTimeMilliseconds(get(props, "max-sleep-time-milliseconds", zkConfig.maxSleepTimeMilliseconds));
            zkConfig.setMaxRetries(get(props, "max-retries", zkConfig.maxRetries));
            zkConfig.setSessionTimeoutMilliseconds(get(props, "session-timeout-milliseconds", zkConfig.sessionTimeoutMilliseconds));
            zkConfig.setConnectionTimeoutMilliseconds(get(props, "connection-timeout-milliseconds", zkConfig.connectionTimeoutMilliseconds));
            zkConfig.setDigest(get(props, "digest", zkConfig.digest));
        }
        return zkConfig;
    }

    private static int get(Map<String, String> properties, String key, int defaultValue) {
        String value = properties.get(key);
        return StringUtils.isEmpty(value) ? defaultValue : Integer.parseInt(value);
    }

    private static String get(Map<String, String> properties, String key, String defaultValue) {
        return properties.containsKey(key) ? properties.get(key) : defaultValue;
    }

}
