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

package org.apache.shardingsphere.agent.plugin.core.recorder;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.agent.api.advice.AgentAdvice;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Method time recorder.
 */
@RequiredArgsConstructor
public final class MethodTimeRecorder {
    
    private static final ThreadLocal<Map<String, Long>> CURRENT_RECORDER = ThreadLocal.withInitial(HashMap::new);
    
    private final Class<? extends AgentAdvice> adviceClass;
    
    /**
     * Record now.
     * 
     * @param method method to be recorded
     */
    public void recordNow(final Method method) {
        CURRENT_RECORDER.get().put(getKey(method), System.currentTimeMillis());
    }
    
    /**
     * Get elapsed time and clean.
     *
     * @param method method to be recorded
     * @return elapsed time
     */
    public long getElapsedTimeAndClean(final Method method) {
        String key = getKey(method);
        try {
            return getElapsedTime(key);
        } finally {
            clean(key);
        }
    }
    
    private String getKey(final Method method) {
        return String.format("%s@%s", adviceClass.getName(), method.getName());
    }
    
    private long getElapsedTime(final String key) {
        return CURRENT_RECORDER.get().containsKey(key) ? System.currentTimeMillis() - CURRENT_RECORDER.get().get(key) : 0L;
    }
    
    private void clean(final String key) {
        CURRENT_RECORDER.get().remove(key);
        if (CURRENT_RECORDER.get().isEmpty()) {
            CURRENT_RECORDER.remove();
        }
    }
}
