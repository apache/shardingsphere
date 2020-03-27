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

package org.apache.shardingsphere.underlying.route.registry;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.underlying.common.exception.ShardingSphereException;
import org.apache.shardingsphere.underlying.common.rule.BaseRule;
import org.apache.shardingsphere.underlying.route.decorator.RouteDecorator;

import java.util.HashMap;
import java.util.Map;

/**
 * Route decorator registry.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RouteDecoratorRegistry {
    
    private static final RouteDecoratorRegistry INSTANCE = new RouteDecoratorRegistry();
    
    private final Map<Class<? extends BaseRule>, Class<? extends RouteDecorator>> registry = new HashMap<>();
    
    /**
     * Get instance.
     * 
     * @return instance of route decorator registry
     */
    public static RouteDecoratorRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Register route decorator.
     * 
     * @param ruleClass rule class
     * @param decoratorClass route decorator class
     */
    public void register(final Class<? extends BaseRule> ruleClass, final Class<? extends RouteDecorator> decoratorClass) {
        registry.put(ruleClass, decoratorClass);
    }
    
    /**
     * Get route decorator.
     * 
     * @param rule rule
     * @return route decorator
     */
    public RouteDecorator getDecorator(final BaseRule rule) {
        Class<? extends BaseRule> ruleClass = rule.getClass();
        Preconditions.checkState(registry.containsKey(ruleClass), "Can not find route decorator with rule `%s`.", ruleClass);
        try {
            return registry.get(ruleClass).newInstance();
        } catch (final InstantiationException | IllegalAccessException ex) {
            throw new ShardingSphereException(String.format("Can not find public default constructor for route decorator `%s`", registry.get(ruleClass)), ex);
        }
    }
}
