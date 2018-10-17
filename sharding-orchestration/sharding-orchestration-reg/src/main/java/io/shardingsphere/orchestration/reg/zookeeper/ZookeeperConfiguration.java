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

package io.shardingsphere.orchestration.reg.zookeeper;

import io.shardingsphere.orchestration.reg.api.RegistryCenterConfiguration;
import lombok.Getter;
import lombok.Setter;

/**
 * Registry center configuration for zookeeper.
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
    
    private String namespace;
    
    /**
     * Default is not need digest.
     */
    private String digest;
    
    private int operationTimeoutMilliseconds;
    
    private int maxRetries = 3;
    
    private int retryIntervalMilliseconds = 1000;
    
    private int timeToLiveSeconds = 60;
    
    /**
     * RegCenter for zookeeper.
     *
     * <p>Default is ZookeeperRegistryCenter</p>
     */
    private boolean useNative;
}
