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

package org.apache.shardingsphere.orchestration.core.configcenter.listener;

import lombok.SneakyThrows;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.algorithm.EncryptAlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.masterslave.api.config.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.orchestration.center.ConfigCenterRepository;
import org.apache.shardingsphere.orchestration.center.listener.DataChangedEvent;
import org.apache.shardingsphere.orchestration.center.listener.DataChangedEvent.ChangedType;
import org.apache.shardingsphere.orchestration.core.common.event.DataSourceChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.IgnoredShardingOrchestrationEvent;
import org.apache.shardingsphere.orchestration.core.common.event.RuleConfigurationsChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.SchemaAddedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.SchemaDeletedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.ShardingOrchestrationEvent;
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
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SchemaChangedListenerTest {
    
    private static final String DATA_SOURCE_FILE = "yaml/data-source.yaml";
    
    private static final String SHARDING_RULE_FILE = "yaml/sharding-rule.yaml";
    
    private static final String MASTER_SLAVE_RULE_FILE = "yaml/master-slave-rule.yaml";
    
    private static final String ENCRYPT_RULE_FILE = "yaml/encrypt-rule.yaml";
    
    private SchemaChangedListener schemaChangedListener;
    
    @Mock
    private ConfigCenterRepository configCenterRepository;
    
    @Before
    public void setUp() {
        schemaChangedListener = new SchemaChangedListener("test", configCenterRepository, Arrays.asList("sharding_db", "masterslave_db", "encrypt_db"));
    }
    
    @Test
    public void assertCreateIgnoredEvent() {
        assertThat(schemaChangedListener.createShardingOrchestrationEvent(new DataChangedEvent("/test/config/schema/logic_db", "test", ChangedType.UPDATED)), 
                instanceOf(IgnoredShardingOrchestrationEvent.class));
        assertThat(schemaChangedListener.createShardingOrchestrationEvent(new DataChangedEvent("/test/config/schema/logic_db/rule", "test", ChangedType.IGNORED)), 
                instanceOf(IgnoredShardingOrchestrationEvent.class));
    }
    
    @Test
    public void assertCreateDataSourceChangedEventForExistedSchema() {
        String dataSource = readYAML(DATA_SOURCE_FILE);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/test/config/schema/sharding_db/datasource", dataSource, ChangedType.UPDATED);
        ShardingOrchestrationEvent actual = schemaChangedListener.createShardingOrchestrationEvent(dataChangedEvent);
        assertThat(actual, instanceOf(DataSourceChangedEvent.class));
        assertThat(((DataSourceChangedEvent) actual).getShardingSchemaName(), is("sharding_db"));
    }
    
    @Test
    public void assertCreateRuleConfigurationsChangedEventForExistedSchema() {
        String shardingRule = readYAML(SHARDING_RULE_FILE);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/test/config/schema/sharding_db/rule", shardingRule, ChangedType.UPDATED);
        ShardingOrchestrationEvent actual = schemaChangedListener.createShardingOrchestrationEvent(dataChangedEvent);
        assertThat(actual, instanceOf(RuleConfigurationsChangedEvent.class));
        assertThat(((RuleConfigurationsChangedEvent) actual).getShardingSchemaName(), is("sharding_db"));
        Collection<RuleConfiguration> ruleConfigurations = ((RuleConfigurationsChangedEvent) actual).getRuleConfigurations();
        assertThat(ruleConfigurations.size(), is(1));
        assertThat(((ShardingRuleConfiguration) ruleConfigurations.iterator().next()).getTables().size(), is(1));
    }
    
    @Test
    public void assertCreateMasterSlaveRuleChangedEventForExistedSchema() {
        String masterSlaveRule = readYAML(MASTER_SLAVE_RULE_FILE);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/test/config/schema/masterslave_db/rule", masterSlaveRule, ChangedType.UPDATED);
        ShardingOrchestrationEvent actual = schemaChangedListener.createShardingOrchestrationEvent(dataChangedEvent);
        assertThat(actual, instanceOf(RuleConfigurationsChangedEvent.class));
        RuleConfigurationsChangedEvent event = (RuleConfigurationsChangedEvent) actual;
        assertThat(event.getShardingSchemaName(), is("masterslave_db"));
        assertThat(event.getRuleConfigurations().iterator().next(), instanceOf(MasterSlaveRuleConfiguration.class));
        MasterSlaveRuleConfiguration masterSlaveRuleConfiguration = (MasterSlaveRuleConfiguration) event.getRuleConfigurations().iterator().next();
        assertThat(masterSlaveRuleConfiguration.getDataSources().iterator().next().getMasterDataSourceName(), is("master_ds"));
    }
    
    @Test
    public void assertCreateEncryptRuleChangedEventForExistedSchema() {
        String encryptRule = readYAML(ENCRYPT_RULE_FILE);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/test/config/schema/encrypt_db/rule", encryptRule, ChangedType.UPDATED);
        ShardingOrchestrationEvent actual = schemaChangedListener.createShardingOrchestrationEvent(dataChangedEvent);
        assertThat(actual, instanceOf(RuleConfigurationsChangedEvent.class));
        RuleConfigurationsChangedEvent event = (RuleConfigurationsChangedEvent) actual;
        assertThat(event.getShardingSchemaName(), is("encrypt_db"));
        assertThat(event.getRuleConfigurations().iterator().next(), instanceOf(EncryptRuleConfiguration.class));
        EncryptRuleConfiguration encryptRuleConfiguration = (EncryptRuleConfiguration) event.getRuleConfigurations().iterator().next();
        assertThat(encryptRuleConfiguration.getEncryptors().size(), is(1));
        EncryptAlgorithmConfiguration encryptAlgorithmConfiguration = encryptRuleConfiguration.getEncryptors().get("order_encryptor");
        assertThat(encryptAlgorithmConfiguration.getType(), is("AES"));
        assertThat(encryptAlgorithmConfiguration.getProperties().get("aes.key.value").toString(), is("123456"));
    }
    
    @Test
    public void assertCreateIgnoredShardingOrchestrationEventForNewSchema() {
        when(configCenterRepository.get("/test/config/schema/logic_db/datasource")).thenReturn("");
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/test/config/schema/logic_db/rule", "rule", ChangedType.UPDATED);
        ShardingOrchestrationEvent actual = schemaChangedListener.createShardingOrchestrationEvent(dataChangedEvent);
        assertThat(actual, instanceOf(IgnoredShardingOrchestrationEvent.class));
    }
    
    @Test
    public void assertCreateShardingSchemaAddedEventForNewSchema() {
        String shardingRule = readYAML(SHARDING_RULE_FILE);
        String dataSource = readYAML(DATA_SOURCE_FILE);
        when(configCenterRepository.get("/test/config/schema/logic_db/rule")).thenReturn(shardingRule);
        when(configCenterRepository.get("/test/config/schema/logic_db/datasource")).thenReturn(dataSource);
        when(configCenterRepository.get("/test/config/schema/logic_db/rule")).thenReturn(shardingRule);
        when(configCenterRepository.get("/test/config/schema/logic_db/datasource")).thenReturn(dataSource);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/test/config/schema/logic_db/datasource", dataSource, ChangedType.UPDATED);
        ShardingOrchestrationEvent actual = schemaChangedListener.createShardingOrchestrationEvent(dataChangedEvent);
        assertThat(actual, instanceOf(SchemaAddedEvent.class));
        assertThat(((SchemaAddedEvent) actual).getRuleConfigurations().iterator().next(), instanceOf(ShardingRuleConfiguration.class));
    }
    
    @Test
    public void assertCreateMasterSlaveSchemaAddedEventForNewSchema() {
        String dataSource = readYAML(DATA_SOURCE_FILE);
        String masterSlaveRule = readYAML(MASTER_SLAVE_RULE_FILE);
        when(configCenterRepository.get("/test/config/schema/logic_db/rule")).thenReturn(masterSlaveRule);
        when(configCenterRepository.get("/test/config/schema/logic_db/datasource")).thenReturn(dataSource);
        when(configCenterRepository.get("/test/config/schema/logic_db/rule")).thenReturn(masterSlaveRule);
        when(configCenterRepository.get("/test/config/schema/logic_db/datasource")).thenReturn(dataSource);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/test/config/schema/logic_db/datasource", dataSource, ChangedType.UPDATED);
        ShardingOrchestrationEvent actual = schemaChangedListener.createShardingOrchestrationEvent(dataChangedEvent);
        assertThat(actual, instanceOf(SchemaAddedEvent.class));
        assertThat(((SchemaAddedEvent) actual).getRuleConfigurations().iterator().next(), instanceOf(MasterSlaveRuleConfiguration.class));
    }
    
    @Test
    public void assertCreateEncryptSchemaAddedEventForNewSchema() {
        String dataSource = readYAML(DATA_SOURCE_FILE);
        String encryptRule = readYAML(ENCRYPT_RULE_FILE);
        when(configCenterRepository.get("/test/config/schema/logic_db/rule")).thenReturn(encryptRule);
        when(configCenterRepository.get("/test/config/schema/logic_db/datasource")).thenReturn(dataSource);
        when(configCenterRepository.get("/test/config/schema/logic_db/rule")).thenReturn(encryptRule);
        when(configCenterRepository.get("/test/config/schema/logic_db/datasource")).thenReturn(dataSource);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/test/config/schema/logic_db/datasource", dataSource, ChangedType.UPDATED);
        ShardingOrchestrationEvent actual = schemaChangedListener.createShardingOrchestrationEvent(dataChangedEvent);
        assertThat(actual, instanceOf(SchemaAddedEvent.class));
        assertThat(((SchemaAddedEvent) actual).getRuleConfigurations().iterator().next(), instanceOf(EncryptRuleConfiguration.class));
    }
    
    @Test
    public void assertCreateSchemaDeletedEventForNewSchema() {
        String dataSource = readYAML(DATA_SOURCE_FILE);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/test/config/schema/logic_db/datasource", dataSource, ChangedType.DELETED);
        ShardingOrchestrationEvent actual = schemaChangedListener.createShardingOrchestrationEvent(dataChangedEvent);
        assertThat(actual, instanceOf(SchemaDeletedEvent.class));
        assertThat(((SchemaDeletedEvent) actual).getShardingSchemaName(), is("logic_db"));
    }
    
    @Test
    public void assertCreateWithInvalidNodeChangedEvent() {
        String dataSource = readYAML(DATA_SOURCE_FILE);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/test/config/schema/logic_db", dataSource, ChangedType.DELETED);
        ShardingOrchestrationEvent actual = schemaChangedListener.createShardingOrchestrationEvent(dataChangedEvent);
        assertThat(actual, instanceOf(IgnoredShardingOrchestrationEvent.class));
    }
    
    @Test
    public void assertCreateWithNullShardingSchemaName() {
        String dataSource = readYAML(DATA_SOURCE_FILE);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/test/config/schema/datasource", dataSource, ChangedType.DELETED);
        ShardingOrchestrationEvent actual = schemaChangedListener.createShardingOrchestrationEvent(dataChangedEvent);
        assertThat(actual, instanceOf(IgnoredShardingOrchestrationEvent.class));
    }
    
    @Test
    public void assertCreateAddedEventWithEncryptRuleConfigurationForNewSchema() {
        String dataSource = readYAML(DATA_SOURCE_FILE);
        String encryptRule = readYAML(ENCRYPT_RULE_FILE);
        when(configCenterRepository.get("/test/config/schema/logic_db/rule")).thenReturn(encryptRule);
        when(configCenterRepository.get("/test/config/schema/logic_db/datasource")).thenReturn(dataSource);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/test/config/schema/logic_db/rule", encryptRule, ChangedType.UPDATED);
        ShardingOrchestrationEvent actual = schemaChangedListener.createShardingOrchestrationEvent(dataChangedEvent);
        assertThat(actual, instanceOf(SchemaAddedEvent.class));
        assertThat(((SchemaAddedEvent) actual).getRuleConfigurations().iterator().next(), instanceOf(EncryptRuleConfiguration.class));
    }
    
    @Test
    public void assertCreateAddedEventWithShardingRuleConfigurationForNewSchema() {
        String dataSource = readYAML(DATA_SOURCE_FILE);
        String shardingRule = readYAML(SHARDING_RULE_FILE);
        when(configCenterRepository.get("/test/config/schema/logic_db/rule")).thenReturn(shardingRule);
        when(configCenterRepository.get("/test/config/schema/logic_db/datasource")).thenReturn(dataSource);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/test/config/schema/logic_db/rule", shardingRule, ChangedType.UPDATED);
        ShardingOrchestrationEvent actual = schemaChangedListener.createShardingOrchestrationEvent(dataChangedEvent);
        assertThat(actual, instanceOf(SchemaAddedEvent.class));
        assertThat(((SchemaAddedEvent) actual).getRuleConfigurations().iterator().next(), instanceOf(ShardingRuleConfiguration.class));
    }
    
    @Test
    public void assertCreateAddedEventWithMasterSlaveRuleConfigurationForNewSchema() {
        String dataSource = readYAML(DATA_SOURCE_FILE);
        String masterSlaveRule = readYAML(MASTER_SLAVE_RULE_FILE);
        when(configCenterRepository.get("/test/config/schema/logic_db/rule")).thenReturn(masterSlaveRule);
        when(configCenterRepository.get("/test/config/schema/logic_db/datasource")).thenReturn(dataSource);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/test/config/schema/logic_db/rule", masterSlaveRule, ChangedType.UPDATED);
        ShardingOrchestrationEvent actual = schemaChangedListener.createShardingOrchestrationEvent(dataChangedEvent);
        assertThat(actual, instanceOf(SchemaAddedEvent.class));
        assertThat(((SchemaAddedEvent) actual).getRuleConfigurations().iterator().next(), instanceOf(MasterSlaveRuleConfiguration.class));
    }
    
    @SneakyThrows
    private String readYAML(final String yamlFile) {
        return Files.readAllLines(Paths.get(ClassLoader.getSystemResource(yamlFile).toURI())).stream().map(each -> each + System.lineSeparator()).collect(Collectors.joining());
    }
}
