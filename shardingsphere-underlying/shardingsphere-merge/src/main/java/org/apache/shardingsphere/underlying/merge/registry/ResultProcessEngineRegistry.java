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

package org.apache.shardingsphere.underlying.merge.registry;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.underlying.common.exception.ShardingSphereException;
import org.apache.shardingsphere.underlying.common.rule.BaseRule;
import org.apache.shardingsphere.underlying.merge.engine.ResultProcessEngine;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Result process engine registry.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResultProcessEngineRegistry {
    
    private static final ResultProcessEngineRegistry INSTANCE = new ResultProcessEngineRegistry();
    
    private final Map<Class<? extends BaseRule>, Class<? extends ResultProcessEngine>> registry = new HashMap<>();
    
    /**
     * Get instance.
     * 
     * @return instance of result process engine registry
     */
    public static ResultProcessEngineRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Register result process engine.
     * 
     * @param ruleClass rule class
     * @param processEngineClass result process engine class
     */
    public void register(final Class<? extends BaseRule> ruleClass, final Class<? extends ResultProcessEngine> processEngineClass) {
        registry.put(ruleClass, processEngineClass);
    }
    
    /**
     * Get result process engine.
     * 
     * @param rule rule
     * @return result process engine
     */
    public Optional<ResultProcessEngine> getResultProcessEngine(final BaseRule rule) {
        Class<? extends BaseRule> ruleClass = rule.getClass();
        // FIXME for orchestration rule, should decouple extend between orchestration rule and sharding rule 
        if (!registry.containsKey(ruleClass)) {
            ruleClass = (Class<? extends BaseRule>) ruleClass.getSuperclass();
        }
        return registry.containsKey(ruleClass) ? Optional.of(createResultProcessEngine(ruleClass)) : Optional.empty();
    }
    
    private ResultProcessEngine createResultProcessEngine(final Class<? extends BaseRule> ruleClass) {
        try {
            return registry.get(ruleClass).newInstance();
        } catch (final InstantiationException | IllegalAccessException ex) {
            throw new ShardingSphereException(String.format("Can not find public default constructor for route decorator `%s`", registry.get(ruleClass)), ex);
        }
    }
}
