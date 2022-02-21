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

package org.apache.shardingsphere.proxy.backend.text.admin.mysql;

import org.apache.shardingsphere.proxy.backend.text.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.text.admin.mysql.executor.ShowFunctionStatusExecutor;
import org.apache.shardingsphere.proxy.backend.text.admin.mysql.executor.ShowProcedureStatusExecutor;
import org.apache.shardingsphere.proxy.backend.text.admin.mysql.executor.ShowTablesExecutor;
import org.apache.shardingsphere.proxy.backend.text.admin.mysql.executor.ShowTablesStatusExecutor;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowFunctionStatusStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowProcedureStatusStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowTableStatusStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowTablesStatement;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class MySQLAdminExecutorFactoryTest {
    
    private final MySQLAdminExecutorFactory mySQLAdminExecutorFactory = new MySQLAdminExecutorFactory();
    
    @Test
    public void assertNewInstanceWithMySQLShowFunctionStatusStatement() {
        Optional<DatabaseAdminExecutor> executorOptional = mySQLAdminExecutorFactory.newInstance(mock(MySQLShowFunctionStatusStatement.class));
        assertTrue(executorOptional.isPresent());
        assertThat(executorOptional.get(), instanceOf(ShowFunctionStatusExecutor.class));
    }
    
    @Test
    public void assertNewInstanceWithMySQLShowProcedureStatusStatement() {
        Optional<DatabaseAdminExecutor> executorOptional = mySQLAdminExecutorFactory.newInstance(mock(MySQLShowProcedureStatusStatement.class));
        assertTrue(executorOptional.isPresent());
        assertThat(executorOptional.get(), instanceOf(ShowProcedureStatusExecutor.class));
    }
    
    @Test
    public void assertNewInstanceWithMySQLShowTablesStatement() {
        Optional<DatabaseAdminExecutor> executorOptional = mySQLAdminExecutorFactory.newInstance(mock(MySQLShowTablesStatement.class));
        assertTrue(executorOptional.isPresent());
        assertThat(executorOptional.get(), instanceOf(ShowTablesExecutor.class));
    }
    
    @Test
    public void assertNewInstanceWithMySQLShowTableStatusStatement() {
        Optional<DatabaseAdminExecutor> executorOptional = mySQLAdminExecutorFactory.newInstance(mock(MySQLShowTableStatusStatement.class));
        assertTrue(executorOptional.isPresent());
        assertThat(executorOptional.get(), instanceOf(ShowTablesStatusExecutor.class));
    }
}
