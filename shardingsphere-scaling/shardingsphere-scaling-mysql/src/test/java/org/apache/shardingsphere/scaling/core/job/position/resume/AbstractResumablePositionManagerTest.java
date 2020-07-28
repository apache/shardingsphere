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

package org.apache.shardingsphere.scaling.core.job.position.resume;

import org.apache.shardingsphere.scaling.core.job.position.PrimaryKeyPosition;
import org.apache.shardingsphere.scaling.core.job.position.PrimaryKeyPositionManager;
import org.apache.shardingsphere.scaling.core.spi.ScalingEntryLoader;
import org.apache.shardingsphere.scaling.mysql.MySQLPositionManager;
import org.apache.shardingsphere.scaling.mysql.MySQLScalingEntry;
import org.apache.shardingsphere.scaling.utils.ReflectionUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public final class AbstractResumablePositionManagerTest {
    
    private AbstractResumablePositionManager resumablePositionManager;
    
    private final String incrementalPosition = "{\"ds0\":{\"filename\":\"mysql-bin.000001\",\"position\":4},\"ds1\":{\"filename\":\"mysql-bin.000002\",\"position\":4}}";
    
    private final String inventoryPosition = "{\"unfinish\":{\"ds1.t_order_1#0\":[0,200],\"ds0.t_order_1#0\":[0,100]},\"finished\":[\"ds0.t_order_1#1\"]}";
    
    @Before
    public void setUp() throws Exception {
        resumablePositionManager = new AbstractResumablePositionManager() {
        };
        resumablePositionManager.setDatabaseType("MySQL");
        resumablePositionManager.setTaskPath("/scalingTest/item-0");
        ReflectionUtil.getFieldValueFromClass(new ScalingEntryLoader(), "SCALING_ENTRY_MAP", Map.class).put("MySQL", new MySQLScalingEntry());
    }
    
    @Test
    public void assertResumeIncrementalPosition() {
        resumablePositionManager.resumeIncrementalPosition(incrementalPosition);
        assertEquals(2, resumablePositionManager.getIncrementalPositionManagerMap().size());
    }
    
    @Test
    public void assertResumeInventoryPosition() {
        resumablePositionManager.resumeInventoryPosition(inventoryPosition);
        assertEquals(3, resumablePositionManager.getInventoryPositionManagerMap().size());
    }
    
    @Test
    public void assertGetIncrementalPositionData() {
        resumablePositionManager.getIncrementalPositionManagerMap().put("ds0", new MySQLPositionManager("{\"filename\":\"mysql-bin.000001\",\"position\":4}"));
        resumablePositionManager.getIncrementalPositionManagerMap().put("ds1", new MySQLPositionManager("{\"filename\":\"mysql-bin.000002\",\"position\":4}"));
        assertEquals(incrementalPosition, resumablePositionManager.getIncrementalPositionData());
    }
    
    @Test
    public void assertGetInventoryPositionData() {
        resumablePositionManager.getInventoryPositionManagerMap().put("ds0.t_order_1#0", new PrimaryKeyPositionManager(new PrimaryKeyPosition(0, 100)));
        resumablePositionManager.getInventoryPositionManagerMap().put("ds0.t_order_1#1", new PrimaryKeyPositionManager(new PrimaryKeyPosition.FinishedPosition()));
        resumablePositionManager.getInventoryPositionManagerMap().put("ds1.t_order_1#0", new PrimaryKeyPositionManager(new PrimaryKeyPosition(0, 200)));
        assertEquals(inventoryPosition, resumablePositionManager.getInventoryPositionData());
    }
}
