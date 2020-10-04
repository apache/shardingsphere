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

package org.apache.shardingsphere.governance.core.config.listener;

import lombok.SneakyThrows;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.governance.core.event.model.GovernanceEvent;
import org.apache.shardingsphere.governance.core.event.model.datasource.DataSourceChangedEvent;
import org.apache.shardingsphere.governance.core.event.model.metadata.MetaDataChangedEvent;
import org.apache.shardingsphere.governance.core.event.model.rule.RuleConfigurationsChangedEvent;
import org.apache.shardingsphere.governance.core.event.model.schema.SchemaAddedEvent;
import org.apache.shardingsphere.governance.core.event.model.schema.SchemaDeletedEvent;
import org.apache.shardingsphere.governance.repository.api.ConfigurationRepository;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent.ChangedType;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.replication.primaryreplica.api.config.PrimaryReplicaReplicationRuleConfiguration;
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

@RunWith(MockitoJUnitRunner.class)
public final class SchemaChangedListenerTest {
    
    private static final String DATA_SOURCE_FILE = "yaml/data-source.yaml";
    
    private static final String SHARDING_RULE_FILE = "yaml/sharding-rule.yaml";
    
    private static final String PRIMARY_REPLICA_REPLICATION_RULE_FILE = "yaml/primary-replica-replication-rule.yaml";
    
    private static final String ENCRYPT_RULE_FILE = "yaml/encrypt-rule.yaml";
    
    private static final String META_DATA_FILE = "yaml/metadata.yaml";
    
    private SchemaChangedListener schemaChangedListener;
    
    @Mock
    private ConfigurationRepository configurationRepository;
    
    @Before
    public void setUp() {
        schemaChangedListener = new SchemaChangedListener(configurationRepository, Arrays.asList("sharding_db", "primary_replica_replication_db", "encrypt_db"));
    }
    
    @Test
    public void assertCreateIgnoredEvent() {
        assertFalse(schemaChangedListener.createGovernanceEvent(new DataChangedEvent("/schemas/encrypt_db", "test", ChangedType.UPDATED)).isPresent());
        assertFalse(schemaChangedListener.createGovernanceEvent(new DataChangedEvent("/schemas/encrypt_db/rule", "test", ChangedType.IGNORED)).isPresent());
    }
    
