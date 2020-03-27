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

package org.apache.shardingsphere.underlying.rewrite.registry;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.underlying.common.exception.ShardingSphereException;
import org.apache.shardingsphere.underlying.common.rule.BaseRule;
import org.apache.shardingsphere.underlying.rewrite.context.SQLRewriteContextDecorator;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Rewrite decorator registry.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RewriteDecoratorRegistry {
    
    private static final RewriteDecoratorRegistry INSTANCE = new RewriteDecoratorRegistry();
    
    private final Map<Class<? extends BaseRule>, Class<? extends SQLRewriteContextDecorator>> registry = new HashMap<>();
    
    /**
     * Get instance.
     * 
     * @return instance of rewrite decorator registry
     */
    public static RewriteDecoratorRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Register rewrite decorator.
     * 
     * @param ruleClass rule class
     * @param decoratorClass rewrite decorator class
     */
    public void register(final Class<? extends BaseRule> ruleClass, final Class<? extends SQLRewriteContextDecorator> decoratorClass) {
        registry.put(ruleClass, decoratorClass);
    }
    
    /**
     * Get rewrite decorator.
     * 
     * @param rule rule
     * @return rewrite decorator
     */
    public Optional<SQLRewriteContextDecorator> getDecorator(final BaseRule rule) {
        return registry.containsKey(rule.getClass()) ? Optional.of(createRewriteDecorator(rule.getClass())) : Optional.empty();
    }
    
    private SQLRewriteContextDecorator createRewriteDecorator(final Class<? extends BaseRule> ruleClass) {
        try {
            return registry.get(ruleClass).newInstance();
        } catch (final InstantiationException | IllegalAccessException ex) {
            throw new ShardingSphereException(String.format("Can not find public default constructor for rewrite decorator `%s`", registry.get(ruleClass)), ex);
        }
    }
}
