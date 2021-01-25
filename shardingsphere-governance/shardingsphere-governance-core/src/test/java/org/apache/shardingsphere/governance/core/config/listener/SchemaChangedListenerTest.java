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
import org.apache.shardingsphere.governance.core.event.model.metadata.MetaDataAddedEvent;
import org.apache.shardingsphere.governance.core.event.model.metadata.MetaDataDeletedEvent;
import org.apache.shardingsphere.governance.core.event.model.rule.RuleConfigurationsChangedEvent;
import org.apache.shardingsphere.governance.core.event.model.schema.SchemaChangedEvent;
import org.apache.shardingsphere.governance.repository.api.ConfigurationRepository;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent.Type;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.replicaquery.api.config.ReplicaQueryRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URISyntaxException;
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
    
    private static final String REPLICA_QUERY_RULE_FILE = "yaml/replica-query-rule.yaml";
    
    private static final String ENCRYPT_RULE_FILE = "yaml/encrypt-rule.yaml";
    
    private static final String META_DATA_FILE = "yaml/schema.yaml";
    
    private SchemaChangedListener schemaChangedListener;
    
    @Mock
    private ConfigurationRepository configurationRepository;
    
    @Before
    public void setUp() {
        schemaChangedListener = new SchemaChangedListener(configurationRepository, Arrays.asList("sharding_db", "replica_query_db", "encrypt_db"));
    }
    
    @Test
    public void assertCreateIgnoredEvent() {
        assertFalse(schemaChangedListener.createEvent(new DataChangedEvent("/metadata/encrypt_db", "test", Type.UPDATED)).isPresent());
        assertFalse(schemaChangedListener.createEvent(new DataChangedEvent("/metadata/encrypt_db/rule", "test", Type.IGNORED)).isPresent());
    }
    
    @Test
    public void assertCreateDataSourceChangedEventForExistedSchema() {
        String dataSource = readYAML(DATA_SOURCE_FILE);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/metadata/sharding_db/datasource", dataSource, Type.UPDATED);
        Optional<GovernanceEvent> actual = schemaChangedListener.createEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(DataSourceChangedEvent.class));
        assertThat(((DataSourceChangedEvent) actual.get()).getSchemaName(), is("sharding_db"));
    }
    
    @Test
    public void assertCreateRuleConfigurationsChangedEventForExistedSchema() {
        String shardingRule = readYAML(SHARDING_RULE_FILE);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/metadata/sharding_db/rule", shardingRule, Type.UPDATED);
        Optional<GovernanceEvent> actual = schemaChangedListener.createEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(((RuleConfigurationsChangedEvent) actual.get()).getSchemaName(), is("sharding_db"));
        Collection<RuleConfiguration> ruleConfigs = ((RuleConfigurationsChangedEvent) actual.get()).getRuleConfigurations();
        assertThat(ruleConfigs.size(), is(1));
        assertThat(((ShardingRuleConfiguration) ruleConfigs.iterator().next()).getTables().size(), is(1));
    }
    
    @Test
    public void assertCreateReplicaQueryRuleChangedEventForExistedSchema() {
        String rule = readYAML(REPLICA_QUERY_RULE_FILE);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/metadata/replica_query_db/rule", rule, Type.UPDATED);
        Optional<GovernanceEvent> actual = schemaChangedListener.createEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        RuleConfigurationsChangedEvent event = (RuleConfigurationsChangedEvent) actual.get();
        assertThat(event.getSchemaName(), is("replica_query_db"));
        assertThat(event.getRuleConfigurations().iterator().next(), instanceOf(ReplicaQueryRuleConfiguration.class));
        ReplicaQueryRuleConfiguration ruleConfig = (ReplicaQueryRuleConfiguration) event.getRuleConfigurations().iterator().next();
        assertThat(ruleConfig.getDataSources().iterator().next().getPrimaryDataSourceName(), is("primary_ds"));
    }
    
    @Test
    public void assertCreateEncryptRuleChangedEventForExistedSchema() {
        String encryptRule = readYAML(ENCRYPT_RULE_FILE);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/metadata/encrypt_db/rule", encryptRule, Type.UPDATED);
        Optional<GovernanceEvent> actual = schemaChangedListener.createEvent(dataChangedEvent);
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
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/metadata/logic_db/rule", "rule", Type.UPDATED);
        Optional<GovernanceEvent> actual = schemaChangedListener.createEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(MetaDataAddedEvent.class));
    }
    
    @Test
    public void assertCreateSchemaAddedEventForNewSchema() {
        String dataSource = readYAML(DATA_SOURCE_FILE);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/metadata/logic_db/datasource", dataSource, Type.UPDATED);
        Optional<GovernanceEvent> actual = schemaChangedListener.createEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(((MetaDataAddedEvent) actual.get()).getSchemaName(), is("logic_db"));
    }
    
    @Test
    public void assertCreateReplicaQuerySchemaAddedEventForNewSchema() {
        String dataSource = readYAML(DATA_SOURCE_FILE);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/metadata/logic_db/datasource", dataSource, Type.UPDATED);
        Optional<GovernanceEvent> actual = schemaChangedListener.createEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(((MetaDataAddedEvent) actual.get()).getSchemaName(), is("logic_db"));
    }
    
    @Test
    public void assertCreateEncryptSchemaAddedEventForNewSchema() {
        String dataSource = readYAML(DATA_SOURCE_FILE);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/metadata/logic_db/datasource", dataSource, Type.UPDATED);
        Optional<GovernanceEvent> actual = schemaChangedListener.createEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(((MetaDataAddedEvent) actual.get()).getSchemaName(), is("logic_db"));
    }
    
    @Test
    public void assertCreateSchemaDeletedEventForNewSchema() {
        String dataSource = readYAML(DATA_SOURCE_FILE);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/metadata/logic_db/datasource", dataSource, Type.DELETED);
        Optional<GovernanceEvent> actual = schemaChangedListener.createEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(((MetaDataDeletedEvent) actual.get()).getSchemaName(), is("logic_db"));
    }
    
    @Test
    public void assertCreateWithSchemaDeletedEvent() {
        String dataSource = readYAML(DATA_SOURCE_FILE);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/metadata/logic_db", dataSource, Type.DELETED);
        Optional<GovernanceEvent> actual = schemaChangedListener.createEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(MetaDataDeletedEvent.class));
    }
    
    @Test
    public void assertCreateWithSchemaDeletedEventWithDataSourceNode() {
        String dataSource = readYAML(DATA_SOURCE_FILE);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/metadata/datasource", dataSource, Type.DELETED);
        Optional<GovernanceEvent> actual = schemaChangedListener.createEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(MetaDataDeletedEvent.class));
    }
    
    @Test
    public void assertCreateAddedEventWithEncryptRuleConfigurationForNewSchema() {
        String encryptRule = readYAML(ENCRYPT_RULE_FILE);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/metadata/logic_db/rule", encryptRule, Type.UPDATED);
        Optional<GovernanceEvent> actual = schemaChangedListener.createEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(((MetaDataAddedEvent) actual.get()).getSchemaName(), is("logic_db"));
    }
    
    @Test
    public void assertCreateAddedEventWithShardingRuleConfigurationForNewSchema() {
        String shardingRule = readYAML(SHARDING_RULE_FILE);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/metadata/logic_db/rule", shardingRule, Type.UPDATED);
        Optional<GovernanceEvent> actual = schemaChangedListener.createEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(((MetaDataAddedEvent) actual.get()).getSchemaName(), is("logic_db"));
    }
    
    @Test
    public void assertCreateAddedEventWithReplicaQueryRuleConfigurationForNewSchema() {
        String rule = readYAML(REPLICA_QUERY_RULE_FILE);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/metadata/logic_db/rule", rule, Type.UPDATED);
        Optional<GovernanceEvent> actual = schemaChangedListener.createEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(((MetaDataAddedEvent) actual.get()).getSchemaName(), is("logic_db"));
    }
    
    @Test
    public void assertCreateSchemaNamesUpdatedEventForAdd() {
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/metadata", "sharding_db,replica_query_db,encrypt_db,shadow_db", Type.UPDATED);
        Optional<GovernanceEvent> actual = schemaChangedListener.createEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(((MetaDataAddedEvent) actual.get()).getSchemaName(), is("shadow_db"));
    }
    
    @Test
    public void assertCreateSchemaNamesUpdatedEventForDelete() {
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/metadata", "sharding_db,replica_query_db", Type.UPDATED);
        Optional<GovernanceEvent> actual = schemaChangedListener.createEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(((MetaDataDeletedEvent) actual.get()).getSchemaName(), is("encrypt_db"));
    }
    
    @Test
    public void assertCreateSchemaNamesUpdatedEventForIgnore() {
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/metadata", "sharding_db,replica_query_db,encrypt_db", Type.UPDATED);
        assertFalse(schemaChangedListener.createEvent(dataChangedEvent).isPresent());
    }
    
    @Test
    public void assertCreateSchemaNameAddEvent() {
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/metadata/shadow_db", "", Type.ADDED);
        Optional<GovernanceEvent> actual = schemaChangedListener.createEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertThat(((MetaDataAddedEvent) actual.get()).getSchemaName(), is("shadow_db"));
    }
    
    @Test
    public void assertCreateSchemaChangedEvent() {
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/metadata/sharding_db/schema", readYAML(META_DATA_FILE), Type.UPDATED);
        Optional<GovernanceEvent> actual = schemaChangedListener.createEvent(dataChangedEvent);
        assertTrue(actual.isPresent());
        assertTrue(((SchemaChangedEvent) actual.get()).getSchema().getAllTableNames().contains("t_order"));
    }
    
    @SneakyThrows({IOException.class, URISyntaxException.class})
    private String readYAML(final String yamlFile) {
        return Files.readAllLines(Paths.get(ClassLoader.getSystemResource(yamlFile).toURI())).stream().map(each -> each + System.lineSeparator()).collect(Collectors.joining());
    }
}
