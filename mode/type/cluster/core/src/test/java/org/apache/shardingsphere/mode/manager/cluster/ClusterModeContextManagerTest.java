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

package org.apache.shardingsphere.mode.manager.cluster;

import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.jdbc.JDBCInstanceMetaData;
import org.apache.shardingsphere.infra.instance.workerid.WorkerIdGenerator;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.pojo.AlterSchemaMetaDataPOJO;
import org.apache.shardingsphere.infra.metadata.database.schema.pojo.AlterSchemaPOJO;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.fixture.ClusterPersistRepositoryFixture;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.fixture.RuleConfigurationFixture;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.ProcessListClusterPersistRepositoryFixture;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

class ClusterModeContextManagerTest {
    
    @Test
    void assertCreateDatabase() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(new ClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, new ShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfig = new ModeConfiguration("Type", new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager.setContextManagerAware(
                new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator, modeConfig, modeContextManager, null, new EventBusContext())));
        assertDoesNotThrow(() -> clusterModeContextManager.createDatabase("db"));
    }
    
    @Test
    void assertDropDatabase() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(new ClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, new ShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfig = new ModeConfiguration("Type", new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager.setContextManagerAware(
                new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator, modeConfig, modeContextManager, null, new EventBusContext())));
        assertDoesNotThrow(() -> clusterModeContextManager.dropDatabase("db"));
    }
    
    @Test
    void assertCreateSchema() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(new ClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, new ShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfig = new ModeConfiguration("Type", new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager.setContextManagerAware(
                new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator, modeConfig, modeContextManager, null, new EventBusContext())));
        assertDoesNotThrow(() -> clusterModeContextManager.createSchema("db", "Schema Name"));
    }
    
    @Test
    void assertAlterSchema() {
        ShardingSphereMetaData shardingSphereMetaData = new ShardingSphereMetaData();
        shardingSphereMetaData.addDatabase("db", TypedSPILoader.getService(DatabaseType.class, "FIXTURE"), new ConfigurationProperties(new Properties()));
        MetaDataContexts metaDataContexts = new MetaDataContexts(new MetaDataPersistService(new ClusterPersistRepositoryFixture()), shardingSphereMetaData);
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfig = new ModeConfiguration("Type", new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        ContextManager contextManager = new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator, modeConfig, modeContextManager, null, new EventBusContext()));
        contextManager.getResourceMetaDataContextManager().addSchema("db", "Schema Name");
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        clusterModeContextManager.setContextManagerAware(contextManager);
        assertDoesNotThrow(() -> clusterModeContextManager.alterSchema(new AlterSchemaPOJO("db", "Schema Name", "Rename Schema Name", new LinkedList<>())));
    }
    
    @Test
    void assertDropSchema() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(new ClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, new ShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfig = new ModeConfiguration("Type", new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager.setContextManagerAware(
                new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator, modeConfig, modeContextManager, null, new EventBusContext())));
        assertDoesNotThrow(() -> clusterModeContextManager.dropSchema("db", new LinkedList<>()));
    }
    
    @Test
    void assertDropSchemaWithEmptyList() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(new ClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, new ShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfig = new ModeConfiguration("Type", new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager.setContextManagerAware(
                new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator, modeConfig, modeContextManager, null, new EventBusContext())));
        assertDoesNotThrow(() -> clusterModeContextManager.dropSchema("db", Collections.singleton("foo")));
    }
    
    @Test
    void assertDropSchemaWithListContainingRootAndNonRoot() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(new ClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, new ShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfig = new ModeConfiguration("Type", new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager.setContextManagerAware(
                new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator, modeConfig, modeContextManager, null, new EventBusContext())));
        Collection<String> stringList = new LinkedList<>();
        stringList.add("/");
        stringList.add("foo");
        assertDoesNotThrow(() -> clusterModeContextManager.dropSchema("db", stringList));
    }
    
    @Test
    void assertAlterSchemaMetaData() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(new ClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, new ShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfig = new ModeConfiguration("Type", new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager.setContextManagerAware(
                new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator, modeConfig, modeContextManager, null, new EventBusContext())));
        AlterSchemaMetaDataPOJO alterSchemaMetaDataPOJO = new AlterSchemaMetaDataPOJO("db", "Schema Name");
        clusterModeContextManager.alterSchemaMetaData(alterSchemaMetaDataPOJO);
        assertNull(alterSchemaMetaDataPOJO.getLogicDataSourceName());
    }
    
    @Test
    void assertRegisterStorageUnits() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(new ClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, new ShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfig = new ModeConfiguration("Type", new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager.setContextManagerAware(
                new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator, modeConfig, modeContextManager, null, new EventBusContext())));
        assertDoesNotThrow(() -> clusterModeContextManager.registerStorageUnits("db", new TreeMap<>()));
    }
    
    @Test
    void assertAlterStorageUnits() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(new ClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, new ShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfig = new ModeConfiguration("Type",
                new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager
                .setContextManagerAware(new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator,
                        modeConfig, modeContextManager, null, new EventBusContext())));
        assertDoesNotThrow(() -> clusterModeContextManager.alterStorageUnits("db", new TreeMap<>()));
    }
    
    @Test
    void assertAlterStorageUnitsWithProcessListClusterPerRepoFix() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(
                new ProcessListClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, new ShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfig = new ModeConfiguration("Type",
                new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager
                .setContextManagerAware(new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator,
                        modeConfig, modeContextManager, null, new EventBusContext())));
        assertDoesNotThrow(() -> clusterModeContextManager.alterStorageUnits("db", new TreeMap<>()));
    }
    
    @Test
    void assertAlterStorageUnitsWithDataSourcePoolProperties() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(new ClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, new ShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfig = new ModeConfiguration("Type",
                new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager.setContextManagerAware(
                new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator, modeConfig, modeContextManager, null, new EventBusContext())));
        Map<String, DataSourcePoolProperties> propsMap = new HashMap<>();
        propsMap.put("active_version", new DataSourcePoolProperties("active_version", new HashMap<>()));
        assertDoesNotThrow(() -> clusterModeContextManager.alterStorageUnits("db", propsMap));
    }
    
    @Test
    void assertAlterStorageUnitsInvalidName() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(new ClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, new ShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfig = new ModeConfiguration("Type",
                new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager.setContextManagerAware(
                new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator, modeConfig, modeContextManager, null, new EventBusContext())));
        Map<String, DataSourcePoolProperties> propsMap = new HashMap<>();
        propsMap.put("\n", new DataSourcePoolProperties("\n", new HashMap<>()));
        propsMap.put("active_version", new DataSourcePoolProperties("active_version", new HashMap<>()));
        assertDoesNotThrow(() -> clusterModeContextManager.alterStorageUnits("db", propsMap));
    }
    
    @Test
    void assertAlterStorageUnitsWithoutDataSourcePoolProperties() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(new ProcessListClusterPersistRepositoryFixture());
        Map<String, ShardingSphereDatabase> databases = new HashMap<>();
        RuleMetaData globalRuleMetaData = new RuleMetaData(new LinkedList<>());
        MetaDataContexts metaDataContexts = new MetaDataContexts(
                persistService, new ShardingSphereMetaData(databases, mock(ResourceMetaData.class), globalRuleMetaData, new ConfigurationProperties(new Properties())));
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfig = new ModeConfiguration("Type", new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager.setContextManagerAware(
                new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator, modeConfig, modeContextManager, null, new EventBusContext())));
        assertDoesNotThrow(() -> clusterModeContextManager.alterStorageUnits("db", new TreeMap<>()));
    }
    
    @Test
    void assertAlterStorageUnitsWithEmptyDataSourcePoolPropertiesMap() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(new ProcessListClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, new ShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfig = new ModeConfiguration("Type", new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager.setContextManagerAware(
                new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator, modeConfig, modeContextManager, null, new EventBusContext())));
        Map<String, DataSourcePoolProperties> propsMap = new HashMap<>(new TreeMap<>());
        assertDoesNotThrow(() -> clusterModeContextManager.alterStorageUnits("db", propsMap));
    }
    
    @Test
    void assertAlterStorageUnitsWithOneDataSourcePoolProperties() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(new ProcessListClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, new ShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfig = new ModeConfiguration("Type", new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager.setContextManagerAware(
                new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator, modeConfig, modeContextManager, null, new EventBusContext())));
        Map<String, DataSourcePoolProperties> propsMap = new HashMap<>();
        propsMap.put("42", new DataSourcePoolProperties("active_version", new HashMap<>()));
        assertDoesNotThrow(() -> clusterModeContextManager.alterStorageUnits("db", propsMap));
    }
    
    @Test
    void assertUnregisterStorageUnits() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(new ClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, createShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfig = new ModeConfiguration("Type", new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager.setContextManagerAware(
                new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator, modeConfig, modeContextManager, null, new EventBusContext())));
        assertDoesNotThrow(() -> clusterModeContextManager.unregisterStorageUnits("db", new LinkedList<>()));
    }
    
    @Test
    void assertUnregisterStorageUnitsWithProcessListClusterPersistRepoFixture() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(new ProcessListClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, createShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfig = new ModeConfiguration("Type", new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager.setContextManagerAware(
                new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator, modeConfig, modeContextManager, null, new EventBusContext())));
        assertDoesNotThrow(() -> clusterModeContextManager.unregisterStorageUnits("db", new LinkedList<>()));
    }
    
    @Test
    void assertUnregisterStorageUnitsWithClusterPersistRepoFixture() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(new ClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, createShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfig = new ModeConfiguration("Type", new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager.setContextManagerAware(
                new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator, modeConfig, modeContextManager, null, new EventBusContext())));
        assertDoesNotThrow(() -> clusterModeContextManager.alterRuleConfiguration("db", new LinkedList<>()));
    }
    
    @Test
    void assertAlterRuleConfiguration() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(new ClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, createShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfig = new ModeConfiguration("Type", new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager.setContextManagerAware(
                new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator, modeConfig, modeContextManager, null, new EventBusContext())));
        Collection<RuleConfiguration> ruleConfigs = new LinkedList<>();
        ruleConfigs.add(new RuleConfigurationFixture());
        assertDoesNotThrow(() -> clusterModeContextManager.alterRuleConfiguration("db", ruleConfigs));
    }
    
    private ShardingSphereMetaData createShardingSphereMetaData() {
        return new ShardingSphereMetaData(Collections.singletonMap("db", new ShardingSphereDatabase("db", TypedSPILoader.getService(DatabaseType.class, "FIXTURE"),
                new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), Collections.emptyMap())),
                mock(ResourceMetaData.class), new RuleMetaData(Collections.emptyList()), new ConfigurationProperties(new Properties()));
    }
    
    @Test
    void assertAlterRuleConfigurationMultiple() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(new ClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, createShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfig = new ModeConfiguration("Type", new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager.setContextManagerAware(
                new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator, modeConfig, modeContextManager, null, new EventBusContext())));
        Collection<RuleConfiguration> ruleConfigurationList = new LinkedList<>();
        ruleConfigurationList.add(new RuleConfigurationFixture());
        ruleConfigurationList.add(new RuleConfigurationFixture());
        assertDoesNotThrow(() -> clusterModeContextManager.alterRuleConfiguration("db", ruleConfigurationList));
    }
    
    @Test
    void assertAlterRuleConfigurationWithPersistService() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(new ProcessListClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, createShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfig = new ModeConfiguration("Type", new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager.setContextManagerAware(
                new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator, modeConfig, modeContextManager, null, new EventBusContext())));
        Collection<RuleConfiguration> ruleConfigs = new LinkedList<>();
        ruleConfigs.add(new RuleConfigurationFixture());
        assertDoesNotThrow(() -> clusterModeContextManager.alterRuleConfiguration("db", ruleConfigs));
    }
    
    @Test
    void assertAlterGlobalRuleConfigurationWithEmptyRuleConfigurations() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(new ClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, createShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfig = new ModeConfiguration("Type", new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager.setContextManagerAware(
                new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator, modeConfig, modeContextManager, null, new EventBusContext())));
        assertDoesNotThrow(() -> clusterModeContextManager.alterGlobalRuleConfiguration(new LinkedList<>()));
    }
    
    @Test
    void assertAlterGlobalRuleConfigurationWithSingleRuleConfigurations() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(new ClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, new ShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfig = new ModeConfiguration("Type", new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager.setContextManagerAware(
                new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator, modeConfig, modeContextManager, null, new EventBusContext())));
        Collection<RuleConfiguration> ruleConfigs = new LinkedList<>();
        ruleConfigs.add(new RuleConfigurationFixture());
        assertDoesNotThrow(() -> clusterModeContextManager.alterGlobalRuleConfiguration(ruleConfigs));
    }
    
    @Test
    void assertAlterGlobalRuleConfigurationWithMultipleRuleConfigurations() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(new ClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, new ShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfig = new ModeConfiguration("Type", new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager.setContextManagerAware(
                new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator, modeConfig, modeContextManager, null, new EventBusContext())));
        Collection<RuleConfiguration> ruleConfigs = new LinkedList<>();
        ruleConfigs.add(new RuleConfigurationFixture());
        ruleConfigs.add(new RuleConfigurationFixture());
        assertDoesNotThrow(() -> clusterModeContextManager.alterGlobalRuleConfiguration(ruleConfigs));
    }
    
    @Test
    void assertAlterProperties() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(new ClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, new ShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfig = new ModeConfiguration("Type", new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager.setContextManagerAware(
                new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator, modeConfig, modeContextManager, null, new EventBusContext())));
        assertDoesNotThrow(() -> clusterModeContextManager.alterProperties(new Properties()));
    }
    
    @Test
    void assertConstructor() {
        MetaDataPersistService persistService = new MetaDataPersistService(new ClusterPersistRepositoryFixture());
        try (MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, new ShardingSphereMetaData())) {
            ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
            WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
            ModeConfiguration modeConfig = new ModeConfiguration("Type", new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
            ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
            assertDoesNotThrow(() -> new ClusterModeContextManager().setContextManagerAware(
                    new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator, modeConfig, modeContextManager, null, new EventBusContext()))));
        }
    }
}
