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

package org.apache.shardingsphere.scaling.core.api.impl;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.scaling.core.api.ScalingClusterAutoSwitchAlgorithm;

import java.util.Collection;
import java.util.Properties;

/**
 * Scaling idle cluster auto switch algorithm.
 */
@Getter
@Setter
public final class ScalingIdleClusterAutoSwitchAlgorithm implements ScalingClusterAutoSwitchAlgorithm {
    
    static final String IDLE_THRESHOLD_KEY = "incremental-task-idle-minute-threshold";
    
    private Properties props = new Properties();
    
    private long incrementalTaskIdleMinuteThreshold = 30;
    
    @Override
    public void init() {
        Preconditions.checkArgument(props.containsKey(IDLE_THRESHOLD_KEY), "%s can not be null.", IDLE_THRESHOLD_KEY);
        incrementalTaskIdleMinuteThreshold = Long.parseLong(props.getProperty(IDLE_THRESHOLD_KEY));
        Preconditions.checkArgument(incrementalTaskIdleMinuteThreshold > 0, "%s value must be positive.", IDLE_THRESHOLD_KEY);
    }
    
    @Override
    public String getType() {
        return "IDLE";
    }
    
    @Override
    public boolean allIncrementalTasksAlmostFinished(final Collection<Long> incrementalTaskIdleMinutes) {
        if (null == incrementalTaskIdleMinutes || incrementalTaskIdleMinutes.isEmpty()) {
            return false;
        }
        return incrementalTaskIdleMinutes.stream().allMatch(idleMinute -> idleMinute >= incrementalTaskIdleMinuteThreshold);
    }
}
