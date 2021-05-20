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

package org.apache.shardingsphere.governance.core.registry.service.config.impl;

import lombok.SneakyThrows;
import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SchemaRuleRegistryServiceTest {
    
    private static final String SHARDING_RULE_YAML = "yaml/registryCenter/data-sharding-rule.yaml";
    
    private static final String READWRITE_SPLITTING_RULE_YAML = "yaml/registryCenter/data-readwrite-splitting-rule.yaml";
    
    private static final String DB_DISCOVERY_RULE_YAML = "yaml/registryCenter/data-database-discovery-rule.yaml";
    
    private static final String ENCRYPT_RULE_YAML = "yaml/registryCenter/data-encrypt-rule.yaml";
    
    private static final String SHADOW_RULE_YAML = "yaml/registryCenter/data-shadow-rule.yaml";
    
    private static final String SHARDING_AND_ENCRYPT_RULE_YAML = "yaml/registryCenter/data-sharding-encrypt-rule.yaml";
    
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
    public void assertLoadShardingAndEncryptRuleConfigurations() {
        when(registryCenterRepository.get("/metadata/sharding_db/rules")).thenReturn(readYAML(SHARDING_AND_ENCRYPT_RULE_YAML));
        Collection<RuleConfiguration> ruleConfigurations = schemaRuleRegistryService.load("sharding_db");
        assertThat(ruleConfigurations.size(), is(2));
        for (RuleConfiguration each : ruleConfigurations) {
            if (each instanceof ShardingRuleConfiguration) {
                ShardingRuleConfiguration shardingRuleConfig = (ShardingRuleConfiguration) each;
                assertThat(shardingRuleConfig.getTables().size(), is(1));
                assertThat(shardingRuleConfig.getTables().iterator().next().getLogicTable(), is("t_order"));
            } else if (each instanceof EncryptRuleConfiguration) {
                EncryptRuleConfiguration encryptRuleConfig = (EncryptRuleConfiguration) each;
                assertThat(encryptRuleConfig.getEncryptors().size(), is(2));
                ShardingSphereAlgorithmConfiguration encryptAlgorithmConfig = encryptRuleConfig.getEncryptors().get("aes_encryptor");
                assertThat(encryptAlgorithmConfig.getType(), is("AES"));
                assertThat(encryptAlgorithmConfig.getProps().get("aes-key-value").toString(), is("123456abcd"));
            }
        }
    }
    
    @Test
    public void assertLoadShardingRuleConfiguration() {
        when(registryCenterRepository.get("/metadata/sharding_db/rules")).thenReturn(readYAML(SHARDING_RULE_YAML));
        Collection<RuleConfiguration> actual = schemaRuleRegistryService.load("sharding_db");
        assertThat(actual.size(), is(1));
        ShardingRuleConfiguration actualShardingRuleConfig = (ShardingRuleConfiguration) actual.iterator().next();
        assertThat(actualShardingRuleConfig.getTables().size(), is(1));
        assertThat(actualShardingRuleConfig.getTables().iterator().next().getLogicTable(), is("t_order"));
    }
    
    @Test
    public void assertLoadReadwriteSplittingRuleConfiguration() {
        when(registryCenterRepository.get("/metadata/sharding_db/rules")).thenReturn(readYAML(READWRITE_SPLITTING_RULE_YAML));
        Collection<RuleConfiguration> actual = schemaRuleRegistryService.load("sharding_db");
        ReadwriteSplittingRuleConfiguration config = (ReadwriteSplittingRuleConfiguration) actual.iterator().next();
        assertThat(config.getDataSources().size(), is(1));
        assertThat(config.getDataSources().iterator().next().getWriteDataSourceName(), is("write_ds"));
        assertThat(config.getDataSources().iterator().next().getReadDataSourceNames().size(), is(2));
    }
    
    @Test
    public void assertLoadDatabaseDiscoveryRuleConfiguration() {
        when(registryCenterRepository.get("/metadata/sharding_db/rules")).thenReturn(readYAML(DB_DISCOVERY_RULE_YAML));
        Collection<RuleConfiguration> actual = schemaRuleRegistryService.load("sharding_db");
        DatabaseDiscoveryRuleConfiguration config = (DatabaseDiscoveryRuleConfiguration) actual.iterator().next();
        assertThat(config.getDataSources().size(), is(1));
        assertThat(config.getDataSources().iterator().next().getDataSourceNames().size(), is(3));
    }
    
    @Test
    public void assertLoadEncryptRuleConfiguration() {
        when(registryCenterRepository.get("/metadata/sharding_db/rules")).thenReturn(readYAML(ENCRYPT_RULE_YAML));
        EncryptRuleConfiguration actual = (EncryptRuleConfiguration) schemaRuleRegistryService.load("sharding_db").iterator().next();
        assertThat(actual.getEncryptors().size(), is(1));
        ShardingSphereAlgorithmConfiguration encryptAlgorithmConfig = actual.getEncryptors().get("order_encryptor");
        assertThat(encryptAlgorithmConfig.getType(), is("AES"));
        assertThat(encryptAlgorithmConfig.getProps().get("aes-key-value").toString(), is("123456"));
    }
    
    @Test
    public void assertLoadShadowRuleConfiguration() {
        when(registryCenterRepository.get("/metadata/sharding_db/rules")).thenReturn(readYAML(SHADOW_RULE_YAML));
        ShadowRuleConfiguration actual = (ShadowRuleConfiguration) schemaRuleRegistryService.load("sharding_db").iterator().next();
        assertThat(actual.getSourceDataSourceNames(), is(Arrays.asList("ds", "ds1")));
        assertThat(actual.getShadowDataSourceNames(), is(Arrays.asList("shadow_ds", "shadow_ds1")));
        assertThat(actual.getColumn(), is("shadow"));
    }
    
    @SneakyThrows({IOException.class, URISyntaxException.class})
    private String readYAML(final String yamlFile) {
        return Files.readAllLines(Paths.get(ClassLoader.getSystemResource(yamlFile).toURI()))
                .stream().filter(each -> !each.startsWith("#")).map(each -> each + System.lineSeparator()).collect(Collectors.joining());
    }
}
