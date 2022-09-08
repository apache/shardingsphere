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
import org.apache.shardingsphere.infra.schedule.ScheduleContext;
import org.apache.shardingsphere.schedule.core.context.ClusterScheduleContext;
import org.apache.shardingsphere.schedule.core.context.StandaloneScheduleContext;

/**
 * Schedule context factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class ScheduleContextFactory {
    
    /**
     * Create new instance of schedule context.
     *
     * @param modeConfig mode configuration
     * @return Schedule context instance
     */
    public static ScheduleContext newInstance(final ModeConfiguration modeConfig) {
        return "Cluster".equalsIgnoreCase(modeConfig.getType()) && "ZooKeeper".equalsIgnoreCase(modeConfig.getRepository().getType())
                ? new ClusterScheduleContext(modeConfig.getRepository().getProps().getProperty("server-lists"), modeConfig.getRepository().getProps().getProperty("namespace"))
                : new StandaloneScheduleContext();
    }
}
