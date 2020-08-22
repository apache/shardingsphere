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

package org.apache.shardingsphere.cluster.heartbeat.detect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.cluster.heartbeat.response.HeartbeatResult;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Abstract heart beat detect.
 */
@RequiredArgsConstructor
@Slf4j
public abstract class AbstractHeartbeatDetect implements Callable<Map<String, HeartbeatResult>> {
    
    private final boolean retryEnable;
    
    private final int retryMaximum;
    
    private final int retryInterval;
    
    private final boolean needDetect;

    /**
     * Detect heart beat.
     *
     * @return heart beat result.
     */
    protected abstract boolean detect();
    
    /**
     * Build heart beat result.
     *
     * @param heartbeatResult heart beat result
     * @return heart beat result
     */
    protected abstract Map<String, HeartbeatResult> buildResult(boolean heartbeatResult);
    
    @Override
    public final Map<String, HeartbeatResult> call() {
        if (!needDetect) {
            return buildResult(false);
        }
        if (retryEnable && retryMaximum > 0) {
            for (int i = 0; i < retryMaximum; i++) {
                if (detect()) {
                    return buildResult(true);
                }
                try {
                    Thread.sleep(retryInterval * 1000L);
                } catch (final InterruptedException ex) {
                    log.warn("Retry heart beat detect sleep error", ex);
                }
            }
            return buildResult(false);
        } else {
            return buildResult(detect());
        }
    }
}
