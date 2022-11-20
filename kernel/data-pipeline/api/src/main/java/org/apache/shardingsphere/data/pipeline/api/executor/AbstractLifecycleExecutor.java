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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Abstract lifecycle executor.
 */
@Slf4j
public abstract class AbstractLifecycleExecutor implements LifecycleExecutor {
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.PROTECTED)
    private volatile boolean running;
    
    private volatile boolean stopped;
    
    private volatile long startTimeMillis;
    
    @Override
    public void start() {
        running = true;
        startTimeMillis = System.currentTimeMillis();
        runBlocking();
        stop();
    }
    
    /**
     * Run blocking.
     */
    protected abstract void runBlocking();
    
    @Override
    public final void stop() {
        if (stopped) {
            return;
        }
        LocalDateTime startTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(startTimeMillis), ZoneId.systemDefault());
        log.info("stop lifecycle executor {}, startTime={}, cost {} ms", this, startTime.format(DATE_TIME_FORMATTER), System.currentTimeMillis() - startTimeMillis);
        try {
            doStop();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            log.warn("doStop failed", ex);
        }
        running = false;
        stopped = true;
    }
    
    protected abstract void doStop() throws Exception;
    
    protected void cancelStatement(final Statement statement) throws SQLException {
        if (null == statement || statement.isClosed()) {
            return;
        }
        statement.cancel();
    }
    
    @Override
    public final void run() {
        start();
    }
}
