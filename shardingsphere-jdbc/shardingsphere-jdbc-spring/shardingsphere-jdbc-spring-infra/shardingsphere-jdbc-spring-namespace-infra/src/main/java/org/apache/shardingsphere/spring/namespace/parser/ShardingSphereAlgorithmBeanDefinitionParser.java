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

package org.apache.shardingsphere.spring.namespace.parser;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.spring.namespace.factorybean.ShardingSphereAlgorithmFactoryBean;
import org.apache.shardingsphere.spring.namespace.tag.ShardingSphereAlgorithmBeanDefinitionTag;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.Properties;

/**
 * ShardingSphere algorithm bean parser for spring namespace.
 */
@RequiredArgsConstructor
public final class ShardingSphereAlgorithmBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    private final Class<? extends ShardingSphereAlgorithmFactoryBean<?>> beanClass;
    
    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(beanClass);
        factory.addConstructorArgValue(element.getAttribute(ShardingSphereAlgorithmBeanDefinitionTag.TYPE_ATTRIBUTE));
        factory.addConstructorArgValue(parsePropsElement(element, parserContext));
        return factory.getBeanDefinition();
    }
    
    private Properties parsePropsElement(final Element element, final ParserContext parserContext) {
        Element propsElement = DomUtils.getChildElementByTagName(element, ShardingSphereAlgorithmBeanDefinitionTag.PROPS_TAG);
        return null == propsElement ? new Properties() : parserContext.getDelegate().parsePropsElement(propsElement);
    }
}
