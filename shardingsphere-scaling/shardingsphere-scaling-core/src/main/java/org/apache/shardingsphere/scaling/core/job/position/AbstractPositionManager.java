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

package org.apache.shardingsphere.scaling.core.job.position;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Position manager.
 */
public abstract class AbstractPositionManager {

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private final String taskId;

    private final int flushInterval = 60;

    private volatile Boolean init = false;

    private Position flushedPosition;

    private Position currentPosition;

    public AbstractPositionManager(final String taskId) {
        this.taskId = taskId;
        executorService.scheduleAtFixedRate(() -> {
            updatePosition(taskId, currentPosition);
            flushedPosition = currentPosition;
        }, 0, flushInterval, TimeUnit.SECONDS);
    }

    /**
     * Get position.
     *
     * @return position
     */
    public Position getPosition() {
        if (!init) {
            synchronized (init) {
                if (!init) {
                    flushedPosition = getPosition(taskId);
                }
            }
        }
        return flushedPosition;
    }

    /**
     * Get position by task id.
     *
     * @param taskId task id
     * @return position
     */
    protected abstract Position getPosition(String taskId);

    /**
     * Update position.
     *
     * @param position position
     */
    public void updatePosition(final Position position) {
        currentPosition = position;
    }

    /**
     * Update position by task id.
     *
     * @param taskId   task id
     * @param position position
     */
    protected abstract void updatePosition(String taskId, Position position);
}
