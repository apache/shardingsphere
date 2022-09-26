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

package org.apache.shardingsphere.proxy.backend.handler.admin.mysql;

import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.parser.config.SQLParserRuleConfiguration;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.JDBCDatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.VariableAssignSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.VariableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLSetStatement;
import org.junit.Test;
import org.mockito.MockedConstruction;

import java.sql.SQLException;
import java.util.Collections;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class MySQLSetVariableAdminExecutorTest extends ProxyContextRestorer {
    
    @Test
    public void assertExecute() throws SQLException {
        SetStatement setStatement = prepareSetStatement();
        MySQLSetVariableAdminExecutor executor = new MySQLSetVariableAdminExecutor(setStatement);
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getDatabaseName()).thenReturn("db");
        JDBCBackendConnection backendConnection = mock(JDBCBackendConnection.class);
        when(connectionSession.getBackendConnection()).thenReturn(backendConnection);
        when(backendConnection.getConnectionSession()).thenReturn(connectionSession);
        ProxyContext.init(mock(ContextManager.class, RETURNS_DEEP_STUBS));
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabase("db")).thenReturn(mock(ShardingSphereDatabase.class));
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().containsDatabase("db")).thenReturn(true);
        when(ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData())
                .thenReturn(new ShardingSphereRuleMetaData(Collections.singletonList(
                        new SQLParserRule(new SQLParserRuleConfiguration(false, new CacheOption(1, 1, false), new CacheOption(1, 1, false))))));
        try (MockedConstruction<JDBCDatabaseCommunicationEngine> mockConstruction = mockConstruction(JDBCDatabaseCommunicationEngine.class)) {
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
}
