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

import org.apache.shardingsphere.scaling.core.config.DataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.DumperConfiguration;
import org.apache.shardingsphere.scaling.core.config.ImporterConfiguration;
import org.apache.shardingsphere.scaling.core.config.JDBCDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.config.ServerConfiguration;
import org.apache.shardingsphere.scaling.core.config.SyncConfiguration;
import org.apache.shardingsphere.scaling.core.datasource.DataSourceManager;
import org.apache.shardingsphere.scaling.core.job.ShardingScalingJob;
import org.apache.shardingsphere.scaling.core.job.position.BasePositionManager;
import org.apache.shardingsphere.scaling.core.job.position.PrimaryKeyPosition;
import org.apache.shardingsphere.scaling.core.job.position.InventoryPositionManager;
import org.apache.shardingsphere.scaling.core.job.position.resume.ResumeBreakPointManager;
import org.apache.shardingsphere.scaling.core.job.position.resume.ResumeBreakPointManagerFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class SyncPositionResumerTest {
    
    private static final String DATA_SOURCE_URL = "jdbc:h2:mem:test_db;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL";
    
    private static final String USERNAME = "root";
    
    private static final String PASSWORD = "password";
    
    private ShardingScalingJob shardingScalingJob;
    
    private ResumeBreakPointManager resumeBreakPointManager;
    
    private SyncPositionResumer syncPositionResumer;
    
    @Before
    public void setUp() {
        ScalingContext.getInstance().init(new ServerConfiguration());
        shardingScalingJob = new ShardingScalingJob("scalingTest", 0);
        shardingScalingJob.getSyncConfigurations().add(mockSyncConfiguration());
        resumeBreakPointManager = ResumeBreakPointManagerFactory.newInstance("MySQL", "/scalingTest/position/0");
        syncPositionResumer = new SyncPositionResumer();
    }
    
    @Test
    public void assertResumePosition() {
        resumeBreakPointManager.getInventoryPositionManagerMap().put("ds0", new InventoryPositionManager<>(new PrimaryKeyPosition(0, 100)));
        resumeBreakPointManager.getIncrementalPositionManagerMap().put("ds0.t_order", new BasePositionManager<>());
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
    
    private SyncConfiguration mockSyncConfiguration() {
        DumperConfiguration dumperConfig = mockDumperConfig();
        ImporterConfiguration importerConfig = new ImporterConfiguration();
        return new SyncConfiguration(3, dumperConfig, importerConfig);
    }
    
    private DumperConfiguration mockDumperConfig() {
        DataSourceConfiguration dataSourceConfiguration = new JDBCDataSourceConfiguration(DATA_SOURCE_URL, USERNAME, PASSWORD);
        DumperConfiguration result = new DumperConfiguration();
        result.setDataSourceName("ds0");
        result.setDataSourceConfiguration(dataSourceConfiguration);
        Map<String, String> tableMap = new HashMap<>();
        tableMap.put("t_order", "t_order");
        result.setTableNameMap(tableMap);
        return result;
    }
}
