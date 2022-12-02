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

package org.apache.shardingsphere.test.runner.executor.key;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.runner.ParallelRunningStrategy.ParallelLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Parallel runner executor key provider factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ParallelRunnerExecutorKeyProviderFactory {
    
    private static final Map<ParallelLevel, ParallelRunnerExecutorKeyProvider> PROVIDERS = new HashMap<>();
    
    static {
        for (ParallelRunnerExecutorKeyProvider each : ServiceLoader.load(ParallelRunnerExecutorKeyProvider.class)) {
            PROVIDERS.put(each.getParallelLevel(), each);
        }
    }
    
    /**
     * Create new instance of parallel runner executor key provider.
     * 
     * @param parallelLevel parallel level
     * @return created instance
     */
    public static ParallelRunnerExecutorKeyProvider newInstance(final ParallelLevel parallelLevel) {
        return PROVIDERS.get(parallelLevel);
    }
}
