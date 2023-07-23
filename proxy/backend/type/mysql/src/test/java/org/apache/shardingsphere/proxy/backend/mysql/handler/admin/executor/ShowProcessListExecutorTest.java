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

import io.netty.util.DefaultAttributeMap;
import org.apache.shardingsphere.infra.database.mysql.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.sql.process.Process;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.internal.configuration.plugins.Plugins;

import java.sql.SQLException;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class ShowProcessListExecutorTest {
    
    @Test
    void assertExecute() throws SQLException, ReflectiveOperationException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        ShowProcessListExecutor showProcessListExecutor = new ShowProcessListExecutor();
        setupProcesses(showProcessListExecutor);
        showProcessListExecutor.execute(new ConnectionSession(mock(MySQLDatabaseType.class), TransactionType.LOCAL, new DefaultAttributeMap()));
        assertThat(showProcessListExecutor.getQueryResultMetaData().getColumnCount(), is(8));
        MergedResult mergedResult = showProcessListExecutor.getMergedResult();
        while (mergedResult.next()) {
            assertThat(mergedResult.getValue(1, String.class), is("f6c2336a-63ba-41bf-941e-2e3504eb2c80"));
            assertThat(mergedResult.getValue(2, String.class), is("root"));
            assertThat(mergedResult.getValue(3, String.class), is("127.0.0.1"));
            assertThat(mergedResult.getValue(4, String.class), is("foo_db"));
            assertThat(mergedResult.getValue(7, String.class), is("Executing 1/2"));
            assertThat(mergedResult.getValue(8, String.class), is("ALTER TABLE t_order ADD COLUMN a varchar(64) AFTER order_id"));
        }
    }
    
    private void setupProcesses(final ShowProcessListExecutor showProcessListExecutor) throws ReflectiveOperationException {
        Process process = new Process("f6c2336a-63ba-41bf-941e-2e3504eb2c80", 1617939785160L,
                "ALTER TABLE t_order ADD COLUMN a varchar(64) AFTER order_id", "foo_db", "root", "127.0.0.1", 2, Collections.emptyList(), new AtomicInteger(1), false);
        Plugins.getMemberAccessor().set(
                showProcessListExecutor.getClass().getDeclaredField("processes"), showProcessListExecutor, Collections.singleton(process));
    }
}
