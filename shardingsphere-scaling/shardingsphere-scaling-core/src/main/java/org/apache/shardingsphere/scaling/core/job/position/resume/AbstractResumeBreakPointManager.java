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
import com.google.gson.Gson;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.scaling.core.constant.ScalingConstant;
import org.apache.shardingsphere.scaling.core.job.position.FinishedPosition;
import org.apache.shardingsphere.scaling.core.job.position.InventoryPositionGroup;
import org.apache.shardingsphere.scaling.core.job.position.Position;
import org.apache.shardingsphere.scaling.core.job.position.PositionManager;
import org.apache.shardingsphere.scaling.core.job.position.PositionManagerFactory;

import java.io.Closeable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Abstract resume from break-point manager.
 */
@Slf4j
public abstract class AbstractResumeBreakPointManager implements ResumeBreakPointManager, Closeable {
    
    private static final Gson GSON = new Gson();
    
    @Getter
    private final Map<String, PositionManager> inventoryPositionManagerMap = Maps.newConcurrentMap();
    
    @Getter
    private final Map<String, PositionManager> incrementalPositionManagerMap = Maps.newConcurrentMap();
    
    @Getter
    private boolean resumable;
    
    private final String databaseType;
    
    private final String taskPath;
    
    private final ScheduledExecutorService executor;
    
    public AbstractResumeBreakPointManager(final String databaseType, final String taskPath) {
        this.databaseType = databaseType;
        this.taskPath = taskPath;
        resumePosition();
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(this::persistPosition, 1, 1, TimeUnit.MINUTES);
    }
    
    private void resumePosition() {
        try {
            resumeInventoryPosition(getInventoryPath());
            resumeIncrementalPosition(getIncrementalPath());
            resumable = !inventoryPositionManagerMap.isEmpty() && !incrementalPositionManagerMap.isEmpty();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            log.error("resume position failed.");
            throw ex;
        }
    }
    
    protected void resumeInventoryPosition(final String path) {
        String data = getPosition(path);
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
    
    protected void resumeIncrementalPosition(final String path) {
        String data = getPosition(path);
        if (Strings.isNullOrEmpty(data)) {
            return;
        }
        log.info("resume incremental position from {} = {}", taskPath, data);
        Map<String, Object> incrementalPosition = GSON.<Map<String, Object>>fromJson(data, Map.class);
        for (Entry<String, Object> entry : incrementalPosition.entrySet()) {
            incrementalPositionManagerMap.put(entry.getKey(), PositionManagerFactory.newInstance(databaseType, entry.getValue().toString()));
        }
    }
    
    @Override
    public void persistPosition() {
        try {
            persistIncrementalPosition();
            persistInventoryPosition();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            log.error("persist position failed.", ex);
        }
    }
    
    private void persistInventoryPosition() {
        InventoryPositionGroup inventoryPositionGroup = new InventoryPositionGroup();
        for (Entry<String, PositionManager> entry : inventoryPositionManagerMap.entrySet()) {
            if (entry.getValue().getPosition() instanceof FinishedPosition) {
                inventoryPositionGroup.getFinished().add(entry.getKey());
                continue;
            }
            inventoryPositionGroup.getUnfinished().put(entry.getKey(), entry.getValue().getPosition());
        }
        String data = inventoryPositionGroup.toJson();
        log.info("persist inventory position {} = {}", getInventoryPath(), data);
        persistPosition(getInventoryPath(), data);
    }
    
    private void persistIncrementalPosition() {
        String data = GSON.toJson(incrementalPositionManagerMap.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getPosition())));
        log.info("persist incremental position {} = {}", getIncrementalPath(), data);
        persistPosition(getIncrementalPath(), data);
    }
    
    protected String getInventoryPath() {
        return String.format("%s/%s", taskPath, ScalingConstant.INVENTORY);
    }
    
    protected String getIncrementalPath() {
        return String.format("%s/%s", taskPath, ScalingConstant.INCREMENTAL);
    }
    
    @Override
    public void close() {
        executor.submit((Runnable) this::persistPosition);
        executor.shutdown();
    }
}
