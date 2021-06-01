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

package org.apache.shardingsphere.governance.core.registry.config.service.impl;

import org.apache.shardingsphere.governance.core.registry.config.event.rule.RuleConfigurationsAlteredEvent;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.Collections;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class SchemaRuleRegistryServiceTest {
    
    @Mock
    private RegistryCenterRepository registryCenterRepository;
    
    private SchemaRuleRegistryService schemaRuleRegistryService;
    
    @Before
    public void setUp() throws ReflectiveOperationException {
        schemaRuleRegistryService = new SchemaRuleRegistryService(registryCenterRepository);
        Field field = schemaRuleRegistryService.getClass().getDeclaredField("repository");
        field.setAccessible(true);
        field.set(schemaRuleRegistryService, registryCenterRepository);
    }
    
    @Test
    public void assertLoadWithoutExistedNode() {
        assertTrue(schemaRuleRegistryService.load("foo_db").isEmpty());
    }
    
    @Test
    public void assertLoadWithExistedNode() {
        // TODO use RuleConfigurationFixture instead of ShardingRuleConfiguration for test case
//        when(registryCenterRepository.get("/metadata/foo_db/rules")).thenReturn(readYAML());
//        Collection<RuleConfiguration> actual = schemaRuleRegistryService.load("foo_db");
//        assertThat(actual.size(), is(1));
//        ShardingRuleConfiguration actualShardingRuleConfig = (ShardingRuleConfiguration) actual.iterator().next();
//        assertThat(actualShardingRuleConfig.getTables().size(), is(1));
//        assertThat(actualShardingRuleConfig.getTables().iterator().next().getLogicTable(), is("t_order"));
    }
    
//    @SneakyThrows({IOException.class, URISyntaxException.class})
//    private String readYAML() {
//        return Files.readAllLines(Paths.get(ClassLoader.getSystemResource("yaml/regcenter/data-schema-rule.yaml").toURI()))
//                .stream().filter(each -> !each.startsWith("#")).map(each -> each + System.lineSeparator()).collect(Collectors.joining());
//    }
    
    @Test
    public void assertUpdate() {
        RuleConfigurationsAlteredEvent event = new RuleConfigurationsAlteredEvent("foo_db", Collections.emptyList());
        schemaRuleRegistryService.update(event);
        verify(registryCenterRepository).persist("/metadata/foo_db/rules", "!!map []\n");
    }
}
