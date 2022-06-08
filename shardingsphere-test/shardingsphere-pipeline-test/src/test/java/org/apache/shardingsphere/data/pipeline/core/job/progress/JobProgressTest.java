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

package org.apache.shardingsphere.data.pipeline.core.job.progress;

import org.apache.shardingsphere.data.pipeline.api.ingest.position.FinishedPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.PlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IntegerPrimaryKeyPosition;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.progress.JobProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.JobProgressYamlSwapper;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.YamlJobProgress;
import org.apache.shardingsphere.data.pipeline.core.util.ConfigurationFileUtil;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class JobProgressTest {
    
    private static final JobProgressYamlSwapper JOB_PROGRESS_YAML_SWAPPER = new JobProgressYamlSwapper();
    
    @Test
    public void assertInit() {
        JobProgress jobProgress = getJobProgress(ConfigurationFileUtil.readFile("job-progress.yaml"));
        assertThat(jobProgress.getStatus(), is(JobStatus.RUNNING));
        assertThat(jobProgress.getSourceDatabaseType(), is("H2"));
        assertThat(jobProgress.getInventoryTaskProgressMap().size(), is(4));
        assertThat(jobProgress.getIncrementalTaskProgressMap().size(), is(1));
    }
    
    @Test
    public void assertGetIncrementalPosition() {
        JobProgress jobProgress = getJobProgress(ConfigurationFileUtil.readFile("job-progress.yaml"));
        Optional<IngestPosition<?>> position = jobProgress.getIncrementalPosition("ds0");
        assertTrue(position.isPresent());
        assertThat(position.get(), instanceOf(PlaceholderPosition.class));
    }
    
    @Test
    public void assertGetInventoryPosition() {
        JobProgress jobProgress = getJobProgress(ConfigurationFileUtil.readFile("job-progress.yaml"));
        assertThat(jobProgress.getInventoryPosition("ds0").size(), is(2));
        assertThat(jobProgress.getInventoryPosition("ds0").get("ds0.t_1"), instanceOf(FinishedPosition.class));
        assertThat(jobProgress.getInventoryPosition("ds1").get("ds1.t_1"), instanceOf(PlaceholderPosition.class));
        assertThat(jobProgress.getInventoryPosition("ds1").get("ds1.t_2"), instanceOf(IntegerPrimaryKeyPosition.class));
    }
    
    @Test
    public void assertGetInventoryFinishedPercentage() {
        JobProgress jobProgress = getJobProgress(ConfigurationFileUtil.readFile("job-progress.yaml"));
        assertThat(jobProgress.getInventoryFinishedPercentage(), is(50));
    }
    
    @Test
    public void assertGetNoFinishedInventoryFinishedPercentage() {
        assertThat(getJobProgress(ConfigurationFileUtil.readFile("job-progress-no-finished.yaml")).getInventoryFinishedPercentage(), is(0));
    }
    
    @Test
    public void assertGetAllFinishedInventoryFinishedPercentage() {
        assertThat(getJobProgress(ConfigurationFileUtil.readFile("job-progress-all-finished.yaml")).getInventoryFinishedPercentage(), is(100));
    }
    
    @Test
    public void assertGetIncrementalLatestActiveTimeMillis() {
        assertThat(getJobProgress(ConfigurationFileUtil.readFile("job-progress.yaml")).getIncrementalLatestActiveTimeMillis(), is(0L));
    }
    
    @Test
    public void assertGetIncrementalDataLatestActiveTimeMillis() {
        assertThat(getJobProgress(ConfigurationFileUtil.readFile("job-progress-all-finished.yaml")).getIncrementalLatestActiveTimeMillis(), is(50L));
    }
    
    @Test
    public void assertGetNoIncrementalDataLatestActiveTimeMillis() {
        assertThat(getJobProgress(ConfigurationFileUtil.readFile("job-progress-no-finished.yaml")).getIncrementalLatestActiveTimeMillis(), is(0L));
    }
    
    private JobProgress getJobProgress(final String data) {
        return JOB_PROGRESS_YAML_SWAPPER.swapToObject(YamlEngine.unmarshal(data, YamlJobProgress.class));
    }
}
