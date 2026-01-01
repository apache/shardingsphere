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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.show;

import io.netty.util.DefaultAttributeMap;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.process.Process;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.show.process.MySQLShowProcessListStatement;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class MySQLShowProcessListExecutorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    @Test
    void assertExecute() throws SQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        Process process = new Process("f6c2336a-63ba-41bf-941e-2e3504eb2c80", 1617939785160L, "ALTER TABLE t_order ADD COLUMN a varchar(64) AFTER order_id",
                "foo_db", "root", "127.0.0.1", new AtomicInteger(2), new AtomicInteger(1), new AtomicBoolean(false), new AtomicBoolean());
        when(contextManager.getPersistServiceFacade().getModeFacade().getProcessService().getProcessList()).thenReturn(Collections.singleton(process));
        MySQLShowProcessListExecutor showProcessListExecutor = new MySQLShowProcessListExecutor(new MySQLShowProcessListStatement(databaseType, false));
        showProcessListExecutor.execute(new ConnectionSession(databaseType, new DefaultAttributeMap()), mock());
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
    
    @Test
    void assertExecuteCoversIdleAndTruncatedProcess() throws SQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        String longSql = "SELECT * FROM t_order WHERE user_id = 1 AND status = 'PAID' ORDER BY create_time DESC LIMIT 100 OFFSET 10";
        String repeatedSql = longSql + longSql;
        when(contextManager.getPersistServiceFacade().getModeFacade().getProcessService().getProcessList()).thenReturn(Arrays.asList(
                new Process("idle-id",
                        System.currentTimeMillis() - 1000L, "", "foo_db", "guest", "192.168.0.1", new AtomicInteger(0), new AtomicInteger(0), new AtomicBoolean(true), new AtomicBoolean()),
                new Process("busy-id",
                        System.currentTimeMillis() - 1000L, repeatedSql, "foo_db", "root", "127.0.0.1", new AtomicInteger(4), new AtomicInteger(2), new AtomicBoolean(false), new AtomicBoolean())));
        MySQLShowProcessListExecutor showProcessListExecutor = new MySQLShowProcessListExecutor(new MySQLShowProcessListStatement(databaseType, false));
        showProcessListExecutor.execute(new ConnectionSession(databaseType, new DefaultAttributeMap()), mock());
        MergedResult mergedResult = showProcessListExecutor.getMergedResult();
        int rowCount = 0;
        assertThat(mergedResult.next(), is(true));
        rowCount++;
        assertThat(mergedResult.getValue(5, String.class), is("Sleep"));
        assertThat(mergedResult.getValue(7, String.class), is(""));
        assertThat(mergedResult.getValue(8, String.class), is(""));
        assertThat(mergedResult.next(), is(true));
        rowCount++;
        assertThat(mergedResult.getValue(5, String.class), is("Execute"));
        assertThat(mergedResult.getValue(7, String.class), is("Executing 2/4"));
        assertThat(((String) mergedResult.getValue(8, String.class)).length(), is(100));
        assertThat((String) mergedResult.getValue(8, String.class), startsWith(longSql.substring(0, 50)));
        assertThat(mergedResult.next(), is(false));
        assertThat(rowCount, is(2));
    }
    
    @Test
    void assertExecuteWithFullOutputKeepsLongSql() throws SQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        String longSql = String.join("", Collections.nCopies(120, "a"));
        Process process = new Process("long-sql-id", System.currentTimeMillis(), longSql, "foo_db", "root", "127.0.0.1",
                new AtomicInteger(3), new AtomicInteger(1), new AtomicBoolean(false), new AtomicBoolean());
        when(contextManager.getPersistServiceFacade().getModeFacade().getProcessService().getProcessList()).thenReturn(Collections.singleton(process));
        MySQLShowProcessListExecutor executor = new MySQLShowProcessListExecutor(new MySQLShowProcessListStatement(databaseType, true));
        executor.execute(new ConnectionSession(databaseType, new DefaultAttributeMap()), mock());
        MergedResult mergedResult = executor.getMergedResult();
        assertThat(mergedResult.next(), is(true));
        assertThat(((String) mergedResult.getValue(8, String.class)).length(), is(120));
        assertThat(mergedResult.next(), is(false));
    }
}
