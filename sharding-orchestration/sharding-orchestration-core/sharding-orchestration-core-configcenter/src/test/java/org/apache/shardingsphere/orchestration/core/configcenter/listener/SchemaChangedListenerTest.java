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

import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.EncryptorRuleConfiguration;
import org.apache.shardingsphere.orchestration.center.ConfigCenterRepository;
import org.apache.shardingsphere.orchestration.center.listener.DataChangedEvent;
import org.apache.shardingsphere.orchestration.center.listener.DataChangedEvent.ChangedType;
import org.apache.shardingsphere.orchestration.core.common.event.DataSourceChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.EncryptRuleChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.IgnoredShardingOrchestrationEvent;
import org.apache.shardingsphere.orchestration.core.common.event.MasterSlaveRuleChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.SchemaAddedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.SchemaDeletedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.ShardingOrchestrationEvent;
import org.apache.shardingsphere.orchestration.core.common.event.ShardingRuleChangedEvent;
import org.apache.shardingsphere.orchestration.core.configuration.YamlDataSourceConfiguration;
import org.apache.shardingsphere.underlying.common.config.RuleConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map.Entry;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SchemaChangedListenerTest {
    
    private static final String DATA_SOURCE_YAML = "master_ds: !!" + YamlDataSourceConfiguration.class.getName() + "\n"
            + "  dataSourceClassName: com.zaxxer.hikari.HikariDataSource\n" + "  properties:\n"
            + "    url: jdbc:mysql://localhost:3306/demo_ds_master\n" + "    username: root\n" + "    password: null\n";
    
    private static final String SHARDING_RULE_YAML = "tables:\n" + "  t_order:\n" + "    logicTable: t_order\n" 
            + "    actualDataNodes: ds_${0..1}.t_order_${0..1}\n" + "    tableStrategy:\n" + "      standard:\n" 
            + "        shardingAlgorithm: \n" + "          type: INLINE\n" + "          props:\n" + "            algorithm.expression: t_order_${order_id % 2}\n" 
            + "        shardingColumn: order_id";
    
    private static final String MASTER_SLAVE_RULE_YAML = "masterDataSourceName: master_ds\n" + "name: ms_ds\n" + "slaveDataSourceNames:\n" + "- slave_ds_0\n" + "- slave_ds_1\n";
    
    private static final String ENCRYPT_RULE_YAML = "tables:\n" + "  t_order:\n" + "    columns:\n" + "      order_id:\n" 
            + "        cipherColumn: order_id\n" + "        encryptor: order_encryptor\n" + "encryptors:\n" + "  order_encryptor:\n" 
            + "    type: aes\n" + "    props:\n" + "      aes.key.value: 123456";
    
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
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/test/config/schema/sharding_db/datasource", DATA_SOURCE_YAML, ChangedType.UPDATED);
        ShardingOrchestrationEvent actual = schemaChangedListener.createShardingOrchestrationEvent(dataChangedEvent);
        assertThat(actual, instanceOf(DataSourceChangedEvent.class));
        assertThat(((DataSourceChangedEvent) actual).getShardingSchemaName(), is("sharding_db"));
    }
    
    @Test
    public void assertCreateShardingRuleChangedEventForExistedSchema() {
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/test/config/schema/sharding_db/rule", SHARDING_RULE_YAML, ChangedType.UPDATED);
        ShardingOrchestrationEvent actual = schemaChangedListener.createShardingOrchestrationEvent(dataChangedEvent);
        assertThat(actual, instanceOf(ShardingRuleChangedEvent.class));
        assertThat(((ShardingRuleChangedEvent) actual).getShardingSchemaName(), is("sharding_db"));
        Collection<RuleConfiguration> ruleConfigurations = ((ShardingRuleChangedEvent) actual).getRuleConfigurations();
        assertThat(ruleConfigurations.size(), is(1));
        assertThat(((ShardingRuleConfiguration) ruleConfigurations.iterator().next()).getTableRuleConfigs().size(), is(1));
    }
    
    @Test
    public void assertCreateMasterSlaveRuleChangedEventForExistedSchema() {
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/test/config/schema/masterslave_db/rule", MASTER_SLAVE_RULE_YAML, ChangedType.UPDATED);
        ShardingOrchestrationEvent actual = schemaChangedListener.createShardingOrchestrationEvent(dataChangedEvent);
        assertThat(actual, instanceOf(MasterSlaveRuleChangedEvent.class));
        assertThat(((MasterSlaveRuleChangedEvent) actual).getShardingSchemaName(), is("masterslave_db"));
        assertThat(((MasterSlaveRuleChangedEvent) actual).getMasterSlaveRuleConfiguration().getMasterDataSourceName(), is("master_ds"));
    }
    
    @Test
    public void assertCreateEncryptRuleChangedEventForExistedSchema() {
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/test/config/schema/encrypt_db/rule", ENCRYPT_RULE_YAML, ChangedType.UPDATED);
        ShardingOrchestrationEvent actual = schemaChangedListener.createShardingOrchestrationEvent(dataChangedEvent);
        assertThat(actual, instanceOf(EncryptRuleChangedEvent.class));
        assertThat(((EncryptRuleChangedEvent) actual).getShardingSchemaName(), is("encrypt_db"));
        assertThat(((EncryptRuleChangedEvent) actual).getEncryptRuleConfiguration().getEncryptors().size(), is(1));
        Entry<String, EncryptorRuleConfiguration> entry = ((EncryptRuleChangedEvent) actual).getEncryptRuleConfiguration().getEncryptors().entrySet().iterator().next();
        assertThat(entry.getKey(), is("order_encryptor"));
        assertThat(entry.getValue().getType(), is("aes"));
        assertThat(entry.getValue().getProperties().get("aes.key.value").toString(), is("123456"));
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
        when(configCenterRepository.get("/test/config/schema/logic_db/rule")).thenReturn(SHARDING_RULE_YAML);
        when(configCenterRepository.get("/test/config/schema/logic_db/datasource")).thenReturn(DATA_SOURCE_YAML);
        when(configCenterRepository.get("/test/config/schema/logic_db/rule")).thenReturn(SHARDING_RULE_YAML);
        when(configCenterRepository.get("/test/config/schema/logic_db/datasource")).thenReturn(DATA_SOURCE_YAML);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/test/config/schema/logic_db/datasource", DATA_SOURCE_YAML, ChangedType.UPDATED);
        ShardingOrchestrationEvent actual = schemaChangedListener.createShardingOrchestrationEvent(dataChangedEvent);
        assertThat(actual, instanceOf(SchemaAddedEvent.class));
        assertThat(((SchemaAddedEvent) actual).getRuleConfiguration(), instanceOf(ShardingRuleConfiguration.class));
    }
    
    @Test
    public void assertCreateMasterSlaveSchemaAddedEventForNewSchema() {
        when(configCenterRepository.get("/test/config/schema/logic_db/rule")).thenReturn(MASTER_SLAVE_RULE_YAML);
        when(configCenterRepository.get("/test/config/schema/logic_db/datasource")).thenReturn(DATA_SOURCE_YAML);
        when(configCenterRepository.get("/test/config/schema/logic_db/rule")).thenReturn(MASTER_SLAVE_RULE_YAML);
        when(configCenterRepository.get("/test/config/schema/logic_db/datasource")).thenReturn(DATA_SOURCE_YAML);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/test/config/schema/logic_db/datasource", DATA_SOURCE_YAML, ChangedType.UPDATED);
        ShardingOrchestrationEvent actual = schemaChangedListener.createShardingOrchestrationEvent(dataChangedEvent);
        assertThat(actual, instanceOf(SchemaAddedEvent.class));
        assertThat(((SchemaAddedEvent) actual).getRuleConfiguration(), instanceOf(MasterSlaveRuleConfiguration.class));
    }
    
    @Test
    public void assertCreateEncryptSchemaAddedEventForNewSchema() {
        when(configCenterRepository.get("/test/config/schema/logic_db/rule")).thenReturn(ENCRYPT_RULE_YAML);
        when(configCenterRepository.get("/test/config/schema/logic_db/datasource")).thenReturn(DATA_SOURCE_YAML);
        when(configCenterRepository.get("/test/config/schema/logic_db/rule")).thenReturn(ENCRYPT_RULE_YAML);
        when(configCenterRepository.get("/test/config/schema/logic_db/datasource")).thenReturn(DATA_SOURCE_YAML);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/test/config/schema/logic_db/datasource", DATA_SOURCE_YAML, ChangedType.UPDATED);
        ShardingOrchestrationEvent actual = schemaChangedListener.createShardingOrchestrationEvent(dataChangedEvent);
        assertThat(actual, instanceOf(SchemaAddedEvent.class));
        assertThat(((SchemaAddedEvent) actual).getRuleConfiguration(), instanceOf(EncryptRuleConfiguration.class));
    }
    
    @Test
    public void assertCreateSchemaDeletedEventForNewSchema() {
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/test/config/schema/logic_db/datasource", DATA_SOURCE_YAML, ChangedType.DELETED);
        ShardingOrchestrationEvent actual = schemaChangedListener.createShardingOrchestrationEvent(dataChangedEvent);
        assertThat(actual, instanceOf(SchemaDeletedEvent.class));
        assertThat(((SchemaDeletedEvent) actual).getShardingSchemaName(), is("logic_db"));
    }
    
    @Test
    public void assertCreateWithInvalidNodeChangedEvent() {
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/test/config/schema/logic_db", DATA_SOURCE_YAML, ChangedType.DELETED);
        ShardingOrchestrationEvent actual = schemaChangedListener.createShardingOrchestrationEvent(dataChangedEvent);
        assertThat(actual, instanceOf(IgnoredShardingOrchestrationEvent.class));
    }
    
    @Test
    public void assertCreateWithNullShardingSchemaName() {
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/test/config/schema/datasource", DATA_SOURCE_YAML, ChangedType.DELETED);
        ShardingOrchestrationEvent actual = schemaChangedListener.createShardingOrchestrationEvent(dataChangedEvent);
        assertThat(actual, instanceOf(IgnoredShardingOrchestrationEvent.class));
    }
    
    @Test
    public void assertCreateAddedEventWithEncryptRuleConfigurationForNewSchema() {
        when(configCenterRepository.get("/test/config/schema/logic_db/rule")).thenReturn(ENCRYPT_RULE_YAML);
        when(configCenterRepository.get("/test/config/schema/logic_db/datasource")).thenReturn(DATA_SOURCE_YAML);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/test/config/schema/logic_db/rule", ENCRYPT_RULE_YAML, ChangedType.UPDATED);
        ShardingOrchestrationEvent actual = schemaChangedListener.createShardingOrchestrationEvent(dataChangedEvent);
        assertThat(actual, instanceOf(SchemaAddedEvent.class));
        assertThat(((SchemaAddedEvent) actual).getRuleConfiguration(), instanceOf(EncryptRuleConfiguration.class));
    }
    
    @Test
    public void assertCreateAddedEventWithShardingRuleConfigurationForNewSchema() {
        when(configCenterRepository.get("/test/config/schema/logic_db/rule")).thenReturn(SHARDING_RULE_YAML);
        when(configCenterRepository.get("/test/config/schema/logic_db/datasource")).thenReturn(DATA_SOURCE_YAML);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/test/config/schema/logic_db/rule", SHARDING_RULE_YAML, ChangedType.UPDATED);
        ShardingOrchestrationEvent actual = schemaChangedListener.createShardingOrchestrationEvent(dataChangedEvent);
        assertThat(actual, instanceOf(SchemaAddedEvent.class));
        assertThat(((SchemaAddedEvent) actual).getRuleConfiguration(), instanceOf(ShardingRuleConfiguration.class));
    }
    
    @Test
    public void assertCreateAddedEventWithMasterSlaveRuleConfigurationForNewSchema() {
        when(configCenterRepository.get("/test/config/schema/logic_db/rule")).thenReturn(MASTER_SLAVE_RULE_YAML);
        when(configCenterRepository.get("/test/config/schema/logic_db/datasource")).thenReturn(DATA_SOURCE_YAML);
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/test/config/schema/logic_db/rule", MASTER_SLAVE_RULE_YAML, ChangedType.UPDATED);
        ShardingOrchestrationEvent actual = schemaChangedListener.createShardingOrchestrationEvent(dataChangedEvent);
        assertThat(actual, instanceOf(SchemaAddedEvent.class));
        assertThat(((SchemaAddedEvent) actual).getRuleConfiguration(), instanceOf(MasterSlaveRuleConfiguration.class));
    }
}
