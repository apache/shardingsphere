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

package org.apache.shardingsphere.driver.executor.batch;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.sql.Statement;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class BatchStatementExecutorTest {
    
    @Test
    void assertExecuteBatchAndClear() throws SQLException {
        Statement statement = mock(Statement.class);
        when(statement.executeUpdate(anyString())).thenReturn(1, 2);
        BatchStatementExecutor executor = new BatchStatementExecutor(statement);
        executor.addBatch("UPDATE t SET col=1 WHERE id=1");
        executor.addBatch("UPDATE t SET col=10 WHERE id=2 OR id=3");
        int[] actual = executor.executeBatch();
        assertThat(actual, is(new int[]{1, 2}));
        executor.clear();
        assertThat(executor.executeBatch(), is(new int[0]));
    }
}
