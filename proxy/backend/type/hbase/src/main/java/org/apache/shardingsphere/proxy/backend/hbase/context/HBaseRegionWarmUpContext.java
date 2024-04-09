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

package org.apache.shardingsphere.proxy.backend.hbase.context;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.RegionLocator;
import org.apache.shardingsphere.proxy.backend.hbase.bean.HBaseCluster;
import org.apache.shardingsphere.proxy.backend.hbase.exception.HBaseOperationException;
import org.apache.shardingsphere.proxy.backend.hbase.executor.HBaseTaskExecutorManager;

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
     * 
     * @param poolSize execute pool size
     */
    public void init(final int poolSize) {
        executorManager = new HBaseTaskExecutorManager(poolSize);
    }
    
    /**
     * Submit region warm up task.
     * 
     * @param tableName table name
     * @param hbaseCluster HBase cluster
     */
    public void submitWarmUpTask(final String tableName, final HBaseCluster hbaseCluster) {
        executorManager.submit(() -> loadRegionInfo(tableName, hbaseCluster));
    }
    
    /**
     * Load one table region info.
     *
     * @param tableName table name
     * @param connection HBase connection
     * @throws HBaseOperationException HBase operation exception
     */
    public void loadRegionInfo(final String tableName, final Connection connection) {
        HBaseRegionWarmUpContext.getInstance().addExecuteCount();
        try {
            if (null == connection) {
                return;
            }
            RegionLocator regionLocator = connection.getRegionLocator(TableName.valueOf(tableName));
            regionLocator.getAllRegionLocations();
        } catch (final IOException ex) {
            throw new HBaseOperationException(String.format("table: %s warm up error, getRegionLocator execute error reason is  %s", tableName, ex));
        }
    }
    
    private void loadRegionInfo(final String tableName, final HBaseCluster hbaseCluster) {
        try {
            RegionLocator regionLocator = hbaseCluster.getConnection().getRegionLocator(TableName.valueOf(tableName));
            warmUpRegion(regionLocator);
            HBaseRegionWarmUpContext.getInstance().addExecuteCount();
        } catch (final IOException ex) {
            log.error(String.format("Table: `%s` load region info error, reason is  %s", tableName, ex));
        }
    }
    
    private void warmUpRegion(final RegionLocator regionLocator) throws IOException {
        regionLocator.getAllRegionLocations();
    }
    
    /**
     * Init statistics info.
     * 
     * @param startWarmUpTime start warm up time
     */
    public void initStatisticsInfo(final long startWarmUpTime) {
        this.startWarmUpTime = startWarmUpTime;
    }
    
    /**
     * Execute count add one.
     */
    public void addExecuteCount() {
        executeCount.incrementAndGet();
    }
    
    /**
     * All need warm up table add one.
     */
    public void addNeedWarmCount() {
        tableCount.incrementAndGet();
    }
    
    /**
     * Sync execute.
     * 
     * @param clusterName cluster name
     */
    public void syncExecuteWarmUp(final String clusterName) {
        while (executeCount.get() < tableCount.get()) {
            try {
                Thread.sleep(100L);
            } catch (final InterruptedException ignore) {
                Thread.currentThread().interrupt();
            }
        }
        log.info(String.format("%s cluster end warm up, execute time: %dms, warm table: %d", clusterName, System.currentTimeMillis() - startWarmUpTime, executeCount.get()));
    }
    
    /**
     * Clear statistics info.
     */
    public void clear() {
        tableCount.set(0);
        executeCount.set(0);
    }
}
