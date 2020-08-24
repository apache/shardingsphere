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

package org.apache.shardingsphere.infra.executor.kernel;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Executor data map for thread local even cross multiple threads.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExecutorDataMap {
    
    private static final ThreadLocal<Map<String, Object>> DATA_MAP = ThreadLocal.withInitial(LinkedHashMap::new);
    
    /**
     * Get value.
     *
     * @return data map
     */
    public static Map<String, Object> getValue() {
        return DATA_MAP.get();
    }
}
