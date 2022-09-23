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

import java.util.Date;

/**
 * Abstract lifecycle executor.
 */
@Slf4j
public abstract class AbstractLifecycleExecutor implements LifecycleExecutor {
    
    @Setter(AccessLevel.PROTECTED)
    @Getter
    private volatile boolean running;
    
    private volatile long startTimeMillis;
    
    @Override
    public void start() {
        log.info("start lifecycle executor {}", super.toString());
        running = true;
        startTimeMillis = System.currentTimeMillis();
        doStart();
        // TODO  1) running = false;, 2) stop();
    }
    
    /**
     * Start blocked running.
     */
    protected abstract void doStart();
    
    @Override
    public final void stop() {
        if (!running) {
            return;
        }
        log.info("stop lifecycle executor {}, startTime={}, cost {} ms", super.toString(), new Date(startTimeMillis), System.currentTimeMillis() - startTimeMillis);
        doStop();
        running = false;
    }
    
    protected abstract void doStop();
    
    @Override
    public final void run() {
        start();
    }
}