    @Test
    public void assertCreateDataSourceChangedEventForExistedSchema() {
        String dataSource = readYAML(DATA_SOURCE_FILE);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/schemas/sharding_db/datasource", dataSource, ChangedType.UPDATED);
        Optional<GovernanceEvent> actual = schemaChangedListener.createGovernanceEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(DataSourceChangedEvent.class));
        assertThat(((DataSourceChangedEvent) actual.get()).getSchemaName(), is("sharding_db"));
    }
    
    @Test
    public void assertCreateRuleConfigurationsChangedEventForExistedSchema() {
        String shardingRule = readYAML(SHARDING_RULE_FILE);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/schemas/sharding_db/rule", shardingRule, ChangedType.UPDATED);
        Optional<GovernanceEvent> actual = schemaChangedListener.createGovernanceEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(((RuleConfigurationsChangedEvent) actual.get()).getSchemaName(), is("sharding_db"));
        Collection<RuleConfiguration> ruleConfigs = ((RuleConfigurationsChangedEvent) actual.get()).getRuleConfigurations();
        assertThat(ruleConfigs.size(), is(1));
        assertThat(((ShardingRuleConfiguration) ruleConfigs.iterator().next()).getTables().size(), is(1));
    }
    
    @Test
    public void assertCreatePrimaryReplicaReplicationRuleChangedEventForExistedSchema() {
        String rule = readYAML(PRIMARY_REPLICA_REPLICATION_RULE_FILE);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/schemas/primary_replica_replication_db/rule", rule, ChangedType.UPDATED);
        Optional<GovernanceEvent> actual = schemaChangedListener.createGovernanceEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        RuleConfigurationsChangedEvent event = (RuleConfigurationsChangedEvent) actual.get();
        assertThat(event.getSchemaName(), is("primary_replica_replication_db"));
        assertThat(event.getRuleConfigurations().iterator().next(), instanceOf(PrimaryReplicaReplicationRuleConfiguration.class));
        PrimaryReplicaReplicationRuleConfiguration ruleConfig = (PrimaryReplicaReplicationRuleConfiguration) event.getRuleConfigurations().iterator().next();
        assertThat(ruleConfig.getDataSources().iterator().next().getPrimaryDataSourceName(), is("primary_ds"));
    }
    
    @Test
    public void assertCreateEncryptRuleChangedEventForExistedSchema() {
        String encryptRule = readYAML(ENCRYPT_RULE_FILE);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/schemas/encrypt_db/rule", encryptRule, ChangedType.UPDATED);
        Optional<GovernanceEvent> actual = schemaChangedListener.createGovernanceEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        RuleConfigurationsChangedEvent event = (RuleConfigurationsChangedEvent) actual.get();
        assertThat(event.getSchemaName(), is("encrypt_db"));
        assertThat(event.getRuleConfigurations().iterator().next(), instanceOf(EncryptRuleConfiguration.class));
        EncryptRuleConfiguration encryptRuleConfig = (EncryptRuleConfiguration) event.getRuleConfigurations().iterator().next();
        assertThat(encryptRuleConfig.getEncryptors().size(), is(1));
        ShardingSphereAlgorithmConfiguration encryptAlgorithmConfig = encryptRuleConfig.getEncryptors().get("order_encryptor");
        assertThat(encryptAlgorithmConfig.getType(), is("AES"));
        assertThat(encryptAlgorithmConfig.getProps().get("aes-key-value"), is(123456));
    }
    
    @Test
    public void assertCreateIgnoredGovernanceEventForNewSchema() {
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/schemas/logic_db/rule", "rule", ChangedType.UPDATED);
        Optional<GovernanceEvent> actual = schemaChangedListener.createGovernanceEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(SchemaAddedEvent.class));
    }
    
    @Test
    public void assertCreateSchemaAddedEventForNewSchema() {
        String dataSource = readYAML(DATA_SOURCE_FILE);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/schemas/logic_db/datasource", dataSource, ChangedType.UPDATED);
        Optional<GovernanceEvent> actual = schemaChangedListener.createGovernanceEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(((SchemaAddedEvent) actual.get()).getSchemaName(), is("logic_db"));
    }
    
    @Test
    public void assertCreatePrimaryReplicaReplicationSchemaAddedEventForNewSchema() {
        String dataSource = readYAML(DATA_SOURCE_FILE);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/schemas/logic_db/datasource", dataSource, ChangedType.UPDATED);
        Optional<GovernanceEvent> actual = schemaChangedListener.createGovernanceEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(((SchemaAddedEvent) actual.get()).getSchemaName(), is("logic_db"));
    }
    
    @Test
    public void assertCreateEncryptSchemaAddedEventForNewSchema() {
        String dataSource = readYAML(DATA_SOURCE_FILE);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/schemas/logic_db/datasource", dataSource, ChangedType.UPDATED);
        Optional<GovernanceEvent> actual = schemaChangedListener.createGovernanceEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(((SchemaAddedEvent) actual.get()).getSchemaName(), is("logic_db"));
    }
    
    @Test
    public void assertCreateSchemaDeletedEventForNewSchema() {
        String dataSource = readYAML(DATA_SOURCE_FILE);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/schemas/logic_db/datasource", dataSource, ChangedType.DELETED);
        Optional<GovernanceEvent> actual = schemaChangedListener.createGovernanceEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(((SchemaDeletedEvent) actual.get()).getSchemaName(), is("logic_db"));
    }
    
    @Test
    public void assertCreateWithSchemaDeletedEvent() {
        String dataSource = readYAML(DATA_SOURCE_FILE);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/schemas/logic_db", dataSource, ChangedType.DELETED);
        Optional<GovernanceEvent> actual = schemaChangedListener.createGovernanceEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(SchemaDeletedEvent.class));
    }
    
    @Test
    public void assertCreateWithSchemaDeletedEventWithDataSourceNode() {
        String dataSource = readYAML(DATA_SOURCE_FILE);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/schemas/datasource", dataSource, ChangedType.DELETED);
        Optional<GovernanceEvent> actual = schemaChangedListener.createGovernanceEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(SchemaDeletedEvent.class));
    }
    
    @Test
    public void assertCreateAddedEventWithEncryptRuleConfigurationForNewSchema() {
        String encryptRule = readYAML(ENCRYPT_RULE_FILE);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/schemas/logic_db/rule", encryptRule, ChangedType.UPDATED);
        Optional<GovernanceEvent> actual = schemaChangedListener.createGovernanceEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(((SchemaAddedEvent) actual.get()).getSchemaName(), is("logic_db"));
    }
    
    @Test
    public void assertCreateAddedEventWithShardingRuleConfigurationForNewSchema() {
        String shardingRule = readYAML(SHARDING_RULE_FILE);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/schemas/logic_db/rule", shardingRule, ChangedType.UPDATED);
        Optional<GovernanceEvent> actual = schemaChangedListener.createGovernanceEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(((SchemaAddedEvent) actual.get()).getSchemaName(), is("logic_db"));
    }
    
    @Test
    public void assertCreateAddedEventWithPrimaryReplicaReplicationRuleConfigurationForNewSchema() {
        String rule = readYAML(PRIMARY_REPLICA_REPLICATION_RULE_FILE);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/schemas/logic_db/rule", rule, ChangedType.UPDATED);
        Optional<GovernanceEvent> actual = schemaChangedListener.createGovernanceEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(((SchemaAddedEvent) actual.get()).getSchemaName(), is("logic_db"));
    }
    
    @Test
    public void assertCreateSchemaNamesUpdatedEventForAdd() {
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/schemas", "sharding_db,primary_replica_replication_db,encrypt_db,shadow_db", ChangedType.UPDATED);
        Optional<GovernanceEvent> actual = schemaChangedListener.createGovernanceEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(((SchemaAddedEvent) actual.get()).getSchemaName(), is("shadow_db"));
    }
    
    @Test
    public void assertCreateSchemaNamesUpdatedEventForDelete() {
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/schemas", "sharding_db,primary_replica_replication_db", ChangedType.UPDATED);
        Optional<GovernanceEvent> actual = schemaChangedListener.createGovernanceEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(((SchemaDeletedEvent) actual.get()).getSchemaName(), is("encrypt_db"));
    }
    
    @Test
    public void assertCreateSchemaNamesUpdatedEventForIgnore() {
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/schemas", "sharding_db,primary_replica_replication_db,encrypt_db", ChangedType.UPDATED);
        assertFalse(schemaChangedListener.createGovernanceEvent(dataChangedEvent).isPresent());
    }
    
    @Test
    public void assertCreateSchemaNameAddEvent() {
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/schemas/shadow_db", "", ChangedType.ADDED);
        Optional<GovernanceEvent> actual = schemaChangedListener.createGovernanceEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(((SchemaAddedEvent) actual.get()).getSchemaName(), is("shadow_db"));
    }
    
    @Test
    public void assertCreateMetaDataChangedEvent() {
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/schemas/sharding_db/table", readYAML(META_DATA_FILE), ChangedType.UPDATED);
        Optional<GovernanceEvent> actual = schemaChangedListener.createGovernanceEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertTrue(((MetaDataChangedEvent) actual.get()).getRuleSchemaMetaData().getConfiguredSchemaMetaData().getAllTableNames().contains("t_order"));
    }
    
    @SneakyThrows
    private String readYAML(final String yamlFile) {
        return Files.readAllLines(Paths.get(ClassLoader.getSystemResource(yamlFile).toURI())).stream().map(each -> each + System.lineSeparator()).collect(Collectors.joining());
    }
}
