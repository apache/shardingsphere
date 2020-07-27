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
import org.apache.shardingsphere.orchestration.center.config.CenterConfiguration;
import org.apache.shardingsphere.orchestration.center.instance.CuratorZookeeperCenterRepository;
import org.apache.shardingsphere.scaling.core.config.ResumeConfiguration;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;

import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Depends on zookeeper to manager position.
 */
@Slf4j
public final class ZookeeperResumablePositionManager extends AbstractResumablePositionManager implements ResumablePositionManager {
    
    private static final String INVENTORY = "/inventory";
    
    private static final String INCREMENTAL = "/incremental";
    
    private static final CuratorZookeeperCenterRepository ZOOKEEPER = new CuratorZookeeperCenterRepository();
    
    private ScheduledExecutorService executor;
    
    private String inventoryPath;
    
    private String incrementalPath;
    
    public ZookeeperResumablePositionManager() {
        ResumeConfiguration resumeConfiguration = ScalingContext.getInstance().getServerConfiguration().getResumeConfiguration();
        if (null != resumeConfiguration) {
            ZOOKEEPER.init(getCenterConfiguration(resumeConfiguration));
            log.info("zookeeper resumable position manager is available.");
            setAvailable(true);
        }
    }
    
    public ZookeeperResumablePositionManager(final String databaseType, final String taskPath) {
        setDatabaseType(databaseType);
        setTaskPath(taskPath);
        this.inventoryPath = taskPath + INVENTORY;
        this.incrementalPath = taskPath + INCREMENTAL;
        resumePosition();
        setResumable(!getInventoryPositionManagerMap().isEmpty() && !getIncrementalPositionManagerMap().isEmpty());
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(this::persistPosition, 1, 1, TimeUnit.MINUTES);
    }
    
    private CenterConfiguration getCenterConfiguration(final ResumeConfiguration resumeConfiguration) {
        CenterConfiguration centerConfiguration = new CenterConfiguration("zookeeper", new Properties());
        centerConfiguration.setServerLists(resumeConfiguration.getServerLists());
        centerConfiguration.setNamespace(resumeConfiguration.getNamespace());
        return centerConfiguration;
    }
    
    @Override
    public void close() {
        executor.submit(this::persistPosition);
        executor.shutdown();
    }
    
    private void resumePosition() {
        resumeInventoryPosition(ZOOKEEPER.get(inventoryPath));
        resumeIncrementalPosition(ZOOKEEPER.get(incrementalPath));
    }
    
    private void persistPosition() {
        persistIncrementalPosition();
        persistInventoryPosition();
    }
    
    @Override
    public void persistInventoryPosition() {
        String result = getInventoryPositionData();
        ZOOKEEPER.persist(inventoryPath, result);
        log.info("persist inventory position {} = {}", inventoryPath, result);
    }
    
    @Override
    public void persistIncrementalPosition() {
        String result = getIncrementalPositionData();
        ZOOKEEPER.persist(incrementalPath, result);
        log.info("persist incremental position {} = {}", incrementalPath, result);
    }
}
