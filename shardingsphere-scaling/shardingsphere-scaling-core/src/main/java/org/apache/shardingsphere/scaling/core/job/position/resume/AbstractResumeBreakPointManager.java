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

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.scaling.core.job.position.FinishedPosition;
import org.apache.shardingsphere.scaling.core.job.position.InventoryPositionGroup;
import org.apache.shardingsphere.scaling.core.job.position.Position;
import org.apache.shardingsphere.scaling.core.job.position.PositionManager;
import org.apache.shardingsphere.scaling.core.job.position.PositionManagerFactory;

import java.io.Closeable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Abstract resume from break-point manager.
 */
@Getter
@Setter
@Slf4j
public abstract class AbstractResumeBreakPointManager implements ResumeBreakPointManager, Closeable {
    
    private static final Gson GSON = new Gson();
    
    private final Map<String, PositionManager> inventoryPositionManagerMap = Maps.newConcurrentMap();
    
    private final Map<String, PositionManager> incrementalPositionManagerMap = Maps.newConcurrentMap();
    
    private boolean resumable;
    
    private String databaseType;
    
    private String taskPath;
    
    protected final void resumeInventoryPosition(final String data) {
        if (Strings.isNullOrEmpty(data)) {
            return;
        }
        log.info("resume inventory position from {} = {}", taskPath, data);
        InventoryPositionGroup inventoryPositionGroup = InventoryPositionGroup.fromJson(data);
        Map<String, Position<?>> unfinished = inventoryPositionGroup.getUnfinished();
        for (Entry<String, Position<?>> entry : unfinished.entrySet()) {
            inventoryPositionManagerMap.put(entry.getKey(), new PositionManager(entry.getValue()));
        }
        for (String each : inventoryPositionGroup.getFinished()) {
            inventoryPositionManagerMap.put(each, new PositionManager(new FinishedPosition()));
        }
    }
    
    protected final void resumeIncrementalPosition(final String data) {
        if (Strings.isNullOrEmpty(data)) {
            return;
        }
        log.info("resume incremental position from {} = {}", taskPath, data);
        Map<String, Object> incrementalPosition = GSON.<Map<String, Object>>fromJson(data, Map.class);
        for (Entry<String, Object> entry : incrementalPosition.entrySet()) {
            incrementalPositionManagerMap.put(entry.getKey(), PositionManagerFactory.newInstance(databaseType, entry.getValue().toString()));
        }
    }
    
    protected final String getInventoryPositionData() {
        InventoryPositionGroup result = new InventoryPositionGroup();
        result.setUnfinished(Maps.newHashMap());
        result.setFinished(Sets.newHashSet());
        for (Entry<String, PositionManager> entry : inventoryPositionManagerMap.entrySet()) {
            if (entry.getValue().getPosition() instanceof FinishedPosition) {
                result.getFinished().add(entry.getKey());
                continue;
            }
            result.getUnfinished().put(entry.getKey(), entry.getValue().getPosition());
        }
        return result.toJson();
    }
    
    protected final String getIncrementalPositionData() {
        return GSON.toJson(incrementalPositionManagerMap.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getPosition())));
    }
    
    @Override
    public void close() {
    }
}
