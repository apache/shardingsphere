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

package org.apache.shardingsphere.agent.core.definition;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class pointcuts registry factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ClassPointcutsRegistryFactory {
    
    private static final Map<String, ClassPointcutsRegistry> REGISTRIES = new ConcurrentHashMap<>();
    
    /**
     * Get class pointcuts registry.
     * 
     * @param key registry key
     * @return class pointcuts registry
     */
    public static ClassPointcutsRegistry getRegistry(final String key) {
        return REGISTRIES.computeIfAbsent(key, each -> new ClassPointcutsRegistry());
    }
}
