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

import lombok.SneakyThrows;
import org.apache.shardingsphere.scaling.core.job.position.FinishedPosition;
import org.apache.shardingsphere.scaling.core.job.position.PlaceholderPosition;
import org.apache.shardingsphere.scaling.core.job.position.PositionManager;
import org.apache.shardingsphere.scaling.core.job.position.PrimaryKeyPosition;
import org.apache.shardingsphere.scaling.core.utils.ReflectionUtil;
import org.junit.After;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class AbstractResumeBreakPointManagerTest {
    
    private AbstractResumeBreakPointManager resumeBreakPointManager;
    
    @Test
    public void assertResumeInventoryPosition() {
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("/base/inventory", "{\"unfinished\":{\"ds1.t_order_1#0\":[0,200],\"ds0.t_order_1#0\":[0,100],\"ds0.t_order_2\":[]},\"finished\":[\"ds0.t_order_1#1\"]}");
        resumeBreakPointManager = mockResumeBreakPointManager(dataMap);
        resumeBreakPointManager.resumeInventoryPosition("");
        assertThat(resumeBreakPointManager.getInventoryPositionManagerMap().size(), is(0));
        resumeBreakPointManager.resumeInventoryPosition("/base/inventory");
        assertThat(resumeBreakPointManager.getInventoryPositionManagerMap().size(), is(4));
    }
    
    @Test
    public void assertResumeIncrementalPosition() {
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("/base/incremental", "{\"ds0\":[],\"ds1\":[]}");
        resumeBreakPointManager = mockResumeBreakPointManager(dataMap);
        resumeBreakPointManager.resumeIncrementalPosition("");
        assertThat(resumeBreakPointManager.getIncrementalPositionManagerMap().size(), is(0));
        resumeBreakPointManager.resumeIncrementalPosition("/base/incremental");
        assertThat(resumeBreakPointManager.getIncrementalPositionManagerMap().size(), is(2));
    }
    
    @Test
    public void assertPersistInventoryPosition() {
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("/base/inventory", "{\"unfinished\":{\"ds0.t_order_2#0\":[],\"ds0.t_order_1#0\":[0,100]},\"finished\":[\"ds0.t_order_1#1\"]}");
        dataMap.put("/base/incremental", "{}");
        resumeBreakPointManager = mockResumeBreakPointManager(dataMap);
        resumeBreakPointManager.getInventoryPositionManagerMap().put("ds0.t_order_1#0", new PositionManager(new PrimaryKeyPosition(0L, 100L)));
        resumeBreakPointManager.getInventoryPositionManagerMap().put("ds0.t_order_1#1", new PositionManager(new FinishedPosition()));
        resumeBreakPointManager.getInventoryPositionManagerMap().put("ds0.t_order_2#0", new PositionManager(new PlaceholderPosition()));
        resumeBreakPointManager.persistPosition();
    }
    
    @Test
    public void assertPersistIncrementalPosition() {
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("/base/inventory", "{\"unfinished\":{},\"finished\":[]}");
        dataMap.put("/base/incremental", "{\"ds0\":[],\"ds1\":[]}");
        resumeBreakPointManager = mockResumeBreakPointManager(dataMap);
        resumeBreakPointManager.getIncrementalPositionManagerMap().put("ds0", new PositionManager(new PlaceholderPosition()));
        resumeBreakPointManager.getIncrementalPositionManagerMap().put("ds1", new PositionManager(new PlaceholderPosition()));
        resumeBreakPointManager.persistPosition();
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private AbstractResumeBreakPointManager mockResumeBreakPointManager(final Map<String, String> dataMap) {
        AbstractResumeBreakPointManager result = new AbstractResumeBreakPointManager("H2", "/base") {
            
            @Override
            public String getPosition(final String path) {
                return dataMap.get(path);
            }
            
            @Override
            public void persistPosition(final String path, final String data) {
                assertThat(data, is(dataMap.get(path)));
            }
        };
        ReflectionUtil.setFieldValue(AbstractResumeBreakPointManager.class, result, "inventoryPositionManagerMap", new TreeMap<String, PositionManager>());
        ReflectionUtil.setFieldValue(AbstractResumeBreakPointManager.class, result, "incrementalPositionManagerMap", new TreeMap<String, PositionManager>());
        return result;
    }
    
    @After
    public void tearDown() {
        resumeBreakPointManager.close();
    }
}
