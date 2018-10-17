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

package io.shardingsphere.orchestration.reg.api;

/**
 * Registry center configuration.
 *
 * @author zhangliang
 */
public interface RegistryCenterConfiguration {
    
    /**
     * Get server list of registry center.
     * 
     * @return server list of registry center
     */
    String getServerLists();
    
    /**
     * Get namespace of registry center.
     * 
     * @return namespace of registry center
     */
    String getNamespace();
    
    /**
     * Get digest of registry center.
     * 
     * @return digest of registry center
     */
    String getDigest();
    
    /**
     * Get operation timeout time in milliseconds.
     * 
     * @return operation timeout time in milliseconds
     */
    int getOperationTimeoutMilliseconds();
    
    /**
     * Get max number of times to retry.
     * 
     * @return max number of times to retry
     */
    int getMaxRetries();
    
    /**
     * Get time interval in milliseconds on each retry.
     * 
     * @return time interval in milliseconds on each retry
     */
    int getRetryIntervalMilliseconds();
    
    /**
     * Get time to live in seconds of ephemeral keys.
     * 
     * @return time to live in seconds of ephemeral keys
     */
    int getTimeToLiveSeconds();
}
