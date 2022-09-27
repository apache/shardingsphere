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

package org.apache.shardingsphere.data.pipeline.core.task;

import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.PlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.api.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.datasource.DefaultPipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.fixture.FixturePipelineJobProgressListener;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.StandardPipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.util.JobConfigurationBuilder;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineContextUtil;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationTaskConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

public final class IncrementalTaskTest {
    
    private IncrementalTask incrementalTask;
    
    @BeforeClass
    public static void beforeClass() {
        PipelineContextUtil.mockModeConfigAndContextManager();
    }
    
    @Before
    public void setUp() {
        MigrationTaskConfiguration taskConfig = PipelineContextUtil.mockMigrationJobItemContext(JobConfigurationBuilder.createJobConfiguration()).getTaskConfig();
        taskConfig.getDumperConfig().setPosition(new PlaceholderPosition());
        PipelineTableMetaDataLoader metaDataLoader = new StandardPipelineTableMetaDataLoader(mock(PipelineDataSourceWrapper.class));
        incrementalTask = new IncrementalTask(3, taskConfig.getDumperConfig(), taskConfig.getImporterConfig(),
                PipelineContextUtil.getPipelineChannelCreator(), new DefaultPipelineDataSourceManager(), metaDataLoader,
                PipelineContextUtil.getExecuteEngine(), PipelineContextUtil.getExecuteEngine(), new FixturePipelineJobProgressListener());
    }
    
    @Test
    public void assertStart() {
        incrementalTask.start();
        assertThat(incrementalTask.getTaskId(), is("standard_0"));
        assertThat(incrementalTask.getTaskProgress().getPosition(), instanceOf(PlaceholderPosition.class));
    }
    
    @After
    public void tearDown() {
        incrementalTask.stop();
    }
}
