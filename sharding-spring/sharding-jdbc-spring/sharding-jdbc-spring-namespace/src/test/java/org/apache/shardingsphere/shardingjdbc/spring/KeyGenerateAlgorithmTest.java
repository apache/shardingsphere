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

package org.apache.shardingsphere.shardingjdbc.spring;

import static org.junit.Assert.assertEquals;

import org.apache.shardingsphere.shardingjdbc.spring.fixture.IncrementKeyGenerateAlgorithm;
import org.apache.shardingsphere.shardingjdbc.spring.namespace.factorybean.KeyGenerateAlgorithmFactoryBean;
import org.apache.shardingsphere.sharding.spi.keygen.KeyGenerateAlgorithm;
import org.junit.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@ContextConfiguration(locations = "classpath:META-INF/rdb/datasource/dataSource.xml")
public final class KeyGenerateAlgorithmTest extends AbstractJUnit4SpringContextTests {
    
    @Test(expected = BeanCreationException.class)
    public void assertTypelessKeyGenerateAlgorithm() {
        GenericApplicationContext context = (GenericApplicationContext) applicationContext;
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(KeyGenerateAlgorithmFactoryBean.class);
        BeanDefinition beanDefinition = builder.getBeanDefinition();
        context.registerBeanDefinition("typelessAlgorithm", beanDefinition);
        context.getBean("typelessAlgorithm");
    }
    
    @Test
    public void assertKeyGenerateAlgorithm() {
        GenericApplicationContext context = (GenericApplicationContext) applicationContext;
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(KeyGenerateAlgorithmFactoryBean.class).addConstructorArgValue("INCREMENT");
        BeanDefinition beanDefinition = builder.getBeanDefinition();
        context.registerBeanDefinition("incrementAlgorithm", beanDefinition);
        KeyGenerateAlgorithm incrementKeyGenerateAlgorithm = (KeyGenerateAlgorithm) context.getBean("incrementAlgorithm");
        KeyGenerateAlgorithm directIncrementKeyGenerateAlgorithm = new IncrementKeyGenerateAlgorithm();
        assertEquals(incrementKeyGenerateAlgorithm.generateKey(), directIncrementKeyGenerateAlgorithm.generateKey());
    }
}
