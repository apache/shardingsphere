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

package org.apache.shardingsphere.dbdiscovery.spring.namespace.handler;

import org.apache.shardingsphere.dbdiscovery.spring.namespace.factorybean.DatabaseDiscoveryAlgorithmFactoryBean;
import org.apache.shardingsphere.dbdiscovery.spring.namespace.parser.DatabaseDiscoveryRuleBeanDefinitionParser;
import org.apache.shardingsphere.dbdiscovery.spring.namespace.tag.DatabaseDiscoveryRuleBeanDefinitionTag;
import org.apache.shardingsphere.dbdiscovery.spring.namespace.tag.DatabaseDiscoveryTypeBeanDefinitionTag;
import org.apache.shardingsphere.spring.namespace.parser.ShardingSphereAlgorithmBeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Spring namespace handler for database discovery.
 */
public final class DatabaseDiscoveryNamespaceHandler extends NamespaceHandlerSupport {
    
    @Override
    public void init() {
        registerBeanDefinitionParser(DatabaseDiscoveryRuleBeanDefinitionTag.ROOT_TAG, new DatabaseDiscoveryRuleBeanDefinitionParser());
        registerBeanDefinitionParser(DatabaseDiscoveryTypeBeanDefinitionTag.ROOT_TAG, new ShardingSphereAlgorithmBeanDefinitionParser(DatabaseDiscoveryAlgorithmFactoryBean.class));
    }
}
