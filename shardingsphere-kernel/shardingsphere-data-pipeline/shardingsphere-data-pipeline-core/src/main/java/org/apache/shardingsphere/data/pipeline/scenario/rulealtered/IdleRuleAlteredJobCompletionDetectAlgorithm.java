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

package org.apache.shardingsphere.data.pipeline.scenario.rulealtered;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.data.pipeline.api.detect.AllIncrementalTasksAlmostFinishedParameter;
import org.apache.shardingsphere.data.pipeline.spi.rulealtered.RuleAlteredJobCompletionDetectAlgorithm;

import java.util.Collection;
import java.util.Properties;

/**
 * Idle rule altered job completion detect algorithm.
 */
public final class IdleRuleAlteredJobCompletionDetectAlgorithm implements RuleAlteredJobCompletionDetectAlgorithm {
    
    public static final String IDLE_THRESHOLD_KEY = "incremental-task-idle-minute-threshold";
    
    private Properties props = new Properties();
    
    private long incrementalTaskIdleMinuteThreshold = 30;
    
    @Override
    public Properties getProps() {
        return props;
    }
    
    @Override
    public void setProps(final Properties props) {
        this.props = props;
    }
    
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
    public boolean allIncrementalTasksAlmostFinished(final AllIncrementalTasksAlmostFinishedParameter parameter) {
        Collection<Long> incrementalTaskIdleMinutes = parameter.getIncrementalTaskIdleMinutes();
        if (null == incrementalTaskIdleMinutes || incrementalTaskIdleMinutes.isEmpty()) {
            return false;
        }
        return incrementalTaskIdleMinutes.stream().allMatch(idleMinute -> idleMinute >= incrementalTaskIdleMinuteThreshold);
    }
}
