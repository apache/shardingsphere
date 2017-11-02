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

import lombok.Getter;
import lombok.Setter;

/**
 * Zookeeper based registry center configuration.
 * 
 * @author zhangliang
 */
@Getter
@Setter
public final class ZookeeperConfiguration {
    
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
}
