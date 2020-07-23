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

package org.apache.shardingsphere.cluster.heartbeat.task;

import com.google.common.base.Preconditions;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Heartbeat task manager.
 */
public final class HeartbeatTaskManager {
    
    private final int interval;
    
    private final ScheduledExecutorService executorService;
    
    public HeartbeatTaskManager(final int interval) {
        this.interval = interval;
        executorService = Executors.newScheduledThreadPool(1);
    }
    
    /**
     * Start heartbeat task.
     *
     * @param heartbeatTask heartbeat task
     */
    public void start(final HeartbeatTask heartbeatTask) {
        Preconditions.checkNotNull(heartbeatTask, "task can not be null");
        executorService.scheduleAtFixedRate(heartbeatTask, 0L, interval, TimeUnit.SECONDS);
    }
    
    /**
     * Close heartbeat task manager.
     */
    public void close() {
        if (null != executorService && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
