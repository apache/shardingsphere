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

/**
 * Abstract lifecycle executor.
 */
@Slf4j
public abstract class AbstractLifecycleExecutor implements LifecycleExecutor {
    
    @Setter(AccessLevel.PROTECTED)
    @Getter(AccessLevel.PROTECTED)
    private volatile boolean running;
    
    private volatile boolean stopped;
    
    @Override
    public void start() {
        log.info("start lifecycle executor: {}", super.toString());
        running = true;
        doStart();
    }
    
    protected abstract void doStart();
    
    @Override
    public final void stop() {
        if (stopped) {
            return;
        }
        log.info("stop lifecycle executor: {}", super.toString());
        running = false;
        doStop();
        stopped = true;
    }
    
    protected abstract void doStop();
    
    @Override
    public final void run() {
        start();
    }
}
