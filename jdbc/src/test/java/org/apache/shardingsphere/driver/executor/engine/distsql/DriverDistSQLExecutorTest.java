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

package org.apache.shardingsphere.driver.executor.engine.distsql;

import org.apache.shardingsphere.distsql.statement.DistSQLStatement;
import org.apache.shardingsphere.distsql.statement.type.ral.RALStatement;
import org.apache.shardingsphere.distsql.statement.type.ral.queryable.QueryableRALStatement;
import org.apache.shardingsphere.distsql.statement.type.rdl.resource.unit.type.RegisterStorageUnitStatement;
import org.apache.shardingsphere.distsql.statement.type.rql.RQLStatement;
import org.apache.shardingsphere.distsql.statement.type.rul.RULStatement;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class DriverDistSQLExecutorTest {
    
    @Test
    void assertExecuteResultHasResultSet() {
        DriverDistSQLExecutor.ExecuteResult result = new DriverDistSQLExecutor.ExecuteResult(true, null, 0);
        assertTrue(result.isHasResultSet());
    }
    
    @Test
    void assertExecuteResultNoResultSet() {
        DriverDistSQLExecutor.ExecuteResult result = new DriverDistSQLExecutor.ExecuteResult(false, null, 0);
        assertFalse(result.isHasResultSet());
    }
    
    @Test
    void assertExecuteResultGetUpdateCount() {
        DriverDistSQLExecutor.ExecuteResult result = new DriverDistSQLExecutor.ExecuteResult(false, null, 5);
        assertFalse(result.isHasResultSet());
        assertThat(result.getUpdateCount(), CoreMatchers.is(5));
    }
    
    @Test
    void assertIsQueryStatementForRQL() throws Exception {
        RQLStatement rqlStatement = mock(RQLStatement.class);
        assertTrue(invokeIsQueryStatement(rqlStatement));
    }
    
    @Test
    void assertIsQueryStatementForRUL() throws Exception {
        RULStatement rulStatement = mock(RULStatement.class);
        assertTrue(invokeIsQueryStatement(rulStatement));
    }
    
    @Test
    void assertIsQueryStatementForQueryableRAL() throws Exception {
        QueryableRALStatement queryableRALStatement = mock(QueryableRALStatement.class);
        assertTrue(invokeIsQueryStatement(queryableRALStatement));
    }
    
    @Test
    void assertIsQueryStatementForNonQueryableRAL() throws Exception {
        RALStatement ralStatement = mock(RALStatement.class);
        assertFalse(invokeIsQueryStatement(ralStatement));
    }
    
    @Test
    void assertIsQueryStatementForRDL() throws Exception {
        RegisterStorageUnitStatement rdlStatement = mock(RegisterStorageUnitStatement.class);
        assertFalse(invokeIsQueryStatement(rdlStatement));
    }
    
    private boolean invokeIsQueryStatement(final DistSQLStatement statement) throws Exception {
        Method method = DriverDistSQLExecutor.class.getDeclaredMethod("isQueryStatement", DistSQLStatement.class);
        method.setAccessible(true);
        DriverDistSQLExecutor executor = new DriverDistSQLExecutor(null);
        return (boolean) method.invoke(executor, statement);
    }
}
