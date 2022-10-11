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

import org.apache.shardingsphere.driver.jdbc.context.JDBCContext;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.state.StateContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.traffic.rule.TrafficRule;
import org.apache.shardingsphere.transaction.core.TransactionTypeHolder;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DriverStateContextTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager contextManager;
    
    @Before
    public void setUp() {
        Map<String, ShardingSphereDatabase> databases = mockDatabases();
        ShardingSphereRuleMetaData globalRuleMetaData = mock(ShardingSphereRuleMetaData.class);
        when(globalRuleMetaData.getSingleRule(TransactionRule.class)).thenReturn(mock(TransactionRule.class, RETURNS_DEEP_STUBS));
        when(globalRuleMetaData.getSingleRule(TrafficRule.class)).thenReturn(mock(TrafficRule.class));
        when(contextManager.getMetaDataContexts()).thenReturn(new MetaDataContexts(mock(MetaDataPersistService.class),
                new ShardingSphereMetaData(databases, globalRuleMetaData, mock(ConfigurationProperties.class)), new ShardingSphereData()));
        when(contextManager.getInstanceContext().getInstance().getState()).thenReturn(new StateContext());
    }
    
    private Map<String, ShardingSphereDatabase> mockDatabases() {
        Map<String, ShardingSphereDatabase> result = new LinkedHashMap<>();
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, Answers.RETURNS_DEEP_STUBS);
        when(database.getResourceMetaData().getDatabaseType()).thenReturn(new MySQLDatabaseType());
        result.put(DefaultDatabase.LOGIC_NAME, database);
        return result;
    }
    
    @Test
    public void assertGetConnectionWithOkState() {
        Connection actual = DriverStateContext.getConnection(DefaultDatabase.LOGIC_NAME, contextManager, mock(JDBCContext.class));
        assertThat(actual, instanceOf(ShardingSphereConnection.class));
    }
    
    @After
    public void tearDown() {
        TransactionTypeHolder.clear();
    }
}
