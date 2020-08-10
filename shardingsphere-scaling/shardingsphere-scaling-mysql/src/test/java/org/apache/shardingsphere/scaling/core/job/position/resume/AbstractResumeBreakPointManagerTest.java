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

import org.apache.shardingsphere.scaling.core.job.position.BasePositionManager;
import org.apache.shardingsphere.scaling.core.job.position.FinishedPosition;
import org.apache.shardingsphere.scaling.core.job.position.IncrementalPosition;
import org.apache.shardingsphere.scaling.core.job.position.InventoryPosition;
import org.apache.shardingsphere.scaling.core.job.position.InventoryPositionManager;
import org.apache.shardingsphere.scaling.core.job.position.PlaceholderPosition;
import org.apache.shardingsphere.scaling.core.job.position.PositionManager;
import org.apache.shardingsphere.scaling.core.job.position.PrimaryKeyPosition;
import org.apache.shardingsphere.scaling.mysql.binlog.BinlogPosition;
import org.apache.shardingsphere.scaling.utils.ReflectionUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.TreeMap;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class AbstractResumeBreakPointManagerTest {
    
    private AbstractResumeBreakPointManager resumeBreakPointManager;
    
    private final String incrementalPosition = "{\"ds0\":{\"filename\":\"mysql-bin.000001\",\"position\":4},\"ds1\":{\"filename\":\"mysql-bin.000002\",\"position\":4}}";
    
    private final String inventoryPosition = "{\"unfinished\":{\"ds0.t_order_1#0\":[0,100],\"ds0.t_order_2\":[],\"ds1.t_order_1#0\":[0,200]},\"finished\":[\"ds0.t_order_1#1\"]}";
    
    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        resumeBreakPointManager = new AbstractResumeBreakPointManager() {
        };
        resumeBreakPointManager.setDatabaseType("MySQL");
        resumeBreakPointManager.setTaskPath("/scalingTest/item-0");
        ReflectionUtil.getFieldFromClass(AbstractResumeBreakPointManager.class, "inventoryPositionManagerMap", true)
                .set(resumeBreakPointManager, new TreeMap<String, PositionManager<InventoryPosition>>());
        ReflectionUtil.getFieldFromClass(AbstractResumeBreakPointManager.class, "incrementalPositionManagerMap", true)
                .set(resumeBreakPointManager, new TreeMap<String, PositionManager<IncrementalPosition>>());
    }
    
    @Test
    public void assertResumeIncrementalPosition() {
        resumeBreakPointManager.resumeIncrementalPosition(incrementalPosition);
        assertThat(resumeBreakPointManager.getIncrementalPositionManagerMap().size(), is(2));
    }
    
    @Test
    public void assertResumeInventoryPosition() {
        resumeBreakPointManager.resumeInventoryPosition(inventoryPosition);
        assertThat(resumeBreakPointManager.getInventoryPositionManagerMap().size(), is(4));
    }
    
    @Test
    public void assertGetIncrementalPositionData() {
        resumeBreakPointManager.getIncrementalPositionManagerMap().put("ds0", new BasePositionManager<>(new BinlogPosition("mysql-bin.000001", 4L)));
        resumeBreakPointManager.getIncrementalPositionManagerMap().put("ds1", new BasePositionManager<>(new BinlogPosition("mysql-bin.000002", 4L)));
        assertThat(resumeBreakPointManager.getIncrementalPositionData(), is(incrementalPosition));
    }
    
    @Test
    public void assertPrimaryKeyPositionJson() {
        resumeBreakPointManager.getInventoryPositionManagerMap().put("ds0.t_order_1#0", new InventoryPositionManager<>(new PrimaryKeyPosition(0L, 100L)));
        assertThat(resumeBreakPointManager.getInventoryPositionData(), is("{\"unfinished\":{\"ds0.t_order_1#0\":[0,100]},\"finished\":[]}"));
    }
    
    @Test
    public void assertPlaceholderPositionJson() {
        resumeBreakPointManager.getInventoryPositionManagerMap().put("ds0.t_order_1#0", new InventoryPositionManager<>(new PlaceholderPosition()));
        assertThat(resumeBreakPointManager.getInventoryPositionData(), is("{\"unfinished\":{\"ds0.t_order_1#0\":[]},\"finished\":[]}"));
    }
    
    @Test
    public void assertFinishedPositionJson() {
        resumeBreakPointManager.getInventoryPositionManagerMap().put("ds0.t_order_1#0", new InventoryPositionManager<>(new FinishedPosition()));
        assertThat(resumeBreakPointManager.getInventoryPositionData(), is("{\"unfinished\":{},\"finished\":[\"ds0.t_order_1#0\"]}"));
    }
    
    @Test
    public void assertGetInventoryPositionData() {
        resumeBreakPointManager.getInventoryPositionManagerMap().put("ds0.t_order_1#0", new InventoryPositionManager<>(new PrimaryKeyPosition(0L, 100L)));
        resumeBreakPointManager.getInventoryPositionManagerMap().put("ds0.t_order_1#1", new InventoryPositionManager<>(new FinishedPosition()));
        resumeBreakPointManager.getInventoryPositionManagerMap().put("ds0.t_order_2", new InventoryPositionManager<>(new PlaceholderPosition()));
        resumeBreakPointManager.getInventoryPositionManagerMap().put("ds1.t_order_1#0", new InventoryPositionManager<>(new PrimaryKeyPosition(0L, 200L)));
        assertThat(resumeBreakPointManager.getInventoryPositionData(), is(inventoryPosition));
    }
}
