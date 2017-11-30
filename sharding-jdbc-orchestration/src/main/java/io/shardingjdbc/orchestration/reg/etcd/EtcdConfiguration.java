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

package io.shardingjdbc.orchestration.reg.etcd;

import io.shardingjdbc.orchestration.reg.api.RegistryCenterConfiguration;
import lombok.Getter;
import lombok.Setter;

/**
 * Etcd configuration.
 *
 * @author junxiong
 */
@Getter
@Setter
public final class EtcdConfiguration implements RegistryCenterConfiguration {
    
    /**
     * Etcd server list.
     * 
     * <p>Include ip address and port, multiple servers split by comma. Etc: {@code http://host1:2379,http://host2:2379}</p>
     */
    private String serverLists;
    
    /**
     * Time to live seconds of ephemeral keys.
     */
    private int timeToLiveSeconds = 60;
    
    /**
     * Timeout when calling a etcd method in milliseconds.
     */
    private int timeoutMilliseconds = 500;
    
    /**
     * Maximal retries when calling a etcd method.
     */
    private int retryIntervalMilliseconds = 200;
    
    /**
     * Maximal retries when calling a etcd method.
     */
    private int maxRetries = 3;
}
