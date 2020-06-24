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

import org.apache.shardingsphere.sharding.spring.namespace.factorybean.KeyGenerateAlgorithmFactoryBean;
import org.apache.shardingsphere.sharding.spring.namespace.factorybean.ShardingAlgorithmFactoryBean;
import org.apache.shardingsphere.sharding.spring.namespace.parser.rule.ShardingRuleBeanDefinitionParser;
import org.apache.shardingsphere.sharding.spring.namespace.parser.strategy.KeyGenerateStrategyBeanDefinitionParser;
import org.apache.shardingsphere.sharding.spring.namespace.parser.strategy.ShardingStrategyBeanDefinitionParser;
import org.apache.shardingsphere.sharding.spring.namespace.tag.algorithm.KeyGenerateAlgorithmBeanDefinitionTag;
import org.apache.shardingsphere.sharding.spring.namespace.tag.algorithm.ShardingAlgorithmBeanDefinitionTag;
import org.apache.shardingsphere.sharding.spring.namespace.tag.rule.ShardingRuleBeanDefinitionTag;
import org.apache.shardingsphere.sharding.spring.namespace.tag.strategy.KeyGenerateStrategyBeanDefinitionTag;
import org.apache.shardingsphere.sharding.spring.namespace.tag.strategy.ShardingStrategyBeanDefinitionTag;
import org.apache.shardingsphere.spring.namespace.parser.ShardingSphereAlgorithmBeanDefinitionParser;
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
        registerBeanDefinitionParser(ShardingAlgorithmBeanDefinitionTag.ROOT_TAG, new ShardingSphereAlgorithmBeanDefinitionParser(ShardingAlgorithmFactoryBean.class));
        registerBeanDefinitionParser(KeyGenerateStrategyBeanDefinitionTag.ROOT_TAG, new KeyGenerateStrategyBeanDefinitionParser());
        registerBeanDefinitionParser(KeyGenerateAlgorithmBeanDefinitionTag.ROOT_TAG, new ShardingSphereAlgorithmBeanDefinitionParser(KeyGenerateAlgorithmFactoryBean.class));
    }
}
