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

package org.apache.shardingsphere.infra.session.query;

import org.apache.shardingsphere.database.exception.core.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.type.AllowNotUseDatabaseSQLStatementAttribute;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QueryContextTest {
    
    @Test
    void assertGetUsedDatabase() {
        ConnectionContext connectionContext = mock(ConnectionContext.class);
        when(connectionContext.getCurrentDatabaseName()).thenReturn(Optional.of("foo_db"));
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(metaData.containsDatabase("foo_db")).thenReturn(true);
        when(metaData.getDatabase("foo_db")).thenReturn(database);
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        QueryContext actual = new QueryContext(sqlStatementContext, "SELECT 1", Collections.emptyList(), mock(HintValueContext.class), connectionContext, metaData);
        assertThat(actual.getUsedDatabase(), is(database));
    }
    
    @Test
    void assertGetUsedDatabaseWhenNoDatabaseSelected() {
        ConnectionContext connectionContext = mock(ConnectionContext.class);
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(connectionContext.getCurrentDatabaseName()).thenReturn(Optional.empty());
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        QueryContext actual = new QueryContext(sqlStatementContext, "SELECT 1", Collections.emptyList(), mock(HintValueContext.class), connectionContext, metaData);
        assertThrows(NoDatabaseSelectedException.class, actual::getUsedDatabase);
    }
    
    @Test
    void assertGetUsedDatabaseForShowTableStatusStatementWhenNoDatabaseSelected() {
        ConnectionContext connectionContext = mock(ConnectionContext.class);
        when(connectionContext.getCurrentDatabaseName()).thenReturn(Optional.empty());
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.isComplete()).thenReturn(true);
        when(database.getName()).thenReturn("foo_db");
        when(metaData.containsDatabase("foo_db")).thenReturn(true);
        when(metaData.getAllDatabases()).thenReturn(Collections.singletonList(database));
        when(metaData.getDatabase("foo_db")).thenReturn(database);
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getSqlStatement().getAttributes().findAttribute(AllowNotUseDatabaseSQLStatementAttribute.class))
                .thenReturn(Optional.of(new AllowNotUseDatabaseSQLStatementAttribute(true)));
        when(sqlStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        QueryContext actual = new QueryContext(sqlStatementContext, "SELECT 1", Collections.emptyList(), mock(HintValueContext.class), connectionContext, metaData);
        assertThat(actual.getUsedDatabase(), is(database));
    }
    
    @Test
    void assertGetUsedDatabaseWhenUseUnknownDatabase() {
        ConnectionContext connectionContext = mock(ConnectionContext.class);
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(connectionContext.getCurrentDatabaseName()).thenReturn(Optional.of("unknown_db"));
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        QueryContext actual = new QueryContext(sqlStatementContext, "SELECT 1", Collections.emptyList(), mock(HintValueContext.class), connectionContext, metaData);
        assertThrows(UnknownDatabaseException.class, actual::getUsedDatabase);
    }
}
