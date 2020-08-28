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

package org.apache.shardingsphere.replica.yaml.swapper;

import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.order.OrderedSPIRegistry;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.replica.api.config.ReplicaTableRuleConfiguration;
import org.apache.shardingsphere.replica.api.config.ReplicaRuleConfiguration;
import org.apache.shardingsphere.replica.yaml.config.YamlReplicaRuleConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public final class ReplicaRuleConfigurationYamlSwapperTest {

    static {
        ShardingSphereServiceLoader.register(YamlRuleConfigurationSwapper.class);
    }

    @Mock
    private ReplicaRuleConfiguration ruleConfig;

    private final String logicTableName = "t_order";

    private final String dataSourceName = "demo_ds_0";

    private final String physicsTable = "t_order_1";

    private final String replicaGroupId = "raftGroupTest1";

    private final String replicaPeers = "127.0.0.1:9090";

    @Test
    public void assertSwapToYamlConfigurationWithMinProperties() {
        ReplicaRuleConfigurationYamlSwapper swapper = getSwapper();
        YamlReplicaRuleConfiguration yamlConfiguration = swapper.swapToYamlConfiguration(new ReplicaRuleConfiguration());
        Map<String, ReplicaTableRuleConfiguration[]> resultTables = yamlConfiguration.getTables();
        assertNotNull(resultTables);
        assertTrue(resultTables.isEmpty());
    }

    @Test
    public void assertSwapToYamlConfigurationWithMaxProperties() {
        ReplicaRuleConfiguration configuration = new ReplicaRuleConfiguration();
        Map<String, ReplicaTableRuleConfiguration[]> tables = new LinkedHashMap<>(2);
        configuration.setTables(tables);
        ReplicaTableRuleConfiguration table = new ReplicaTableRuleConfiguration();
        tables.put(logicTableName, new ReplicaTableRuleConfiguration[] {table});
        table.setDataSourceName(dataSourceName);
        table.setPhysicsTable(physicsTable);
        table.setReplicaGroupId(replicaGroupId);
        table.setReplicaPeers(replicaPeers);

        ReplicaRuleConfigurationYamlSwapper swapper = getSwapper();
        YamlReplicaRuleConfiguration yamlConfiguration = swapper.swapToYamlConfiguration(configuration);
        Map<String, ReplicaTableRuleConfiguration[]> resultTables = yamlConfiguration.getTables();
        assertNotNull(resultTables);
        assertThat(resultTables, is(tables));
        assertThat(resultTables.size(), is(1));
        assertThat(resultTables.get(logicTableName)[0], is(table));
        assertThat(table.getDataSourceName(), is(dataSourceName));
        assertThat(table.getPhysicsTable(), is(physicsTable));
        assertThat(table.getReplicaGroupId(), is(replicaGroupId));
        assertThat(table.getReplicaPeers(), is(replicaPeers));
    }

    @Test
    public void assertSwapToObjectWithMinProperties() {
        ReplicaRuleConfigurationYamlSwapper swapper = getSwapper();
        ReplicaRuleConfiguration configuration = swapper.swapToObject(new YamlReplicaRuleConfiguration());
        Map<String, ReplicaTableRuleConfiguration[]> resultTables = configuration.getTables();
        assertNotNull(resultTables);
        assertTrue(resultTables.isEmpty());
    }

    @Test
    public void assertSwapToObjectWithMaxProperties() {
        YamlReplicaRuleConfiguration yamlConfiguration = new YamlReplicaRuleConfiguration();
        Map<String, ReplicaTableRuleConfiguration[]> tables = new LinkedHashMap<>(2);
        yamlConfiguration.setTables(tables);
        ReplicaTableRuleConfiguration table = new ReplicaTableRuleConfiguration();
        tables.put(logicTableName, new ReplicaTableRuleConfiguration[] {table});
        table.setDataSourceName(dataSourceName);
        table.setPhysicsTable(physicsTable);
        table.setReplicaGroupId(replicaGroupId);
        table.setReplicaPeers(replicaPeers);
        ReplicaRuleConfigurationYamlSwapper swapper = getSwapper();
        ReplicaRuleConfiguration configuration = swapper.swapToObject(yamlConfiguration);
        Map<String, ReplicaTableRuleConfiguration[]> resultTables = configuration.getTables();
        assertNotNull(resultTables);
        assertThat(resultTables, is(tables));
        assertThat(resultTables.size(), is(1));
        assertThat(resultTables.get(logicTableName)[0], is(table));
        assertThat(table.getDataSourceName(), is(dataSourceName));
        assertThat(table.getPhysicsTable(), is(physicsTable));
        assertThat(table.getReplicaGroupId(), is(replicaGroupId));
        assertThat(table.getReplicaPeers(), is(replicaPeers));
    }

    private ReplicaRuleConfigurationYamlSwapper getSwapper() {
        return (ReplicaRuleConfigurationYamlSwapper) OrderedSPIRegistry.getRegisteredServices(Collections.singletonList(ruleConfig), YamlRuleConfigurationSwapper.class).get(ruleConfig);
    }
}
