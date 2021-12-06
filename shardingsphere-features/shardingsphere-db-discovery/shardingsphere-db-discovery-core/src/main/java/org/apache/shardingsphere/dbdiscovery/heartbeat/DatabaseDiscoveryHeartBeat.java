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

package org.apache.shardingsphere.dbdiscovery.heartbeat;

import org.apache.shardingsphere.dbdiscovery.job.HeartbeatJob;
import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryType;
import org.apache.shardingsphere.elasticjob.api.JobConfiguration;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.ScheduleJobBootstrap;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperConfiguration;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * database discovery heartbeat.
 */
public final class DatabaseDiscoveryHeartBeat {
    
    private static CoordinatorRegistryCenter coordinatorRegistryCenter;
    
    private static final Map<String, ScheduleJobBootstrap> SCHEDULE_JOB_BOOTSTRAP_MAP = new HashMap<>(16, 1);
    
    /**
     * start monitor heartbeat.
     *
     * @param schemaName schema name
     * @param groupName group name
     * @param dataSourceMap datasource
     * @param databaseDiscoveryType database discovery type
     * @param props props
     * @param disabledDataSourceNames disabled datasource names
     */
    public void startMonitorHeartbeat(final String schemaName, final String groupName, final Map<String, DataSource> dataSourceMap, final DatabaseDiscoveryType databaseDiscoveryType,
                                      final Properties props, final Collection<String> disabledDataSourceNames) {
        if (null == coordinatorRegistryCenter) {
            //TODO use cluster zk lists
            ZookeeperConfiguration zkConfig = new ZookeeperConfiguration("localhost:2181", "mgr-elasticjob");
            coordinatorRegistryCenter = new ZookeeperRegistryCenter(zkConfig);
            coordinatorRegistryCenter.init();
        }
        if (null != SCHEDULE_JOB_BOOTSTRAP_MAP.get(groupName)) {
            SCHEDULE_JOB_BOOTSTRAP_MAP.get(groupName).shutdown();
        }
        // TODO create real discovery type job
        SCHEDULE_JOB_BOOTSTRAP_MAP.put(groupName, new ScheduleJobBootstrap(coordinatorRegistryCenter, new HeartbeatJob(databaseDiscoveryType, schemaName, dataSourceMap, disabledDataSourceNames,
                groupName), JobConfiguration.newBuilder("MGR-" + groupName, 1).cron(props.getProperty("keepAliveCron")).build()));
        SCHEDULE_JOB_BOOTSTRAP_MAP.get(groupName).schedule();
    }
}
