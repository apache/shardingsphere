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
import org.apache.shardingsphere.scaling.core.job.position.PositionManager;
import org.apache.shardingsphere.scaling.core.job.position.PositionManagerFactory;
import org.apache.shardingsphere.scaling.core.job.position.PrimaryKeyPosition;
import org.apache.shardingsphere.scaling.core.job.position.PrimaryKeyPositionManager;

import java.io.Closeable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Abstract resumable position manager.
 */
@Getter
@Setter
@Slf4j
public abstract class AbstractResumablePositionManager implements ResumablePositionManager, Closeable {
    
    private static final Gson GSON = new Gson();
    
    private final Map<String, PositionManager<PrimaryKeyPosition>> inventoryPositionManagerMap = Maps.newConcurrentMap();
    
    private final Map<String, PositionManager> incrementalPositionManagerMap = Maps.newConcurrentMap();
    
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
        InventoryPosition inventoryPosition = InventoryPosition.fromJson(data);
        Map<String, PrimaryKeyPosition> unfinish = inventoryPosition.getUnfinish();
        for (Map.Entry<String, PrimaryKeyPosition> entry : unfinish.entrySet()) {
            getInventoryPositionManagerMap().put(entry.getKey(), new PrimaryKeyPositionManager(entry.getValue()));
        }
        for (String each : inventoryPosition.getFinished()) {
            getInventoryPositionManagerMap().put(each, new PrimaryKeyPositionManager(new PrimaryKeyPosition.FinishedPosition()));
        }
    }
    
    protected void resumeIncrementalPosition(final String data) {
        if (Strings.isNullOrEmpty(data)) {
            return;
        }
        log.info("resume incremental position from {} = {}", taskPath, data);
        Map<String, Object> incrementalPosition = GSON.fromJson(data, Map.class);
        for (Map.Entry<String, Object> entry : incrementalPosition.entrySet()) {
            getIncrementalPositionManagerMap().put(entry.getKey(), PositionManagerFactory.newInstance(databaseType, entry.getValue().toString()));
        }
    }
    
    protected String getInventoryPositionData() {
        JsonObject result = new JsonObject();
        JsonObject unfinish = new JsonObject();
        Set<String> finished = Sets.newHashSet();
        for (Map.Entry<String, PositionManager<PrimaryKeyPosition>> entry : getInventoryPositionManagerMap().entrySet()) {
            if (entry.getValue().getCurrentPosition() instanceof PrimaryKeyPosition.FinishedPosition) {
                finished.add(entry.getKey());
                continue;
            }
            unfinish.add(entry.getKey(), entry.getValue().getCurrentPosition().toJson());
        }
        result.add("unfinish", unfinish);
        result.add("finished", GSON.toJsonTree(finished));
        return result.toString();
    }
    
    protected String getIncrementalPositionData() {
        JsonObject result = new JsonObject();
        for (Map.Entry<String, PositionManager> entry : getIncrementalPositionManagerMap().entrySet()) {
            result.add(entry.getKey(), entry.getValue().getCurrentPosition().toJson());
        }
        return result.toString();
    }
    
    @Override
    public void close() {
    
    }
    
    @Getter
    @Setter
    private static final class InventoryPosition {
        
        private Map<String, PrimaryKeyPosition> unfinish;
        
        private Set<String> finished;
        
        /**
         * Transform inventory position from json to object.
         *
         * @param data json data
         * @return inventory position
         */
        public static InventoryPosition fromJson(final String data) {
            InventoryPosition result = new InventoryPosition();
            JsonObject json = JsonParser.parseString(data).getAsJsonObject();
            Map<String, Object> unfinish = GSON.fromJson(json.getAsJsonObject("unfinish"), Map.class);
            result.setUnfinish(unfinish.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> PrimaryKeyPosition.fromJson(entry.getValue().toString()))));
            result.setFinished(GSON.fromJson(json.getAsJsonArray("finished"), Set.class));
            return result;
        }
    }
}
