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

package org.apache.shardingsphere.infra.util.retry;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Retry executor.
 */
@RequiredArgsConstructor
public final class RetryExecutor {
    
    private final long timeoutMillis;
    
    private final long intervalMillis;
    
    private long expendMillis;
    
    /**
     * Execute and retry.
     *
     * @param function function to be executed
     * @param arg argument
     * @param <T> argument type
     * @return execute result
     */
    public <T> boolean execute(final Function<T, Boolean> function, final T arg) {
        do {
            if (function.apply(arg)) {
                return true;
            }
        } while (!isTimeout());
        return false;
    }
    
    /**
     * Execute and retry.
     *
     * @param function function to be executed
     * @param arg1 the first argument
     * @param arg2 the second argument
     * @param <T> the first argument type
     * @param <U> the second argument type
     * @return execute result
     */
    public <T, U> boolean execute(final BiFunction<T, U, Boolean> function, final T arg1, final U arg2) {
        do {
            if (function.apply(arg1, arg2)) {
                return true;
            }
        } while (!isTimeout());
        return false;
    }
    
    @SneakyThrows(InterruptedException.class)
    private boolean isTimeout() {
        TimeUnit.MILLISECONDS.sleep(intervalMillis);
        if (-1L == timeoutMillis) {
            return false;
        }
        expendMillis += intervalMillis;
        return expendMillis > timeoutMillis;
    }
}
