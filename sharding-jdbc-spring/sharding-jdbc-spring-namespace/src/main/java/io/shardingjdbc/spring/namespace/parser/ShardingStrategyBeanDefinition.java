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

package io.shardingjdbc.spring.namespace.parser;

import io.shardingjdbc.core.api.config.strategy.ComplexShardingStrategyConfiguration;
import io.shardingjdbc.core.api.config.strategy.HintShardingStrategyConfiguration;
import io.shardingjdbc.core.api.config.strategy.InlineShardingStrategyConfiguration;
import io.shardingjdbc.core.api.config.strategy.NoneShardingStrategyConfiguration;
import io.shardingjdbc.core.api.config.strategy.StandardShardingStrategyConfiguration;
import io.shardingjdbc.core.exception.ShardingJdbcException;
import io.shardingjdbc.spring.namespace.constants.ShardingStrategyBeanDefinitionParserTag;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.w3c.dom.Element;

/**
 * Sharding strategy parser for spring namespace.
 * 
 * @author caohao
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ShardingStrategyBeanDefinition {
    
    static AbstractBeanDefinition getBeanDefinitionByElement(final Element element) {
        String type = element.getLocalName();
        switch (type) {
            case ShardingStrategyBeanDefinitionParserTag.STANDARD_STRATEGY_ROOT_TAG:
                return getStandardShardingStrategyConfigBeanDefinition(element);
            case ShardingStrategyBeanDefinitionParserTag.COMPLEX_STRATEGY_ROOT_TAG:
                return getComplexShardingStrategyConfigBeanDefinition(element);
            case ShardingStrategyBeanDefinitionParserTag.INLINE_STRATEGY_ROOT_TAG:
                return getInlineShardingStrategyConfigBeanDefinition(element);
            case ShardingStrategyBeanDefinitionParserTag.HINT_STRATEGY_ROOT_TAG:
                return getHintShardingStrategyConfigBeanDefinition(element);
            case ShardingStrategyBeanDefinitionParserTag.NONE_STRATEGY_ROOT_TAG:
                return getNoneShardingStrategyConfigBeanDefinition();
            default:
                throw new ShardingJdbcException("Cannot support type: %s", type);
        }
    }
    
    private static AbstractBeanDefinition getStandardShardingStrategyConfigBeanDefinition(final Element element) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(StandardShardingStrategyConfiguration.class);
        factory.addConstructorArgValue(element.getAttribute(ShardingStrategyBeanDefinitionParserTag.SHARDING_COLUMN_ATTRIBUTE));
        factory.addConstructorArgValue(element.getAttribute(ShardingStrategyBeanDefinitionParserTag.PRECISE_ALGORITHM_CLASS_ATTRIBUTE));
        factory.addConstructorArgValue(element.getAttribute(ShardingStrategyBeanDefinitionParserTag.RANGE_ALGORITHM_CLASS_ATTRIBUTE));
        return factory.getBeanDefinition();
    }
    
    private static AbstractBeanDefinition getComplexShardingStrategyConfigBeanDefinition(final Element element) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(ComplexShardingStrategyConfiguration.class);
        factory.addConstructorArgValue(element.getAttribute(ShardingStrategyBeanDefinitionParserTag.SHARDING_COLUMNS_ATTRIBUTE));
        factory.addConstructorArgValue(element.getAttribute(ShardingStrategyBeanDefinitionParserTag.ALGORITHM_CLASS_ATTRIBUTE));
        return factory.getBeanDefinition();
    }
    
    private static AbstractBeanDefinition getInlineShardingStrategyConfigBeanDefinition(final Element element) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(InlineShardingStrategyConfiguration.class);
        factory.addConstructorArgValue(element.getAttribute(ShardingStrategyBeanDefinitionParserTag.SHARDING_COLUMN_ATTRIBUTE));
        factory.addConstructorArgValue(element.getAttribute(ShardingStrategyBeanDefinitionParserTag.ALGORITHM_EXPRESSION_ATTRIBUTE));
        return factory.getBeanDefinition();
    }
    
    private static AbstractBeanDefinition getHintShardingStrategyConfigBeanDefinition(final Element element) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(HintShardingStrategyConfiguration.class);
        factory.addConstructorArgValue(element.getAttribute(ShardingStrategyBeanDefinitionParserTag.ALGORITHM_CLASS_ATTRIBUTE));
        return factory.getBeanDefinition();
    }
    
    private static AbstractBeanDefinition getNoneShardingStrategyConfigBeanDefinition() {
        return BeanDefinitionBuilder.rootBeanDefinition(NoneShardingStrategyConfiguration.class).getBeanDefinition();
    }
}
