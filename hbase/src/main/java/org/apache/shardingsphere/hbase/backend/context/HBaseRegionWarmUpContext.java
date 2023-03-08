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

package org.apache.shardingsphere.hbase.backend.context;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.RegionLocator;
import org.apache.shardingsphere.hbase.backend.bean.HBaseCluster;
import org.apache.shardingsphere.hbase.backend.connector.HBaseTaskExecutorManager;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * HBase region warn up context.
 */
@Getter
@Slf4j
public final class HBaseRegionWarmUpContext {
    
    private static final HBaseRegionWarmUpContext INSTANCE = new HBaseRegionWarmUpContext();
    
    private final AtomicInteger executeCount = new AtomicInteger(0);
    
    private final AtomicInteger tableCount = new AtomicInteger(0);
    
    private HBaseTaskExecutorManager executorManager;
    
    private long startWarmUpTime;
    
    /**
     * Get instance of HBase context.
     *
     * @return instance of HBase context
     */
    public static HBaseRegionWarmUpContext getInstance() {
        return INSTANCE;
    }
    
    /**
     * Init.
     * @param poolSize mul execute size
     */
    public void init(final int poolSize) {
        executorManager = new HBaseTaskExecutorManager(poolSize);
    }
    
    /**
     * Submit region warm up task.
     * @param tableName tableName
     * @param hbaseCluster hbaseCluster
     */
    public void submitWarmUpTask(final String tableName, final HBaseCluster hbaseCluster) {
        executorManager.submit(() -> loadRegionInfo(tableName, hbaseCluster));
    }
    
    private void loadRegionInfo(final String tableName, final HBaseCluster hbaseCluster) {
        try {
            RegionLocator regionLocator = hbaseCluster.getConnection().getRegionLocator(TableName.valueOf(tableName));
            regionLocator.getAllRegionLocations();
            HBaseRegionWarmUpContext.getInstance().addExecuteCount();
        } catch (IOException e) {
            log.error(String.format("table: %s warm up error, getRegionLocator execute error reason is  %s", tableName, e));
        }
    }
    
    /**
     * Init statistics info.
     * @param startWarmUpTime start warm up time
     */
    public void initStatisticsInfo(final long startWarmUpTime) {
        this.startWarmUpTime = startWarmUpTime;
    }
    
    /**
     * Execute count add one.
     */
    public void addExecuteCount() {
        this.executeCount.incrementAndGet();
    }
    
    /**
     * All need warm up table add one.
     */
    public void addNeedWarmCount() {
        this.tableCount.incrementAndGet();
    }
    
    /**
     * Sync execute.
     * @param clusterName clusterName
     */
    public void syncExecuteWarmUp(final String clusterName) {
        while (this.executeCount.get() < tableCount.get()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignore) {
                
            }
        }
        log.info(String.format("%s cluster end warm up, execute time: %dms, warm table: %d", clusterName, System.currentTimeMillis() - startWarmUpTime, executeCount.get()));
    }
    
    /**
     * Clear statistics info.
     */
    public void clear() {
        this.tableCount.set(0);
        this.executeCount.set(0);
    }
    
}
