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

import com.google.common.collect.Maps;
import org.apache.shardingsphere.scaling.core.job.JobStatus;
import org.apache.shardingsphere.scaling.core.job.position.FinishedPosition;
import org.apache.shardingsphere.scaling.core.job.position.PlaceholderPosition;
import org.apache.shardingsphere.scaling.core.job.position.PrimaryKeyPosition;
import org.apache.shardingsphere.scaling.core.job.task.incremental.IncrementalTaskProgress;
import org.apache.shardingsphere.scaling.core.job.task.inventory.InventoryTaskProgress;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class JobProgressTest {
    
    @Test
    public void assertInit() {
        JobProgress jobProgress = JobProgress.init(mockJobProgressYamlString());
        assertThat(jobProgress.getStatus(), is(JobStatus.RUNNING));
        assertThat(jobProgress.getDatabaseType(), is("H2"));
        assertThat(jobProgress.getInventoryTaskProgressMap().size(), is(4));
        assertThat(jobProgress.getIncrementalTaskProgressMap().size(), is(1));
    }
    
    @Test
    public void assertGetIncrementalPosition() {
        JobProgress jobProgress = JobProgress.init(mockJobProgressYamlString());
        assertTrue(jobProgress.getIncrementalPosition("ds0") instanceof PlaceholderPosition);
    }
    
    @Test
    public void assertGetInventoryPosition() {
        JobProgress jobProgress = JobProgress.init(mockJobProgressYamlString());
        assertThat(jobProgress.getInventoryPosition("ds0").size(), is(2));
        assertTrue(jobProgress.getInventoryPosition("ds0").get("ds0.t_1") instanceof FinishedPosition);
        assertTrue(jobProgress.getInventoryPosition("ds1").get("ds1.t_1") instanceof PlaceholderPosition);
        assertTrue(jobProgress.getInventoryPosition("ds1").get("ds1.t_2") instanceof PrimaryKeyPosition);
    }
    
    @Test
    public void assertToString() {
        JobProgress jobProgress = new JobProgress();
        jobProgress.setStatus(JobStatus.RUNNING);
        jobProgress.setDatabaseType("H2");
        jobProgress.setIncrementalTaskProgressMap(mockIncrementalTaskProgressMap());
        jobProgress.setInventoryTaskProgressMap(mockInventoryTaskProgressMap());
        assertThat(jobProgress.toString(), is(mockJobProgressYamlString()));
    }
    
    private Map<String, IncrementalTaskProgress> mockIncrementalTaskProgressMap() {
        Map<String, IncrementalTaskProgress> result = Maps.newHashMap();
        result.put("ds0", new IncrementalTaskProgress(new PlaceholderPosition()));
        return result;
    }
    
    private Map<String, InventoryTaskProgress> mockInventoryTaskProgressMap() {
        Map<String, InventoryTaskProgress> result = Maps.newHashMap();
        result.put("ds0.t_1", new InventoryTaskProgress(new FinishedPosition()));
        result.put("ds0.t_2", new InventoryTaskProgress(new FinishedPosition()));
        result.put("ds1.t_1", new InventoryTaskProgress(new PlaceholderPosition()));
        result.put("ds1.t_2", new InventoryTaskProgress(new PrimaryKeyPosition(1, 2)));
        return result;
    }
    
    private String mockJobProgressYamlString() {
        return "databaseType: H2\n"
                + "incremental:\n"
                + "  ds0:\n"
                + "    delay:\n"
                + "      delayMilliseconds: -1\n"
                + "      lastEventTimestamps: 0\n"
                + "    position: ''\n"
                + "inventory:\n"
                + "  finished:\n"
                + "  - ds0.t_2\n"
                + "  - ds0.t_1\n"
                + "  unfinished:\n"
                + "    ds1.t_2: 1,2\n"
                + "    ds1.t_1: ''\n"
                + "status: RUNNING\n";
    }
}
