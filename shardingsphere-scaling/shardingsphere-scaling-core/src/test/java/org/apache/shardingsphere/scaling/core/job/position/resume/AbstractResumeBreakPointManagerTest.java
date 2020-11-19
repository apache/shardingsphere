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
import org.apache.shardingsphere.scaling.core.job.position.FinishedInventoryPosition;
import org.apache.shardingsphere.scaling.core.job.position.InventoryPosition;
import org.apache.shardingsphere.scaling.core.job.position.InventoryPositionManager;
import org.apache.shardingsphere.scaling.core.job.position.PlaceholderIncrementalPosition;
import org.apache.shardingsphere.scaling.core.job.position.PlaceholderInventoryPosition;
import org.apache.shardingsphere.scaling.core.job.position.PositionManager;
import org.apache.shardingsphere.scaling.core.job.position.PrimaryKeyPosition;
import org.apache.shardingsphere.scaling.core.utils.ReflectionUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.TreeMap;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class AbstractResumeBreakPointManagerTest {
    
    private AbstractResumeBreakPointManager resumeBreakPointManager;
    
    private final String incrementalPosition = "{\"ds0\":{},\"ds1\":{}}";
    
    private final String inventoryPosition = "{\"unfinished\":{\"ds1.t_order_1#0\":[0,200],\"ds0.t_order_1#0\":[0,100],\"ds0.t_order_2\":[]},\"finished\":[\"ds0.t_order_1#1\"]}";
    
    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        resumeBreakPointManager = new AbstractResumeBreakPointManager() {
            
            @Override
            public void persistInventoryPosition() {
            }
            
            @Override
            public void persistIncrementalPosition() {
            }
        };
        resumeBreakPointManager.setDatabaseType("H2");
        resumeBreakPointManager.setTaskPath("/");
        setPositionManagerMap("inventoryPositionManagerMap");
        setPositionManagerMap("incrementalPositionManagerMap");
    }
    
    private void setPositionManagerMap(final String inventoryPositionManagerMap) throws NoSuchFieldException, IllegalAccessException {
        ReflectionUtil.setFieldValue(AbstractResumeBreakPointManager.class, resumeBreakPointManager, inventoryPositionManagerMap, new TreeMap<String, PositionManager<InventoryPosition>>());
    }
    
    @Test
    public void assertResumeIncrementalPosition() {
        resumeBreakPointManager.resumeInventoryPosition("");
        assertThat(resumeBreakPointManager.getInventoryPositionManagerMap().size(), is(0));
        resumeBreakPointManager.resumeInventoryPosition(inventoryPosition);
        assertThat(resumeBreakPointManager.getInventoryPositionManagerMap().size(), is(4));
    }
    
    @Test
    public void assertResumeInventoryPosition() {
        resumeBreakPointManager.resumeIncrementalPosition("");
        assertThat(resumeBreakPointManager.getIncrementalPositionManagerMap().size(), is(0));
        resumeBreakPointManager.resumeIncrementalPosition(incrementalPosition);
        assertThat(resumeBreakPointManager.getIncrementalPositionManagerMap().size(), is(2));
    }
    
    @Test
    public void assertGetIncrementalPositionData() {
        resumeBreakPointManager.getIncrementalPositionManagerMap().put("ds0", new BasePositionManager<>(new PlaceholderIncrementalPosition()));
        resumeBreakPointManager.getIncrementalPositionManagerMap().put("ds1", new BasePositionManager<>(new PlaceholderIncrementalPosition()));
        assertThat(resumeBreakPointManager.getIncrementalPositionData(), is(incrementalPosition));
    }
    
    @Test
    public void assertPrimaryKeyPositionJson() {
        resumeBreakPointManager.getInventoryPositionManagerMap().put("ds0.t_order_1#0", new InventoryPositionManager<>(new PrimaryKeyPosition(0L, 100L)));
        assertThat(resumeBreakPointManager.getInventoryPositionData(), is("{\"unfinished\":{\"ds0.t_order_1#0\":[0,100]},\"finished\":[]}"));
    }
    
    @Test
    public void assertPlaceholderPositionJson() {
        resumeBreakPointManager.getInventoryPositionManagerMap().put("ds0.t_order_1#0", new InventoryPositionManager<>(new PlaceholderInventoryPosition()));
        assertThat(resumeBreakPointManager.getInventoryPositionData(), is("{\"unfinished\":{\"ds0.t_order_1#0\":[]},\"finished\":[]}"));
        assertThat(new PlaceholderInventoryPosition().toJson().toString(), is("[]"));
    }
    
    @Test
    public void assertFinishedPositionJson() {
        resumeBreakPointManager.getInventoryPositionManagerMap().put("ds0.t_order_1#0", new InventoryPositionManager<>(new FinishedInventoryPosition()));
        assertThat(resumeBreakPointManager.getInventoryPositionData(), is("{\"unfinished\":{},\"finished\":[\"ds0.t_order_1#0\"]}"));
        assertThat(new FinishedInventoryPosition().toJson().toString(), is("{}"));
    }
    
    @Test
    public void assertGetInventoryPositionData() {
        resumeBreakPointManager.getInventoryPositionManagerMap().put("ds0.t_order_1#0", new InventoryPositionManager<>(new PrimaryKeyPosition(0L, 100L)));
        resumeBreakPointManager.getInventoryPositionManagerMap().put("ds0.t_order_1#1", new InventoryPositionManager<>(new FinishedInventoryPosition()));
        resumeBreakPointManager.getInventoryPositionManagerMap().put("ds0.t_order_2", new InventoryPositionManager<>(new PlaceholderInventoryPosition()));
        resumeBreakPointManager.getInventoryPositionManagerMap().put("ds1.t_order_1#0", new InventoryPositionManager<>(new PrimaryKeyPosition(0L, 200L)));
        assertThat(resumeBreakPointManager.getInventoryPositionData(), is(inventoryPosition));
    }
    
    @Test
    public void assertGetDatabaseType() {
        assertThat(resumeBreakPointManager.getDatabaseType(), is("H2"));
    }
    
    @Test
    public void assertGetTaskPath() {
        assertThat(resumeBreakPointManager.getTaskPath(), is("/"));
    }
    
    @After
    public void tearDown() {
        resumeBreakPointManager.close();
    }
}
