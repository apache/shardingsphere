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

package org.apache.shardingsphere.driver.state;

import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.state.cluster.ClusterStateContext;
import org.apache.shardingsphere.infra.state.instance.InstanceStateContext;
import org.apache.shardingsphere.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.traffic.rule.TrafficRule;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DriverStateContextTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager contextManager;
    
    @BeforeEach
    void setUp() {
        Map<String, ShardingSphereDatabase> databases = mockDatabases();
        ShardingSphereRuleMetaData globalRuleMetaData = new ShardingSphereRuleMetaData(Arrays.asList(mock(TransactionRule.class, RETURNS_DEEP_STUBS), mock(TrafficRule.class)));
        MetaDataContexts metaDataContexts = new MetaDataContexts(
                mock(MetaDataPersistService.class), new ShardingSphereMetaData(databases, mock(ShardingSphereResourceMetaData.class),
                        globalRuleMetaData, new ConfigurationProperties(new Properties())));
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        when(contextManager.getInstanceContext().getInstance().getState()).thenReturn(new InstanceStateContext());
        when(contextManager.getClusterStateContext()).thenReturn(new ClusterStateContext());
    }
    
    private Map<String, ShardingSphereDatabase> mockDatabases() {
        Map<String, ShardingSphereDatabase> result = new LinkedHashMap<>();
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, Answers.RETURNS_DEEP_STUBS);
        when(database.getProtocolType()).thenReturn(new MySQLDatabaseType());
        result.put(DefaultDatabase.LOGIC_NAME, database);
        return result;
    }
    
    @Test
    void assertGetConnectionWithOkState() {
        Connection actual = DriverStateContext.getConnection(DefaultDatabase.LOGIC_NAME, contextManager);
        assertThat(actual, instanceOf(ShardingSphereConnection.class));
    }
}
