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

package org.apache.shardingsphere.shadow.spring.namespace.parser;

import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.spring.namespace.tag.ShadowDataSourceBeanDefinitionParserTag;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Shadow rule parser for spring namespace.
 */
public final class ShadowRuleBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    /**
     * Parse shadow rule element.
     *
     * @param element element
     * @return bean definition of shadow rule
     */
    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(ShadowRuleConfiguration.class);
        factory.addConstructorArgValue(element.getAttribute(ShadowDataSourceBeanDefinitionParserTag.COLUMN_CONFIG_TAG));
        factory.addConstructorArgValue(element.getAttribute(ShadowDataSourceBeanDefinitionParserTag.SOURCE_DATASOURCE_NAMES_TAG).split(","));
        factory.addConstructorArgValue(element.getAttribute(ShadowDataSourceBeanDefinitionParserTag.SHADOW_DATASOURCE_NAMES_TAG).split(","));
        return factory.getBeanDefinition();
    }
}
