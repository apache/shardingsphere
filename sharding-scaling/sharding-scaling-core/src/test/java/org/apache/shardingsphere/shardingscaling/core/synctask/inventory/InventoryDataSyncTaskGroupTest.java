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

package org.apache.shardingsphere.shardingscaling.core.synctask.inventory;

import org.apache.shardingsphere.shardingscaling.core.config.DataSourceConfiguration;
import org.apache.shardingsphere.shardingscaling.core.config.JDBCDataSourceConfiguration;
import org.apache.shardingsphere.shardingscaling.core.config.RdbmsConfiguration;
import org.apache.shardingsphere.shardingscaling.core.config.SyncConfiguration;
import org.apache.shardingsphere.shardingscaling.core.controller.SyncProgress;
import org.apache.shardingsphere.shardingscaling.core.controller.task.ReportCallback;
import org.apache.shardingsphere.shardingscaling.core.datasource.DataSourceManager;
import org.apache.shardingsphere.shardingscaling.core.synctask.SyncTask;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class InventoryDataSyncTaskGroupTest {
    
    private static String dataSourceUrl = "jdbc:h2:mem:test_db;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL";
    
    private static String userName = "root";
    
    private static String password = "password";
    
    private SyncConfiguration syncConfiguration;
    
    private DataSourceManager dataSourceManager;
    
    @Before
    public void setUp() {
        RdbmsConfiguration dumperConfig = mockDumperConfig();
        RdbmsConfiguration importerConfig = new RdbmsConfiguration();
        Map<String, String> tableMap = new HashMap<>();
        tableMap.put("t_order", "t_order");
        syncConfiguration = new SyncConfiguration(3, tableMap,
                dumperConfig, importerConfig);
        dataSourceManager = new DataSourceManager();
    }
    
    @After
    public void tearDown() {
        dataSourceManager.close();
    }
    
    @Test
    public void assertStart() {
        SyncTask syncTask = mock(SyncTask.class);
        InventoryDataSyncTaskGroup inventoryDataSyncTaskGroup = new InventoryDataSyncTaskGroup(syncConfiguration, Collections.singletonList(syncTask));
        inventoryDataSyncTaskGroup.start(event -> { });
        verify(syncTask).start(any(ReportCallback.class));
    }
    
    @Test
    public void assertStop() {
        SyncTask syncTask = mock(SyncTask.class);
        InventoryDataSyncTaskGroup inventoryDataSyncTaskGroup = new InventoryDataSyncTaskGroup(syncConfiguration, Collections.singletonList(syncTask));
        inventoryDataSyncTaskGroup.stop();
        verify(syncTask).stop();
    }
    
    @Test
    public void assertGetProgress() {
        InventoryDataSyncTaskGroup inventoryDataSyncTaskGroup = new InventoryDataSyncTaskGroup(syncConfiguration, Collections.emptyList());
        assertThat(inventoryDataSyncTaskGroup.getProgress(), instanceOf(SyncProgress.class));
    }
    
    private RdbmsConfiguration mockDumperConfig() {
        DataSourceConfiguration dataSourceConfiguration = new JDBCDataSourceConfiguration(dataSourceUrl, userName, password);
        RdbmsConfiguration result = new RdbmsConfiguration();
        result.setDataSourceConfiguration(dataSourceConfiguration);
        return result;
    }
}
