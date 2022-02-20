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
import org.apache.shardingsphere.data.pipeline.api.ingest.position.PrimaryKeyPosition;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.progress.JobProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.JobProgressYamlSwapper;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.YamlJobProgress;
import org.apache.shardingsphere.data.pipeline.core.util.ResourceUtil;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class JobProgressTest {
    
    private static final JobProgressYamlSwapper JOB_PROGRESS_YAML_SWAPPER = new JobProgressYamlSwapper();
    
    private JobProgress getJobProgress(final String data) {
        return JOB_PROGRESS_YAML_SWAPPER.swapToObject(YamlEngine.unmarshal(data, YamlJobProgress.class));
    }
    
    @Test
    public void assertInit() {
        JobProgress jobProgress = getJobProgress(ResourceUtil.readFileAndIgnoreComments("job-progress.yaml"));
        assertThat(jobProgress.getStatus(), is(JobStatus.RUNNING));
        assertThat(jobProgress.getSourceDatabaseType(), is("H2"));
        assertThat(jobProgress.getInventoryTaskProgressMap().size(), is(4));
        assertThat(jobProgress.getIncrementalTaskProgressMap().size(), is(1));
    }
    
    @Test
    public void assertGetIncrementalPosition() {
        JobProgress jobProgress = getJobProgress(ResourceUtil.readFileAndIgnoreComments("job-progress.yaml"));
        Optional<IngestPosition<?>> positionOptional = jobProgress.getIncrementalPosition("ds0");
        assertTrue(positionOptional.isPresent());
        assertTrue(positionOptional.get() instanceof PlaceholderPosition);
    }
    
    @Test
    public void assertGetInventoryPosition() {
        JobProgress jobProgress = getJobProgress(ResourceUtil.readFileAndIgnoreComments("job-progress.yaml"));
        assertThat(jobProgress.getInventoryPosition("ds0").size(), is(2));
        assertTrue(jobProgress.getInventoryPosition("ds0").get("ds0.t_1") instanceof FinishedPosition);
        assertTrue(jobProgress.getInventoryPosition("ds1").get("ds1.t_1") instanceof PlaceholderPosition);
        assertTrue(jobProgress.getInventoryPosition("ds1").get("ds1.t_2") instanceof PrimaryKeyPosition);
    }
}
