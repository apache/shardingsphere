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

package io.shardingjdbc.orchestration.spring.namespace.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Sharding strategy parser tag constants.
 * 
 * @author caohao
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingStrategyBeanDefinitionParserTag {
    
    public static final String STANDARD_STRATEGY_ROOT_TAG = "standard-strategy";
    
    public static final String COMPLEX_STRATEGY_ROOT_TAG = "complex-strategy";
    
    public static final String INLINE_STRATEGY_ROOT_TAG = "inline-strategy";
    
    public static final String HINT_STRATEGY_ROOT_TAG = "hint-strategy";
    
    public static final String NONE_STRATEGY_ROOT_TAG = "none-strategy";
    
    public static final String SHARDING_COLUMN_ATTRIBUTE = "sharding-column";
    
    public static final String SHARDING_COLUMNS_ATTRIBUTE = "sharding-columns";
    
    public static final String ALGORITHM_CLASS_ATTRIBUTE = "algorithm-class";
    
    public static final String PRECISE_ALGORITHM_CLASS_ATTRIBUTE = "precise-algorithm-class";
    
    public static final String RANGE_ALGORITHM_CLASS_ATTRIBUTE = "range-algorithm-class";
    
    public static final String ALGORITHM_EXPRESSION_ATTRIBUTE = "algorithm-expression";
}
