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

package org.apache.shardingsphere.api.config.sharding.strategy;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;

/**
 * Inline sharding strategy configuration.
 * 
 * @author zhangliang
 */
@Getter
public final class InlineShardingStrategyConfiguration implements ShardingStrategyConfiguration {
    
    private final String shardingColumn;
    
    private final String algorithmExpression;
    
    public InlineShardingStrategyConfiguration(final String shardingColumn, final String algorithmExpression) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(shardingColumn), "ShardingColumn is required.");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(algorithmExpression), "AlgorithmExpression is required.");
        this.shardingColumn = shardingColumn;
        this.algorithmExpression = algorithmExpression;
    }
    
    @Override
    public String toString() {
        return "Inline{" + "shardingColumn='" + shardingColumn + '\'' + ", algorithmExpression='" + algorithmExpression + '\'' + '}';
    }
}
