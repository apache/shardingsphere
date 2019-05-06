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

import lombok.Getter;
import lombok.Setter;

/**
 * Registry center configuration.
 *
 * @author zhangliang
 */
@Getter
@Setter
public final class RegistryCenterConfiguration {
    
    /**
     * Server list of registry center.
     */
    private String serverLists;
    
    /**
     * Namespace of registry center.
     */
    private String namespace;
    
    /**
     * Digest of registry center.
     */
    private String digest;
    
    /**
     * Operation timeout time in milliseconds.
     */
    private int operationTimeoutMilliseconds = 500;
    
    /**
     * Max number of times to retry.
     */
    private int maxRetries = 3;
    
    /**
     * Time interval in milliseconds on each retry.
     */
    private int retryIntervalMilliseconds = 500;
    
    /**
     * Time to live in seconds of ephemeral keys.
     */
    private int timeToLiveSeconds = 60;
}
