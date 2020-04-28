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

package org.apache.shardingsphere.shardingscaling.core.job.synctask.inventory;

import org.apache.shardingsphere.shardingscaling.core.job.SyncProgress;
import org.apache.shardingsphere.shardingscaling.core.datasource.DataSourceManager;
import org.apache.shardingsphere.shardingscaling.core.job.synctask.SyncTask;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class InventoryDataSyncTaskGroupTest {
    
    private DataSourceManager dataSourceManager;
    
    @Before
    public void setUp() {
        dataSourceManager = new DataSourceManager();
    }
    
    @After
    public void tearDown() {
        dataSourceManager.close();
    }
    
    @Test
    public void assertStart() {
        SyncTask syncTask = mock(SyncTask.class);
        InventoryDataSyncTaskGroup inventoryDataSyncTaskGroup = new InventoryDataSyncTaskGroup(Collections.singletonList(syncTask));
        inventoryDataSyncTaskGroup.start();
        verify(syncTask).start(null);
    }
    
    @Test
    public void assertStop() {
        SyncTask syncTask = mock(SyncTask.class);
        InventoryDataSyncTaskGroup inventoryDataSyncTaskGroup = new InventoryDataSyncTaskGroup(Collections.singletonList(syncTask));
        inventoryDataSyncTaskGroup.stop();
        verify(syncTask).stop();
    }
    
    @Test
    public void assertGetProgress() {
        InventoryDataSyncTaskGroup inventoryDataSyncTaskGroup = new InventoryDataSyncTaskGroup(Collections.emptyList());
        assertThat(inventoryDataSyncTaskGroup.getProgress(), instanceOf(SyncProgress.class));
    }
}
