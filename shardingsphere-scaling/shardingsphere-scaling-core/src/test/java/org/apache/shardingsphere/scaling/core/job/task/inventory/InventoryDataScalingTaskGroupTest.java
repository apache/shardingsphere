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

package org.apache.shardingsphere.scaling.core.job.task.inventory;

import org.apache.shardingsphere.scaling.core.datasource.DataSourceManager;
import org.apache.shardingsphere.scaling.core.job.SyncProgress;
import org.apache.shardingsphere.scaling.core.job.position.PositionManager;
import org.apache.shardingsphere.scaling.core.job.position.NopPosition;
import org.apache.shardingsphere.scaling.core.job.task.ScalingTask;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class InventoryDataScalingTaskGroupTest {
    
    private DataSourceManager dataSourceManager;
    
    @Mock
    private ScalingTask scalingTask;
    
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
        when(scalingTask.getPositionManager()).thenReturn(new PositionManager(new NopPosition()));
        InventoryDataScalingTaskGroup inventoryDataSyncTaskGroup = new InventoryDataScalingTaskGroup(Collections.singletonList(scalingTask));
        inventoryDataSyncTaskGroup.start();
        verify(scalingTask).start();
    }
    
    @Test
    public void assertStop() {
        InventoryDataScalingTaskGroup inventoryDataSyncTaskGroup = new InventoryDataScalingTaskGroup(Collections.singletonList(scalingTask));
        inventoryDataSyncTaskGroup.stop();
        verify(scalingTask).stop();
    }
    
    @Test
    public void assertGetProgress() {
        InventoryDataScalingTaskGroup inventoryDataSyncTaskGroup = new InventoryDataScalingTaskGroup(Collections.emptyList());
        assertThat(inventoryDataSyncTaskGroup.getProgress(), instanceOf(SyncProgress.class));
    }
}
