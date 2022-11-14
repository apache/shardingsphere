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

package org.apache.shardingsphere.spring.boot.registry;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithm;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmFactory;
import org.apache.shardingsphere.infra.util.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spring.boot.util.PropertyUtil;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.core.env.Environment;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Abstract algorithm provided bean registry.
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractAlgorithmProvidedBeanRegistry<T extends ShardingSphereAlgorithm> implements BeanDefinitionRegistryPostProcessor, BeanPostProcessor {
    
    private static final String POINT = ".";
    
    private static final String PROPS = "props";
    
    private static final String PROPS_SUFFIX = POINT + PROPS;
    
    private static final String TYPE_SUFFIX = ".type";
    
    private final Environment env;
    
    private final Map<String, Properties> propsMap = new HashMap<>();
    
    @SuppressWarnings("unchecked")
    protected final void registerBean(final String prefix, final Class<T> algorithmClass, final BeanDefinitionRegistry registry) {
        if (!PropertyUtil.containsPropertyPrefix(env, prefix)) {
            return;
        }
        Map<String, Object> parameterMap = PropertyUtil.handle(env, prefix, Map.class);
        Collection<String> algorithmNames = parameterMap.keySet().stream().map(key -> key.contains(POINT) ? key.substring(0, key.indexOf(POINT)) : key).collect(Collectors.toSet());
        Map<String, AlgorithmConfiguration> algorithmConfigs = createAlgorithmConfigurations(prefix, algorithmNames);
        ShardingSphereServiceLoader.register(algorithmClass);
        for (Entry<String, AlgorithmConfiguration> entry : algorithmConfigs.entrySet()) {
            AlgorithmConfiguration algorithmConfig = entry.getValue();
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(ShardingSphereAlgorithmFactory.createAlgorithm(algorithmConfig, algorithmClass).getClass());
            registry.registerBeanDefinition(entry.getKey(), builder.getBeanDefinition());
            propsMap.put(entry.getKey(), algorithmConfig.getProps());
        }
    }
    
    private Map<String, AlgorithmConfiguration> createAlgorithmConfigurations(final String prefix, final Collection<String> algorithmNames) {
        Map<String, AlgorithmConfiguration> result = new LinkedHashMap<>(algorithmNames.size(), 1);
        for (String each : algorithmNames) {
            result.put(each, createAlgorithmConfiguration(prefix, each));
        }
        return result;
    }
    
    private AlgorithmConfiguration createAlgorithmConfiguration(final String prefix, final String algorithmName) {
        String type = env.getProperty(String.join("", prefix, algorithmName, TYPE_SUFFIX));
        Properties props = getProperties(prefix, algorithmName);
        return new AlgorithmConfiguration(type, props);
    }
    
    private Properties getProperties(final String prefix, final String algorithmName) {
        String propsPrefix = String.join("", prefix, algorithmName, PROPS_SUFFIX);
        Properties result = new Properties();
        if (PropertyUtil.containsPropertyPrefix(env, propsPrefix)) {
            result.putAll(PropertyUtil.handle(env, propsPrefix, Map.class));
        }
        return convertToStringTypedProperties(result);
    }
    
    private Properties convertToStringTypedProperties(final Properties props) {
        Properties result = new Properties();
        props.forEach((key, value) -> result.setProperty(key.toString(), null == value ? null : value.toString()));
        return result;
    }
    
    @Override
    public final void postProcessBeanFactory(final ConfigurableListableBeanFactory configurableListableBeanFactory) {
    }
    
    @Override
    public final Object postProcessBeforeInitialization(final Object bean, final String beanName) {
        return bean;
    }
    
    @Override
    public final Object postProcessAfterInitialization(final Object bean, final String beanName) {
        if (bean instanceof ShardingSphereAlgorithm && propsMap.containsKey(beanName)) {
            ((ShardingSphereAlgorithm) bean).init(propsMap.get(beanName));
        }
        return bean;
    }
}
