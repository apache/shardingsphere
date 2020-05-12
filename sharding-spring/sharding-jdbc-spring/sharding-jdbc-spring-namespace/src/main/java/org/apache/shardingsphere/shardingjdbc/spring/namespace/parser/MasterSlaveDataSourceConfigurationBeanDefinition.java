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

package org.apache.shardingsphere.shardingjdbc.spring.namespace.parser;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.masterslave.api.config.MasterSlaveDataSourceConfiguration;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.List;

/**
 * Master slave data source configuration bean definition.
 */
@Getter
public final class MasterSlaveDataSourceConfigurationBeanDefinition {
    
    private static final String MASTER_DATA_SOURCE_NAME_ATTRIBUTE = "master-data-source-name";
    
    private static final String SLAVE_DATA_SOURCE_NAMES_ATTRIBUTE = "slave-data-source-names";
    
    private static final String LOAD_BALANCE_ALGORITHM_REF_ATTRIBUTE = "strategy-ref";
    
    private String masterDataSourceName;
    
    private String slaveDataSourceNames;
    
    private BeanDefinition beanDefinition;
    
    public MasterSlaveDataSourceConfigurationBeanDefinition(final Element element) {
        masterDataSourceName = element.getAttribute(MASTER_DATA_SOURCE_NAME_ATTRIBUTE);
        slaveDataSourceNames = element.getAttribute(SLAVE_DATA_SOURCE_NAMES_ATTRIBUTE);
        beanDefinition = getBeanDefinition(element);
    }
    
    private BeanDefinition getBeanDefinition(final Element element) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(MasterSlaveDataSourceConfiguration.class);
        factory.addConstructorArgValue(element.getAttribute("id"));
        factory.addConstructorArgValue(masterDataSourceName);
        factory.addConstructorArgValue(parseSlaveDataSourcesRef());
        parseMasterSlaveRuleLoadBalanceConfiguration(element, factory);
        return factory.getBeanDefinition();
    }
    
    private void parseMasterSlaveRuleLoadBalanceConfiguration(final Element element, final BeanDefinitionBuilder factory) {
        String loadBalanceStrategyConfiguration = element.getAttribute(LOAD_BALANCE_ALGORITHM_REF_ATTRIBUTE);
        if (!Strings.isNullOrEmpty(loadBalanceStrategyConfiguration)) {
            factory.addConstructorArgReference(loadBalanceStrategyConfiguration);
        }
    }
    
    private Collection<String> parseSlaveDataSourcesRef() {
        List<String> slaveDataSources = Splitter.on(",").trimResults().splitToList(slaveDataSourceNames);
        Collection<String> result = new ManagedList<>(slaveDataSources.size());
        result.addAll(slaveDataSources);
        return result;
    }
}
