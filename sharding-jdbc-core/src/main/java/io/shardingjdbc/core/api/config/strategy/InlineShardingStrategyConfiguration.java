/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.api.config.strategy;

import com.google.common.base.Preconditions;
import io.shardingjdbc.core.routing.strategy.ShardingStrategy;
import io.shardingjdbc.core.routing.strategy.inline.InlineShardingStrategy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Inline sharding strategy configuration.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
public final class InlineShardingStrategyConfiguration implements ShardingStrategyConfiguration {
    
    private final String shardingColumn;
    
    private final String algorithmInlineExpression;
    
    @Override
    public ShardingStrategy build() {
        Preconditions.checkNotNull(shardingColumn, "Sharding column cannot be null.");
        Preconditions.checkNotNull(algorithmInlineExpression, "Algorithm inline expression cannot be null.");
        return new InlineShardingStrategy(shardingColumn, algorithmInlineExpression);
    }
}
