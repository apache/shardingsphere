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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Time recorder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TimeRecorder {
    
    private static final ThreadLocal<Map<Method, Long>> CURRENT_RECORDER = ThreadLocal.withInitial(HashMap::new);
    
    /**
     * Record now.
     *
     * @param method method to be recorded
     */
    public static void record(final Method method) {
        CURRENT_RECORDER.get().put(method, System.currentTimeMillis());
    }
    
    /**
     * Get elapsed time and clean.
     *
     * @param method method to be recorded
     * @return elapsed time
     */
    public static long getElapsedTimeAndClean(final Method method) {
        try {
            return getElapsedTime(method);
        } finally {
            clean(method);
        }
    }
    
    private static long getElapsedTime(final Method method) {
        return isRecorded(method) ? System.currentTimeMillis() - CURRENT_RECORDER.get().get(method) : 0L;
    }
    
    private static boolean isRecorded(final Method method) {
        return null != CURRENT_RECORDER.get().get(method);
    }
    
    private static void clean(final Method method) {
        CURRENT_RECORDER.get().remove(method);
    }
}
