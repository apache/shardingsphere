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
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLKillStatement;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.*;

class MySQLKillProcessExecutorTest {
    
    private static final String PROCESS_ID = "cluster-process-id-001";

    @Test
    void assertKillDelegationWithClusterProcessId() throws Exception {
        DatabaseType databaseType = mock(DatabaseType.class);

        MySQLKillStatement killStatement =
                new MySQLKillStatement(databaseType, PROCESS_ID, null);

        ProxyContext proxyContext = mock(ProxyContext.class, RETURNS_DEEP_STUBS);

        try (MockedStatic<ProxyContext> proxyContextStatic = mockStatic(ProxyContext.class)) {
            proxyContextStatic.when(ProxyContext::getInstance)
                    .thenReturn(proxyContext);

            MySQLKillProcessExecutor executor =
                    new MySQLKillProcessExecutor(killStatement);

            executor.execute(
                    mock(ConnectionSession.class),
                    mock(ShardingSphereMetaData.class));

            verify(proxyContext.getContextManager()
                            .getPersistServiceFacade()
                            .getModeFacade()
                            .getProcessService(),
                    times(1))
                    .killProcess(PROCESS_ID);
        }
    }
}
