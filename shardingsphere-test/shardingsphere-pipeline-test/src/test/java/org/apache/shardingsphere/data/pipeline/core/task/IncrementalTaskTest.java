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

import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.TaskConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.PlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineContextUtil;
import org.apache.shardingsphere.data.pipeline.core.util.ResourceUtil;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.RuleAlteredJobContext;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class IncrementalTaskTest {
    
    private IncrementalTask incrementalTask;
    
    @BeforeClass
    public static void beforeClass() {
        PipelineContextUtil.mockModeConfig();
    }
    
    @Before
    public void setUp() {
        TaskConfiguration taskConfig = new RuleAlteredJobContext(ResourceUtil.mockJobConfig()).getTaskConfig();
        taskConfig.getDumperConfig().setPosition(new PlaceholderPosition());
        PipelineTableMetaDataLoader metaDataLoader = new PipelineTableMetaDataLoader(mock(PipelineDataSourceWrapper.class));
        incrementalTask = new IncrementalTask(3, taskConfig.getDumperConfig(), taskConfig.getImporterConfig(),
                PipelineContextUtil.getPipelineChannelFactory(),
                new PipelineDataSourceManager(), metaDataLoader, PipelineContextUtil.getExecuteEngine());
    }
    
    @Test
    public void assertStart() {
        incrementalTask.start();
        assertThat(incrementalTask.getTaskId(), is("ds_0"));
        assertTrue(incrementalTask.getProgress().getPosition() instanceof PlaceholderPosition);
    }
    
    @After
    public void tearDown() {
        incrementalTask.stop();
    }
}
