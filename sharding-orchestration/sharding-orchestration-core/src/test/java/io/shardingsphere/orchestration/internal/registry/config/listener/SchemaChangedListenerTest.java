/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.orchestration.internal.registry.config.listener;

import io.shardingsphere.orchestration.internal.registry.config.event.DataSourceChangedEvent;
import io.shardingsphere.orchestration.internal.registry.config.event.IgnoredShardingOrchestrationEvent;
import io.shardingsphere.orchestration.internal.registry.listener.ShardingOrchestrationEvent;
import io.shardingsphere.orchestration.reg.api.RegistryCenter;
import io.shardingsphere.orchestration.reg.listener.DataChangedEvent;
import io.shardingsphere.orchestration.reg.listener.DataChangedEvent.ChangedType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class SchemaChangedListenerTest {
    
    private static final String DATA_SOURCE_YAML = "master_ds: !!io.shardingsphere.orchestration.yaml.YamlDataSourceConfiguration\n"
            + "  dataSourceClassName: com.zaxxer.hikari.HikariDataSource\n" + "  properties:\n"
            + "    url: jdbc:mysql://localhost:3306/demo_ds_master\n" + "    username: root\n" + "    password: null\n";
    
    private static final String SHARDING_RULE_YAML = "tables:\n" + "  t_order:\n" + "    logicTable: t_order\n" + "    actualDataNodes: ds_${0..1}.t_order_${0..1}\n"
            + "    tableStrategy:\n" + "      inline:\n" + "        algorithmExpression: t_order_${order_id % 2}\n" + "        shardingColumn: order_id";
    
    private static final String MASTER_SLAVE_RULE_YAML = "masterDataSourceName: master_ds\n" + "name: ms_ds\n" + "slaveDataSourceNames:\n" + "- slave_ds_0\n" + "- slave_ds_1\n";
    
    private SchemaChangedListener schemaChangedListener;
    
    @Mock
    private RegistryCenter regCenter;
    
    @Before
    public void setUp() {
        schemaChangedListener = new SchemaChangedListener("test", regCenter, Arrays.asList("sharding_db", "masterslave_db"));
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
        DataChangedEvent dataChangedEvent = new DataChangedEvent("/test/config/schema/sharding_db/rule", DATA_SOURCE_YAML, ChangedType.UPDATED);
        ShardingOrchestrationEvent actual = schemaChangedListener.createShardingOrchestrationEvent(dataChangedEvent);
        assertThat(actual, instanceOf(DataSourceChangedEvent.class));
        assertThat(((DataSourceChangedEvent) actual).getShardingSchemaName(), is("sharding_db"));
    }
}
