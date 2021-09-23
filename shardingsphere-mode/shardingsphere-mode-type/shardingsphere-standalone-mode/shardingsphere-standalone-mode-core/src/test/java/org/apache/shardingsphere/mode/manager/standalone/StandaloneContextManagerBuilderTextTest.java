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

import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.node.GlobalNode;
import org.apache.shardingsphere.mode.metadata.persist.node.SchemaMetaDataNode;
import org.apache.shardingsphere.mode.persist.PersistRepository;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class StandaloneContextManagerBuilderTextTest {

    public static final String TEST_DATA_SOURCE_INNER_MAP = "TEST_DATA_SOURCE_INNER_MAP";

    public static final String TEST_CONNECTION_URL = "jdbc:mysql://testhost:3306/testdatabase";

    @Test
    public void assertBuild() throws SQLException {
        Map<String, Map<String, DataSource>> dataSourceMap = getDataSourceMap();
        Map<String, Collection<RuleConfiguration>> schemaRuleConfigs = getSchemaRuleConfigs();
        Collection<RuleConfiguration> globalRuleConfigurationCollection = getGlobalRuleConfigurationCollection();
        Properties props = new Properties();
        ModeConfiguration modeConfiguration = new ModeConfiguration("testType", null, false);
        StandaloneContextManagerBuilder standaloneContextManagerBuilder = new StandaloneContextManagerBuilder();
        ContextManager actual = standaloneContextManagerBuilder.build(modeConfiguration, dataSourceMap, schemaRuleConfigs, globalRuleConfigurationCollection, props, false, 1000);
        MetaDataContexts metaDataContexts = actual.getMetaDataContexts();
        assertNotNull(metaDataContexts.getMetaDataMap().get(TEST_DATA_SOURCE_INNER_MAP));
        assertNotNull(metaDataContexts.getExecutorEngine());
        PersistRepository resultRepository = metaDataContexts.getMetaDataPersistService().get().getRepository();
        assertNotNull(resultRepository.get(GlobalNode.getGlobalRuleNode()));
        assertNotNull(resultRepository.get(SchemaMetaDataNode.getMetaDataDataSourcePath(TEST_DATA_SOURCE_INNER_MAP)));
        assertNotNull(resultRepository.get(SchemaMetaDataNode.getRulePath(TEST_DATA_SOURCE_INNER_MAP)));
        TransactionContexts transactionContexts = actual.getTransactionContexts();
        assertNotNull(transactionContexts.getEngines());
        assertNotNull(transactionContexts.getEngines().get(TEST_DATA_SOURCE_INNER_MAP));
    }

    private Map<String, Map<String, DataSource>> getDataSourceMap() throws SQLException {
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.getMetaData().getURL()).thenReturn(TEST_CONNECTION_URL);
        DataSource dataSource = mock(DataSource.class, RETURNS_DEEP_STUBS);
        when(dataSource.getConnection()).thenReturn(connection);
        Map<String, DataSource> dataSourceInnerMap = new HashMap<>();
        dataSourceInnerMap.put("testDataSource", dataSource);
        Map<String, Map<String, DataSource>> result = new HashMap<>();
        result.put(TEST_DATA_SOURCE_INNER_MAP, dataSourceInnerMap);
        return result;
    }

    private Map<String, Collection<RuleConfiguration>> getSchemaRuleConfigs() {
        RuleConfiguration ruleConfiguration = mock(RuleConfiguration.class);
        Collection<RuleConfiguration> ruleConfigurationCollection = new LinkedList<>();
        ruleConfigurationCollection.add(ruleConfiguration);
        Map<String, Collection<RuleConfiguration>> result = new HashMap<>();
        result.put(TEST_DATA_SOURCE_INNER_MAP, ruleConfigurationCollection);
        return result;
    }

    private Collection<RuleConfiguration> getGlobalRuleConfigurationCollection() {
        RuleConfiguration globalRuleConfiguration = mock(RuleConfiguration.class);
        Collection<RuleConfiguration> result = new HashSet<>();
        result.add(globalRuleConfiguration);
        return result;
    }
}
