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

package org.apache.shardingsphere.cluster.heartbeat;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.cluster.configuration.config.HeartBeatConfiguration;
import org.apache.shardingsphere.cluster.heartbeat.event.HeartBeatDetectNoticeEvent;
import org.apache.shardingsphere.cluster.heartbeat.task.HeartBeatTask;
import org.apache.shardingsphere.cluster.heartbeat.task.HeartBeatTaskManager;

/**
 * Cluster heart beat instance.
 */
public final class ClusterHeartBeatInstance {
    
    private HeartBeatTaskManager heartBeatTaskManager;
    
    /**
     * Get cluster heart beat instance.
     *
     * @return cluster heart beat instance
     */
    public static ClusterHeartBeatInstance getInstance() {
        return ClusterHeartBeatInstanceHolder.INSTANCE;
    }
    
    /**
     * Init heart beat task manager.
     *
     * @param configuration heart beat configuration
     */
    public void init(final HeartBeatConfiguration configuration) {
        Preconditions.checkNotNull(configuration, "heart beat configuration can not be null.");
        heartBeatTaskManager = new HeartBeatTaskManager(configuration.getInterval(), configuration.getThreadCount());
        HeartBeatTask task = new HeartBeatTask(new HeartBeatDetectNoticeEvent(configuration.getSql()));
        heartBeatTaskManager.start(task);
    }
    
    private static final class ClusterHeartBeatInstanceHolder {
        
        private static final ClusterHeartBeatInstance INSTANCE = new ClusterHeartBeatInstance();
    }
}
