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

package org.apache.shardingsphere.ha.spring.namespace.handler;

import org.apache.shardingsphere.ha.spring.namespace.factorybean.ReplicaLoadBalanceAlgorithmFactoryBean;
import org.apache.shardingsphere.ha.spring.namespace.parser.HARuleBeanDefinitionParser;
import org.apache.shardingsphere.ha.spring.namespace.tag.LoadBalanceAlgorithmBeanDefinitionTag;
import org.apache.shardingsphere.ha.spring.namespace.tag.HARuleBeanDefinitionTag;
import org.apache.shardingsphere.spring.namespace.parser.ShardingSphereAlgorithmBeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Spring namespace handler for HA.
 */
public final class HANamespaceHandler extends NamespaceHandlerSupport {
    
    @Override
    public void init() {
        registerBeanDefinitionParser(HARuleBeanDefinitionTag.ROOT_TAG, new HARuleBeanDefinitionParser());
        registerBeanDefinitionParser(LoadBalanceAlgorithmBeanDefinitionTag.ROOT_TAG, new ShardingSphereAlgorithmBeanDefinitionParser(ReplicaLoadBalanceAlgorithmFactoryBean.class));
    }
}
