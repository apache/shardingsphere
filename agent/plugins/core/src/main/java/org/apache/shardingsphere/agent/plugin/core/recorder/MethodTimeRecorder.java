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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Method time recorder.
 */
public final class MethodTimeRecorder {
    
    private static final ThreadLocal<Map<String, Long>> CURRENT_RECORDER = ThreadLocal.withInitial(HashMap::new);
    
    private final String key;
    
    public MethodTimeRecorder(final Class<?> adviceClass, final Method targetMethod) {
        key = String.format("%s@%s", adviceClass.getCanonicalName(), targetMethod.getName());
    }
    
    /**
     * Record now.
     */
    public void record() {
        CURRENT_RECORDER.get().put(key, System.currentTimeMillis());
    }
    
    /**
     * Get elapsed time and clean.
     *
     * @return elapsed time
     */
    public long getElapsedTimeAndClean() {
        try {
            return getElapsedTime();
        } finally {
            clean();
        }
    }
    
    private long getElapsedTime() {
        return isRecorded() ? System.currentTimeMillis() - CURRENT_RECORDER.get().get(key) : 0L;
    }
    
    private boolean isRecorded() {
        return null != CURRENT_RECORDER.get().get(key);
    }
    
    private void clean() {
        CURRENT_RECORDER.get().remove(key);
    }
}
