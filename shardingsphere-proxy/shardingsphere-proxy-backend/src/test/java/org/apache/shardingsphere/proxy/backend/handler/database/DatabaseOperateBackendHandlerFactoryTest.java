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

package org.apache.shardingsphere.proxy.backend.handler.database;

import org.apache.shardingsphere.infra.util.exception.external.sql.UnsupportedSQLOperationException;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.AlterDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateDatabaseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropDatabaseStatement;
import org.junit.Test;
import java.sql.SQLException;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class DatabaseOperateBackendHandlerFactoryTest {
    
    @Test
    public void assertDatabaseOperateBackendHandlerFactoryReturnCreateDatabaseBackendHandler() throws SQLException {
        assertThat(DatabaseOperateBackendHandlerFactory.newInstance(mock(CreateDatabaseStatement.class), mock(ConnectionSession.class)), instanceOf(CreateDatabaseBackendHandler.class));
    }
    
    @Test
    public void assertDatabaseOperateBackendHandlerFactoryReturnDropDatabaseBackendHandler() throws SQLException {
        assertThat(DatabaseOperateBackendHandlerFactory.newInstance(mock(DropDatabaseStatement.class), mock(ConnectionSession.class)), instanceOf(DropDatabaseBackendHandler.class));
    }
    
    @Test(expected = UnsupportedSQLOperationException.class)
    public void assertDatabaseOperateBackendHandlerFactoryThrowUnsupportedOperationException() throws SQLException {
        DatabaseOperateBackendHandlerFactory.newInstance(mock(AlterDatabaseStatement.class), mock(ConnectionSession.class));
    }
}
