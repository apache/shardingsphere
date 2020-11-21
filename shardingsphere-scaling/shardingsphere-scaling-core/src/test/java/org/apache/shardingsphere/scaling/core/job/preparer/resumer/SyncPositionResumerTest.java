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

package org.apache.shardingsphere.scaling.core.job.preparer.resumer;

import lombok.SneakyThrows;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.config.ServerConfiguration;
import org.apache.shardingsphere.scaling.core.datasource.DataSourceManager;
import org.apache.shardingsphere.scaling.core.job.ShardingScalingJob;
import org.apache.shardingsphere.scaling.core.job.position.PlaceholderPosition;
import org.apache.shardingsphere.scaling.core.job.position.PositionManager;
import org.apache.shardingsphere.scaling.core.job.position.PrimaryKeyPosition;
import org.apache.shardingsphere.scaling.core.job.position.resume.ResumeBreakPointManager;
import org.apache.shardingsphere.scaling.core.job.position.resume.ResumeBreakPointManagerFactory;
import org.apache.shardingsphere.scaling.core.util.ScalingConfigurationUtil;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class SyncPositionResumerTest {
    
    private ShardingScalingJob shardingScalingJob;
    
    private SyncPositionResumer syncPositionResumer;
    
    @Before
    public void setUp() {
        ScalingContext.getInstance().init(new ServerConfiguration());
        shardingScalingJob = mockShardingScalingJob();
        syncPositionResumer = new SyncPositionResumer();
    }
    
    @Test
    public void assertResumePosition() {
        ResumeBreakPointManager resumeBreakPointManager = ResumeBreakPointManagerFactory.newInstance("MySQL", "/scalingTest/position/0");
        resumeBreakPointManager.getInventoryPositionManagerMap().put("ds0", new PositionManager(new PrimaryKeyPosition(0, 100)));
        resumeBreakPointManager.getIncrementalPositionManagerMap().put("ds0.t_order", new PositionManager(new PlaceholderPosition()));
        syncPositionResumer.resumePosition(shardingScalingJob, new DataSourceManager(), resumeBreakPointManager);
        assertThat(shardingScalingJob.getIncrementalDataTasks().size(), is(1));
        assertTrue(shardingScalingJob.getInventoryDataTasks().isEmpty());
    }
    
    @Test
    public void assertPersistPosition() {
        ResumeBreakPointManager resumeBreakPointManager = mock(ResumeBreakPointManager.class);
        syncPositionResumer.persistPosition(shardingScalingJob, resumeBreakPointManager);
        verify(resumeBreakPointManager).persistIncrementalPosition();
        verify(resumeBreakPointManager).persistInventoryPosition();
    }
    
    @SneakyThrows(IOException.class)
    private ShardingScalingJob mockShardingScalingJob() {
        return ScalingConfigurationUtil.initJob("/config.json");
    }
}
