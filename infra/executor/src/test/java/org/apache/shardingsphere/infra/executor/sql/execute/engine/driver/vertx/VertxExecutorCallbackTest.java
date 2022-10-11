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

package org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.vertx;

import io.vertx.core.Future;
import io.vertx.mysqlclient.MySQLClient;
import io.vertx.sqlclient.PreparedQuery;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.vertx.VertxQueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.update.UpdateResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class VertxExecutorCallbackTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private VertxExecutionUnit vertxExecutionUnit;
    
    @Mock
    private PreparedQuery<RowSet<Row>> preparedQuery;
    
    @Mock
    private RowSet<Row> rowSet;
    
    private final VertxExecutorCallback callback = new VertxExecutorCallback();
    
    @Before
    public void setup() {
        when(vertxExecutionUnit.getExecutionUnit().getSqlUnit().getParameters()).thenReturn(Collections.emptyList());
        when(vertxExecutionUnit.getStorageResource()).thenReturn(Future.succeededFuture(preparedQuery));
        when(preparedQuery.execute(any(Tuple.class))).thenReturn(Future.succeededFuture(rowSet));
    }
    
    @Test
    public void assertExecuteQuery() {
        Collection<Future<ExecuteResult>> actual = callback.execute(Collections.singletonList(vertxExecutionUnit), true, Collections.emptyMap());
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().result(), instanceOf(VertxQueryResult.class));
    }
    
    @Test
    public void assertExecuteUpdate() {
        when(rowSet.columnDescriptors()).thenReturn(null);
        when(rowSet.rowCount()).thenReturn(10);
        when(rowSet.property(MySQLClient.LAST_INSERTED_ID)).thenReturn(-1L);
        Collection<Future<ExecuteResult>> actual = callback.execute(Collections.singletonList(vertxExecutionUnit), true, Collections.emptyMap());
        assertThat(actual.size(), is(1));
        ExecuteResult actualResult = actual.iterator().next().result();
        assertThat(actualResult, instanceOf(UpdateResult.class));
        UpdateResult actualUpdateResult = (UpdateResult) actualResult;
        assertThat(actualUpdateResult.getUpdateCount(), is(10));
        assertThat(actualUpdateResult.getLastInsertId(), is(-1L));
    }
}
