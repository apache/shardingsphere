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
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.scaling.core.job.position.FinishedPosition;
import org.apache.shardingsphere.scaling.core.job.position.IncrementalPosition;
import org.apache.shardingsphere.scaling.core.job.position.InventoryPosition;
import org.apache.shardingsphere.scaling.core.job.position.InventoryPositionManager;
import org.apache.shardingsphere.scaling.core.job.position.PositionManager;
import org.apache.shardingsphere.scaling.core.job.position.PositionManagerFactory;
import org.apache.shardingsphere.scaling.core.utils.InventoryPositionUtil;

import java.io.Closeable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Abstract resume from break-point manager.
 */
@Getter
@Setter
@Slf4j
public abstract class AbstractResumeBreakPointManager implements ResumeBreakPointManager, Closeable {
    
    private static final Gson GSON = new Gson();
    
    private static final String UNFINISHED = "unfinished";
    
    private static final String FINISHED = "finished";
    
    private final Map<String, PositionManager<InventoryPosition>> inventoryPositionManagerMap = Maps.newConcurrentMap();
    
    private final Map<String, PositionManager<IncrementalPosition>> incrementalPositionManagerMap = Maps.newConcurrentMap();
    
    private boolean resumable;
    
    private String databaseType;
    
    private String taskPath;
    
    @Override
    public void persistInventoryPosition() {
    }
    
    @Override
    public void persistIncrementalPosition() {
    }
    
    protected void resumeInventoryPosition(final String data) {
        if (Strings.isNullOrEmpty(data)) {
            return;
        }
        log.info("resume inventory position from {} = {}", taskPath, data);
        InventoryPositions inventoryPositions = InventoryPositions.fromJson(data);
        Map<String, InventoryPosition> unfinished = inventoryPositions.getUnfinished();
        for (Entry<String, InventoryPosition> entry : unfinished.entrySet()) {
            inventoryPositionManagerMap.put(entry.getKey(), new InventoryPositionManager<>(entry.getValue()));
        }
        for (String each : inventoryPositions.getFinished()) {
            inventoryPositionManagerMap.put(each, new InventoryPositionManager<>(new FinishedPosition()));
        }
    }
    
    protected void resumeIncrementalPosition(final String data) {
        if (Strings.isNullOrEmpty(data)) {
            return;
        }
        log.info("resume incremental position from {} = {}", taskPath, data);
        Map<String, Object> incrementalPosition = GSON.<Map<String, Object>>fromJson(data, Map.class);
        for (Entry<String, Object> entry : incrementalPosition.entrySet()) {
            incrementalPositionManagerMap.put(entry.getKey(), PositionManagerFactory.newInstance(databaseType, entry.getValue().toString()));
        }
    }
    
    protected String getInventoryPositionData() {
        JsonObject result = new JsonObject();
        JsonObject unfinished = new JsonObject();
        Set<String> finished = Sets.newHashSet();
        for (Entry<String, PositionManager<InventoryPosition>> entry : inventoryPositionManagerMap.entrySet()) {
            if (entry.getValue().getPosition() instanceof FinishedPosition) {
                finished.add(entry.getKey());
                continue;
            }
            unfinished.add(entry.getKey(), entry.getValue().getPosition().toJson());
        }
        result.add(UNFINISHED, unfinished);
        result.add(FINISHED, GSON.toJsonTree(finished));
        return result.toString();
    }
    
    protected String getIncrementalPositionData() {
        JsonObject result = new JsonObject();
        for (Entry<String, PositionManager<IncrementalPosition>> entry : incrementalPositionManagerMap.entrySet()) {
            result.add(entry.getKey(), entry.getValue().getPosition().toJson());
        }
        return result.toString();
    }
    
    @Override
    public void close() {
    }
    
    @Getter
    @Setter
    private static final class InventoryPositions {
        
        private Map<String, InventoryPosition> unfinished;
        
        private Set<String> finished;
        
        /**
         * Transform inventory position from json to object.
         *
         * @param data json data
         * @return inventory position
         */
        public static InventoryPositions fromJson(final String data) {
            InventoryPositions result = new InventoryPositions();
            JsonObject json = JsonParser.parseString(data).getAsJsonObject();
            Map<String, Object> unfinished = GSON.<Map<String, Object>>fromJson(json.getAsJsonObject(UNFINISHED), Map.class);
            result.setUnfinished(unfinished.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> InventoryPositionUtil.fromJson(entry.getValue().toString()))));
            result.setFinished(GSON.<Set<String>>fromJson(json.getAsJsonArray(FINISHED), Set.class));
            return result;
        }
    }
}
