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

import org.apache.shardingsphere.scaling.core.config.DumperConfiguration;
import org.apache.shardingsphere.scaling.core.config.ImporterConfiguration;
import org.apache.shardingsphere.scaling.core.config.rule.StandardJDBCDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.config.rule.DataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.ServerConfiguration;
import org.apache.shardingsphere.scaling.core.job.SyncProgress;
import org.apache.shardingsphere.scaling.core.job.position.PlaceholderPosition;
import org.apache.shardingsphere.scaling.core.job.position.PositionManager;
import org.apache.shardingsphere.scaling.core.job.task.DefaultSyncTaskFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class IncrementalDataScalingTaskTest {
    
    private static final String DATA_SOURCE_URL = "jdbc:h2:mem:test_db;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL";
    
    private static final String USERNAME = "root";
    
    private static final String PASSWORD = "password";
    
    private IncrementalDataScalingTask incrementalDataSyncTask;
    
    @Before
    public void setUp() {
        ScalingContext.getInstance().init(new ServerConfiguration());
        incrementalDataSyncTask = new DefaultSyncTaskFactory().createIncrementalDataSyncTask(3, mockDumperConfig(), mockImporterConfiguration());
    }
    
    @Test
    public void assertStart() {
        incrementalDataSyncTask.start();
        SyncProgress progress = incrementalDataSyncTask.getProgress();
        assertTrue(progress instanceof IncrementalDataSyncTaskProgress);
        assertThat(((IncrementalDataSyncTaskProgress) progress).getId(), is("ds0"));
        assertThat(((IncrementalDataSyncTaskProgress) progress).getDelayMillisecond(), is(Long.MAX_VALUE));
    }
    
    @After
    public void tearDown() {
        incrementalDataSyncTask.stop();
    }
    
    private ImporterConfiguration mockImporterConfiguration() {
        ImporterConfiguration result = new ImporterConfiguration();
        result.setDataSourceConfiguration(new StandardJDBCDataSourceConfiguration(DATA_SOURCE_URL, USERNAME, PASSWORD));
        return result;
    }
    
    private DumperConfiguration mockDumperConfig() {
        DataSourceConfiguration dataSourceConfig = new StandardJDBCDataSourceConfiguration(DATA_SOURCE_URL, USERNAME, PASSWORD);
        DumperConfiguration result = new DumperConfiguration();
        result.setDataSourceName("ds0");
        result.setDataSourceConfiguration(dataSourceConfig);
        Map<String, String> tableMap = new HashMap<>(1, 1);
        tableMap.put("t_order", "t_order");
        result.setTableNameMap(tableMap);
        result.setPositionManager(new PositionManager(new PlaceholderPosition()));
        return result;
    }
}
