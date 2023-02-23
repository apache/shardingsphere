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

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.LinkedList;
import java.util.Collection;
import java.util.HashMap;

import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.jdbc.JDBCInstanceMetaData;
import org.apache.shardingsphere.infra.instance.workerid.WorkerIdGenerator;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.pojo.AlterSchemaMetaDataPOJO;
import org.apache.shardingsphere.infra.metadata.database.schema.pojo.AlterSchemaPOJO;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.fixture.ClusterPersistRepositoryFixture;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.fixture.RuleConfigurationFixture;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.process.ProcessListClusterPersistRepositoryFixture;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryConfiguration;
import org.junit.Test;

public final class ClusterModeContextManagerTest {
    
    @Test
    public void assertCreateDatabase() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(new ClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, new ShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfiguration = new ModeConfiguration("Type",
                new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager
                .setContextManagerAware(new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator,
                        modeConfiguration, modeContextManager, null, new EventBusContext())));
        clusterModeContextManager.createDatabase("Database Name");
    }
    
    @Test
    public void assertDropDatabase() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(new ClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, new ShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfiguration = new ModeConfiguration("Type",
                new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager
                .setContextManagerAware(new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator,
                        modeConfiguration, modeContextManager, null, new EventBusContext())));
        clusterModeContextManager.dropDatabase("Database Name");
    }
    
    @Test
    public void assertCreateSchema() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(new ClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, new ShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfiguration = new ModeConfiguration("Type",
                new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager
                .setContextManagerAware(new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator,
                        modeConfiguration, modeContextManager, null, new EventBusContext())));
        clusterModeContextManager.createSchema("Database Name", "Schema Name");
    }
    
    @Test
    public void assertAlterSchema() {
        ShardingSphereMetaData shardingSphereMetaData = new ShardingSphereMetaData();
        shardingSphereMetaData.addDatabase("Database Name", new H2DatabaseType());
        MetaDataContexts metaDataContexts = new MetaDataContexts(
                new MetaDataPersistService(new ClusterPersistRepositoryFixture()), shardingSphereMetaData);
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfiguration = new ModeConfiguration("Type",
                new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        ContextManager contextManager = new ContextManager(metaDataContexts, new InstanceContext(instance,
                workerIdGenerator, modeConfiguration, modeContextManager, null, new EventBusContext()));
        contextManager.addSchema("Database Name", "Schema Name");
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        clusterModeContextManager.setContextManagerAware(contextManager);
        clusterModeContextManager
                .alterSchema(new AlterSchemaPOJO("Database Name", "Schema Name", "Rename Schema Name", new LinkedList<>()));
    }
    
    @Test
    public void assertDropSchema() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(new ClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, new ShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfiguration = new ModeConfiguration("Type",
                new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager
                .setContextManagerAware(new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator,
                        modeConfiguration, modeContextManager, null, new EventBusContext())));
        clusterModeContextManager.dropSchema("Database Name", new LinkedList<>());
    }
    
    @Test
    public void assertDropSchemaWithEmptyList() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(new ClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, new ShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfiguration = new ModeConfiguration("Type",
                new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager
                .setContextManagerAware(new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator,
                        modeConfiguration, modeContextManager, null, new EventBusContext())));
        Collection<String> stringList = new LinkedList<>();
        stringList.add("foo");
        clusterModeContextManager.dropSchema("Database Name", stringList);
    }
    
    @Test
    public void assertDropSchemaWithListContainingRootAndNonRoot() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(new ClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, new ShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfiguration = new ModeConfiguration("Type",
                new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager
                .setContextManagerAware(new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator,
                        modeConfiguration, modeContextManager, null, new EventBusContext())));
        Collection<String> stringList = new LinkedList<>();
        stringList.add("/");
        stringList.add("foo");
        clusterModeContextManager.dropSchema("Database Name", stringList);
    }
    
    @Test
    public void assertAlterSchemaMetaData() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(new ClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, new ShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfiguration = new ModeConfiguration("Type",
                new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager
                .setContextManagerAware(new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator,
                        modeConfiguration, modeContextManager, null, new EventBusContext())));
        AlterSchemaMetaDataPOJO alterSchemaMetaDataPOJO = new AlterSchemaMetaDataPOJO("Database Name", "Schema Name");
        clusterModeContextManager.alterSchemaMetaData(alterSchemaMetaDataPOJO);
        assertNull(alterSchemaMetaDataPOJO.getLogicDataSourceName());
    }
    
    @Test
    public void assertRegisterStorageUnits() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(new ClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, new ShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfiguration = new ModeConfiguration("Type",
                new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager
                .setContextManagerAware(new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator,
                        modeConfiguration, modeContextManager, null, new EventBusContext())));
        clusterModeContextManager.registerStorageUnits("Database Name", new TreeMap<>());
    }
    
    @Test
    public void assertAlterStorageUnits() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(new ClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, new ShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfiguration = new ModeConfiguration("Type",
                new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager
                .setContextManagerAware(new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator,
                        modeConfiguration, modeContextManager, null, new EventBusContext())));
        clusterModeContextManager.alterStorageUnits("Database Name", new TreeMap<>());
    }
    
    @Test
    public void assertAlterStorageUnitsWithProcessListClusterPerRepoFix() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(
                new ProcessListClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, new ShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfiguration = new ModeConfiguration("Type",
                new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager
                .setContextManagerAware(new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator,
                        modeConfiguration, modeContextManager, null, new EventBusContext())));
        clusterModeContextManager.alterStorageUnits("Database Name", new TreeMap<>());
    }
    
    @Test
    public void assertAlterStorageUnitsWithDataSourceProperties() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(new ClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, new ShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfiguration = new ModeConfiguration("Type",
                new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager
                .setContextManagerAware(new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator,
                        modeConfiguration, modeContextManager, null, new EventBusContext())));
        Map<String, DataSourceProperties> stringDataSourcePropertiesMap = new HashMap<>();
        stringDataSourcePropertiesMap.put("active_version", new DataSourceProperties("active_version", new HashMap<>()));
        clusterModeContextManager.alterStorageUnits("Database Name", stringDataSourcePropertiesMap);
    }
    
    @Test
    public void assertAlterStorageUnitsInvalidName() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(new ClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, new ShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfiguration = new ModeConfiguration("Type",
                new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager
                .setContextManagerAware(new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator,
                        modeConfiguration, modeContextManager, null, new EventBusContext())));
        Map<String, DataSourceProperties> stringDataSourcePropertiesMap = new HashMap<>();
        stringDataSourcePropertiesMap.put("\n", new DataSourceProperties("\n", new HashMap<>()));
        stringDataSourcePropertiesMap.put("active_version", new DataSourceProperties("active_version", new HashMap<>()));
        clusterModeContextManager.alterStorageUnits("Database Name", stringDataSourcePropertiesMap);
    }
    
    @Test
    public void assertAlterStorageUnitsWithoutDataSourceProperties() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(
                new ProcessListClusterPersistRepositoryFixture());
        Map<String, ShardingSphereDatabase> databases = new HashMap<>();
        ShardingSphereRuleMetaData globalRuleMetaData = new ShardingSphereRuleMetaData(new LinkedList<>());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService,
                new ShardingSphereMetaData(databases, globalRuleMetaData, new ConfigurationProperties(new Properties())));
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfiguration = new ModeConfiguration("Type",
                new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager
                .setContextManagerAware(new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator,
                        modeConfiguration, modeContextManager, null, new EventBusContext())));
        clusterModeContextManager.alterStorageUnits("Database Name", new TreeMap<>());
    }
    
    @Test
    public void assertAlterStorageUnitsWithEmptyDataSourcePropertiesMap() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(
                new ProcessListClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, new ShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfiguration = new ModeConfiguration("Type",
                new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager
                .setContextManagerAware(new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator,
                        modeConfiguration, modeContextManager, null, new EventBusContext())));
        Map<String, DataSourceProperties> stringDataSourcePropertiesMap = new HashMap<>(new TreeMap<>());
        clusterModeContextManager.alterStorageUnits("Database Name", stringDataSourcePropertiesMap);
    }
    
    @Test
    public void assertAlterStorageUnitsWithOneDataSourceProperties() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(
                new ProcessListClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, new ShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfiguration = new ModeConfiguration("Type",
                new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager
                .setContextManagerAware(new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator,
                        modeConfiguration, modeContextManager, null, new EventBusContext())));
        Map<String, DataSourceProperties> stringDataSourcePropertiesMap = new HashMap<>();
        stringDataSourcePropertiesMap.put("42", new DataSourceProperties("active_version", new HashMap<>()));
        clusterModeContextManager.alterStorageUnits("Database Name", stringDataSourcePropertiesMap);
    }
    
    @Test
    public void assertUnregisterStorageUnits() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(new ClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, new ShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfiguration = new ModeConfiguration("Type",
                new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager
                .setContextManagerAware(new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator,
                        modeConfiguration, modeContextManager, null, new EventBusContext())));
        clusterModeContextManager.unregisterStorageUnits("Database Name", new LinkedList<>());
    }
    
    @Test
    public void assertUnregisterStorageUnitsWithProcessListClusterPersistRepoFixture() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(
                new ProcessListClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, new ShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfiguration = new ModeConfiguration("Type",
                new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager
                .setContextManagerAware(new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator,
                        modeConfiguration, modeContextManager, null, new EventBusContext())));
        clusterModeContextManager.unregisterStorageUnits("Database Name", new LinkedList<>());
    }
    
    @Test
    public void assertUnregisterStorageUnitsWithClusterPersistRepoFixture() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(new ClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, new ShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfiguration = new ModeConfiguration("Type",
                new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager
                .setContextManagerAware(new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator,
                        modeConfiguration, modeContextManager, null, new EventBusContext())));
        clusterModeContextManager.alterRuleConfiguration("Database Name", new LinkedList<>());
    }
    
    @Test
    public void assertAlterRuleConfiguration() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(new ClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, new ShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfiguration = new ModeConfiguration("Type",
                new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager
                .setContextManagerAware(new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator,
                        modeConfiguration, modeContextManager, null, new EventBusContext())));
        Collection<RuleConfiguration> ruleConfigurationList = new LinkedList<>();
        ruleConfigurationList.add(new RuleConfigurationFixture());
        clusterModeContextManager.alterRuleConfiguration("Database Name", ruleConfigurationList);
    }
    
    @Test
    public void assertAlterRuleConfigurationMultiple() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(new ClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, new ShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfiguration = new ModeConfiguration("Type",
                new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager
                .setContextManagerAware(new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator,
                        modeConfiguration, modeContextManager, null, new EventBusContext())));
        Collection<RuleConfiguration> ruleConfigurationList = new LinkedList<>();
        ruleConfigurationList.add(new RuleConfigurationFixture());
        ruleConfigurationList.add(new RuleConfigurationFixture());
        clusterModeContextManager.alterRuleConfiguration("Database Name", ruleConfigurationList);
    }
    
    @Test
    public void assertAlterRuleConfigurationWithPersistService() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(
                new ProcessListClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, new ShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfiguration = new ModeConfiguration("Type",
                new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager
                .setContextManagerAware(new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator,
                        modeConfiguration, modeContextManager, null, new EventBusContext())));
        Collection<RuleConfiguration> ruleConfigurationList = new LinkedList<>();
        ruleConfigurationList.add(new RuleConfigurationFixture());
        clusterModeContextManager.alterRuleConfiguration("Database Name", ruleConfigurationList);
    }
    
    @Test
    public void assertAlterGlobalRuleConfigurationWithEmptyRuleConfigurations() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(new ClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, new ShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfiguration = new ModeConfiguration("Type",
                new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager
                .setContextManagerAware(new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator,
                        modeConfiguration, modeContextManager, null, new EventBusContext())));
        clusterModeContextManager.alterGlobalRuleConfiguration(new LinkedList<>());
    }
    
    @Test
    public void assertAlterGlobalRuleConfigurationWithSingleRuleConfigurations() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(new ClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, new ShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfiguration = new ModeConfiguration("Type",
                new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager
                .setContextManagerAware(new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator,
                        modeConfiguration, modeContextManager, null, new EventBusContext())));
        Collection<RuleConfiguration> ruleConfigurationList = new LinkedList<>();
        ruleConfigurationList.add(new RuleConfigurationFixture());
        clusterModeContextManager.alterGlobalRuleConfiguration(ruleConfigurationList);
    }
    
    @Test
    public void assertAlterGlobalRuleConfigurationWithMultipleRuleConfigurations() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(new ClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, new ShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfiguration = new ModeConfiguration("Type",
                new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager
                .setContextManagerAware(new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator,
                        modeConfiguration, modeContextManager, null, new EventBusContext())));
        Collection<RuleConfiguration> ruleConfigurationList = new LinkedList<>();
        ruleConfigurationList.add(new RuleConfigurationFixture());
        ruleConfigurationList.add(new RuleConfigurationFixture());
        clusterModeContextManager.alterGlobalRuleConfiguration(ruleConfigurationList);
    }
    
    @Test
    public void assertAlterProperties() {
        ClusterModeContextManager clusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(new ClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, new ShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfiguration = new ModeConfiguration("Type",
                new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        clusterModeContextManager
                .setContextManagerAware(new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator,
                        modeConfiguration, modeContextManager, null, new EventBusContext())));
        clusterModeContextManager.alterProperties(new Properties());
    }
    
    @Test
    public void assertConstructor() {
        ClusterModeContextManager actualClusterModeContextManager = new ClusterModeContextManager();
        MetaDataPersistService persistService = new MetaDataPersistService(new ClusterPersistRepositoryFixture());
        MetaDataContexts metaDataContexts = new MetaDataContexts(persistService, new ShardingSphereMetaData());
        ComputeNodeInstance instance = new ComputeNodeInstance(new JDBCInstanceMetaData("42"));
        WorkerIdGenerator workerIdGenerator = mock(WorkerIdGenerator.class);
        ModeConfiguration modeConfiguration = new ModeConfiguration("Type",
                new ClusterPersistRepositoryConfiguration("Type", "Namespace", "Server Lists", new Properties()));
        ClusterModeContextManager modeContextManager = new ClusterModeContextManager();
        actualClusterModeContextManager
                .setContextManagerAware(new ContextManager(metaDataContexts, new InstanceContext(instance, workerIdGenerator,
                        modeConfiguration, modeContextManager, null, new EventBusContext())));
    }
}
