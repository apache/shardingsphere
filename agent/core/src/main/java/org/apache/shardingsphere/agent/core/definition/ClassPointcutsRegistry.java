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

import org.apache.shardingsphere.agent.advisor.ClassAdvisor;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class pointcuts registry.
 */
public final class ClassPointcutsRegistry {
    
    private final Map<String, ClassAdvisor> pointcutsMap = new ConcurrentHashMap<>();
    
    /**
     * Get class pointcuts.
     * 
     * @param targetClassName target class name to be intercepted
     * @return class pointcuts
     */
    public ClassAdvisor getClassPointcuts(final String targetClassName) {
        return pointcutsMap.computeIfAbsent(targetClassName, ClassAdvisor::new);
    }
    
    /**
     * Get all class pointcuts.
     * 
     * @return all class pointcuts
     */
    public Collection<ClassAdvisor> getAllClassPointcuts() {
        return pointcutsMap.values();
    }
}
