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

package org.apache.shardingsphere.orchestration.reg.api;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.underlying.common.config.TypeBasedSPIConfiguration;

import java.util.Properties;

/**
 * Registry center configuration.
 *
 * @author zhangliang
 * @author zhaojun
 */
@Getter
@Setter
public final class RegistryCenterConfiguration extends TypeBasedSPIConfiguration {
    
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
    
    public RegistryCenterConfiguration(final String type) {
        super(type);
    }
    
    public RegistryCenterConfiguration(final String type, final Properties properties) {
        super(type, properties);
    }
}
