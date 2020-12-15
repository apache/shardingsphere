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

package org.apache.shardingsphere.proxy.backend.text.admin.mysql.schema;

import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.text.admin.DatabaseAdminBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.admin.mysql.schema.impl.ShowCurrentDatabaseBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.admin.mysql.schema.impl.ShowDatabasesBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.admin.mysql.schema.impl.ShowTablesBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.admin.mysql.schema.impl.UseDatabaseBackendHandler;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLShowTablesStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLUseStatement;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class SchemaBackendHandlerFactoryTest {
    
    @Test
    public void assertUseDatabase() {
        Optional<DatabaseAdminBackendHandler> actual = SchemaBackendHandlerFactory.newInstance(mock(MySQLUseStatement.class), mock(BackendConnection.class));
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(UseDatabaseBackendHandler.class));
    }
    
    @Test
    public void assertShowDatabases() {
        Optional<DatabaseAdminBackendHandler> actual = SchemaBackendHandlerFactory.newInstance(mock(MySQLShowDatabasesStatement.class), mock(BackendConnection.class));
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ShowDatabasesBackendHandler.class));
    }
    
    @Test
    public void assertShowCurrentDatabase() {
        SelectStatement sqlStatement = mock(SelectStatement.class, RETURNS_DEEP_STUBS);
        when(sqlStatement.getProjections().getProjections().iterator().next()).thenReturn(new ExpressionProjectionSegment(0, 0, ShowCurrentDatabaseBackendHandler.FUNCTION_NAME));
        Optional<DatabaseAdminBackendHandler> actual = SchemaBackendHandlerFactory.newInstance(sqlStatement, mock(BackendConnection.class));
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ShowCurrentDatabaseBackendHandler.class));
    }
    
    @Test
    public void assertShowTables() {
        Optional<DatabaseAdminBackendHandler> actual = SchemaBackendHandlerFactory.newInstance(mock(MySQLShowTablesStatement.class), mock(BackendConnection.class));
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(ShowTablesBackendHandler.class));
    }
}
