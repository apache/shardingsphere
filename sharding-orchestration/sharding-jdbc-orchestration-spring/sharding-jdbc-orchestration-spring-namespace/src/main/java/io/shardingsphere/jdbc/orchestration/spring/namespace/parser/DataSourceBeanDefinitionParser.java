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

package io.shardingsphere.jdbc.orchestration.spring.namespace.parser;

import com.google.common.base.Strings;
import io.shardingsphere.jdbc.orchestration.config.OrchestrationConfiguration;
import io.shardingsphere.jdbc.orchestration.config.OrchestrationType;
import io.shardingsphere.jdbc.orchestration.spring.datasource.OrchestrationSpringMasterSlaveDataSource;
import io.shardingsphere.jdbc.orchestration.spring.datasource.OrchestrationSpringShardingDataSource;
import io.shardingsphere.jdbc.orchestration.spring.namespace.constants.EtcdRegistryCenterBeanDefinitionParserTag;
import io.shardingsphere.jdbc.orchestration.spring.namespace.constants.ShardingDataSourceBeanDefinitionParserTag;
import io.shardingsphere.jdbc.orchestration.spring.namespace.constants.ZookeeperRegistryCenterBeanDefinitionParserTag;
import io.shardingsphere.orchestration.reg.api.RegistryCenterConfiguration;
import io.shardingsphere.orchestration.reg.etcd.EtcdConfiguration;
import io.shardingsphere.orchestration.reg.zookeeper.ZookeeperConfiguration;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.lang.reflect.Field;

/**
 * Data source parser for spring namespace.
 * 
 * @author panjuan
 */
public final class DataSourceBeanDefinitionParser extends AbstractBeanDefinitionParser {
    
    private OrchestrationType orchestrationType;
    
    @Override
    protected AbstractBeanDefinition parseInternal(final Element element, final ParserContext parserContext) {
        setOrchestrationType(element);
        BeanDefinitionBuilder factory = OrchestrationType.SHARDING == orchestrationType ? BeanDefinitionBuilder.rootBeanDefinition(OrchestrationSpringShardingDataSource.class)
                : BeanDefinitionBuilder.rootBeanDefinition(OrchestrationSpringMasterSlaveDataSource.class);
        
        factory.addConstructorArgReference(element.getAttribute(ShardingDataSourceBeanDefinitionParserTag.DATA_SOURCE_REF_TAG));
        factory.addConstructorArgValue(getOrchestrationConfiguration(element));
        return factory.getBeanDefinition();
    }
    
    private void setOrchestrationType(final Element element) {
        orchestrationType = ShardingDataSourceBeanDefinitionParserTag.ROOT_TAG.equals(element.getLocalName()) ? OrchestrationType.SHARDING : OrchestrationType.MASTER_SLAVE;
    }
    
    private BeanDefinition getOrchestrationConfiguration(final Element element) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(OrchestrationConfiguration.class);
        factory.addConstructorArgValue(element.getAttribute(ID_ATTRIBUTE));
        factory.addConstructorArgReference(element.getAttribute(ShardingDataSourceBeanDefinitionParserTag.REG_REF_TAG));
        factory.addConstructorArgValue(element.getAttribute(ShardingDataSourceBeanDefinitionParserTag.OVERWRITE_TAG));
        factory.addConstructorArgValue(orchestrationType);
        return factory.getBeanDefinition();
    }
    
    private RegistryCenterConfiguration getRegCenterConfig(final Element element) {
        Element regElement = DomUtils.getChildElementByTagName(element, ZookeeperRegistryCenterBeanDefinitionParserTag.ROOT_TAG);
        if (null == regElement) {
            regElement = DomUtils.getChildElementByTagName(element, EtcdRegistryCenterBeanDefinitionParserTag.ROOT_TAG);
            return getEtcdConfiguration(regElement);
        }
        return getZookeeperConfiguration(regElement);
    }
    
    private ZookeeperConfiguration getZookeeperConfiguration(final Element element) {
        ZookeeperConfiguration result = new ZookeeperConfiguration();
        setRegistryCenterConfiguration(ZookeeperRegistryCenterBeanDefinitionParserTag.SERVER_LISTS_TAG, "serverLists", element, result);
        setRegistryCenterConfiguration(ZookeeperRegistryCenterBeanDefinitionParserTag.NAMESPACE_TAG, "namespace", element, result);
        setRegistryCenterConfiguration(ZookeeperRegistryCenterBeanDefinitionParserTag.BASE_SLEEP_TIME_MILLISECONDS_TAG, "baseSleepTimeMilliseconds", element, result);
        setRegistryCenterConfiguration(ZookeeperRegistryCenterBeanDefinitionParserTag.MAX_SLEEP_TIME_MILLISECONDS_TAG, "maxSleepTimeMilliseconds", element, result);
        setRegistryCenterConfiguration(ZookeeperRegistryCenterBeanDefinitionParserTag.MAX_RETRIES_TAG, "maxRetries", element, result);
        setRegistryCenterConfiguration(ZookeeperRegistryCenterBeanDefinitionParserTag.SESSION_TIMEOUT_MILLISECONDS_TAG, "sessionTimeoutMilliseconds", element, result);
        setRegistryCenterConfiguration(ZookeeperRegistryCenterBeanDefinitionParserTag.CONNECTION_TIMEOUT_MILLISECONDS_TAG, "connectionTimeoutMilliseconds", element, result);
        setRegistryCenterConfiguration(ZookeeperRegistryCenterBeanDefinitionParserTag.DIGEST_TAG, "digest", element, result);
        return result;
    }
    
    private EtcdConfiguration getEtcdConfiguration(final Element element) {
        EtcdConfiguration result = new EtcdConfiguration();
        setRegistryCenterConfiguration(EtcdRegistryCenterBeanDefinitionParserTag.SERVER_LISTS_TAG, "serverLists", element, result);
        setRegistryCenterConfiguration(EtcdRegistryCenterBeanDefinitionParserTag.TIME_TO_LIVE_SECONDS_TAG, "timeToLiveSeconds", element, result);
        setRegistryCenterConfiguration(EtcdRegistryCenterBeanDefinitionParserTag.TIMEOUT_MILLISECONDS_TAG, "timeoutMilliseconds", element, result);
        setRegistryCenterConfiguration(EtcdRegistryCenterBeanDefinitionParserTag.RETRY_INTERVAL_MILLISECONDS_TAG, "retryIntervalMilliseconds", element, result);
        setRegistryCenterConfiguration(EtcdRegistryCenterBeanDefinitionParserTag.MAX_RETRIES_TAG, "maxRetries", element, result);
        return result;
    }
    
    private void setRegistryCenterConfiguration(final String attributeName, final String propertyName, final Element element, final RegistryCenterConfiguration regConfiguration) {
        String attributeValue = element.getAttribute(attributeName);
        if (!Strings.isNullOrEmpty(attributeValue)) {
            try {
                Field field = ZookeeperConfiguration.class.getDeclaredField(propertyName);
                field.setAccessible(true);
                String fieldTypeName = field.getType().toString();
                if (fieldTypeName.endsWith("int")) {
                    field.set(regConfiguration, Integer.valueOf(attributeValue));
                } else {
                    field.set(regConfiguration, attributeValue);
                }
            } catch (final ReflectiveOperationException ignored) {
            }
        }
    }
}

