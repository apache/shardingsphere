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

package org.apache.shardingsphere.proxy.backend.hbase.result.update;

import org.apache.shardingsphere.infra.executor.sql.execute.result.update.UpdateResult;
import org.apache.shardingsphere.proxy.backend.hbase.impl.HBaseDatabaseBackendUpdateHandler;
import org.apache.shardingsphere.proxy.backend.hbase.result.HBaseSupportedSQLStatement;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.Test;
import java.util.Collections;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class HBaseDatabaseBackendUpdateHandlerTest {
    
    @Test
    public void assertExecuteDeleteStatement() {
        HBaseDatabaseDeleteUpdater updater = mock(HBaseDatabaseDeleteUpdater.class);
        when(updater.executeUpdate(any())).thenReturn(Collections.singletonList(new UpdateResult(1, 0)));
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(HBaseSupportedSQLStatement.getDeleteStatement());
        HBaseDatabaseBackendUpdateHandler handler = new HBaseDatabaseBackendUpdateHandler(sqlStatement, updater);
        UpdateResponseHeader result = handler.execute();
        assertUpdateResponseHeader(sqlStatement, result);
    }
    
    @Test
    public void assertExecuteUpdateStatement() {
        HBaseDatabaseUpdateUpdater updater = mock(HBaseDatabaseUpdateUpdater.class);
        when(updater.executeUpdate(any())).thenReturn(Collections.singletonList(new UpdateResult(1, 0)));
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(HBaseSupportedSQLStatement.getUpdateStatement());
        HBaseDatabaseBackendUpdateHandler handler = new HBaseDatabaseBackendUpdateHandler(sqlStatement, updater);
        UpdateResponseHeader result = handler.execute();
        assertUpdateResponseHeader(sqlStatement, result);
    }
    
    @Test
    public void assertFlushTableStatement() {
        HBaseRegionReloadUpdater updater = mock(HBaseRegionReloadUpdater.class);
        when(updater.executeUpdate(any())).thenReturn(Collections.singletonList(new UpdateResult(1, 0)));
        SQLStatement sqlStatement = HBaseSupportedSQLStatement.parseSQLStatement(HBaseSupportedSQLStatement.getFlushTablesStatement());
        HBaseDatabaseBackendUpdateHandler handler = new HBaseDatabaseBackendUpdateHandler(sqlStatement, updater);
        UpdateResponseHeader result = handler.execute();
        assertUpdateResponseHeader(sqlStatement, result);
    }
    
    private void assertUpdateResponseHeader(final SQLStatement sqlStatement, final UpdateResponseHeader responseHeader) {
        assertThat(responseHeader, instanceOf(UpdateResponseHeader.class));
        assertThat(responseHeader.getSqlStatement(), is(sqlStatement));
        assertThat(responseHeader.getUpdateCount(), is(1L));
    }
}
