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

package org.apache.shardingsphere.proxy.backend.handler.tcl.xa;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseProxyConnectorFactory;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.tcl.xa.type.XABeginProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.tcl.xa.type.XACommitProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.tcl.xa.type.XAOtherOperationProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.tcl.xa.type.XARecoveryProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.tcl.xa.type.XARollbackProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.xa.XABeginStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.xa.XACommitStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.xa.XAPrepareStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.xa.XARecoveryStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.xa.XARollbackStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.xa.XAStatement;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class XATCLProxyBackendHandlerFactoryTest {
    
    private static final DatabaseType DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideXAStatements")
    void assertCreateHandlersForEachXAStatement(final String name, final XAStatement statement, final Class<? extends ProxyBackendHandler> expectedClass) {
        QueryContext queryContext = mock(QueryContext.class);
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        ProxyDatabaseConnectionManager databaseConnectionManager = mock(ProxyDatabaseConnectionManager.class);
        when(connectionSession.getDatabaseConnectionManager()).thenReturn(databaseConnectionManager);
        try (MockedStatic<DatabaseProxyConnectorFactory> mockedStatic = mockStatic(DatabaseProxyConnectorFactory.class)) {
            SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class);
            when(sqlStatementContext.getSqlStatement()).thenReturn(statement);
            when(queryContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
            assertThat(XATCLProxyBackendHandlerFactory.newInstance(queryContext, connectionSession), isA(expectedClass));
            mockedStatic.verify(() -> DatabaseProxyConnectorFactory.newInstance(eq(queryContext), eq(databaseConnectionManager), anyBoolean()));
        }
    }
    
    private static Stream<Arguments> provideXAStatements() {
        return Stream.of(
                Arguments.of("recovery", new XARecoveryStatement(DATABASE_TYPE), XARecoveryProxyBackendHandler.class),
                Arguments.of("begin", new XABeginStatement(DATABASE_TYPE, "xid"), XABeginProxyBackendHandler.class),
                Arguments.of("commit", new XACommitStatement(DATABASE_TYPE, "xid"), XACommitProxyBackendHandler.class),
                Arguments.of("rollback", new XARollbackStatement(DATABASE_TYPE, "xid"), XARollbackProxyBackendHandler.class),
                Arguments.of("other", new XAPrepareStatement(DATABASE_TYPE, "xid"), XAOtherOperationProxyBackendHandler.class));
    }
}
