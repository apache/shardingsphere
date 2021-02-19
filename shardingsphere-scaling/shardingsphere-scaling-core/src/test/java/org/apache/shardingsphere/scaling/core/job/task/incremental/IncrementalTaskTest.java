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

package org.apache.shardingsphere.scaling.core.job.task.incremental;

import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.config.ServerConfiguration;
import org.apache.shardingsphere.scaling.core.config.TaskConfiguration;
import org.apache.shardingsphere.scaling.core.executor.engine.ExecuteEngine;
import org.apache.shardingsphere.scaling.core.job.JobContext;
import org.apache.shardingsphere.scaling.core.job.position.PlaceholderPosition;
import org.apache.shardingsphere.scaling.core.job.task.ScalingTaskFactory;
import org.apache.shardingsphere.scaling.core.util.ReflectionUtil;
import org.apache.shardingsphere.scaling.core.util.ResourceUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.Future;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class IncrementalTaskTest {
    
    @Mock
    private ExecuteEngine executeEngine;
    
    @Mock
    @SuppressWarnings("rawtypes")
    private Future future;
    
    private IncrementalTask incrementalTask;
    
    @Before
    public void setUp() throws Exception {
        ReflectionUtil.setFieldValue(ScalingContext.getInstance(), "serverConfig", new ServerConfiguration());
        ReflectionUtil.setFieldValue(ScalingContext.getInstance(), "incrementalDumperExecuteEngine", executeEngine);
        TaskConfiguration taskConfig = new JobContext(ResourceUtil.mockJobConfig()).getTaskConfigs().iterator().next();
        taskConfig.getDumperConfig().setPosition(new PlaceholderPosition());
        incrementalTask = ScalingTaskFactory.createIncrementalTask(3, taskConfig.getDumperConfig(), taskConfig.getImporterConfig());
        
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void assertStart() {
        when(executeEngine.submitAll(any(), any())).thenReturn(future);
        incrementalTask.start();
        assertThat(incrementalTask.getTaskId(), is("ds_0"));
        assertTrue(incrementalTask.getProgress().getPosition() instanceof PlaceholderPosition);
    }
    
    @After
    public void tearDown() {
        incrementalTask.stop();
    }
}
