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

package org.apache.shardingsphere.scaling.core.job.progress;

import org.apache.shardingsphere.data.pipeline.core.ingest.position.FinishedPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.PlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.PrimaryKeyPosition;
import org.apache.shardingsphere.scaling.core.job.JobStatus;
import org.apache.shardingsphere.scaling.core.job.task.incremental.IncrementalTaskProgress;
import org.apache.shardingsphere.scaling.core.job.task.inventory.InventoryTaskProgress;
import org.apache.shardingsphere.scaling.core.util.ResourceUtil;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class JobProgressTest {
    
    @Test
    public void assertInit() {
        JobProgress jobProgress = JobProgress.init(ResourceUtil.readFileAndIgnoreComments("job-progress.yaml"));
        assertThat(jobProgress.getStatus(), is(JobStatus.RUNNING));
        assertThat(jobProgress.getSourceDatabaseType(), is("H2"));
        assertThat(jobProgress.getInventoryTaskProgressMap().size(), is(4));
        assertThat(jobProgress.getIncrementalTaskProgressMap().size(), is(1));
    }
    
    @Test
    public void assertGetIncrementalPosition() {
        JobProgress jobProgress = JobProgress.init(ResourceUtil.readFileAndIgnoreComments("job-progress.yaml"));
        Optional<IngestPosition<?>> positionOptional = jobProgress.getIncrementalPosition("ds0");
        assertTrue(positionOptional.isPresent());
        assertTrue(positionOptional.get() instanceof PlaceholderPosition);
    }
    
    @Test
    public void assertGetInventoryPosition() {
        JobProgress jobProgress = JobProgress.init(ResourceUtil.readFileAndIgnoreComments("job-progress.yaml"));
        assertThat(jobProgress.getInventoryPosition("ds0").size(), is(2));
        assertTrue(jobProgress.getInventoryPosition("ds0").get("ds0.t_1") instanceof FinishedPosition);
        assertTrue(jobProgress.getInventoryPosition("ds1").get("ds1.t_1") instanceof PlaceholderPosition);
        assertTrue(jobProgress.getInventoryPosition("ds1").get("ds1.t_2") instanceof PrimaryKeyPosition);
    }
    
    @Test
    public void assertToString() {
        JobProgress jobProgress = new JobProgress();
        jobProgress.setStatus(JobStatus.RUNNING);
        jobProgress.setSourceDatabaseType("H2");
        jobProgress.setIncrementalTaskProgressMap(mockIncrementalTaskProgressMap());
        jobProgress.setInventoryTaskProgressMap(mockInventoryTaskProgressMap());
        assertThat(jobProgress.toString(), is(ResourceUtil.readFileAndIgnoreComments("job-progress.yaml")));
    }
    
    private Map<String, IncrementalTaskProgress> mockIncrementalTaskProgressMap() {
        return Collections.singletonMap("ds0", new IncrementalTaskProgress(new PlaceholderPosition()));
    }
    
    private Map<String, InventoryTaskProgress> mockInventoryTaskProgressMap() {
        Map<String, InventoryTaskProgress> result = new HashMap<>(4, 1);
        result.put("ds0.t_1", new InventoryTaskProgress(new FinishedPosition()));
        result.put("ds0.t_2", new InventoryTaskProgress(new FinishedPosition()));
        result.put("ds1.t_1", new InventoryTaskProgress(new PlaceholderPosition()));
        result.put("ds1.t_2", new InventoryTaskProgress(new PrimaryKeyPosition(1, 2)));
        return result;
    }
}
