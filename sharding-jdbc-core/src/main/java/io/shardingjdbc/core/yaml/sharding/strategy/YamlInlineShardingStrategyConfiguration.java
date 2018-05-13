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

package io.shardingjdbc.core.yaml.sharding.strategy;

import lombok.Getter;
import lombok.Setter;

/**
 * Inline sharding strategy configuration for yaml.
 * 
 * @author zhangliang
 */
@Getter
@Setter
public final class YamlInlineShardingStrategyConfiguration implements YamlShardingStrategyConfiguration {
    
    private String shardingColumn;
    
    private String algorithmExpression;
}
