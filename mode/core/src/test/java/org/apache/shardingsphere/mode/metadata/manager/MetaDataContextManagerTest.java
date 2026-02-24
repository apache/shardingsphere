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

package org.apache.shardingsphere.mode.metadata.manager;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class MetaDataContextManagerTest {
    
    @Mock
    private PersistRepository persistRepository;
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertConstructorWithAllDependencies() {
        MetaDataContexts metaDataContexts = createMetaDataContexts();
        ComputeNodeInstanceContext computeNodeInstanceContext = mock(ComputeNodeInstanceContext.class);
        MetaDataContextManager manager = new MetaDataContextManager(metaDataContexts, computeNodeInstanceContext, persistRepository);
        assertNotNull(manager);
        assertThat(manager.getMetaDataContexts(), is(metaDataContexts));
        assertThat(manager.getComputeNodeInstanceContext(), is(computeNodeInstanceContext));
    }
    
    @Test
    void assertGetStatisticsManager() {
        MetaDataContexts metaDataContexts = createMetaDataContexts();
        ComputeNodeInstanceContext computeNodeInstanceContext = mock(ComputeNodeInstanceContext.class);
        MetaDataContextManager manager = new MetaDataContextManager(metaDataContexts, computeNodeInstanceContext, persistRepository);
        assertNotNull(manager.getStatisticsManager());
    }
    
    @Test
    void assertGetDatabaseMetaDataManager() {
        MetaDataContexts metaDataContexts = createMetaDataContexts();
        ComputeNodeInstanceContext computeNodeInstanceContext = mock(ComputeNodeInstanceContext.class);
        MetaDataContextManager manager = new MetaDataContextManager(metaDataContexts, computeNodeInstanceContext, persistRepository);
        assertNotNull(manager.getDatabaseMetaDataManager());
    }
    
    @Test
    void assertGetDatabaseRuleItemManager() {
        MetaDataContexts metaDataContexts = createMetaDataContexts();
        ComputeNodeInstanceContext computeNodeInstanceContext = mock(ComputeNodeInstanceContext.class);
        MetaDataContextManager manager = new MetaDataContextManager(metaDataContexts, computeNodeInstanceContext, persistRepository);
        assertNotNull(manager.getDatabaseRuleItemManager());
    }
    
    @Test
    void assertGetResourceSwitchManager() {
        MetaDataContexts metaDataContexts = createMetaDataContexts();
        ComputeNodeInstanceContext computeNodeInstanceContext = mock(ComputeNodeInstanceContext.class);
        MetaDataContextManager manager = new MetaDataContextManager(metaDataContexts, computeNodeInstanceContext, persistRepository);
        assertNotNull(manager.getResourceSwitchManager());
    }
    
    @Test
    void assertGetStorageUnitManager() {
        MetaDataContexts metaDataContexts = createMetaDataContexts();
        ComputeNodeInstanceContext computeNodeInstanceContext = mock(ComputeNodeInstanceContext.class);
        MetaDataContextManager manager = new MetaDataContextManager(metaDataContexts, computeNodeInstanceContext, persistRepository);
        assertNotNull(manager.getStorageUnitManager());
    }
    
    @Test
    void assertGetDatabaseRuleConfigurationManager() {
        MetaDataContexts metaDataContexts = createMetaDataContexts();
        ComputeNodeInstanceContext computeNodeInstanceContext = mock(ComputeNodeInstanceContext.class);
        MetaDataContextManager manager = new MetaDataContextManager(metaDataContexts, computeNodeInstanceContext, persistRepository);
        assertNotNull(manager.getDatabaseRuleConfigurationManager());
    }
    
    @Test
    void assertGetGlobalConfigurationManager() {
        MetaDataContexts metaDataContexts = createMetaDataContexts();
        ComputeNodeInstanceContext computeNodeInstanceContext = mock(ComputeNodeInstanceContext.class);
        MetaDataContextManager manager = new MetaDataContextManager(metaDataContexts, computeNodeInstanceContext, persistRepository);
        assertNotNull(manager.getGlobalConfigurationManager());
    }
    
    @Test
    void assertGetMetaDataPersistFacade() {
        MetaDataContexts metaDataContexts = createMetaDataContexts();
        ComputeNodeInstanceContext computeNodeInstanceContext = mock(ComputeNodeInstanceContext.class);
        MetaDataContextManager manager = new MetaDataContextManager(metaDataContexts, computeNodeInstanceContext, persistRepository);
        assertNotNull(manager.getMetaDataPersistFacade());
    }
    
    private MetaDataContexts createMetaDataContexts() {
        Properties props = new Properties();
        props.setProperty(ConfigurationPropertyKey.PERSIST_SCHEMAS_TO_REPOSITORY_ENABLED.getKey(), "true");
        ConfigurationProperties configurationProps = new ConfigurationProperties(props);
        ShardingSphereDatabase database = new ShardingSphereDatabase(
                "test_db", databaseType, new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), Collections.emptyList());
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(
                Collections.singleton(database), new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), configurationProps);
        return new MetaDataContexts(metaData, new ShardingSphereStatistics());
    }
}
