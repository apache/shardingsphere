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

package org.apache.shardingsphere.sharding.spring.namespace.handler;

import org.apache.shardingsphere.sharding.spring.namespace.parser.KeyGenerateAlgorithmBeanDefinitionParser;
import org.apache.shardingsphere.sharding.spring.namespace.parser.KeyGeneratorBeanDefinitionParser;
import org.apache.shardingsphere.sharding.spring.namespace.parser.ShardingAlgorithmBeanDefinitionParser;
import org.apache.shardingsphere.sharding.spring.namespace.parser.ShardingRuleBeanDefinitionParser;
import org.apache.shardingsphere.sharding.spring.namespace.parser.ShardingStrategyBeanDefinitionParser;
import org.apache.shardingsphere.sharding.spring.namespace.tag.SPIBeanDefinitionTag;
import org.apache.shardingsphere.sharding.spring.namespace.tag.ShardingRuleBeanDefinitionTag;
import org.apache.shardingsphere.sharding.spring.namespace.tag.ShardingStrategyBeanDefinitionTag;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Spring namespace handler for sharding.
 */
public final class ShardingNamespaceHandler extends NamespaceHandlerSupport {
    
    @Override
    public void init() {
        registerBeanDefinitionParser(ShardingRuleBeanDefinitionTag.ROOT_TAG, new ShardingRuleBeanDefinitionParser());
        registerBeanDefinitionParser(ShardingStrategyBeanDefinitionTag.STANDARD_STRATEGY_ROOT_TAG, new ShardingStrategyBeanDefinitionParser());
        registerBeanDefinitionParser(ShardingStrategyBeanDefinitionTag.COMPLEX_STRATEGY_ROOT_TAG, new ShardingStrategyBeanDefinitionParser());
        registerBeanDefinitionParser(ShardingStrategyBeanDefinitionTag.HINT_STRATEGY_ROOT_TAG, new ShardingStrategyBeanDefinitionParser());
        registerBeanDefinitionParser(ShardingStrategyBeanDefinitionTag.NONE_STRATEGY_ROOT_TAG, new ShardingStrategyBeanDefinitionParser());
        registerBeanDefinitionParser(ShardingRuleBeanDefinitionTag.KEY_GENERATOR_REF_TAG, new KeyGeneratorBeanDefinitionParser());
        registerBeanDefinitionParser(SPIBeanDefinitionTag.KEY_GENERATE_ALGORITHM_TAG, new KeyGenerateAlgorithmBeanDefinitionParser());
        registerBeanDefinitionParser(SPIBeanDefinitionTag.SHARDING_ALGORITHM_TAG, new ShardingAlgorithmBeanDefinitionParser());
    }
}
