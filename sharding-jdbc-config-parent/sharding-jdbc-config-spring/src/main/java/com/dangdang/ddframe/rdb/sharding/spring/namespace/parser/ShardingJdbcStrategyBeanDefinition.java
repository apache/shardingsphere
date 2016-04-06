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

package com.dangdang.ddframe.rdb.sharding.spring.namespace.parser;

import lombok.NoArgsConstructor;
import com.dangdang.ddframe.rdb.sharding.config.common.api.config.StrategyConfig;
import com.dangdang.ddframe.rdb.sharding.spring.namespace.constants.ShardingJdbcStrategyBeanDefinitionParserTag;
import lombok.AccessLevel;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.w3c.dom.Element;

/**
 * 基于Spring命名空间的ShardingJdbc的分库分表策略定义.
 * 
 * @author caohao
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ShardingJdbcStrategyBeanDefinition {
    
    static AbstractBeanDefinition getBeanDefinitionByElement(final Element element) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(StrategyConfig.class);
        factory.addPropertyValue("shardingColumns", element.getAttribute(ShardingJdbcStrategyBeanDefinitionParserTag.SHARDING_COLUMNS_TAG));
        factory.addPropertyValue("algorithmClassName", element.getAttribute(ShardingJdbcStrategyBeanDefinitionParserTag.ALGORITHM_CLASS_TAG));
        factory.addPropertyValue("algorithmExpression", element.getAttribute(ShardingJdbcStrategyBeanDefinitionParserTag.ALGORITHM_EXPRESSION_TAG));
        return factory.getBeanDefinition();
    }
}
