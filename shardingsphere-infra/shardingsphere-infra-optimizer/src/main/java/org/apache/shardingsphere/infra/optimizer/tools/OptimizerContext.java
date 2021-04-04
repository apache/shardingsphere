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

package org.apache.shardingsphere.infra.optimizer.tools;

import lombok.Getter;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Optional;

/**
 * optimizer context.
 */
@Getter
public final class OptimizerContext {
    
    private static final ThreadLocal<OptimizerContext> CONTEXT = ThreadLocal.withInitial(() -> null);
    
    private ShardingRule shardingRule;
    
    private OptimizerContext(final ShardingRule shardingRule) {
        this.shardingRule = shardingRule;
    }
    
    /**
     * Create optimizer context.
     * @param shardingRule sharding rule
     * @return <code>OptimizerContext</code>
     */
    public static OptimizerContext create(final ShardingRule shardingRule) {
        OptimizerContext optimizerContext = new OptimizerContext(shardingRule);
        CONTEXT.set(optimizerContext);
        return optimizerContext;
    }
    
    /**
     * Get current <code>OptimizerContext</code> of this <code>ThreadLocal</code>.
     * @return <code>OptimizerContext</code> 
     */
    public static Optional<OptimizerContext> getCurrentOptimizerContext() {
        if (CONTEXT.get() == null) {
            return Optional.empty();
        }
        return Optional.of(CONTEXT.get());
    }
}
