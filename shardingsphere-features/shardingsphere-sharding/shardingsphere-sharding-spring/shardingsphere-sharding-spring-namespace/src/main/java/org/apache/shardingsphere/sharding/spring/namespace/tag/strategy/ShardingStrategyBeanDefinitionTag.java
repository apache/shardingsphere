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

package org.apache.shardingsphere.sharding.spring.namespace.tag.strategy;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Sharding strategy bean definition tag.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingStrategyBeanDefinitionTag {
    
    public static final String STANDARD_STRATEGY_ROOT_TAG = "standard-strategy";
    
    public static final String COMPLEX_STRATEGY_ROOT_TAG = "complex-strategy";
    
    public static final String HINT_STRATEGY_ROOT_TAG = "hint-strategy";
    
    public static final String NONE_STRATEGY_ROOT_TAG = "none-strategy";
    
    public static final String SHARDING_COLUMN_ATTRIBUTE = "sharding-column";
    
    public static final String SHARDING_COLUMNS_ATTRIBUTE = "sharding-columns";
    
    public static final String ALGORITHM_REF_ATTRIBUTE = "algorithm-ref";
}
