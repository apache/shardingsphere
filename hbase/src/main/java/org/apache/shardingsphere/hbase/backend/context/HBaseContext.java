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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.shardingsphere.hbase.backend.bean.HBaseCluster;
import org.apache.shardingsphere.hbase.backend.connector.HBaseBackgroundExecutorManager;
import org.apache.shardingsphere.hbase.backend.connector.HBaseExecutor;
import org.apache.shardingsphere.hbase.backend.exception.HBaseOperationException;
import org.apache.shardingsphere.hbase.backend.props.HBaseProperties;
import org.apache.shardingsphere.hbase.backend.props.HBasePropertyKey;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * HBase context, parser config and create connection.
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
    
    private Map<String, HBaseCluster> tableConnectionMap = new ConcurrentHashMap<>();
    
    /**
     * Get instance of HBase context.
     *
     * @return instance of HBase context
     */
    public static HBaseContext getInstance() {
        return INSTANCE;
    }
    
    /**
     * Init hbase context.
     * @param connections A connection for per HBase cluster
     */
    public void init(final Map<String, Connection> connections) {
        this.connections = new ArrayList<>(connections.size());
        this.warmUpContext = HBaseRegionWarmUpContext.getInstance();
        this.warmUpContext.init(getWarmUpThreadSize());
        this.isSyncWarmUp = isSyncWarm();
        for (Map.Entry<String, Connection> entry : connections.entrySet()) {
            HBaseCluster cluster = new HBaseCluster(entry.getKey(), entry.getValue());
            loadTablesFromHBase(cluster);
            this.connections.add(cluster);
        }
        log.info("{} tables loaded from {} clusters", tableConnectionMap.size(), connections.size());
    }
    
    private boolean isSyncWarm() {
        return HBaseContext.getInstance().getProps().<Boolean>getValue(HBasePropertyKey.IS_SYNC_WARM_UP);
    }
    
    /**
     * Get warmUpThreadSize.
     * @return warmUpThreadSize, 0 < warmUpThreadSize <= 30
     */
    private int getWarmUpThreadSize() {
        int warmUpThreadSize = HBaseContext.getInstance().getProps().<Integer>getValue(HBasePropertyKey.WARM_UP_THREAD_NUM);
        if (warmUpThreadSize < 0) {
            return 1;
        }
        
        return Math.min(warmUpThreadSize, 30);
    }
    
    /**
     * Load tables from HBase database.
     * @param hbaseCluster hbase cluster object
     */
    public synchronized void loadTablesFromHBase(final HBaseCluster hbaseCluster) {
        HTableDescriptor[] hTableDescriptor = HBaseExecutor.executeAdmin(hbaseCluster.getConnection(), Admin::listTables);
        List<String> tableNames = Arrays.stream(hTableDescriptor).map(HTableDescriptor::getNameAsString).collect(Collectors.toList());
        this.warmUpContext.initStatisticsInfo(System.currentTimeMillis());
        for (String tableName : tableNames) {
            if (!tableConnectionMap.containsKey(tableName)) {
                this.warmUpContext.addNeedWarmCount();
                log.info("Load table `{}` from cluster `{}`", tableName, hbaseCluster.getClusterName());
                tableConnectionMap.put(tableName, hbaseCluster);
                this.warmUpContext.submitWarmUpTask(tableName, hbaseCluster);
            }
        }
        if (isSyncWarmUp) {
            this.warmUpContext.syncExecuteWarmUp(hbaseCluster.getClusterName());
            this.warmUpContext.clear();
        }
    }
    
    /**
     * get connection via table name.
     * @param tableName table name.
     *                  
     * @return HBase Connection.
     */
    public Connection getConnection(final String tableName) {
        if (tableConnectionMap.containsKey(tableName)) {
            return tableConnectionMap.get(tableName).getConnection();
        } else {
            throw new HBaseOperationException(String.format("Table `%s` is not exists", tableName));
        }
    }
    
    /**
     * Is table exists.
     * @param tableName tableName
     * @return result
     */
    public boolean isTableExists(final String tableName) {
        return tableConnectionMap.containsKey(tableName);
    }
    
    /**
     * Get connection via cluster name.
     * @param clusterName cluster name
     * @return HBase Connection
     */
    public Connection getConnectionByClusterName(final String clusterName) {
        Optional<HBaseCluster> cluster = connections.stream().filter(each -> each.getClusterName().equalsIgnoreCase(clusterName)).findFirst();
        if (cluster.isPresent()) {
            return cluster.get().getConnection();
        } else {
            throw new HBaseOperationException(String.format("Cluster `%s` is not exists", clusterName));
        }
    }
    
    @Override
    public void close() {
        connections.clear();
        tableConnectionMap.clear();
        executorManager.close();
        for (Connection connection : connections.stream().map(HBaseCluster::getConnection).collect(Collectors.toList())) {
            try {
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
