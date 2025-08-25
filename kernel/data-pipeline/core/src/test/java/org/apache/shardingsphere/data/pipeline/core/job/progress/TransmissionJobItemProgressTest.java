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

import org.apache.shardingsphere.data.pipeline.core.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.finished.IngestFinishedPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.type.IntegerPrimaryKeyIngestPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.type.StringPrimaryKeyIngestPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.type.UnsupportedKeyIngestPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.placeholder.IngestPlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.core.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.config.YamlTransmissionJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.swapper.YamlTransmissionJobItemProgressSwapper;
import org.apache.shardingsphere.data.pipeline.core.task.progress.InventoryTaskProgress;
import org.apache.shardingsphere.infra.util.file.SystemResourceFileUtils;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransmissionJobItemProgressTest {
    
    private static final YamlTransmissionJobItemProgressSwapper SWAPPER = new YamlTransmissionJobItemProgressSwapper();
    
    @Test
    void assertInit() {
        TransmissionJobItemProgress actual = getJobItemProgress(SystemResourceFileUtils.readFile("job-progress.yaml"));
        assertThat(actual.getStatus(), is(JobStatus.RUNNING));
        assertThat(actual.getSourceDatabaseType().getType(), is("H2"));
        assertThat(actual.getInventory().getProgresses().size(), is(4));
        assertNotNull(actual.getIncremental().getIncrementalTaskProgress());
    }
    
    @Test
    void assertGetIncrementalPosition() {
        TransmissionJobItemProgress actual = getJobItemProgress(SystemResourceFileUtils.readFile("job-progress.yaml"));
        Optional<IngestPosition> position = actual.getIncremental().getIncrementalPosition();
        assertTrue(position.isPresent());
        assertThat(position.get(), isA(IngestPlaceholderPosition.class));
    }
    
    @Test
    void assertGetInventoryPosition() {
        TransmissionJobItemProgress actual = getJobItemProgress(SystemResourceFileUtils.readFile("job-progress.yaml"));
        assertThat(actual.getInventory().getInventoryPosition("t_1").get("ds0.t_1#1"), isA(IngestFinishedPosition.class));
        assertThat(actual.getInventory().getInventoryPosition("t_1").get("ds1.t_1#1"), isA(IngestPlaceholderPosition.class));
        assertThat(actual.getInventory().getInventoryPosition("t_2").get("ds0.t_2#2"), isA(IngestFinishedPosition.class));
        assertThat(actual.getInventory().getInventoryPosition("t_2").get("ds1.t_2#2"), isA(IntegerPrimaryKeyIngestPosition.class));
    }
    
    @Test
    void assertGetIncrementalLatestActiveTimeMillis() {
        assertThat(getJobItemProgress(SystemResourceFileUtils.readFile("job-progress.yaml")).getIncremental().getIncrementalLatestActiveTimeMillis(), is(0L));
    }
    
    @Test
    void assertGetIncrementalDataLatestActiveTimeMillis() {
        assertThat(getJobItemProgress(SystemResourceFileUtils.readFile("job-progress-all-finished.yaml")).getIncremental().getIncrementalLatestActiveTimeMillis(), is(50L));
    }
    
    @Test
    void assertGetProgressesCorrectly() {
        Map<String, InventoryTaskProgress> progresses = new HashMap<>(4, 1F);
        progresses.put("ds.order_item#0", new InventoryTaskProgress(new IntegerPrimaryKeyIngestPosition(1L, 100L)));
        progresses.put("ds.order_item#1", new InventoryTaskProgress(new UnsupportedKeyIngestPosition()));
        progresses.put("ds.order#0", new InventoryTaskProgress(new IngestFinishedPosition()));
        progresses.put("ds.test_order#0", new InventoryTaskProgress(new StringPrimaryKeyIngestPosition("1", "100")));
        JobItemInventoryTasksProgress progress = new JobItemInventoryTasksProgress(progresses);
        Map<String, IngestPosition> orderPosition = progress.getInventoryPosition("order");
        assertThat(orderPosition.size(), is(1));
        assertThat(orderPosition.get("ds.order#0"), isA(IngestFinishedPosition.class));
        Map<String, IngestPosition> testOrderPosition = progress.getInventoryPosition("test_order");
        assertThat(testOrderPosition.size(), is(1));
        assertThat(testOrderPosition.get("ds.test_order#0"), isA(StringPrimaryKeyIngestPosition.class));
        Map<String, IngestPosition> orderItemPosition = progress.getInventoryPosition("order_item");
        assertThat(orderItemPosition.size(), is(2));
        assertThat(orderItemPosition.get("ds.order_item#0"), isA(IntegerPrimaryKeyIngestPosition.class));
        assertThat(orderItemPosition.get("ds.order_item#1"), isA(UnsupportedKeyIngestPosition.class));
    }
    
    private TransmissionJobItemProgress getJobItemProgress(final String data) {
        return SWAPPER.swapToObject(YamlEngine.unmarshal(data, YamlTransmissionJobItemProgress.class));
    }
}
