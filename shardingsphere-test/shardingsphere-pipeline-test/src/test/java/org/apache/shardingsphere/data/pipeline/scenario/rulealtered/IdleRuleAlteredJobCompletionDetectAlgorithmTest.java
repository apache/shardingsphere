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
import org.apache.shardingsphere.data.pipeline.api.job.progress.InventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.data.pipeline.api.job.progress.JobItemIncrementalTasksProgress;
import org.apache.shardingsphere.data.pipeline.api.job.progress.JobItemInventoryTasksProgress;
import org.apache.shardingsphere.data.pipeline.api.task.progress.IncrementalTaskProgress;
import org.apache.shardingsphere.data.pipeline.api.task.progress.InventoryTaskProgress;
import org.apache.shardingsphere.data.pipeline.spi.detect.JobCompletionDetectAlgorithmFactory;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
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
        JobCompletionDetectAlgorithmFactory.newInstance(new AlgorithmConfiguration("IDLE", props));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertInitFailNegativeIdleThresholdKey() {
        Properties props = new Properties();
        props.setProperty("incremental-task-idle-seconds-threshold", "-8");
        JobCompletionDetectAlgorithmFactory.newInstance(new AlgorithmConfiguration("IDLE", props));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertFalseOnFewJobProgresses() {
        int jobShardingCount = 2;
        Collection<InventoryIncrementalJobItemProgress> jobItemProgresses = Collections.singleton(new InventoryIncrementalJobItemProgress());
        RuleAlteredJobAlmostCompletedParameter parameter = new RuleAlteredJobAlmostCompletedParameter(jobShardingCount, jobItemProgresses);
        assertFalse(JobCompletionDetectAlgorithmFactory.newInstance(new AlgorithmConfiguration("IDLE", new Properties())).isAlmostCompleted(parameter));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertFalseOnUnFinishedPosition() {
        int jobShardingCount = 1;
        InventoryIncrementalJobItemProgress jobItemProgress = new InventoryIncrementalJobItemProgress();
        jobItemProgress.setInventory(new JobItemInventoryTasksProgress(Collections.singletonMap("foo_ds", new InventoryTaskProgress(new PlaceholderPosition()))));
        RuleAlteredJobAlmostCompletedParameter parameter = new RuleAlteredJobAlmostCompletedParameter(jobShardingCount, Collections.singleton(jobItemProgress));
        assertFalse(JobCompletionDetectAlgorithmFactory.newInstance(new AlgorithmConfiguration("IDLE", new Properties())).isAlmostCompleted(parameter));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertTrueWhenIdleMinutesNotReach() {
        int jobShardingCount = 1;
        long latestActiveTimeMillis = System.currentTimeMillis() - ThreadLocalRandom.current().nextLong(1, 1800L);
        InventoryIncrementalJobItemProgress jobItemProgress = createJobProgress(latestActiveTimeMillis);
        RuleAlteredJobAlmostCompletedParameter parameter = new RuleAlteredJobAlmostCompletedParameter(jobShardingCount, Collections.singleton(jobItemProgress));
        assertFalse(JobCompletionDetectAlgorithmFactory.newInstance(new AlgorithmConfiguration("IDLE", new Properties())).isAlmostCompleted(parameter));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertTrueWhenJobAlmostCompleted() {
        int jobShardingCount = 1;
        long latestActiveTimeMillis = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1800L + 1800L);
        InventoryIncrementalJobItemProgress jobItemProgress = createJobProgress(latestActiveTimeMillis);
        RuleAlteredJobAlmostCompletedParameter parameter = new RuleAlteredJobAlmostCompletedParameter(jobShardingCount, Collections.singleton(jobItemProgress));
        assertTrue(JobCompletionDetectAlgorithmFactory.newInstance(new AlgorithmConfiguration("IDLE", new Properties())).isAlmostCompleted(parameter));
    }
    
    private InventoryIncrementalJobItemProgress createJobProgress(final long latestActiveTimeMillis) {
        InventoryIncrementalJobItemProgress result = new InventoryIncrementalJobItemProgress();
        result.setInventory(new JobItemInventoryTasksProgress(Collections.singletonMap("foo_ds", new InventoryTaskProgress(new FinishedPosition()))));
        IncrementalTaskProgress incrementalTaskProgress = new IncrementalTaskProgress();
        incrementalTaskProgress.getIncrementalTaskDelay().setLatestActiveTimeMillis(latestActiveTimeMillis);
        JobItemIncrementalTasksProgress incremental = new JobItemIncrementalTasksProgress(Collections.singletonMap("foo_ds", incrementalTaskProgress));
        result.setIncremental(incremental);
        return result;
    }
}
