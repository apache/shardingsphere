/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingjdbc.orchestration.spring.namespace.parser;

import com.google.common.base.Strings;
import io.shardingsphere.orchestration.config.OrchestrationConfiguration;
import io.shardingsphere.shardingjdbc.orchestration.spring.datasource.OrchestrationSpringMasterSlaveDataSource;
import io.shardingsphere.shardingjdbc.orchestration.spring.datasource.OrchestrationSpringShardingDataSource;
import io.shardingsphere.shardingjdbc.orchestration.spring.namespace.constants.ShardingDataSourceBeanDefinitionParserTag;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Data source parser for spring namespace.
 * 
 * @author panjuan
 */
public final class DataSourceBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder factory = ShardingDataSourceBeanDefinitionParserTag.ROOT_TAG.equals(element.getLocalName())
                ? BeanDefinitionBuilder.rootBeanDefinition(OrchestrationSpringShardingDataSource.class) : BeanDefinitionBuilder.rootBeanDefinition(OrchestrationSpringMasterSlaveDataSource.class);
        configureFactory(element, factory);
        return factory.getBeanDefinition();
    }
    
    private void configureFactory(final Element element, final BeanDefinitionBuilder factory) {
        String dataSourceName = element.getAttribute(ShardingDataSourceBeanDefinitionParserTag.DATA_SOURCE_REF_TAG);
        if (!Strings.isNullOrEmpty(dataSourceName)) {
            factory.addConstructorArgReference(dataSourceName);
        }
        factory.addConstructorArgValue(getOrchestrationConfiguration(element));
    }
    
    private BeanDefinition getOrchestrationConfiguration(final Element element) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(OrchestrationConfiguration.class);
        factory.addConstructorArgValue(element.getAttribute(ID_ATTRIBUTE));
        factory.addConstructorArgReference(element.getAttribute(ShardingDataSourceBeanDefinitionParserTag.REG_REF_TAG));
        factory.addConstructorArgValue(element.getAttribute(ShardingDataSourceBeanDefinitionParserTag.OVERWRITE_TAG));
        return factory.getBeanDefinition();
    }
}

