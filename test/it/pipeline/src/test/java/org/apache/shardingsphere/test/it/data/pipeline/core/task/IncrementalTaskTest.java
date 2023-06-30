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

package org.apache.shardingsphere.test.it.data.pipeline.core.task;

import org.apache.shardingsphere.data.pipeline.api.ingest.dumper.Dumper;
import org.apache.shardingsphere.data.pipeline.common.ingest.position.PlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.common.task.progress.IncrementalTaskProgress;
import org.apache.shardingsphere.data.pipeline.core.importer.Importer;
import org.apache.shardingsphere.data.pipeline.core.task.IncrementalTask;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationTaskConfiguration;
import org.apache.shardingsphere.test.it.data.pipeline.core.util.JobConfigurationBuilder;
import org.apache.shardingsphere.test.it.data.pipeline.core.util.PipelineContextUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

class IncrementalTaskTest {
    
    private IncrementalTask incrementalTask;
    
    @BeforeAll
    static void beforeClass() {
        PipelineContextUtils.mockModeConfigAndContextManager();
    }
    
    @BeforeEach
    void setUp() {
        MigrationTaskConfiguration taskConfig = PipelineContextUtils.mockMigrationJobItemContext(JobConfigurationBuilder.createJobConfiguration()).getTaskConfig();
        taskConfig.getDumperConfig().setPosition(new PlaceholderPosition());
        incrementalTask = new IncrementalTask("ds_0", PipelineContextUtils.getExecuteEngine(), mock(Dumper.class),
                Collections.singletonList(mock(Importer.class)), new IncrementalTaskProgress(new PlaceholderPosition()));
    }
    
    @AfterEach
    void tearDown() {
        incrementalTask.stop();
    }
    
    @Test
    @Disabled("H2 doesn't support incremental")
    void assertStart() throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture.allOf(incrementalTask.start().toArray(new CompletableFuture[0])).get(10, TimeUnit.SECONDS);
        assertThat(incrementalTask.getTaskId(), is("ds_0"));
        assertThat(incrementalTask.getTaskProgress().getPosition(), instanceOf(PlaceholderPosition.class));
    }
}
