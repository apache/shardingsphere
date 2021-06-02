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

package org.apache.shardingsphere.governance.core.registry.cache.subscriber;

import org.apache.shardingsphere.governance.core.registry.cache.RegistryCacheManager;
import org.apache.shardingsphere.governance.core.registry.config.service.impl.SchemaRuleRegistryService;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;

@RunWith(MockitoJUnitRunner.class)
public final class ScalingRegistrySubscriberTest {
    
    @Mock
    private RegistryCenterRepository registryCenterRepository;
    
    @Mock
    private SchemaRuleRegistryService schemaRuleService;
    
    @Mock
    private RegistryCacheManager registryCacheManager;
    
    private ScalingRegistrySubscriber scalingRegistrySubscriber;
    
    @Before
    public void setUp() throws ReflectiveOperationException {
        scalingRegistrySubscriber = new ScalingRegistrySubscriber(registryCenterRepository, schemaRuleService);
    }
    
    @Test
    public void assertSwitchRuleConfiguration() throws ReflectiveOperationException {
        Field field = ScalingRegistrySubscriber.class.getDeclaredField("registryCacheManager");
        field.setAccessible(true);
        field.set(scalingRegistrySubscriber, registryCacheManager);
        // Move to scaling module
//        when(registryCacheManager.loadCache(anyString(), eq("testCacheId"))).thenReturn(readYAML());
//        SwitchRuleConfigurationEvent event = new SwitchRuleConfigurationEvent("sharding_db", "testCacheId");
//        scalingRegistrySubscriber.switchRuleConfiguration(event);
        // TODO finish verify
    }
    
    @Test
    public void assertCacheRuleConfiguration() {
        // TODO finish test case
    }
    
//    @SneakyThrows({IOException.class, URISyntaxException.class})
//    private String readYAML() {
//        return Files.readAllLines(Paths.get(ClassLoader.getSystemResource("yaml/regcenter/data-schema-rule.yaml").toURI()))
//                .stream().filter(each -> !each.startsWith("#")).map(each -> each + System.lineSeparator()).collect(Collectors.joining());
//    }
}
