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

import lombok.SneakyThrows;
import org.apache.shardingsphere.authority.api.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class GlobalRuleRegistryServiceTest {
    
    private static final String YAML_DATA = "yaml/regcenter/data-global-rule.yaml";
    
    @Mock
    private RegistryCenterRepository registryCenterRepository;
    
    private GlobalRuleRegistryService globalRuleRegistryService;
    
    @Before
    public void setUp() throws ReflectiveOperationException {
        globalRuleRegistryService = new GlobalRuleRegistryService(registryCenterRepository);
        Field field = globalRuleRegistryService.getClass().getDeclaredField("repository");
        field.setAccessible(true);
        field.set(globalRuleRegistryService, registryCenterRepository);
    }
    
    @Test
    public void assertLoad() {
        when(registryCenterRepository.get("/rules")).thenReturn(readYAML(YAML_DATA));
        Collection<RuleConfiguration> globalRuleConfigs = globalRuleRegistryService.load();
        assertFalse(globalRuleConfigs.isEmpty());
        Collection<ShardingSphereUser> users = globalRuleConfigs.stream().filter(each -> each instanceof AuthorityRuleConfiguration)
                .flatMap(each -> ((AuthorityRuleConfiguration) each).getUsers().stream()).collect(Collectors.toList());
        Optional<ShardingSphereUser> user = users.stream().filter(each -> each.getGrantee().equals(new Grantee("root", ""))).findFirst();
        assertTrue(user.isPresent());
        assertThat(user.get().getPassword(), is("root"));
        Collection<ShardingSphereAlgorithmConfiguration> providers = globalRuleConfigs.stream()
                .filter(each -> each instanceof AuthorityRuleConfiguration && Objects.nonNull(((AuthorityRuleConfiguration) each).getProvider()))
                .map(each -> ((AuthorityRuleConfiguration) each).getProvider()).collect(Collectors.toList());
        assertFalse(providers.isEmpty());
        Optional<ShardingSphereAlgorithmConfiguration> nativeProvider = providers.stream().filter(each -> "NATIVE".equals(each.getType())).findFirst();
        assertTrue(nativeProvider.isPresent());
    }
    
    @SneakyThrows({IOException.class, URISyntaxException.class})
    private String readYAML(final String yamlFile) {
        return Files.readAllLines(Paths.get(ClassLoader.getSystemResource(yamlFile).toURI()))
                .stream().filter(each -> !each.startsWith("#")).map(each -> each + System.lineSeparator()).collect(Collectors.joining());
    }
}
