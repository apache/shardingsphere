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

import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.mysql.dal.MySQLUseStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLUseDatabaseExecutorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Test
    void assertExecuteExistedDatabase() {
        MySQLUseStatement sqlStatement = mock(MySQLUseStatement.class);
        when(sqlStatement.getDatabase()).thenReturn("foo_db");
        when(connectionSession.getConnectionContext().getGrantee()).thenReturn(null);
        MySQLUseDatabaseExecutor executor = new MySQLUseDatabaseExecutor(sqlStatement);
        executor.execute(connectionSession, mockMetaData());
        verify(connectionSession).setCurrentDatabaseName("foo_db");
    }
    
    @Test
    void assertExecuteNotExistedDatabase() {
        MySQLUseStatement sqlStatement = mock(MySQLUseStatement.class);
        when(sqlStatement.getDatabase()).thenReturn("bar_db");
        MySQLUseDatabaseExecutor executor = new MySQLUseDatabaseExecutor(sqlStatement);
        assertThrows(UnknownDatabaseException.class, () -> executor.execute(connectionSession, mockMetaData()));
    }
    
    @Test
    void assertExecuteUnauthorizedDatabase() {
        MySQLUseStatement sqlStatement = mock(MySQLUseStatement.class);
        when(sqlStatement.getDatabase()).thenReturn("foo_db");
        MySQLUseDatabaseExecutor executor = new MySQLUseDatabaseExecutor(sqlStatement);
        assertThrows(UnknownDatabaseException.class, () -> executor.execute(connectionSession, mockMetaData()));
    }
    
    private ShardingSphereMetaData mockMetaData() {
        AuthorityRule rule = mock(AuthorityRule.class);
        return new ShardingSphereMetaData(
                Collections.singleton(new ShardingSphereDatabase("foo_db", databaseType, mock(), mock(), Collections.emptyList())), mock(), new RuleMetaData(Collections.singleton(rule)), mock());
    }
}
