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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.shardingsphere.proxy.backend.hbase.bean.HBaseCluster;
import org.apache.shardingsphere.proxy.backend.hbase.executor.HBaseBackgroundExecutorManager;
import org.apache.shardingsphere.proxy.backend.hbase.executor.HBaseExecutor;
import org.apache.shardingsphere.proxy.backend.hbase.exception.HBaseOperationException;
import org.apache.shardingsphere.proxy.backend.hbase.props.HBaseProperties;
import org.apache.shardingsphere.proxy.backend.hbase.props.HBasePropertyKey;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * HBase context.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Slf4j
public final class HBaseContext implements AutoCloseable {
    
    private static final HBaseContext INSTANCE = new HBaseContext();
    
    private final HBaseBackgroundExecutorManager executorManager = new HBaseBackgroundExecutorManager();
    
    private HBaseRegionWarmUpContext warmUpContext;
    
    @Setter
    private HBaseProperties props;
    
    private final String columnFamily = "i";
    
    private Collection<HBaseCluster> connections;
    
    private boolean isSyncWarmUp;
    
    private final Map<String, HBaseCluster> tableConnectionMap = new ConcurrentHashMap<>();
    
    /**
     * Get instance of HBase context.
     *
     * @return got instance
     */
    public static HBaseContext getInstance() {
        return INSTANCE;
    }
    
    /**
     * Initialize HBase context.
     * 
     * @param connections A connection for per HBase cluster
     */
    public void init(final Map<String, Connection> connections) {
        this.connections = new ArrayList<>(connections.size());
        warmUpContext = HBaseRegionWarmUpContext.getInstance();
        warmUpContext.init(getWarmUpThreadSize());
        isSyncWarmUp = HBaseContext.getInstance().getProps().<Boolean>getValue(HBasePropertyKey.IS_SYNC_WARM_UP);
        for (Entry<String, Connection> entry : connections.entrySet()) {
            HBaseCluster cluster = new HBaseCluster(entry.getKey(), entry.getValue());
            loadTables(cluster);
            this.connections.add(cluster);
        }
        log.info("{} tables loaded from {} clusters.", tableConnectionMap.size(), connections.size());
    }
    
    /**
     * Get warm up thread size.
     * 
     * @return warm up thread size, value should be in (0, 30]
     */
    private int getWarmUpThreadSize() {
        int warmUpThreadSize = HBaseContext.getInstance().getProps().<Integer>getValue(HBasePropertyKey.WARM_UP_THREAD_NUM);
        return warmUpThreadSize < 0 ? 1 : Math.min(warmUpThreadSize, 30);
    }
    
    /**
     * Load tables.
     * 
     * @param hbaseCluster HBase cluster
     */
    public synchronized void loadTables(final HBaseCluster hbaseCluster) {
        warmUpContext.initStatisticsInfo(System.currentTimeMillis());
        HTableDescriptor[] hTableDescriptor = HBaseExecutor.executeAdmin(hbaseCluster.getConnection(), Admin::listTables);
        for (String each : Arrays.stream(hTableDescriptor).map(HTableDescriptor::getNameAsString).collect(Collectors.toList())) {
            if (tableConnectionMap.containsKey(each)) {
                continue;
            }
            warmUpContext.addNeedWarmCount();
            log.info("Load table `{}` from cluster `{}`.", each, hbaseCluster.getClusterName());
            tableConnectionMap.put(each, hbaseCluster);
            warmUpContext.submitWarmUpTask(each, hbaseCluster);
        }
        if (isSyncWarmUp) {
            warmUpContext.syncExecuteWarmUp(hbaseCluster.getClusterName());
            warmUpContext.clear();
        }
    }
    
    /**
     * Get connection via table name.
     * 
     * @param tableName table name
     * @return HBase connection
     */
    public Connection getConnection(final String tableName) {
        ShardingSpherePreconditions.checkState(tableConnectionMap.containsKey(tableName), () -> new HBaseOperationException(String.format("Table `%s` is not exists", tableName)));
        return tableConnectionMap.get(tableName).getConnection();
    }
    
    /**
     * Is table exists.
     * 
     * @param tableName table name
     * @return table exists or not
     */
    public boolean isTableExists(final String tableName) {
        return tableConnectionMap.containsKey(tableName);
    }
    
    /**
     * Get connection via cluster name.
     * 
     * @param clusterName cluster name
     * @return HBase connection
     */
    public Connection getConnectionByClusterName(final String clusterName) {
        Optional<HBaseCluster> cluster = connections.stream().filter(each -> each.getClusterName().equalsIgnoreCase(clusterName)).findFirst();
        ShardingSpherePreconditions.checkState(cluster.isPresent(), () -> new HBaseOperationException(String.format("Cluster `%s` is not exists", clusterName)));
        return cluster.get().getConnection();
    }
    
    @Override
    public void close() {
        connections.clear();
        tableConnectionMap.clear();
        executorManager.close();
        for (Connection connection : connections.stream().map(HBaseCluster::getConnection).collect(Collectors.toList())) {
            try {
                connection.close();
            } catch (final IOException ex) {
                // TODO define new exception, do not use RuntimeException
                throw new RuntimeException(ex);
            }
        }
    }
}
