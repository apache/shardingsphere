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

import java.util.HashMap;
import java.util.Map;

/**
 * Time recorder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TimeRecorder {
    
    private static final ThreadLocal<Map<String, Long>> CURRENT_RECORDER = ThreadLocal.withInitial(HashMap::new);
    
    /**
     * Record now.
     *
     * @param recordPointMark record point mark
     */
    public static void record(final RecordPointMark recordPointMark) {
        CURRENT_RECORDER.get().put(recordPointMark.getMark(), System.currentTimeMillis());
    }
    
    /**
     * Get elapsed time and clean.
     *
     * @param recordPointMark record point mark
     * @return elapsed time
     */
    public static long getElapsedTimeAndClean(final RecordPointMark recordPointMark) {
        try {
            return getElapsedTime(recordPointMark);
        } finally {
            clean(recordPointMark);
        }
    }
    
    private static long getElapsedTime(final RecordPointMark recordPointMark) {
        return isRecorded(recordPointMark) ? System.currentTimeMillis() - CURRENT_RECORDER.get().get(recordPointMark.getMark()) : 0;
    }
    
    private static boolean isRecorded(final RecordPointMark recordPointMark) {
        return null != CURRENT_RECORDER.get().get(recordPointMark.getMark());
    }
    
    private static void clean(final RecordPointMark recordPointMark) {
        CURRENT_RECORDER.get().remove(recordPointMark.getMark());
    }
}
