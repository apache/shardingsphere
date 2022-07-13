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

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.spring.namespace.tag.DataSourceBeanDefinitionTag;
import org.apache.shardingsphere.spring.namespace.tag.mode.ModeBeanDefinitionTag;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * ShardingSphere data source parser for spring namespace.
 */
public final class DataSourceBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(ShardingSphereDataSource.class);
        factory.addConstructorArgValue(parseDatabaseName(element));
        factory.addConstructorArgValue(parseModeConfiguration(element));
        if (!Strings.isNullOrEmpty(element.getAttribute(DataSourceBeanDefinitionTag.DATA_SOURCE_NAMES_ATTRIBUTE))) {
            factory.addConstructorArgValue(parseDataSources(element));
            factory.addConstructorArgValue(parseRuleConfigurations(element));
            factory.addConstructorArgValue(parseProperties(element, parserContext));
        }
        factory.setDestroyMethodName("close");
        return factory.getBeanDefinition();
    }
    
    private String parseDatabaseName(final Element element) {
        String databaseName = element.getAttribute(DataSourceBeanDefinitionTag.DATABASE_NAME_ATTRIBUTE);
        return Strings.isNullOrEmpty(databaseName) ? element.getAttribute(DataSourceBeanDefinitionTag.SCHEMA_NAME_ATTRIBUTE) : databaseName;
    }
    
    // TODO parse mode
    private BeanDefinition parseModeConfiguration(final Element element) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(ModeConfiguration.class);
        Element modeElement = DomUtils.getChildElementByTagName(element, ModeBeanDefinitionTag.ROOT_TAG);
        if (null == modeElement) {
            addDefaultModeConfiguration(factory);
        } else {
            addConfiguredModeConfiguration(factory, modeElement);
        }
        return factory.getBeanDefinition();
    }
    
    private void addDefaultModeConfiguration(final BeanDefinitionBuilder factory) {
        factory.addConstructorArgValue("Standalone");
        factory.addConstructorArgValue(null);
        factory.addConstructorArgValue(true);
    }
    
    private void addConfiguredModeConfiguration(final BeanDefinitionBuilder factory, final Element modeElement) {
        factory.addConstructorArgValue(modeElement.getAttribute(ModeBeanDefinitionTag.TYPE_ATTRIBUTE));
        if (null == modeElement.getAttribute(ModeBeanDefinitionTag.REPOSITORY_REF_ATTRIBUTE)) {
            factory.addConstructorArgValue(null);
        } else {
            factory.addConstructorArgReference(modeElement.getAttribute(ModeBeanDefinitionTag.REPOSITORY_REF_ATTRIBUTE));
        }
        factory.addConstructorArgValue(modeElement.getAttribute(ModeBeanDefinitionTag.OVERWRITE_ATTRIBUTE));
    }
    
    private Map<String, RuntimeBeanReference> parseDataSources(final Element element) {
        List<String> dataSources = Splitter.on(",").trimResults().splitToList(element.getAttribute(DataSourceBeanDefinitionTag.DATA_SOURCE_NAMES_ATTRIBUTE));
        Map<String, RuntimeBeanReference> result = new ManagedMap<>(dataSources.size());
        for (String each : dataSources) {
            result.put(each, new RuntimeBeanReference(each));
        }
        return result;
    }
    
    private Collection<RuntimeBeanReference> parseRuleConfigurations(final Element element) {
        List<String> ruleIds = Splitter.on(",").trimResults().splitToList(element.getAttribute(DataSourceBeanDefinitionTag.RULE_REFS_ATTRIBUTE));
        Collection<RuntimeBeanReference> result = new ManagedList<>(ruleIds.size());
        for (String each : ruleIds) {
            result.add(new RuntimeBeanReference(each));
        }
        return result;
    }
    
    private Properties parseProperties(final Element element, final ParserContext parserContext) {
        Element propsElement = DomUtils.getChildElementByTagName(element, DataSourceBeanDefinitionTag.PROPS_TAG);
        return null == propsElement ? new Properties() : parserContext.getDelegate().parsePropsElement(propsElement);
    }
}
