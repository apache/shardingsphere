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

package org.apache.shardingsphere.mode.retry;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.function.Predicate;

/**
 * Retry executor.
 */
@RequiredArgsConstructor
public final class RetryExecutor {
    
    private final long timeoutMillis;
    
    private final long intervalMillis;
    
    private long elapsedMillis;
    
    /**
     * Execute and retry.
     *
     * @param predicate predicate to be executed
     * @param arg argument
     * @param <T> argument type
     * @return execute result success or not
     */
    public <T> boolean execute(final Predicate<T> predicate, final T arg) {
        do {
            if (predicate.test(arg)) {
                return true;
            }
        } while (!isTimeout());
        return false;
    }
    
    @SneakyThrows(InterruptedException.class)
    private boolean isTimeout() {
        Thread.sleep(intervalMillis);
        if (timeoutMillis < 0L) {
            return false;
        }
        elapsedMillis += intervalMillis;
        return elapsedMillis > timeoutMillis;
    }
}
