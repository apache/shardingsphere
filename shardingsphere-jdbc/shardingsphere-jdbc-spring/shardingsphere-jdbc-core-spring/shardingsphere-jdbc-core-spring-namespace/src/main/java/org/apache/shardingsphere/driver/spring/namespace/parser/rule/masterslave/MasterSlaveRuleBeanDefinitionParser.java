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

package org.apache.shardingsphere.driver.spring.namespace.parser.rule.masterslave;

import org.apache.shardingsphere.driver.spring.namespace.constants.rules.masterslave.MasterSlaveRuleBeanDefinitionParserTag;
import org.apache.shardingsphere.masterslave.api.config.MasterSlaveRuleConfiguration;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.List;

/**
 * Master-slave rule parser for spring namespace.
 */
public final class MasterSlaveRuleBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        List<Element> masterSlaveDataSourceElements = DomUtils.getChildElementsByTagName(element, MasterSlaveRuleBeanDefinitionParserTag.MASTER_SLAVE_DATA_SOURCE_TAG);
        List<BeanDefinition> masterSlaveDataSources = new ManagedList<>(masterSlaveDataSourceElements.size());
        for (Element each : masterSlaveDataSourceElements) {
            masterSlaveDataSources.add(new MasterSlaveDataSourceConfigurationBeanDefinition(each).getBeanDefinition());
        }
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(MasterSlaveRuleConfiguration.class);
        factory.addConstructorArgValue(masterSlaveDataSources);
        return factory.getBeanDefinition();
    }
}
