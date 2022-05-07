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
import org.apache.shardingsphere.data.pipeline.spi.detect.JobCompletionDetectAlgorithmFactory;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class IdleRuleAlteredJobCompletionDetectAlgorithmTest {
    
    @Test(expected = IllegalArgumentException.class)
    public void assertInitFailInvalidIdleThresholdKey() {
        Properties props = new Properties();
        props.setProperty("incremental-task-idle-seconds-threshold", "invalid_value");
        JobCompletionDetectAlgorithmFactory.newInstance(new ShardingSphereAlgorithmConfiguration("IDLE", props));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertInitFailNegativeIdleThresholdKey() {
        Properties props = new Properties();
        props.setProperty("incremental-task-idle-seconds-threshold", "-8");
        JobCompletionDetectAlgorithmFactory.newInstance(new ShardingSphereAlgorithmConfiguration("IDLE", props));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertFalseOnFewJobProgresses() {
        int jobShardingCount = 2;
        Collection<JobProgress> jobProgresses = Collections.singleton(new JobProgress());
        RuleAlteredJobAlmostCompletedParameter parameter = new RuleAlteredJobAlmostCompletedParameter(jobShardingCount, jobProgresses);
        assertFalse(JobCompletionDetectAlgorithmFactory.newInstance(new ShardingSphereAlgorithmConfiguration("IDLE", new Properties())).isAlmostCompleted(parameter));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertFalseOnUnFinishedPosition() {
        int jobShardingCount = 1;
        JobProgress jobProgress = new JobProgress();
        Map<String, InventoryTaskProgress> inventoryTaskProgressMap = new LinkedHashMap<>();
        jobProgress.setInventoryTaskProgressMap(inventoryTaskProgressMap);
        inventoryTaskProgressMap.put("foo_ds", new InventoryTaskProgress(new PlaceholderPosition()));
        Collection<JobProgress> jobProgresses = Collections.singleton(jobProgress);
        RuleAlteredJobAlmostCompletedParameter parameter = new RuleAlteredJobAlmostCompletedParameter(jobShardingCount, jobProgresses);
        assertFalse(JobCompletionDetectAlgorithmFactory.newInstance(new ShardingSphereAlgorithmConfiguration("IDLE", new Properties())).isAlmostCompleted(parameter));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertTrueWhenIdleMinutesNotReach() {
        int jobShardingCount = 1;
        long latestActiveTimeMillis = System.currentTimeMillis() - ThreadLocalRandom.current().nextLong(1, 1800L);
        JobProgress jobProgress = createJobProgress(latestActiveTimeMillis);
        Collection<JobProgress> jobProgresses = Collections.singleton(jobProgress);
        RuleAlteredJobAlmostCompletedParameter parameter = new RuleAlteredJobAlmostCompletedParameter(jobShardingCount, jobProgresses);
        assertFalse(JobCompletionDetectAlgorithmFactory.newInstance(new ShardingSphereAlgorithmConfiguration("IDLE", new Properties())).isAlmostCompleted(parameter));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertTrueWhenJobAlmostCompleted() {
        int jobShardingCount = 1;
        long latestActiveTimeMillis = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1800L + 1800L);
        JobProgress jobProgress = createJobProgress(latestActiveTimeMillis);
        Collection<JobProgress> jobProgresses = Collections.singleton(jobProgress);
        RuleAlteredJobAlmostCompletedParameter parameter = new RuleAlteredJobAlmostCompletedParameter(jobShardingCount, jobProgresses);
        assertTrue(JobCompletionDetectAlgorithmFactory.newInstance(new ShardingSphereAlgorithmConfiguration("IDLE", new Properties())).isAlmostCompleted(parameter));
    }
    
    private JobProgress createJobProgress(final long latestActiveTimeMillis) {
        JobProgress result = new JobProgress();
        result.setInventoryTaskProgressMap(Collections.singletonMap("foo_ds", new InventoryTaskProgress(new FinishedPosition())));
        IncrementalTaskProgress incrementalTaskProgress = new IncrementalTaskProgress();
        incrementalTaskProgress.getIncrementalTaskDelay().setLatestActiveTimeMillis(latestActiveTimeMillis);
        result.setIncrementalTaskProgressMap(Collections.singletonMap("foo_ds", incrementalTaskProgress));
        return result;
    }
}
