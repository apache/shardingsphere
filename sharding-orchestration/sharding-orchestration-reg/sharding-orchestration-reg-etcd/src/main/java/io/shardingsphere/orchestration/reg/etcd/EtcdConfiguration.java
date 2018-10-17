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

package io.shardingsphere.orchestration.reg.etcd;

import io.shardingsphere.orchestration.reg.api.RegistryCenterConfiguration;
import lombok.Getter;
import lombok.Setter;

/**
 * Registry center configuration for etcd.
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
    
    private int operationTimeoutMilliseconds = 500;
    
    private int maxRetries = 3;
    
    private int retryIntervalMilliseconds = 200;
    
    private int timeToLiveSeconds = 60;
    
    @Override
    public String getNamespace() {
        throw new UnsupportedOperationException("Cannot support namespace on ETCD");
    }
    
    @Override
    public String getDigest() {
        throw new UnsupportedOperationException("Cannot support digest on ETCD");
    }
}
