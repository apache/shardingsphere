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

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.governance.repository.api.RegistryRepository;
import org.apache.shardingsphere.scaling.core.constant.ScalingConstant;
import org.apache.shardingsphere.scaling.core.service.RegistryRepositoryHolder;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Repository resume from break-point manager.
 */
@Slf4j
public final class RepositoryResumeBreakPointManager extends AbstractResumeBreakPointManager {
    
    private static final RegistryRepository REGISTRY_REPOSITORY = RegistryRepositoryHolder.getInstance();
    
    private final ScheduledExecutorService executor;
    
    private final String inventoryPath;
    
    private final String incrementalPath;
    
    public RepositoryResumeBreakPointManager(final String databaseType, final String taskPath) {
        setDatabaseType(databaseType);
        setTaskPath(taskPath);
        inventoryPath = String.format("%s/%s", taskPath, ScalingConstant.INVENTORY);
        incrementalPath = String.format("%s/%s", taskPath, ScalingConstant.INCREMENTAL);
        resumePosition();
        setResumable(!getInventoryPositionManagerMap().isEmpty() && !getIncrementalPositionManagerMap().isEmpty());
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(this::persistPosition, 1, 1, TimeUnit.MINUTES);
    }
    
    private void resumePosition() {
        resumeInventoryPosition(REGISTRY_REPOSITORY.get(inventoryPath));
        resumeIncrementalPosition(REGISTRY_REPOSITORY.get(incrementalPath));
    }
    
    private void persistPosition() {
        persistIncrementalPosition();
        persistInventoryPosition();
    }
    
    @Override
    public void persistInventoryPosition() {
        String result = getInventoryPositionData();
        REGISTRY_REPOSITORY.persist(inventoryPath, result);
        log.info("persist inventory position {} = {}", inventoryPath, result);
    }
    
    @Override
    public void persistIncrementalPosition() {
        String result = getIncrementalPositionData();
        REGISTRY_REPOSITORY.persist(incrementalPath, result);
        log.info("persist incremental position {} = {}", incrementalPath, result);
    }
    
    @Override
    public void close() {
        executor.submit(this::persistPosition);
        executor.shutdown();
    }
}
