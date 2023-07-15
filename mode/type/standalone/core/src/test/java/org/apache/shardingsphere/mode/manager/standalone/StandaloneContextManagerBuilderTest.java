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

package org.apache.shardingsphere.mode.manager.standalone;

import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.database.impl.DataSourceProvidedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.proxy.ProxyInstanceMetaData;
import org.apache.shardingsphere.metadata.persist.node.DatabaseMetaDataNode;
import org.apache.shardingsphere.metadata.persist.node.GlobalNode;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.repository.standalone.StandalonePersistRepositoryConfiguration;
import org.apache.shardingsphere.mode.spi.PersistRepository;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class StandaloneContextManagerBuilderTest {
    
    @Test
    void assertBuild() throws SQLException {
        try (ContextManager actual = new StandaloneContextManagerBuilder().build(createContextManagerBuilderParameter())) {
            assertNotNull(actual.getMetaDataContexts().getMetaData().getDatabase("foo_db"));
            PersistRepository repository = actual.getMetaDataContexts().getPersistService().getRepository();
            assertNotNull(repository.getDirectly(GlobalNode.getGlobalRuleNode()));
            assertNotNull(repository.getDirectly(DatabaseMetaDataNode.getMetaDataDataSourceUnitsPath("foo_db", "0")));
            assertNotNull(repository.getDirectly(DatabaseMetaDataNode.getRulePath("foo_db", "0")));
        }
    }
    
    private ContextManagerBuilderParameter createContextManagerBuilderParameter() {
        ModeConfiguration modeConfig = new ModeConfiguration("Standalone", new StandalonePersistRepositoryConfiguration("FIXTURE", new Properties()));
        Map<String, DatabaseConfiguration> databaseConfigs = Collections.singletonMap(
                "foo_db", new DataSourceProvidedDatabaseConfiguration(Collections.singletonMap("foo_ds", new MockedDataSource()), Collections.singleton(mock(RuleConfiguration.class))));
        Collection<RuleConfiguration> globalRuleConfigs = Collections.singleton(mock(RuleConfiguration.class));
        InstanceMetaData instanceMetaData = new ProxyInstanceMetaData(UUID.fromString("00000000-000-0000-0000-000000000001").toString(), 3307);
        return new ContextManagerBuilderParameter(modeConfig, databaseConfigs, Collections.emptyMap(), globalRuleConfigs, new Properties(), Collections.emptyList(), instanceMetaData, false);
    }
}
