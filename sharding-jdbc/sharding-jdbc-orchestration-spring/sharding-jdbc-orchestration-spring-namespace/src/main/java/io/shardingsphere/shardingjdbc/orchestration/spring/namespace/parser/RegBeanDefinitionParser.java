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
import io.shardingsphere.orchestration.reg.etcd.EtcdConfiguration;
import io.shardingsphere.orchestration.reg.zookeeper.curator.CuratorZookeeperConfiguration;
import io.shardingsphere.shardingjdbc.orchestration.spring.namespace.constants.EtcdRegistryCenterBeanDefinitionParserTag;
import io.shardingsphere.shardingjdbc.orchestration.spring.namespace.constants.ZookeeperRegistryCenterBeanDefinitionParserTag;
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
        return ZookeeperRegistryCenterBeanDefinitionParserTag.ROOT_TAG.equals(element.getLocalName()) ? getZookeeperConfiguration(element) : getEtcdConfiguration(element);
    }
    
    private AbstractBeanDefinition getZookeeperConfiguration(final Element element) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(CuratorZookeeperConfiguration.class);
        addPropertyValueIfNotEmpty(ZookeeperRegistryCenterBeanDefinitionParserTag.SERVER_LISTS_TAG, "serverLists", element, factory);
        addPropertyValueIfNotEmpty(ZookeeperRegistryCenterBeanDefinitionParserTag.NAMESPACE_TAG, "namespace", element, factory);
        addPropertyValueIfNotEmpty(ZookeeperRegistryCenterBeanDefinitionParserTag.DIGEST_TAG, "digest", element, factory);
        addPropertyValueIfNotEmpty(ZookeeperRegistryCenterBeanDefinitionParserTag.OPERATION_TIMEOUT_MILLISECONDS_TAG, "operationTimeoutMilliseconds", element, factory);
        addPropertyValueIfNotEmpty(ZookeeperRegistryCenterBeanDefinitionParserTag.MAX_RETRIES_TAG, "maxRetries", element, factory);
        addPropertyValueIfNotEmpty(ZookeeperRegistryCenterBeanDefinitionParserTag.RETRY_INTERVAL_MILLISECONDS_TAG, "retryIntervalMilliseconds", element, factory);
        addPropertyValueIfNotEmpty(ZookeeperRegistryCenterBeanDefinitionParserTag.TIME_TO_LIVE_SECONDS_TAG, "timeToLiveSeconds", element, factory);
        return factory.getBeanDefinition();
    }
    
    private AbstractBeanDefinition getEtcdConfiguration(final Element element) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(EtcdConfiguration.class);
        addPropertyValueIfNotEmpty(EtcdRegistryCenterBeanDefinitionParserTag.SERVER_LISTS_TAG, "serverLists", element, factory);
        addPropertyValueIfNotEmpty(EtcdRegistryCenterBeanDefinitionParserTag.OPERATION_TIMEOUT_MILLISECONDS_TAG, "operationTimeoutMilliseconds", element, factory);
        addPropertyValueIfNotEmpty(EtcdRegistryCenterBeanDefinitionParserTag.MAX_RETRIES_TAG, "maxRetries", element, factory);
        addPropertyValueIfNotEmpty(EtcdRegistryCenterBeanDefinitionParserTag.RETRY_INTERVAL_MILLISECONDS_TAG, "retryIntervalMilliseconds", element, factory);
        addPropertyValueIfNotEmpty(EtcdRegistryCenterBeanDefinitionParserTag.TIME_TO_LIVE_SECONDS_TAG, "timeToLiveSeconds", element, factory);
        return factory.getBeanDefinition();
    }
    
    private void addPropertyValueIfNotEmpty(final String attributeName, final String propertyName, final Element element, final BeanDefinitionBuilder factory) {
        String attributeValue = element.getAttribute(attributeName);
        if (!Strings.isNullOrEmpty(attributeValue)) {
            factory.addPropertyValue(propertyName, attributeValue);
        }
    }
}

