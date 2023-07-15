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

package org.apache.shardingsphere.data.pipeline.common.job.progress;

import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.common.ingest.position.FinishedPosition;
import org.apache.shardingsphere.data.pipeline.common.ingest.position.pk.type.IntegerPrimaryKeyPosition;
import org.apache.shardingsphere.data.pipeline.common.ingest.position.PlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.common.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.common.job.progress.yaml.YamlInventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.data.pipeline.common.job.progress.yaml.YamlInventoryIncrementalJobItemProgressSwapper;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.test.util.ConfigurationFileUtils;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryIncrementalJobItemProgressTest {
    
    private static final YamlInventoryIncrementalJobItemProgressSwapper SWAPPER = new YamlInventoryIncrementalJobItemProgressSwapper();
    
    @Test
    void assertInit() {
        InventoryIncrementalJobItemProgress actual = getJobItemProgress(ConfigurationFileUtils.readFile("job-progress.yaml"));
        assertThat(actual.getStatus(), is(JobStatus.RUNNING));
        assertThat(actual.getSourceDatabaseType().getType(), is("H2"));
        assertThat(actual.getInventory().getProgresses().size(), is(4));
        assertNotNull(actual.getIncremental().getIncrementalTaskProgress());
    }
    
    @Test
    void assertGetIncrementalPosition() {
        InventoryIncrementalJobItemProgress actual = getJobItemProgress(ConfigurationFileUtils.readFile("job-progress.yaml"));
        Optional<IngestPosition> position = actual.getIncremental().getIncrementalPosition();
        assertTrue(position.isPresent());
        assertThat(position.get(), instanceOf(PlaceholderPosition.class));
    }
    
    @Test
    void assertGetInventoryPosition() {
        InventoryIncrementalJobItemProgress actual = getJobItemProgress(ConfigurationFileUtils.readFile("job-progress.yaml"));
        assertThat(actual.getInventory().getInventoryPosition("ds0").size(), is(2));
        assertThat(actual.getInventory().getInventoryPosition("ds0").get("ds0.t_1"), instanceOf(FinishedPosition.class));
        assertThat(actual.getInventory().getInventoryPosition("ds1").get("ds1.t_1"), instanceOf(PlaceholderPosition.class));
        assertThat(actual.getInventory().getInventoryPosition("ds1").get("ds1.t_2"), instanceOf(IntegerPrimaryKeyPosition.class));
    }
    
    @Test
    void assertGetIncrementalLatestActiveTimeMillis() {
        assertThat(getJobItemProgress(ConfigurationFileUtils.readFile("job-progress.yaml")).getIncremental().getIncrementalLatestActiveTimeMillis(), is(0L));
    }
    
    @Test
    void assertGetIncrementalDataLatestActiveTimeMillis() {
        assertThat(getJobItemProgress(ConfigurationFileUtils.readFile("job-progress-all-finished.yaml")).getIncremental().getIncrementalLatestActiveTimeMillis(), is(50L));
    }
    
    private InventoryIncrementalJobItemProgress getJobItemProgress(final String data) {
        return SWAPPER.swapToObject(YamlEngine.unmarshal(data, YamlInventoryIncrementalJobItemProgress.class));
    }
}
