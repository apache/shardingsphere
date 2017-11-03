/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.orchestration.spring.namespace.parser;

import com.google.common.base.Strings;
import io.shardingjdbc.orchestration.reg.zookeeper.ZookeeperConfiguration;
import io.shardingjdbc.orchestration.reg.zookeeper.ZookeeperRegistryCenter;
import io.shardingjdbc.orchestration.spring.namespace.constants.RegistryCenterBeanDefinitionParserTag;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Zookeeper namespace parser for spring namespace.
 * 
 * @author caohao
 */
public final class ZookeeperBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder result = BeanDefinitionBuilder.rootBeanDefinition(ZookeeperRegistryCenter.class);
        result.addConstructorArgValue(buildZookeeperConfigurationBeanDefinition(element));
        result.setInitMethodName("init");
        return result.getBeanDefinition();
    }
    
    private AbstractBeanDefinition buildZookeeperConfigurationBeanDefinition(final Element element) {
        BeanDefinitionBuilder configuration = BeanDefinitionBuilder.rootBeanDefinition(ZookeeperConfiguration.class);
        addPropertyValueIfNotEmpty(RegistryCenterBeanDefinitionParserTag.SERVER_LISTS_TAG, "serverLists", element, configuration);
        addPropertyValueIfNotEmpty(RegistryCenterBeanDefinitionParserTag.NAMESPACE_TAG, "namespace", element, configuration);
        addPropertyValueIfNotEmpty(RegistryCenterBeanDefinitionParserTag.BASE_SLEEP_TIME_MILLISECONDS_TAG, "baseSleepTimeMilliseconds", element, configuration);
        addPropertyValueIfNotEmpty(RegistryCenterBeanDefinitionParserTag.MAX_SLEEP_TIME_MILLISECONDS_TAG, "maxSleepTimeMilliseconds", element, configuration);
        addPropertyValueIfNotEmpty(RegistryCenterBeanDefinitionParserTag.MAX_RETRIES_TAG, "maxRetries", element, configuration);
        addPropertyValueIfNotEmpty(RegistryCenterBeanDefinitionParserTag.SESSION_TIMEOUT_MILLISECONDS_TAG, "sessionTimeoutMilliseconds", element, configuration);
        addPropertyValueIfNotEmpty(RegistryCenterBeanDefinitionParserTag.CONNECTION_TIMEOUT_MILLISECONDS_TAG, "connectionTimeoutMilliseconds", element, configuration);
        addPropertyValueIfNotEmpty(RegistryCenterBeanDefinitionParserTag.DIGEST_TAG, "digest", element, configuration);
        return configuration.getBeanDefinition();
    }
    
    private void addPropertyValueIfNotEmpty(final String attributeName, final String propertyName, final Element element, final BeanDefinitionBuilder factory) {
        String attributeValue = element.getAttribute(attributeName);
        if (!Strings.isNullOrEmpty(attributeValue)) {
            factory.addPropertyValue(propertyName, attributeValue);
        }
    }
}
