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

package org.apache.shardingsphere.data.pipeline.api.executor;

import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Abstract lifecycle executor.
 */
@Slf4j
public abstract class AbstractLifecycleExecutor implements LifecycleExecutor {
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private final AtomicReference<Boolean> running = new AtomicReference<>(null);
    
    private volatile long startTimeMillis;
    
    protected boolean isRunning() {
        Boolean running = this.running.get();
        return null != running && running;
    }
    
    @Override
    public final void start() {
        if (null != running.get() || !running.compareAndSet(null, true)) {
            return;
        }
        startTimeMillis = System.currentTimeMillis();
        runBlocking();
    }
    
    /**
     * Run blocking.
     */
    protected abstract void runBlocking();
    
    @Override
    public final void stop() {
        Boolean running = this.running.get();
        if (null == running) {
            this.running.set(false);
            return;
        }
        if (!running) {
            return;
        }
        LocalDateTime startTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(startTimeMillis), ZoneId.systemDefault());
        log.info("stop lifecycle executor {}, startTime={}, cost {} ms", this, startTime.format(DATE_TIME_FORMATTER), System.currentTimeMillis() - startTimeMillis);
        try {
            doStop();
            // CHECKSTYLE:OFF
        } catch (final SQLException | RuntimeException ex) {
            // CHECKSTYLE:ON
            log.warn("doStop failed", ex);
        }
        this.running.set(false);
    }
    
    protected abstract void doStop() throws SQLException;
    
    @Override
    public final void run() {
        start();
    }
}
