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

package org.apache.shardingsphere.orchestration.core.config.listener;

import lombok.SneakyThrows;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.masterslave.api.config.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.orchestration.core.common.event.DataSourceChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.OrchestrationEvent;
import org.apache.shardingsphere.orchestration.core.common.event.RuleConfigurationsChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.schema.SchemaAddedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.schema.SchemaDeletedEvent;
import org.apache.shardingsphere.orchestration.repository.api.ConfigurationRepository;
import org.apache.shardingsphere.orchestration.repository.api.listener.DataChangedEvent;
import org.apache.shardingsphere.orchestration.repository.api.listener.DataChangedEvent.ChangedType;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SchemaChangedListenerTest {
    
    private static final String DATA_SOURCE_FILE = "yaml/data-source.yaml";
    
    private static final String SHARDING_RULE_FILE = "yaml/sharding-rule.yaml";
    
    private static final String MASTER_SLAVE_RULE_FILE = "yaml/master-slave-rule.yaml";
    
    private static final String ENCRYPT_RULE_FILE = "yaml/encrypt-rule.yaml";
    
    private SchemaChangedListener schemaChangedListener;
    
    @Mock
    private ConfigurationRepository configurationRepository;
    
    @Before
    public void setUp() {
        schemaChangedListener = new SchemaChangedListener(configurationRepository, Arrays.asList("sharding_db", "masterslave_db", "encrypt_db"));
    }
    
    @Test
    public void assertCreateIgnoredEvent() {
        assertFalse(schemaChangedListener.createOrchestrationEvent(new DataChangedEvent("/config/schema/encrypt_db", "test", ChangedType.UPDATED)).isPresent());
        assertFalse(schemaChangedListener.createOrchestrationEvent(new DataChangedEvent("/config/schema/encrypt_db/rule", "test", ChangedType.IGNORED)).isPresent());
    }
    
    @Test
    public void assertCreateDataSourceChangedEventForExistedSchema() {
        String dataSource = readYAML(DATA_SOURCE_FILE);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/config/schema/sharding_db/datasource", dataSource, ChangedType.UPDATED);
        Optional<OrchestrationEvent> actual = schemaChangedListener.createOrchestrationEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(DataSourceChangedEvent.class));
        assertThat(((DataSourceChangedEvent) actual.get()).getShardingSchemaName(), is("sharding_db"));
    }
    
    @Test
    public void assertCreateRuleConfigurationsChangedEventForExistedSchema() {
        String shardingRule = readYAML(SHARDING_RULE_FILE);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/config/schema/sharding_db/rule", shardingRule, ChangedType.UPDATED);
        Optional<OrchestrationEvent> actual = schemaChangedListener.createOrchestrationEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(((RuleConfigurationsChangedEvent) actual.get()).getShardingSchemaName(), is("sharding_db"));
        Collection<RuleConfiguration> ruleConfigurations = ((RuleConfigurationsChangedEvent) actual.get()).getRuleConfigurations();
        assertThat(ruleConfigurations.size(), is(1));
        assertThat(((ShardingRuleConfiguration) ruleConfigurations.iterator().next()).getTables().size(), is(1));
    }
    
    @Test
    public void assertCreateMasterSlaveRuleChangedEventForExistedSchema() {
        String masterSlaveRule = readYAML(MASTER_SLAVE_RULE_FILE);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/config/schema/masterslave_db/rule", masterSlaveRule, ChangedType.UPDATED);
        Optional<OrchestrationEvent> actual = schemaChangedListener.createOrchestrationEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        RuleConfigurationsChangedEvent event = (RuleConfigurationsChangedEvent) actual.get();
        assertThat(event.getShardingSchemaName(), is("masterslave_db"));
        assertThat(event.getRuleConfigurations().iterator().next(), instanceOf(MasterSlaveRuleConfiguration.class));
        MasterSlaveRuleConfiguration masterSlaveRuleConfig = (MasterSlaveRuleConfiguration) event.getRuleConfigurations().iterator().next();
        assertThat(masterSlaveRuleConfig.getDataSources().iterator().next().getMasterDataSourceName(), is("master_ds"));
    }
    
    @Test
    public void assertCreateEncryptRuleChangedEventForExistedSchema() {
        String encryptRule = readYAML(ENCRYPT_RULE_FILE);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/config/schema/encrypt_db/rule", encryptRule, ChangedType.UPDATED);
        Optional<OrchestrationEvent> actual = schemaChangedListener.createOrchestrationEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        RuleConfigurationsChangedEvent event = (RuleConfigurationsChangedEvent) actual.get();
        assertThat(event.getShardingSchemaName(), is("encrypt_db"));
        assertThat(event.getRuleConfigurations().iterator().next(), instanceOf(EncryptRuleConfiguration.class));
        EncryptRuleConfiguration encryptRuleConfig = (EncryptRuleConfiguration) event.getRuleConfigurations().iterator().next();
        assertThat(encryptRuleConfig.getEncryptors().size(), is(1));
        ShardingSphereAlgorithmConfiguration encryptAlgorithmConfig = encryptRuleConfig.getEncryptors().get("order_encryptor");
        assertThat(encryptAlgorithmConfig.getType(), is("AES"));
        assertThat(encryptAlgorithmConfig.getProps().get("aes.key.value"), is(123456));
    }
    
    @Test
    public void assertCreateIgnoredOrchestrationEventForNewSchema() {
        when(configurationRepository.get("/config/schema/logic_db/datasource")).thenReturn("");
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/config/schema/logic_db/rule", "rule", ChangedType.UPDATED);
        Optional<OrchestrationEvent> actual = schemaChangedListener.createOrchestrationEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(SchemaAddedEvent.class));
    }
    
    @Test
    public void assertCreateShardingSchemaAddedEventForNewSchema() {
        String shardingRule = readYAML(SHARDING_RULE_FILE);
        String dataSource = readYAML(DATA_SOURCE_FILE);
        when(configurationRepository.get("/config/schema/logic_db/rule")).thenReturn(shardingRule);
        when(configurationRepository.get("/config/schema/logic_db/datasource")).thenReturn(dataSource);
        when(configurationRepository.get("/config/schema/logic_db/rule")).thenReturn(shardingRule);
        when(configurationRepository.get("/config/schema/logic_db/datasource")).thenReturn(dataSource);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/config/schema/logic_db/datasource", dataSource, ChangedType.UPDATED);
        Optional<OrchestrationEvent> actual = schemaChangedListener.createOrchestrationEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(((SchemaAddedEvent) actual.get()).getRuleConfigurations().iterator().next(), instanceOf(ShardingRuleConfiguration.class));
    }
    
    @Test
    public void assertCreateMasterSlaveSchemaAddedEventForNewSchema() {
        String dataSource = readYAML(DATA_SOURCE_FILE);
        String masterSlaveRule = readYAML(MASTER_SLAVE_RULE_FILE);
        when(configurationRepository.get("/config/schema/logic_db/rule")).thenReturn(masterSlaveRule);
        when(configurationRepository.get("/config/schema/logic_db/datasource")).thenReturn(dataSource);
        when(configurationRepository.get("/config/schema/logic_db/rule")).thenReturn(masterSlaveRule);
        when(configurationRepository.get("/config/schema/logic_db/datasource")).thenReturn(dataSource);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/config/schema/logic_db/datasource", dataSource, ChangedType.UPDATED);
        Optional<OrchestrationEvent> actual = schemaChangedListener.createOrchestrationEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(((SchemaAddedEvent) actual.get()).getRuleConfigurations().iterator().next(), instanceOf(MasterSlaveRuleConfiguration.class));
    }
    
    @Test
    public void assertCreateEncryptSchemaAddedEventForNewSchema() {
        String dataSource = readYAML(DATA_SOURCE_FILE);
        String encryptRule = readYAML(ENCRYPT_RULE_FILE);
        when(configurationRepository.get("/config/schema/logic_db/rule")).thenReturn(encryptRule);
        when(configurationRepository.get("/config/schema/logic_db/datasource")).thenReturn(dataSource);
        when(configurationRepository.get("/config/schema/logic_db/rule")).thenReturn(encryptRule);
        when(configurationRepository.get("/config/schema/logic_db/datasource")).thenReturn(dataSource);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/config/schema/logic_db/datasource", dataSource, ChangedType.UPDATED);
        Optional<OrchestrationEvent> actual = schemaChangedListener.createOrchestrationEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(((SchemaAddedEvent) actual.get()).getRuleConfigurations().iterator().next(), instanceOf(EncryptRuleConfiguration.class));
    }
    
    @Test
    public void assertCreateSchemaDeletedEventForNewSchema() {
        String dataSource = readYAML(DATA_SOURCE_FILE);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/config/schema/logic_db/datasource", dataSource, ChangedType.DELETED);
        Optional<OrchestrationEvent> actual = schemaChangedListener.createOrchestrationEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(((SchemaDeletedEvent) actual.get()).getSchemaName(), is("logic_db"));
    }
    
    @Test
    public void assertCreateWithSchemaDeletedEvent() {
        String dataSource = readYAML(DATA_SOURCE_FILE);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/config/schema/logic_db", dataSource, ChangedType.DELETED);
        Optional<OrchestrationEvent> actual = schemaChangedListener.createOrchestrationEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(SchemaDeletedEvent.class));
    }
    
    @Test
    public void assertCreateWithSchemaDeletedEventWithDataSourceNode() {
        String dataSource = readYAML(DATA_SOURCE_FILE);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/config/schema/datasource", dataSource, ChangedType.DELETED);
        Optional<OrchestrationEvent> actual = schemaChangedListener.createOrchestrationEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(SchemaDeletedEvent.class));
    }
    
    @Test
    public void assertCreateAddedEventWithEncryptRuleConfigurationForNewSchema() {
        String dataSource = readYAML(DATA_SOURCE_FILE);
        String encryptRule = readYAML(ENCRYPT_RULE_FILE);
        when(configurationRepository.get("/config/schema/logic_db/rule")).thenReturn(encryptRule);
        when(configurationRepository.get("/config/schema/logic_db/datasource")).thenReturn(dataSource);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/config/schema/logic_db/rule", encryptRule, ChangedType.UPDATED);
        Optional<OrchestrationEvent> actual = schemaChangedListener.createOrchestrationEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(((SchemaAddedEvent) actual.get()).getRuleConfigurations().iterator().next(), instanceOf(EncryptRuleConfiguration.class));
    }
    
    @Test
    public void assertCreateAddedEventWithShardingRuleConfigurationForNewSchema() {
        String dataSource = readYAML(DATA_SOURCE_FILE);
        String shardingRule = readYAML(SHARDING_RULE_FILE);
        when(configurationRepository.get("/config/schema/logic_db/rule")).thenReturn(shardingRule);
        when(configurationRepository.get("/config/schema/logic_db/datasource")).thenReturn(dataSource);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/config/schema/logic_db/rule", shardingRule, ChangedType.UPDATED);
        Optional<OrchestrationEvent> actual = schemaChangedListener.createOrchestrationEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(((SchemaAddedEvent) actual.get()).getRuleConfigurations().iterator().next(), instanceOf(ShardingRuleConfiguration.class));
    }
    
    @Test
    public void assertCreateAddedEventWithMasterSlaveRuleConfigurationForNewSchema() {
        String dataSource = readYAML(DATA_SOURCE_FILE);
        String masterSlaveRule = readYAML(MASTER_SLAVE_RULE_FILE);
        when(configurationRepository.get("/config/schema/logic_db/rule")).thenReturn(masterSlaveRule);
        when(configurationRepository.get("/config/schema/logic_db/datasource")).thenReturn(dataSource);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/config/schema/logic_db/rule", masterSlaveRule, ChangedType.UPDATED);
        Optional<OrchestrationEvent> actual = schemaChangedListener.createOrchestrationEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(((SchemaAddedEvent) actual.get()).getRuleConfigurations().iterator().next(), instanceOf(MasterSlaveRuleConfiguration.class));
    }
    
    @Test
    public void assertCreateSchemaNamesUpdatedEventForAdd() {
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/config/schema", "sharding_db,masterslave_db,encrypt_db,shadow_db", ChangedType.UPDATED);
        Optional<OrchestrationEvent> actual = schemaChangedListener.createOrchestrationEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(((SchemaAddedEvent) actual.get()).getSchemaName(), is("shadow_db"));
    }
    
    @Test
    public void assertCreateSchemaNamesUpdatedEventForDelete() {
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/config/schema", "sharding_db,masterslave_db", ChangedType.UPDATED);
        Optional<OrchestrationEvent> actual = schemaChangedListener.createOrchestrationEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(((SchemaDeletedEvent) actual.get()).getSchemaName(), is("encrypt_db"));
    }
    
    @Test
    public void assertCreateSchemaNamesUpdatedEventForIgnore() {
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/config/schema", "sharding_db,masterslave_db,encrypt_db", ChangedType.UPDATED);
        assertFalse(schemaChangedListener.createOrchestrationEvent(dataChangedEvent).isPresent());
    }
    
    @Test
    public void assertCreateSchemaNameAddEvent() {
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/config/schema/shadow_db", "", ChangedType.ADDED);
        Optional<OrchestrationEvent> actual = schemaChangedListener.createOrchestrationEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(((SchemaAddedEvent) actual.get()).getSchemaName(), is("shadow_db"));
    }
    
    @SneakyThrows
    private String readYAML(final String yamlFile) {
        return Files.readAllLines(Paths.get(ClassLoader.getSystemResource(yamlFile).toURI())).stream().map(each -> each + System.lineSeparator()).collect(Collectors.joining());
    }
}
