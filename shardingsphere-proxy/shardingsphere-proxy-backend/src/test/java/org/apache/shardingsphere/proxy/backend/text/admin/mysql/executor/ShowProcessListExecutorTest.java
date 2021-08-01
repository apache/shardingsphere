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

package org.apache.shardingsphere.proxy.backend.text.admin.mysql.executor;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Collections;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ShowProcessListExecutorTest {
    
    private ShowProcessListExecutor showProcessListExecutor;
    
    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        showProcessListExecutor = new ShowProcessListExecutor();
        setupProcessListData();
    }
    
    private void setupProcessListData() throws NoSuchFieldException, IllegalAccessException {
        Field processListDataField = showProcessListExecutor.getClass().getDeclaredField("processListData");
        processListDataField.setAccessible(true);
        String executionNodeValue = "executionID: f6c2336a-63ba-41bf-941e-2e3504eb2c80\n"
            + "sql: alter table t_order add column a varchar(64) after order_id\n"
            + "startTimeMillis: 1617939785160\n"
            + "schemaName: sharding_db\n"
            + "username: sharding\n"
            + "hostname: 127.0.0.1\n"
            + "unitStatuses:\n"
            + "- status: EXECUTE_STATUS_START\n"
            + "  unitID: unitID1\n"
            + "- status: EXECUTE_STATUS_DONE\n"
            + "  unitID: unitID2\n";
        processListDataField.set(showProcessListExecutor, Collections.singleton(executionNodeValue));
    }
    
    @Test
    public void assertExecute() throws SQLException {
        showProcessListExecutor.execute(new BackendConnection(TransactionType.LOCAL));
        assertThat(showProcessListExecutor.getQueryResultMetaData().getColumnCount(), is(8));
        MergedResult mergedResult = showProcessListExecutor.getMergedResult();
        while (mergedResult.next()) {
            assertThat(mergedResult.getValue(1, String.class), is("f6c2336a-63ba-41bf-941e-2e3504eb2c80"));
            assertThat(mergedResult.getValue(2, String.class), is("sharding"));
            assertThat(mergedResult.getValue(3, String.class), is("127.0.0.1"));
            assertThat(mergedResult.getValue(4, String.class), is("sharding_db"));
            assertThat(mergedResult.getValue(7, String.class), is("Executing 1/2"));
            assertThat(mergedResult.getValue(8, String.class), is("alter table t_order add column a varchar(64) after order_id"));
        }
    }
}
