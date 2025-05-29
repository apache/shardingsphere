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

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.exception.dialect.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.infra.exception.dialect.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        QueryContext actual = new QueryContext(mock(SQLStatementContext.class), "SELECT 1", Collections.emptyList(), mock(HintValueContext.class), connectionContext, metaData);
        assertThat(actual.getUsedDatabase(), is(database));
    }
    
    @Test
    void assertGetUsedDatabaseWhenNoDatabaseSelected() {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class);
        ConnectionContext connectionContext = mock(ConnectionContext.class);
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(connectionContext.getCurrentDatabaseName()).thenReturn(Optional.empty());
        QueryContext queryContext = new QueryContext(sqlStatementContext, "SELECT 1", Collections.emptyList(), mock(HintValueContext.class), connectionContext, metaData);
        assertThrows(NoDatabaseSelectedException.class, queryContext::getUsedDatabase);
    }
    
    @Test
    void assertGetUsedDatabaseWhenUseUnknownDatabase() {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class);
        ConnectionContext connectionContext = mock(ConnectionContext.class);
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(connectionContext.getCurrentDatabaseName()).thenReturn(Optional.of("unknown_db"));
        QueryContext queryContext = new QueryContext(sqlStatementContext, "SELECT 1", Collections.emptyList(), mock(HintValueContext.class), connectionContext, metaData);
        assertThrows(UnknownDatabaseException.class, queryContext::getUsedDatabase);
    }
}
