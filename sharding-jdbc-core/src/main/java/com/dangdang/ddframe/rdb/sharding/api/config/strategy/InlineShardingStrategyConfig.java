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

package com.dangdang.ddframe.rdb.sharding.api.config.strategy;

import com.dangdang.ddframe.rdb.sharding.routing.strategy.ShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.routing.strategy.inline.InlineShardingStrategy;
import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;

/**
 * Inline sharding strategy configuration.
 * 
 * @author zhangliang
 */
@Getter
@Setter
public final class InlineShardingStrategyConfig implements ShardingStrategyConfig {
    
    private String shardingColumn;
    
    private String algorithmInlineExpression;
    
    @Override
    public ShardingStrategy build() {
        Preconditions.checkNotNull(shardingColumn, "Sharding column cannot be null.");
        Preconditions.checkNotNull(algorithmInlineExpression, "Algorithm inline expression cannot be null.");
        return new InlineShardingStrategy(shardingColumn, algorithmInlineExpression);
    }
}
