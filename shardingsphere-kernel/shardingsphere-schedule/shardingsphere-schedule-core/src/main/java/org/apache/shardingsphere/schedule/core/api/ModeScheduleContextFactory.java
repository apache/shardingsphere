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

package org.apache.shardingsphere.schedule.core.api;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mode schedule context factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class ModeScheduleContextFactory {
    
    private static final ModeScheduleContextFactory INSTANCE = new ModeScheduleContextFactory();
    
    private final Map<String, ModeScheduleContext> modeScheduleContexts = new ConcurrentHashMap<>();
    
    /**
     * Init mode schedule context.
     * 
     * @param instanceId instance id
     * @param modeConfig mode configuration
     */
    public void init(final String instanceId, final ModeConfiguration modeConfig) {
        modeScheduleContexts.put(instanceId, new ModeScheduleContext(modeConfig));
    }
    
    /**
     * Get mode schedule context of current instance.
     * 
     * @param instanceId instance id
     * @return mode schedule context
     */
    public Optional<ModeScheduleContext> get(final String instanceId) {
        if (modeScheduleContexts.values().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(modeScheduleContexts.get(instanceId));
    }
    
    /**
     * Get instance.
     *
     * @return singleton instance
     */
    public static ModeScheduleContextFactory getInstance() {
        return INSTANCE;
    }
}
