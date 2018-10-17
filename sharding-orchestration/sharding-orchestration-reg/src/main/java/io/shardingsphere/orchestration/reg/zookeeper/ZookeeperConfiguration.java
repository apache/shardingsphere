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
     * Namespace of registry center.
     */
    private String namespace;
    
    /**
     * Digest for registry center.
     *
     * <p>Default is not need digest.</p>
     */
    private String digest;
    
    /**
     * Operation timeout time in milliseconds.
     */
    private int operationTimeoutMilliseconds;
    
    /**
     * Max number of times to retry.
     */
    private int maxRetries = 3;
    
    /**
     * Time interval in milliseconds on each retry.
     */
    private int retryIntervalMilliseconds = 1000;
    
    /**
     * Time to live in seconds of ephemeral keys.
     */
    private int timeToLiveSeconds = 60;
    
    /**
     * RegCenter for zookeeper.
     *
     * <p>Default is ZookeeperRegistryCenter</p>
     */
    private boolean useNative;
}
