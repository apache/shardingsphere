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

package org.apache.shardingsphere.ui.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.orchestration.center.ConfigCenterRepository;
import org.apache.shardingsphere.orchestration.center.RegistryCenterRepository;
import org.apache.shardingsphere.orchestration.center.config.CenterConfiguration;
import org.apache.shardingsphere.orchestration.center.instance.CuratorZookeeperCenterRepository;
import org.apache.shardingsphere.ui.common.constant.InstanceType;
import org.apache.shardingsphere.ui.common.domain.CenterConfig;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Center factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CenterRepositoryFactory {
    
    private static final ConcurrentHashMap<String, RegistryCenterRepository> REGISTRY_CENTER_MAP = new ConcurrentHashMap<>();
    
    private static final ConcurrentHashMap<String, ConfigCenterRepository> CONFIG_CENTER_MAP = new ConcurrentHashMap<>();
    
    /**
     * Create registry center instance.
     *
     * @param config registry center config
     * @return registry center
     */
    public static RegistryCenterRepository createRegistryCenter(final CenterConfig config) {
        RegistryCenterRepository result = REGISTRY_CENTER_MAP.get(config.getName());
        if (null != result) {
            return result;
        }
        InstanceType instanceType = InstanceType.nameOf(config.getInstanceType());
        switch (instanceType) {
            case ZOOKEEPER:
                result = new CuratorZookeeperCenterRepository();
                break;
            default:
                throw new UnsupportedOperationException(config.getName());
        }
        result.init(convert(config));
        REGISTRY_CENTER_MAP.put(config.getName(), result);
        return result;
    }
    
    /**
     * Create config center instance
     * @param config config center config
     * @return config center
     */
    public static ConfigCenterRepository createConfigCenter(final CenterConfig config) {
        ConfigCenterRepository result = CONFIG_CENTER_MAP.get(config.getName());
        if (null != result) {
            return result;
        }
        InstanceType instanceType = InstanceType.nameOf(config.getInstanceType());
        switch (instanceType) {
            case ZOOKEEPER:
                result = new CuratorZookeeperCenterRepository();
                break;
            default:
                throw new UnsupportedOperationException(config.getName());
        }
        result.init(convert(config));
        CONFIG_CENTER_MAP.put(config.getName(), result);
        return result;
    }
    
    
    private static CenterConfiguration convert(final CenterConfig config) {
        CenterConfiguration result = new CenterConfiguration(config.getInstanceType());
        result.setServerLists(config.getServerLists());
        result.setNamespace(config.getNamespace());
        result.getProperties().put("digest", config.getDigest());
        return result;
    }
    
}
