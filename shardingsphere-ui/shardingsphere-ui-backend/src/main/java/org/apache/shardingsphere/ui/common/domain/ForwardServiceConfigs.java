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

package org.apache.shardingsphere.ui.common.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Forward service configs.
 */
@Getter
@Setter
public final class ForwardServiceConfigs {
    
    private Map<String, ForwardServiceConfig> forwardServiceConfigs = new HashMap<>();
    
    /**
     * Put forward service config.
     *
     * @param forwardServiceType forward service type
     * @param forwardServiceConfig forward service config
     */
    public void putForwardServiceConfig(final String forwardServiceType, final ForwardServiceConfig forwardServiceConfig) {
        forwardServiceConfigs.put(forwardServiceType, forwardServiceConfig);
    }
    
    /**
     * Get forward service config by type.
     *
     * @param forwardServiceType forward service config
     * @return forward service config
     */
    public Optional<ForwardServiceConfig> getForwardServiceConfig(final String forwardServiceType) {
        return Optional.ofNullable(forwardServiceConfigs.get(forwardServiceType));
    }
    
    /**
     * Remove forward service config by type.
     *
     * @param forwardServiceType forward service config
     * @return forward service config
     */
    public Optional<ForwardServiceConfig> removeForwardServiceConfig(final String forwardServiceType) {
        return Optional.ofNullable(forwardServiceConfigs.remove(forwardServiceType));
    }
}
