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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.persist.service.ProcessPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLKillStatement;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class MySQLKillProcessExecutorTest {
    
    @Mock
    private ProcessPersistService processPersistService;
    
    @BeforeEach
    void setUpProxyContext() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        lenient().when(contextManager.getPersistServiceFacade().getModeFacade().getProcessService()).thenReturn(processPersistService);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
    }
    
    @Test
    void assertExecuteKillQueryScope() throws SQLException {
        MySQLKillProcessExecutor executor = new MySQLKillProcessExecutor(new MySQLKillStatement(mock(DatabaseType.class), "foo-pid", "QUERY"));
        executor.execute(mock(ConnectionSession.class), mock(ShardingSphereMetaData.class));
        verify(processPersistService).killProcess("foo-pid");
    }
    
    @Test
    void assertExecuteKillNonQueryScope() throws SQLException {
        MySQLKillProcessExecutor executor = new MySQLKillProcessExecutor(new MySQLKillStatement(mock(DatabaseType.class), "foo-pid", "CONNECTION"));
        assertThrows(UnsupportedSQLOperationException.class, () -> executor.execute(mock(ConnectionSession.class), mock(ShardingSphereMetaData.class)));
        verify(processPersistService, never()).killProcess(any());
    }
}
