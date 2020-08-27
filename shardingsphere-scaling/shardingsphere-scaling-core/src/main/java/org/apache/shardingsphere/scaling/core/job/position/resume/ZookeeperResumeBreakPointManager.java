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
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.orchestration.repository.api.config.OrchestrationCenterConfiguration;
import org.apache.shardingsphere.orchestration.repository.zookeeper.CuratorZookeeperRepository;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Depends on zookeeper resume from break-point manager.
 */
@Slf4j
public final class ZookeeperResumeBreakPointManager extends AbstractResumeBreakPointManager {
    
    private static final String INVENTORY = "/inventory";
    
    private static final String INCREMENTAL = "/incremental";
    
    private static final CuratorZookeeperRepository CURATOR_ZOOKEEPER_REPOSITORY = new CuratorZookeeperRepository();
    
    private static boolean available;
    
    private final ScheduledExecutorService executor;
    
    private final String inventoryPath;
    
    private final String incrementalPath;
    
    static {
        String name = ScalingContext.getInstance().getServerConfiguration().getName();
        OrchestrationCenterConfiguration registryCenter = ScalingContext.getInstance().getServerConfiguration().getRegistryCenter();
        if (!Strings.isNullOrEmpty(name) && null != registryCenter) {
            CURATOR_ZOOKEEPER_REPOSITORY.init(name, registryCenter);
            log.info("zookeeper resume from break-point manager is available.");
            available = true;
        }
    }
    
    public ZookeeperResumeBreakPointManager(final String databaseType, final String taskPath) {
        setDatabaseType(databaseType);
        setTaskPath(taskPath);
        inventoryPath = taskPath + INVENTORY;
        incrementalPath = taskPath + INCREMENTAL;
        resumePosition();
        setResumable(!getInventoryPositionManagerMap().isEmpty() && !getIncrementalPositionManagerMap().isEmpty());
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(this::persistPosition, 1, 1, TimeUnit.MINUTES);
    }
    
    /**
     * If it is available.
     *
     * @return is available
     */
    public static boolean isAvailable() {
        return available;
    }
    
    @Override
    public void close() {
        executor.submit(this::persistPosition);
        executor.shutdown();
    }
    
    private void resumePosition() {
        resumeInventoryPosition(CURATOR_ZOOKEEPER_REPOSITORY.get(inventoryPath));
        resumeIncrementalPosition(CURATOR_ZOOKEEPER_REPOSITORY.get(incrementalPath));
    }
    
    private void persistPosition() {
        persistIncrementalPosition();
        persistInventoryPosition();
    }
    
    @Override
    public void persistInventoryPosition() {
        String result = getInventoryPositionData();
        CURATOR_ZOOKEEPER_REPOSITORY.persist(inventoryPath, result);
        log.info("persist inventory position {} = {}", inventoryPath, result);
    }
    
    @Override
    public void persistIncrementalPosition() {
        String result = getIncrementalPositionData();
        CURATOR_ZOOKEEPER_REPOSITORY.persist(incrementalPath, result);
        log.info("persist incremental position {} = {}", incrementalPath, result);
    }
}
