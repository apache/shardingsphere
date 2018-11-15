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
import io.shardingsphere.orchestration.reg.api.RegistryCenterConfiguration;
import io.shardingsphere.shardingjdbc.orchestration.spring.namespace.constants.RegistryCenterBeanDefinitionParserTag;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Registry parser for spring namespace.
 * 
 * @author panjuan
 */
public final class RegBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(RegistryCenterConfiguration.class);
        addPropertyValueIfNotEmpty(RegistryCenterBeanDefinitionParserTag.SERVER_LISTS_TAG, "serverLists", element, factory);
        addPropertyValueIfNotEmpty(RegistryCenterBeanDefinitionParserTag.NAMESPACE_TAG, "namespace", element, factory);
        addPropertyValueIfNotEmpty(RegistryCenterBeanDefinitionParserTag.DIGEST_TAG, "digest", element, factory);
        addPropertyValueIfNotEmpty(RegistryCenterBeanDefinitionParserTag.OPERATION_TIMEOUT_MILLISECONDS_TAG, "operationTimeoutMilliseconds", element, factory);
        addPropertyValueIfNotEmpty(RegistryCenterBeanDefinitionParserTag.MAX_RETRIES_TAG, "maxRetries", element, factory);
        addPropertyValueIfNotEmpty(RegistryCenterBeanDefinitionParserTag.RETRY_INTERVAL_MILLISECONDS_TAG, "retryIntervalMilliseconds", element, factory);
        addPropertyValueIfNotEmpty(RegistryCenterBeanDefinitionParserTag.TIME_TO_LIVE_SECONDS_TAG, "timeToLiveSeconds", element, factory);
        return factory.getBeanDefinition();
    }
    
    private void addPropertyValueIfNotEmpty(final String attributeName, final String propertyName, final Element element, final BeanDefinitionBuilder factory) {
        String attributeValue = element.getAttribute(attributeName);
        if (!Strings.isNullOrEmpty(attributeValue)) {
            factory.addPropertyValue(propertyName, attributeValue);
        }
    }
}

