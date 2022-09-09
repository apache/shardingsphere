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

package org.apache.shardingsphere.sharding.spring.namespace.parser.strategy;

import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.spring.namespace.tag.strategy.ShardingAuditStrategyBeanDefinitionTag;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.Collections;
import java.util.List;

/**
 * Sharding audit strategy bean parser for spring namespace.
 */
public final class ShardingAuditStrategyBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(ShardingAuditStrategyConfiguration.class);
        factory.addConstructorArgValue(parseAuditorsConfiguration(element));
        factory.addConstructorArgValue(element.getAttribute(ShardingAuditStrategyBeanDefinitionTag.ALLOW_HINT_DISABLE_ATTRIBUTE));
        return factory.getBeanDefinition();
    }
    
    private List<String> parseAuditorsConfiguration(final Element element) {
        Element auditorsElement = DomUtils.getChildElementByTagName(element, ShardingAuditStrategyBeanDefinitionTag.AUDITORS_TAG);
        if (null == auditorsElement) {
            return Collections.emptyList();
        }
        List<Element> auditorElements = DomUtils.getChildElementsByTagName(auditorsElement, ShardingAuditStrategyBeanDefinitionTag.AUDITOR_TAG);
        List<String> result = new ManagedList<>(auditorElements.size());
        for (Element each : auditorElements) {
            String algorithmRef = each.getAttribute(ShardingAuditStrategyBeanDefinitionTag.ALGORITHM_REF_ATTRIBUTE);
            result.add(algorithmRef);
        }
        return result;
    }
}
