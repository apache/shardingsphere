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

package org.apache.shardingsphere.schedule.core;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.schedule.core.strategy.ScheduleStrategy;
import org.apache.shardingsphere.schedule.core.strategy.type.ClusterScheduleStrategy;
import org.apache.shardingsphere.schedule.core.strategy.type.StandaloneScheduleStrategy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Schedule context factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class ScheduleContextFactory {
    
    private static final ScheduleContextFactory INSTANCE = new ScheduleContextFactory();
    
    private final Map<String, ScheduleStrategy> scheduleStrategy = new ConcurrentHashMap<>();
    
    /**
     * Get instance.
     *
     * @return singleton instance
     */
    public static ScheduleContextFactory getInstance() {
        return INSTANCE;
    }
    
    /**
     * Init schedule context.
     * 
     * @param instanceId instance id
     * @param modeConfig mode configuration
     */
    public void init(final String instanceId, final ModeConfiguration modeConfig) {
        scheduleStrategy.put(instanceId, "Cluster".equalsIgnoreCase(modeConfig.getType()) && "ZooKeeper".equalsIgnoreCase(modeConfig.getRepository().getType())
                ? new ClusterScheduleStrategy(modeConfig.getRepository().getProps().getProperty("server-lists"), modeConfig.getRepository().getProps().getProperty("namespace"))
                : new StandaloneScheduleStrategy());
    }
    
    /**
     * Get schedule strategy.
     * 
     * @param instanceId instance id
     * @return get schedule strategy
     */
    public ScheduleStrategy get(final String instanceId) {
        return scheduleStrategy.get(instanceId);
    }
}
