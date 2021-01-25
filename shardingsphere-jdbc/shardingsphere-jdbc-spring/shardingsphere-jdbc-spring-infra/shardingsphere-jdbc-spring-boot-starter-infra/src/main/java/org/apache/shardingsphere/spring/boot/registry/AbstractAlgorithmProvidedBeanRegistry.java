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
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithm;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmPostProcessor;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;
import org.apache.shardingsphere.infra.yaml.config.algorithm.YamlShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.spring.boot.util.PropertyUtil;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.core.env.Environment;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
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
    
    private final Environment environment;
    
    @SuppressWarnings("all")
    protected void registerBean(final String prefix, final Class<T> algorithmClass, final BeanDefinitionRegistry registry) {
        boolean existPrefix = PropertyUtil.containPropertyPrefix(environment, prefix);
        if (existPrefix) {
            Map<String, Object> paramMap = PropertyUtil.handle(environment, prefix, Map.class);
            Set<String> keys = paramMap.keySet().stream().map(key -> {
                return key.contains(POINT) ? key.substring(0, key.indexOf(POINT)) : key;
            }).collect(Collectors.toSet());
            Map<String, YamlShardingSphereAlgorithmConfiguration> shardingAlgorithmMap = new LinkedHashMap<>();
            keys.forEach(each -> {
                YamlShardingSphereAlgorithmConfiguration config = new YamlShardingSphereAlgorithmConfiguration();
                String propsPrefix = String.join("", prefix, each, PROPS_SUFFIX);
                boolean existProps = PropertyUtil.containPropertyPrefix(environment, propsPrefix);
                if (existProps) {
                    Map<String, Object> propsMap = PropertyUtil.handle(environment, propsPrefix, Map.class);
                    config.getProps().putAll(propsMap);
                }
                String typePrefix = String.join("", prefix, each, TYPE_SUFFIX);
                String algorithmType = environment.getProperty(typePrefix);
                config.setType(algorithmType);
                shardingAlgorithmMap.put(each, config);
            });
            ShardingSphereServiceLoader.register(algorithmClass);
            shardingAlgorithmMap.forEach((key, algorithmConfiguration) -> {
                ShardingSphereAlgorithm algorithm = TypedSPIRegistry.getRegisteredService(algorithmClass, algorithmConfiguration.getType(), algorithmConfiguration.getProps());
                BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(algorithm.getClass());
                builder.addPropertyValue(PROPS, algorithmConfiguration.getProps());
                registry.registerBeanDefinition(key, builder.getBeanDefinition());
            });
        }
    }
    
    @Override
    public void postProcessBeanFactory(final ConfigurableListableBeanFactory configurableListableBeanFactory) {
    }
    
    @Override
    public Object postProcessBeforeInitialization(final Object bean, final String beanName) {
        return bean;
    }
    
    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) {
        if (bean instanceof ShardingSphereAlgorithmPostProcessor) {
            ((ShardingSphereAlgorithmPostProcessor) bean).init();
        }
        return bean;
    }
}
