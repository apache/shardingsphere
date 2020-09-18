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

import org.apache.shardingsphere.scaling.core.fixture.FixtureNopManager;
import org.apache.shardingsphere.scaling.core.job.position.InventoryPosition;
import org.apache.shardingsphere.scaling.core.job.position.PositionManager;

import java.util.HashMap;
import java.util.Map;

public final class IncrementalPositionResumeBreakPointManager extends AbstractResumeBreakPointManager {
    
    public IncrementalPositionResumeBreakPointManager(final String databaseType, final String taskPath) {
        getIncrementalPositionManagerMap().put("ds0", new FixtureNopManager(""));
    }
    
    @Override
    public boolean isResumable() {
        return true;
    }
    
    @Override
    public Map<String, PositionManager<InventoryPosition>> getInventoryPositionManagerMap() {
        return new HashMap<>(1, 1);
    }
    
    @Override
    public void persistInventoryPosition() {
    }
    
    @Override
    public void persistIncrementalPosition() {
    }
}
