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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor;

import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.connector.BackendConnection;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseConnector;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.MySQLSetVariableAdminExecutor;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.VariableAssignSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.VariableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLSetStatement;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;

import java.sql.SQLException;
import java.util.Collections;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
public final class MySQLSetVariableAdminExecutorTest {
    
    @Test
    public void assertExecute() throws SQLException {
        SetStatement setStatement = prepareSetStatement();
        MySQLSetVariableAdminExecutor executor = new MySQLSetVariableAdminExecutor(setStatement);
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getDatabaseName()).thenReturn("foo_db");
        BackendConnection backendConnection = mock(BackendConnection.class);
        when(connectionSession.getBackendConnection()).thenReturn(backendConnection);
        when(backendConnection.getConnectionSession()).thenReturn(connectionSession);
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        try (MockedConstruction<DatabaseConnector> mockConstruction = mockConstruction(DatabaseConnector.class)) {
            executor.execute(connectionSession);
            verify(mockConstruction.constructed().get(0)).execute();
        }
        verify(connectionSession).setCurrentDatabase("'value'");
    }
    
    private SetStatement prepareSetStatement() {
        VariableAssignSegment setGlobalMaxConnectionAssignSegment = new VariableAssignSegment();
        VariableSegment maxConnectionVariableSegment = new VariableSegment();
        maxConnectionVariableSegment.setScope("global");
        maxConnectionVariableSegment.setVariable("max_connections");
        setGlobalMaxConnectionAssignSegment.setVariable(maxConnectionVariableSegment);
        setGlobalMaxConnectionAssignSegment.setAssignValue("151");
        VariableAssignSegment setTestFixtureAssignSegment = new VariableAssignSegment();
        VariableSegment testFixtureSegment = new VariableSegment();
        testFixtureSegment.setVariable("test_fixture");
        setTestFixtureAssignSegment.setVariable(testFixtureSegment);
        setTestFixtureAssignSegment.setAssignValue("'value'");
        SetStatement result = new MySQLSetStatement();
        result.getVariableAssigns().add(setGlobalMaxConnectionAssignSegment);
        result.getVariableAssigns().add(setTestFixtureAssignSegment);
        return result;
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataContexts().getMetaData().getDatabase("foo_db")).thenReturn(mock(ShardingSphereDatabase.class));
        when(result.getMetaDataContexts().getMetaData().getGlobalRuleMetaData())
                .thenReturn(new ShardingSphereRuleMetaData(Collections.singleton(new SQLParserRule(new SQLParserRuleConfiguration(false, new CacheOption(1, 1), new CacheOption(1, 1))))));
        return result;
    }
}
