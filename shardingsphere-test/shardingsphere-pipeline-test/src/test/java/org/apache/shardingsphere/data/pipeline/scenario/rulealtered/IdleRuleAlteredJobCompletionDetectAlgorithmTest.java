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

import org.apache.shardingsphere.data.pipeline.api.detect.RuleAlteredJobAlmostCompletedParameter;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.FinishedPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.PlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.api.job.progress.JobProgress;
import org.apache.shardingsphere.data.pipeline.api.task.progress.IncrementalTaskProgress;
import org.apache.shardingsphere.data.pipeline.api.task.progress.InventoryTaskProgress;
import org.apache.shardingsphere.data.pipeline.core.util.ReflectionUtil;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.spi.IdleRuleAlteredJobCompletionDetectAlgorithm;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class IdleRuleAlteredJobCompletionDetectAlgorithmTest {
    
    @Mock
    private Properties propsMock;
    
    private final IdleRuleAlteredJobCompletionDetectAlgorithm detectAlgorithm = new IdleRuleAlteredJobCompletionDetectAlgorithm();
    
    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        ReflectionUtil.setFieldValue(detectAlgorithm, "props", propsMock);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertInitFailNoIdleThresholdKey() {
        when(propsMock.containsKey(IdleRuleAlteredJobCompletionDetectAlgorithm.IDLE_THRESHOLD_KEY)).thenReturn(false);
        detectAlgorithm.init();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertInitFailInvalidIdleThresholdKey() {
        when(propsMock.containsKey(IdleRuleAlteredJobCompletionDetectAlgorithm.IDLE_THRESHOLD_KEY)).thenReturn(true);
        when(propsMock.getProperty(IdleRuleAlteredJobCompletionDetectAlgorithm.IDLE_THRESHOLD_KEY)).thenReturn("@");
        detectAlgorithm.init();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertInitFailNegativeIdleThresholdKey() {
        when(propsMock.containsKey(IdleRuleAlteredJobCompletionDetectAlgorithm.IDLE_THRESHOLD_KEY)).thenReturn(true);
        when(propsMock.getProperty(IdleRuleAlteredJobCompletionDetectAlgorithm.IDLE_THRESHOLD_KEY)).thenReturn("-8");
        detectAlgorithm.init();
    }
    
    @Test
    public void assertInitSuccess() {
        when(propsMock.containsKey(IdleRuleAlteredJobCompletionDetectAlgorithm.IDLE_THRESHOLD_KEY)).thenReturn(true);
        when(propsMock.getProperty(IdleRuleAlteredJobCompletionDetectAlgorithm.IDLE_THRESHOLD_KEY)).thenReturn("4");
        detectAlgorithm.init();
    }
    
    @Test
    public void assertGetType() {
        assertThat(detectAlgorithm.getType(), is("IDLE"));
    }
    
    @Test
    public void assertFalseOnFewJobProgresses() {
        int jobShardingCount = 2;
        Collection<JobProgress> jobProgresses = Collections.singleton(new JobProgress());
        RuleAlteredJobAlmostCompletedParameter parameter = new RuleAlteredJobAlmostCompletedParameter(jobShardingCount, jobProgresses);
        assertFalse(detectAlgorithm.isAlmostCompleted(parameter));
    }
    
    @Test
    public void assertFalseOnUnFinishedPosition() {
        int jobShardingCount = 1;
        JobProgress jobProgress = new JobProgress();
        Map<String, InventoryTaskProgress> inventoryTaskProgressMap = new LinkedHashMap<>();
        jobProgress.setInventoryTaskProgressMap(inventoryTaskProgressMap);
        inventoryTaskProgressMap.put("ds_0", new InventoryTaskProgress(new PlaceholderPosition()));
        Collection<JobProgress> jobProgresses = Collections.singleton(jobProgress);
        RuleAlteredJobAlmostCompletedParameter parameter = new RuleAlteredJobAlmostCompletedParameter(jobShardingCount, jobProgresses);
        assertFalse(detectAlgorithm.isAlmostCompleted(parameter));
    }
    
    @Test
    public void assertTrueWhenIdleMinutesNotReach() {
        int jobShardingCount = 1;
        long latestActiveTimeMillis = System.currentTimeMillis() - ThreadLocalRandom.current().nextLong(1, detectAlgorithm.getIncrementalTaskIdleMinuteThreshold());
        JobProgress jobProgress = createJobProgress(latestActiveTimeMillis);
        Collection<JobProgress> jobProgresses = Collections.singleton(jobProgress);
        RuleAlteredJobAlmostCompletedParameter parameter = new RuleAlteredJobAlmostCompletedParameter(jobShardingCount, jobProgresses);
        assertFalse(detectAlgorithm.isAlmostCompleted(parameter));
    }
    
    private JobProgress createJobProgress(final long latestActiveTimeMillis) {
        JobProgress result = new JobProgress();
        Map<String, InventoryTaskProgress> inventoryTaskProgressMap = new LinkedHashMap<>();
        result.setInventoryTaskProgressMap(inventoryTaskProgressMap);
        inventoryTaskProgressMap.put("ds_0", new InventoryTaskProgress(new FinishedPosition()));
        Map<String, IncrementalTaskProgress> incrementalTaskProgressMap = new LinkedHashMap<>();
        result.setIncrementalTaskProgressMap(incrementalTaskProgressMap);
        IncrementalTaskProgress incrementalTaskProgress = new IncrementalTaskProgress();
        incrementalTaskProgress.getIncrementalTaskDelay().setLatestActiveTimeMillis(latestActiveTimeMillis);
        incrementalTaskProgressMap.put("ds_0", incrementalTaskProgress);
        return result;
    }
    
    @Test
    public void assertTrueWhenJobAlmostCompleted() {
        int jobShardingCount = 1;
        long latestActiveTimeMillis = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(detectAlgorithm.getIncrementalTaskIdleMinuteThreshold() + 5);
        JobProgress jobProgress = createJobProgress(latestActiveTimeMillis);
        Collection<JobProgress> jobProgresses = Collections.singleton(jobProgress);
        RuleAlteredJobAlmostCompletedParameter parameter = new RuleAlteredJobAlmostCompletedParameter(jobShardingCount, jobProgresses);
        assertTrue(detectAlgorithm.isAlmostCompleted(parameter));
    }
}
